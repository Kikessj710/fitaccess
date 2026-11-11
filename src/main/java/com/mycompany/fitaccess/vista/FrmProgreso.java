package com.mycompany.fitaccess.vista;



import com.mycompany.fitaccess.dao.MedidaDAO;
import com.mycompany.fitaccess.dao.impl.MedidaDAOImpl;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Medida;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Validaciones;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/** Historial de peso del cliente: tabla y grafica (JFreeChart). */
public class FrmProgreso extends JFrame {

    private final Cliente cliente;
    private final MedidaDAO medidaDAO = new MedidaDAOImpl();
    private final DefaultTableModel modelo = new DefaultTableModel(new Object[]{"Fecha", "Peso (kg)", "IMC"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };
    private final JPanel zonaGrafica = new JPanel(new BorderLayout());

    public FrmProgreso(Cliente cliente) {
        this.cliente = cliente;
        Estilo.ventana(this, "Progreso de " + cliente.getPrimerNombre(), 1000, 600);
        setLayout(new BorderLayout(16, 14));
        getRootPane().setBorder(Estilo.margen(20));

        add(Estilo.titulo("Progreso de peso - " + cliente.getNombreCompleto()), BorderLayout.NORTH);
        JScrollPane tabla = new JScrollPane(new JTable(modelo));
        tabla.setPreferredSize(new Dimension(320, 0));
        add(tabla, BorderLayout.WEST);
        Estilo.transparente(zonaGrafica);
        add(zonaGrafica, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Estilo.transparente(abajo);
        JButton btnNuevo = Estilo.boton("Registrar peso de hoy");
        abajo.add(btnNuevo);
        add(abajo, BorderLayout.SOUTH);
        btnNuevo.addActionListener(e -> registrar());
        cargar();
    }

    private void cargar() {
        List<Medida> medidas;
        try {
            medidas = medidaDAO.listarPorCliente(cliente.getId());
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
            return;
        }
        modelo.setRowCount(0);
        TimeSeries serie = new TimeSeries("Peso");
        for (Medida m : medidas) {
            double imc = Medida.imc(m.getPesoKg(), cliente.getAlturaCm());
            modelo.addRow(new Object[]{m.getFecha(), m.getPesoKg(), String.format("%.1f", imc)});
            LocalDate f = m.getFecha();
            serie.addOrUpdate(new Day(f.getDayOfMonth(), f.getMonthValue(), f.getYear()), m.getPesoKg());
        }
        JFreeChart grafica = ChartFactory.createTimeSeriesChart(null, "Fecha", "Peso (kg)",
                new TimeSeriesCollection(serie), false, true, false);
        temaOscuro(grafica);
        zonaGrafica.removeAll();
        zonaGrafica.add(new ChartPanel(grafica), BorderLayout.CENTER);
        zonaGrafica.revalidate();
    }

    /** Aplica los colores de la app a la grafica. */
    static void temaOscuro(JFreeChart grafica) {
        grafica.setBackgroundPaint(Estilo.FONDO);
        if (grafica.getPlot() instanceof XYPlot plot) {
            plot.setBackgroundPaint(Estilo.PANEL);
            plot.setDomainGridlinePaint(Estilo.BORDE);
            plot.setRangeGridlinePaint(Estilo.BORDE);
            plot.getDomainAxis().setTickLabelPaint(Estilo.TEXTO_SUAVE);
            plot.getRangeAxis().setTickLabelPaint(Estilo.TEXTO_SUAVE);
            plot.getDomainAxis().setLabelPaint(Estilo.TEXTO_SUAVE);
            plot.getRangeAxis().setLabelPaint(Estilo.TEXTO_SUAVE);
            XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, true);
            r.setSeriesPaint(0, Estilo.ACENTO);
            r.setSeriesStroke(0, new BasicStroke(3f));
            plot.setRenderer(r);
        }
    }

    private void registrar() {
        String texto = JOptionPane.showInputDialog(this, "Peso de hoy (kg):");
        if (texto == null) {
            return;
        }
        Double peso = Validaciones.decimal(texto);
        if (!Validaciones.enRango(peso, 30, 250)) {
            Estilo.error(this, "Peso entre 30 y 250 kg");
            return;
        }
        try {
            medidaDAO.insertar(new Medida(cliente.getId(), LocalDate.now(), peso));
            cargar();
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
