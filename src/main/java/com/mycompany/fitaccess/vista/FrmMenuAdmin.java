package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Admin;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/** Menu principal del administrador con un pequeno tablero de datos del dia. */
public class FrmMenuAdmin extends JFrame {

    private final JPanel tablero = new JPanel(new GridLayout(1, 4, 14, 0));

    public FrmMenuAdmin(Admin admin) {
        Estilo.ventana(this, "Administrador", 1000, 640);
        setLayout(new BorderLayout(0, 18));
        getRootPane().setBorder(Estilo.margen(24));

        // Encabezado
        JPanel arriba = new JPanel(new BorderLayout());
        Estilo.transparente(arriba);
        JPanel textos = new JPanel(new GridLayout(2, 1));
        Estilo.transparente(textos);
        textos.add(Estilo.titulo("Hola, " + admin.getNombre()));
        textos.add(Estilo.subtitulo("Panel de administracion - " + LocalDate.now()));
        JButton btnSalir = Estilo.botonSecundario("Cerrar sesion");
        arriba.add(textos, BorderLayout.WEST);
        arriba.add(btnSalir, BorderLayout.EAST);

        // Tablero
        Estilo.transparente(tablero);
        JPanel norte = new JPanel(new BorderLayout(0, 18));
        Estilo.transparente(norte);
        norte.add(arriba, BorderLayout.NORTH);
        norte.add(tablero, BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        // Botones de modulos
        JPanel modulos = new JPanel(new GridLayout(2, 3, 16, 16));
        Estilo.transparente(modulos);
        modulos.add(modulo("Clientes", FrmClientes::new));
        modulos.add(modulo("Asistencia y horas pico", FrmAsistencia::new));
        modulos.add(modulo("Pagos y planes", FrmPagos::new));
        modulos.add(modulo("Reportes PDF", FrmReportes::new));
        modulos.add(modulo("Registrar cliente", FrmRegistro::new));
        modulos.add(modulo("Versiones (Git)", FrmAcercaDe::new));
        add(modulos, BorderLayout.CENTER);

        btnSalir.addActionListener(e -> {
            new FrmInicio().setVisible(true);
            dispose();
        });

        // Cada vez que se vuelve a esta ventana, se actualizan los numeros
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarTablero();
            }
        });
    }

    private JButton modulo(String texto, Supplier<JFrame> ventana) {
        JButton b = Estilo.boton(texto);
        b.putClientProperty("JButton.buttonType", null);   // esquinas normales, no "pastilla"
        b.putClientProperty("FlatLaf.style", "arc: 18");
        b.setFont(b.getFont().deriveFont(17f));
        b.addActionListener(e -> ventana.get().setVisible(true));
        return b;
    }

    private void cargarTablero() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                AsistenciaDAOImpl asis = new AsistenciaDAOImpl();
                return new int[]{
                    asis.listarAbiertas().size(),
                    asis.listarPorFecha(LocalDate.now()).size(),
                    new SuscripcionDAOImpl().listarPorVencer(5).size(),
                    new ClienteDAOImpl().listar().size()
                };
            }

            @Override
            protected void done() {
                try {
                    int[] d = get();
                    tablero.removeAll();
                    tablero.add(Estilo.dato(String.valueOf(d[0]), "Dentro ahora", Estilo.VERDE));
                    tablero.add(Estilo.dato(String.valueOf(d[1]), "Visitas hoy", Estilo.ACENTO));
                    tablero.add(Estilo.dato(String.valueOf(d[2]), "Vencen en 5 dias", Estilo.AMARILLO));
                    tablero.add(Estilo.dato(String.valueOf(d[3]), "Clientes activos", Estilo.TEXTO));
                    tablero.revalidate();
                    tablero.repaint();
                } catch (Exception ex) {
                    Estilo.error(FrmMenuAdmin.this, "No se pudo cargar el tablero: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
