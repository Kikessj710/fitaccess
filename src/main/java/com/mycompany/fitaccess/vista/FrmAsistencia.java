package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Asistencia;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.servicio.AccesoService;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

/** Asistencia para el administrador: quien esta dentro, historial, por vencer y horas pico. */
public class FrmAsistencia extends JFrame {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final ClienteDAO clienteDAO = new ClienteDAOImpl();
    private final AsistenciaDAOImpl asistenciaDAO = new AsistenciaDAOImpl();

    private final DefaultTableModel mDentro = modelo("ID", "Cliente", "Entrada", "Tiempo dentro");
    private final DefaultTableModel mHistorial = modelo("Cliente", "Entrada", "Salida", "Duracion", "Cierre");
    private final DefaultTableModel mVencer = modelo("Cliente", "Cedula", "Telefono", "Vence", "Dias");
    private final JTable tDentro = new JTable(mDentro);
    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JPanel panelPico = new JPanel(new BorderLayout());
    private final Timer refresco = new Timer(10_000, e -> cargarDentro());

    public FrmAsistencia() {
        Estilo.ventana(this, "Asistencia", 1050, 640);
        setLayout(new BorderLayout(0, 12));
        getRootPane().setBorder(Estilo.margen(20));
        add(Estilo.titulo("Asistencia"), BorderLayout.NORTH);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Dentro ahora", tabDentro());
        pestanas.addTab("Historial por fecha", tabHistorial());
        pestanas.addTab("Por vencer", new JScrollPane(new JTable(mVencer)));
        pestanas.addTab("Horas pico del mes", panelPico);
        add(pestanas, BorderLayout.CENTER);

        cargarDentro();
        cargarHistorial();
        cargarPorVencer();
        cargarHorasPico();
        refresco.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                refresco.stop();
            }
        });
    }

    private static DefaultTableModel modelo(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int f, int c) {
                return false;
            }
        };
    }

    private JPanel tabDentro() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.add(new JScrollPane(tDentro), BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSalida = Estilo.botonSecundario("Marcar salida");
        JButton btnCerrar = Estilo.botonSecundario("Cierre del dia");
        botones.add(btnCerrar);
        botones.add(btnSalida);
        p.add(botones, BorderLayout.SOUTH);
        tDentro.getColumnModel().getColumn(0).setMaxWidth(60);

        btnSalida.addActionListener(e -> {
            int fila = tDentro.getSelectedRow();
            if (fila < 0) {
                Estilo.error(this, "Seleccione un cliente");
                return;
            }
            int id = (int) mDentro.getValueAt(fila, 0);
            AccesoService.Resultado r = new AccesoService().registrarSalida(id);
            Estilo.info(this, r.mensaje);
            cargarDentro();
        });
        btnCerrar.addActionListener(e -> {
            int n = new AccesoService().cerrarOlvidados();
            Estilo.info(this, n == 0 ? "Aun no es la hora de cierre o no hay entradas abiertas"
                    : "Se cerraron " + n + " entradas");
            cargarDentro();
        });
        return p;
    }

    private JPanel tabHistorial() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));
        JButton btnVer = Estilo.botonSecundario("Ver");
        arriba.add(Estilo.etiqueta("Fecha:"));
        arriba.add(spnFecha);
        arriba.add(btnVer);
        p.add(arriba, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(mHistorial)), BorderLayout.CENTER);
        btnVer.addActionListener(e -> cargarHistorial());
        return p;
    }

    private String nombre(int clienteId) {
        Cliente c = clienteDAO.buscarPorId(clienteId);
        return c == null ? "?" : c.getNombreCompleto();
    }

    private void cargarDentro() {
        try {
            mDentro.setRowCount(0);
            for (Asistencia a : asistenciaDAO.listarAbiertas()) {
                long min = Duration.between(a.getHoraEntrada(), LocalDateTime.now()).toMinutes();
                mDentro.addRow(new Object[]{a.getClienteId(), nombre(a.getClienteId()),
                    a.getHoraEntrada().format(HORA), AccesoService.formatoDuracion((int) min)});
            }
        } catch (RuntimeException ex) {
            refresco.stop();
            Estilo.error(this, ex.getMessage());
        }
    }

    private void cargarHistorial() {
        LocalDate fecha = ((Date) spnFecha.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        try {
            mHistorial.setRowCount(0);
            for (Asistencia a : asistenciaDAO.listarPorFecha(fecha)) {
                mHistorial.addRow(new Object[]{nombre(a.getClienteId()), a.getHoraEntrada().format(HORA),
                    a.getHoraSalida() == null ? "Dentro" : a.getHoraSalida().format(HORA),
                    a.getDuracionMinutos() == null ? "-" : AccesoService.formatoDuracion(a.getDuracionMinutos()),
                    a.isCierreAutomatico() ? "Automatico" : "Normal"});
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void cargarPorVencer() {
        try {
            mVencer.setRowCount(0);
            for (Suscripcion s : new SuscripcionDAOImpl().listarPorVencer(7)) {
                Cliente c = clienteDAO.buscarPorId(s.getClienteId());
                mVencer.addRow(new Object[]{c.getNombreCompleto(), c.getCedula(), c.getTelefono(),
                    s.getFechaFin(), s.diasRestantes()});
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }

    private void cargarHorasPico() {
        YearMonth mes = YearMonth.now();
        int[] horas;
        try {
            horas = asistenciaDAO.entradasPorHora(mes.getMonthValue(), mes.getYear());
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
            return;
        }
        DefaultCategoryDataset datos = new DefaultCategoryDataset();
        for (int h = 5; h <= 22; h++) {
            datos.addValue(horas[h], "Entradas", String.format("%02d:00", h));
        }
        JFreeChart g = ChartFactory.createBarChart("Entradas por hora - " + mes, "Hora", "Entradas",
                datos, PlotOrientation.VERTICAL, false, true, false);
        g.setBackgroundPaint(Estilo.FONDO);
        g.getTitle().setPaint(Estilo.TEXTO);
        CategoryPlot plot = g.getCategoryPlot();
        plot.setBackgroundPaint(Estilo.PANEL);
        plot.setRangeGridlinePaint(Estilo.BORDE);
        plot.getDomainAxis().setTickLabelPaint(Estilo.TEXTO_SUAVE);
        plot.getRangeAxis().setTickLabelPaint(Estilo.TEXTO_SUAVE);
        plot.getDomainAxis().setLabelPaint(Estilo.TEXTO_SUAVE);
        plot.getRangeAxis().setLabelPaint(Estilo.TEXTO_SUAVE);
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setBarPainter(new StandardBarPainter());
        r.setSeriesPaint(0, Estilo.ACENTO);
        r.setShadowVisible(false);
        panelPico.removeAll();
        panelPico.add(new ChartPanel(g), BorderLayout.CENTER);
    }
}
