package com.mycompany.fitaccess.vista;



import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 * Control de versiones visible dentro de la app:
 * version compilada (git.properties del plugin de Maven) y el historial de commits del equipo.
 */
public class FrmAcercaDe extends JFrame {

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Commit", "Autor", "Fecha", "Mensaje"}, 0) {
        @Override
        public boolean isCellEditable(int f, int c) {
            return false;
        }
    };

    public FrmAcercaDe() {
        Estilo.ventana(this, "Versiones", 1000, 620);
        setLayout(new BorderLayout(0, 16));
        getRootPane().setBorder(Estilo.margen(20));

        Properties git = leerGitProperties();
        JPanel arriba = new JPanel(new GridLayout(1, 4, 14, 0));
        Estilo.transparente(arriba);
        arriba.add(Estilo.dato(git.getProperty("git.build.version", "1.0.0"), "Version", Estilo.ACENTO));
        arriba.add(Estilo.dato(git.getProperty("git.commit.id.abbrev", "-"), "Ultimo commit", Estilo.TEXTO));
        arriba.add(Estilo.dato(git.getProperty("git.branch", "-"), "Rama", Estilo.VERDE));
        arriba.add(Estilo.dato(corta(git.getProperty("git.build.time", "-")), "Compilado", Estilo.TEXTO));

        JPanel norte = new JPanel(new BorderLayout(0, 12));
        Estilo.transparente(norte);
        norte.add(Estilo.titulo("FitAccess - Control de versiones"), BorderLayout.NORTH);
        norte.add(arriba, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        tabla.getColumnModel().getColumn(0).setMaxWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setMinWidth(105);
        tabla.getColumnModel().getColumn(2).setMaxWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(520);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(Estilo.etiqueta("Historial del repositorio (git log). Cada fila es un commit de un integrante."),
                BorderLayout.SOUTH);

        cargarHistorial();
    }

    private static String corta(String fecha) {
        return fecha.length() > 10 ? fecha.substring(0, 10) : fecha;
    }

    private Properties leerGitProperties() {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/git.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (Exception ignored) {
            // si no existe, se muestran guiones
        }
        return p;
    }

    /** Ejecuta "git log" en la carpeta del proyecto. Requiere Git instalado (git-scm.com). */
    private void cargarHistorial() {
        new SwingWorker<Void, String[]>() {
            @Override
            protected Void doInBackground() throws Exception {
                ProcessBuilder pb = new ProcessBuilder("git", "log", "--all", "-n", "200",
                        "--date=short", "--pretty=format:%h|%an|%ad|%s");
                pb.redirectErrorStream(true);
                Process proceso = pb.start();
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8))) {
                    String linea;
                    while ((linea = r.readLine()) != null) {
                        String[] partes = linea.split("\\|", 4);
                        if (partes.length == 4) {
                            publish(partes);
                        }
                    }
                }
                proceso.waitFor();
                return null;
            }

            @Override
            protected void process(java.util.List<String[]> filas) {
                filas.forEach(modelo::addRow);
            }

            @Override
            protected void done() {
                if (modelo.getRowCount() == 0) {
                    modelo.addRow(new Object[]{"-", "-", "-",
                        "No se pudo leer el historial: instale Git para Windows (git-scm.com) y reinicie NetBeans"});
                }
            }
        }.execute();
    }
}
