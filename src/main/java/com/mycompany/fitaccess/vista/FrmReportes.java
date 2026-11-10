package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.reportes.GeneradorReportes;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;

/** Reportes mensuales en PDF con JasperReports. */
public class FrmReportes extends JFrame {

    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
        "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

    private final JComboBox<GeneradorReportes.Tipo> cmbReporte = new JComboBox<>(GeneradorReportes.Tipo.values());
    private final JComboBox<String> cmbMes = new JComboBox<>(MESES);
    private final JSpinner spnAnio = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2100, 1));

    public FrmReportes() {
        Estilo.ventana(this, "Reportes", 620, 460);
        setLayout(new GridBagLayout());
        spnAnio.setEditor(new JSpinner.NumberEditor(spnAnio, "#"));
        cmbMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

        JPanel t = Estilo.tarjeta();
        t.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 8, 6, 8);
        JButton btnVer = Estilo.boton("Ver reporte");
        JButton btnPdf = Estilo.botonSecundario("Guardar PDF");

        g.gridy = 0; t.add(Estilo.titulo("Reportes mensuales"), g);
        g.gridy = 1; t.add(Estilo.etiqueta("Reporte"), g);
        g.gridy = 2; t.add(cmbReporte, g);
        g.gridy = 3; t.add(Estilo.etiqueta("Mes y anio"), g);
        JPanel fecha = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        Estilo.transparente(fecha);
        fecha.add(cmbMes);
        fecha.add(spnAnio);
        g.gridy = 4; t.add(fecha, g);
        g.gridy = 5; g.insets = new Insets(18, 8, 6, 8); t.add(btnVer, g);
        g.gridy = 6; g.insets = new Insets(4, 8, 6, 8); t.add(btnPdf, g);
        add(t);

        btnVer.addActionListener(e -> generar(false));
        btnPdf.addActionListener(e -> generar(true));
    }

    private void generar(boolean pdf) {
        GeneradorReportes.Tipo tipo = (GeneradorReportes.Tipo) cmbReporte.getSelectedItem();
        int mes = cmbMes.getSelectedIndex() + 1;
        int anio = (Integer) spnAnio.getValue();
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        new SwingWorker<File, Void>() {
            @Override
            protected File doInBackground() throws Exception {
                GeneradorReportes gen = new GeneradorReportes();
                if (pdf) {
                    return gen.exportarPdf(tipo, mes, anio);
                }
                gen.ver(tipo, mes, anio);
                return null;
            }

            @Override
            protected void done() {
                setCursor(java.awt.Cursor.getDefaultCursor());
                try {
                    File f = get();
                    if (f != null) {
                        Estilo.info(FrmReportes.this, "PDF guardado en:\n" + f.getAbsolutePath());
                        if (java.awt.Desktop.isDesktopSupported()) {
                            java.awt.Desktop.getDesktop().open(f);
                        }
                    }
                } catch (Exception ex) {
                    Estilo.error(FrmReportes.this, "No se pudo generar: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
