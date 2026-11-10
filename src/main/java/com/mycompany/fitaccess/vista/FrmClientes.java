package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.dao.SuscripcionDAO;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.hardware.ReconocedorFacial;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Validaciones;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** CRUD de clientes: tabla, buscador, Nuevo, Editar y Eliminar. */
public class FrmClientes extends JFrame {

    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final SuscripcionDAO suscripcionDAO = new SuscripcionDAOImpl();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Cedula", "Nombre", "Edad", "Telefono", "Vence", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> orden = new TableRowSorter<>(modelo);
    private final JTextField txtBuscar = new JTextField(24);

    public FrmClientes() {
        Estilo.ventana(this, "Clientes", 1050, 620);
        setLayout(new BorderLayout(0, 14));
        getRootPane().setBorder(Estilo.margen(20));

        JPanel arriba = new JPanel(new BorderLayout());
        Estilo.transparente(arriba);
        arriba.add(Estilo.titulo("Clientes"), BorderLayout.WEST);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Buscar por nombre o cedula...");
        txtBuscar.putClientProperty("JTextField.showClearButton", true);
        arriba.add(txtBuscar, BorderLayout.EAST);
        add(arriba, BorderLayout.NORTH);

        tabla.setRowSorter(orden);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.getColumnModel().getColumn(3).setMaxWidth(70);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new RenderEstado());
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        Estilo.transparente(botones);
        JButton btnNuevo = Estilo.boton("Nuevo");
        JButton btnEditar = Estilo.botonSecundario("Editar");
        JButton btnEliminar = Estilo.botonSecundario("Eliminar");
        JButton btnProgreso = Estilo.botonSecundario("Ver progreso");
        btnNuevo.setPreferredSize(new java.awt.Dimension(140, 38));
        botones.add(btnProgreso);
        botones.add(btnEliminar);
        botones.add(btnEditar);
        botones.add(btnNuevo);
        add(botones, BorderLayout.SOUTH);

        btnNuevo.addActionListener(e -> {
            FrmRegistro f = new FrmRegistro();
            f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent ev) {
                    cargar();
                }
            });
            f.setVisible(true);
        });
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnProgreso.addActionListener(e -> {
            Cliente c = seleccionado();
            if (c != null) {
                new FrmProgreso(c).setVisible(true);
            }
        });
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
        });

        cargar();
    }

    private void cargar() {
        modelo.setRowCount(0);
        try {
            List<Cliente> clientes = clienteDAO.listar();
            for (Cliente c : clientes) {
                Suscripcion s = suscripcionDAO.buscarUltima(c.getId());
                String vence = s == null ? "-" : s.getFechaFin().toString();
                String estado;
                if (suscripcionDAO.buscarVigente(c.getId()) != null) {
                    estado = "Vigente";
                } else {
                    estado = (s != null && s.getFechaInicio().isAfter(java.time.LocalDate.now())) ? "Por iniciar" : "Vencido";
                }
                modelo.addRow(new Object[]{c.getId(), c.getCedula(), c.getNombreCompleto(), c.getEdad(),
                    c.getTelefono(), vence, estado});
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void filtrar() {
        String texto = txtBuscar.getText().trim();
        orden.setRowFilter(texto.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(texto), 1, 2));
    }

    private Cliente seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Estilo.error(this, "Seleccione un cliente de la tabla");
            return null;
        }
        int id = (int) modelo.getValueAt(tabla.convertRowIndexToModel(fila), 0);
        return clienteDAO.buscarPorId(id);
    }

    private void editar() {
        Cliente c = seleccionado();
        if (c == null) {
            return;
        }
        JTextField nombres = new JTextField(c.getNombres());
        JTextField apellidos = new JTextField(c.getApellidos());
        JTextField telefono = new JTextField(c.getTelefono());
        JTextField correo = new JTextField(c.getCorreo());
        JTextField altura = new JTextField(String.valueOf(c.getAlturaCm()));
        JCheckBox consentimiento = new JCheckBox("Acepta el uso de su rostro", c.isConsentimiento());
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.add(new JLabel("Nombres")); p.add(nombres);
        p.add(new JLabel("Apellidos")); p.add(apellidos);
        p.add(new JLabel("Telefono")); p.add(telefono);
        p.add(new JLabel("Correo")); p.add(correo);
        p.add(new JLabel("Altura (cm)")); p.add(altura);
        p.add(consentimiento);
        if (JOptionPane.showConfirmDialog(this, p, "Editar " + c.getCedula(),
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        Double alt = Validaciones.decimal(altura.getText());
        if (!Validaciones.soloLetras(nombres.getText()) || !Validaciones.soloLetras(apellidos.getText())) {
            Estilo.error(this, "Nombres y apellidos solo con letras");
            return;
        }
        if (!Validaciones.telefonoValido(telefono.getText())) {
            Estilo.error(this, "Telefono invalido (09XXXXXXXX)");
            return;
        }
        if (!correo.getText().isBlank() && !Validaciones.correoValido(correo.getText())) {
            Estilo.error(this, "Correo invalido");
            return;
        }
        if (!Validaciones.enRango(alt, 100, 230)) {
            Estilo.error(this, "Altura entre 100 y 230 cm");
            return;
        }
        c.setNombres(nombres.getText().trim());
        c.setApellidos(apellidos.getText().trim());
        c.setTelefono(telefono.getText().trim());
        c.setCorreo(correo.getText().trim());
        c.setAlturaCm(alt);
        c.setConsentimiento(consentimiento.isSelected());
        try {
            clienteDAO.actualizar(c);
            cargar();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void eliminar() {
        Cliente c = seleccionado();
        if (c == null || !Estilo.confirmar(this, "Eliminar a " + c.getNombreCompleto()
                + "?\nSe borraran tambien sus fotos del rostro.")) {
            return;
        }
        try {
            clienteDAO.eliminar(c.getId());
            ReconocedorFacial.get().borrarRostros(c.getId());
            new Thread(() -> ReconocedorFacial.get().entrenar()).start();
            cargar();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    /** Pinta la columna Estado en verde o rojo. */
    private static class RenderEstado extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foco, int f, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, foco, f, c);
            Color color = "Vigente".equals(v) ? Estilo.VERDE : "Vencido".equals(v) ? Estilo.ROJO : Estilo.AMARILLO;
            comp.setForeground(color);
            return comp;
        }
    }
}