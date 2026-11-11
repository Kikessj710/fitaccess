package com.mycompany.fitaccess.vista;


import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.dao.PlanDAO;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.PagoDAOImpl;
import com.mycompany.fitaccess.dao.impl.PlanDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Pago;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.servicio.DescuentoService;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Validaciones;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Renovacion con descuento por constancia, recibo y pagos del mes. */
public class FrmPagos extends JFrame {

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final PlanDAO planDAO = new PlanDAOImpl();
    private final DescuentoService descuentos = new DescuentoService();

    private final JComboBox<Cliente> cmbCliente = new JComboBox<>();
    private final JComboBox<Plan> cmbPlan = new JComboBox<>();
    private final JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"Efectivo", "Transferencia", "Tarjeta"});
    private final JLabel lblInfo = Estilo.subtitulo(" ");
    private final JLabel lblTotalMes = Estilo.subtitulo(" ");
    private final DefaultTableModel mPagos = new DefaultTableModel(
            new Object[]{"N.", "Fecha", "Cliente", "Base", "Desc. %", "Total", "Metodo"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    public FrmPagos() {
        Estilo.ventana(this, "Pagos y planes", 1050, 650);
        setLayout(new BorderLayout(0, 14));
        getRootPane().setBorder(Estilo.margen(20));

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        Estilo.transparente(arriba);
        arriba.add(Estilo.titulo("Pagos y renovaciones"), BorderLayout.NORTH);

        JPanel form = Estilo.tarjeta();
        form.setLayout(new GridLayout(2, 4, 12, 6));
        form.add(Estilo.etiqueta("Cliente"));
        form.add(Estilo.etiqueta("Plan"));
        form.add(Estilo.etiqueta("Metodo"));
        form.add(new JLabel());
        form.add(cmbCliente);
        form.add(cmbPlan);
        form.add(cmbMetodo);
        JButton btnRenovar = Estilo.boton("Renovar y cobrar");
        form.add(btnRenovar);
        arriba.add(form, BorderLayout.CENTER);
        arriba.add(lblInfo, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        add(new JScrollPane(new JTable(mPagos)), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        Estilo.transparente(abajo);
        abajo.add(lblTotalMes, BorderLayout.WEST);
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Estilo.transparente(bots);
        JButton btnPlan = Estilo.botonSecundario("Nuevo plan");
        bots.add(btnPlan);
        abajo.add(bots, BorderLayout.EAST);
        add(abajo, BorderLayout.SOUTH);

        cmbCliente.addActionListener(e -> mostrarInfo());
        cmbPlan.addActionListener(e -> mostrarInfo());
        btnRenovar.addActionListener(e -> renovar());
        btnPlan.addActionListener(e -> nuevoPlan());

        try {
            clienteDAO.listar().forEach(cmbCliente::addItem);
            cargarPlanes();
            cargarPagos();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void cargarPlanes() {
        cmbPlan.removeAllItems();
        planDAO.listarActivos().forEach(cmbPlan::addItem);
        if (cmbPlan.getItemCount() > 1) {
            cmbPlan.setSelectedIndex(1);   // Mensual por defecto
        }
    }

    /** Muestra la asistencia del cliente y cuanto pagaria con descuento. */
    private void mostrarInfo() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        Plan p = (Plan) cmbPlan.getSelectedItem();
        if (c == null || p == null) {
            return;
        }
        double asistencia = descuentos.porcentajeAsistencia(c.getId());
        double desc = DescuentoService.porcentajePorAsistencia(asistencia);
        Suscripcion s = new SuscripcionDAOImpl().buscarUltima(c.getId());
        String vence = s == null ? "sin suscripcion" : "vence " + s.getFechaFin();
        lblInfo.setText(String.format("Asistencia del periodo: %.0f%%  |  Descuento: %.0f%%  |  A pagar: $%.2f  |  %s",
                asistencia, desc, p.getPrecio() * (100 - desc) / 100, vence));
        lblInfo.setForeground(desc > 0 ? Estilo.VERDE : Estilo.TEXTO_SUAVE);
    }

    private void renovar() {
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        Plan p = (Plan) cmbPlan.getSelectedItem();
        if (c == null || p == null) {
            Estilo.error(this, "Elija cliente y plan");
            return;
        }
        if (!Estilo.confirmar(this, "Renovar " + p.getNombre() + " a " + c.getNombreCompleto() + "?")) {
            return;
        }
        try {
            Pago pago = descuentos.renovar(c, p, (String) cmbMetodo.getSelectedItem());
            Suscripcion s = new SuscripcionDAOImpl().buscarUltima(c.getId());
            mostrarRecibo(descuentos.recibo(pago, c, p, s));
            cargarPagos();
            mostrarInfo();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void mostrarRecibo(String texto) {
        JTextArea area = new JTextArea(texto);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(380, 380));
        JOptionPane.showMessageDialog(this, sp, "Recibo", JOptionPane.PLAIN_MESSAGE);
    }

    private void cargarPagos() {
        LocalDate hoy = LocalDate.now();
        mPagos.setRowCount(0);
        double total = 0;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        for (Pago p : new PagoDAOImpl().listarPorMes(hoy.getMonthValue(), hoy.getYear())) {
            Cliente c = clienteDAO.buscarPorId(p.getClienteId());
            mPagos.addRow(new Object[]{p.getId(), p.getFecha().format(f), c == null ? "?" : c.getNombreCompleto(),
                String.format("$%.2f", p.getMontoBase()), String.format("%.0f", p.getPorcentajeDescuento()),
                String.format("$%.2f", p.getMontoFinal()), p.getMetodo()});
            total += p.getMontoFinal();
        }
        lblTotalMes.setText(String.format("Ingresos del mes: $%.2f  (%d pagos)", total, mPagos.getRowCount()));
    }

    private void nuevoPlan() {
        JTextField nombre = new JTextField();
        JTextField dias = new JTextField();
        JTextField precio = new JTextField();
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Nombre")); p.add(nombre);
        p.add(new JLabel("Dias de duracion")); p.add(dias);
        p.add(new JLabel("Precio ($)")); p.add(precio);
        if (JOptionPane.showConfirmDialog(this, p, "Nuevo plan", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        Double d = Validaciones.decimal(dias.getText());
        Double pr = Validaciones.decimal(precio.getText());
        if (nombre.getText().isBlank() || !Validaciones.enRango(d, 1, 365) || !Validaciones.enRango(pr, 0.5, 2000)) {
            Estilo.error(this, "Datos invalidos: nombre, dias (1-365) y precio");
            return;
        }
        try {
            planDAO.insertar(new Plan(nombre.getText().trim(), d.intValue(), pr));
            cargarPlanes();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
