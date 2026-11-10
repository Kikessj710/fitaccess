package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.hardware.ArduinoPuerto;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.Estilo;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/** Pantalla de bienvenida: Iniciar sesion (rostro), Registrarse o Administrador. */
public class FrmInicio extends JFrame {

    private final JLabel lblEstado = Estilo.etiqueta(" ");

    public FrmInicio() {
        Estilo.ventana(this, "Inicio", 900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        Estilo.transparente(centro);

        JLabel logo = Estilo.logo(56);
        JLabel lema = Estilo.subtitulo("Control de acceso inteligente para tu gimnasio");
        JButton btnLogin = Estilo.boton("Iniciar sesion con mi rostro");
        JButton btnRegistro = Estilo.botonSecundario("Registrarme");
        JButton btnAdmin = Estilo.botonSecundario("Entrar como administrador");

        for (Component c : new Component[]{logo, lema, btnLogin, btnRegistro, btnAdmin}) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        btnLogin.setMaximumSize(new java.awt.Dimension(320, 52));
        btnRegistro.setMaximumSize(new java.awt.Dimension(320, 44));
        btnAdmin.setMaximumSize(new java.awt.Dimension(320, 44));

        centro.add(logo);
        centro.add(Box.createVerticalStrut(6));
        centro.add(lema);
        centro.add(Box.createVerticalStrut(40));
        centro.add(btnLogin);
        centro.add(Box.createVerticalStrut(14));
        centro.add(btnRegistro);
        centro.add(Box.createVerticalStrut(10));
        centro.add(btnAdmin);

        JPanel contenedor = new JPanel(new GridBagLayout());
        Estilo.transparente(contenedor);
        contenedor.add(centro);
        add(contenedor, BorderLayout.CENTER);

        // Barra inferior: estado de la base y del Arduino
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        barra.setBackground(Estilo.PANEL);
        JButton btnPuerto = Estilo.botonSecundario("Puerto Arduino");
        btnPuerto.setPreferredSize(new java.awt.Dimension(150, 30));
        barra.add(lblEstado);
        barra.add(btnPuerto);
        add(barra, BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> {
            new FrmLoginFacial().setVisible(true);
            dispose();
        });
        btnRegistro.addActionListener(e -> new FrmRegistro().setVisible(true));
        btnAdmin.addActionListener(e -> {
            new FrmLoginAdmin().setVisible(true);
            dispose();
        });
        btnPuerto.addActionListener(e -> elegirPuerto());

        actualizarEstado();
        new Timer(5000, e -> actualizarEstado()).start();
    }

    private void actualizarEstado() {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                String bd = Conexion.probar() ? "BD: conectada" : "BD: SIN CONEXION (encienda MySQL en XAMPP)";
                ArduinoPuerto ar = ArduinoPuerto.get();
                String arduino = ar.conectado() ? "Arduino: " + ar.nombrePuerto() : "Arduino: sin conectar";
                return bd + "     |     " + arduino;
            }

            @Override
            protected void done() {
                try {
                    lblEstado.setText(get());
                } catch (Exception ignored) {
                    // no pasa nada si falla una actualizacion
                }
            }
        }.execute();
    }

    /** Permite elegir el puerto COM si la deteccion automatica no lo encontro. */
    private void elegirPuerto() {
        ArduinoPuerto ar = ArduinoPuerto.get();
        String[] puertos = ar.listarPuertos();
        if (puertos.length == 0) {
            Estilo.error(this, "No hay puertos COM. Conecte el Arduino por USB.");
            return;
        }
        JComboBox<String> combo = new JComboBox<>(puertos);
        if (JOptionPane.showConfirmDialog(this, combo, "Elija el puerto del Arduino",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
            return;
        }
        String elegido = (String) combo.getSelectedItem();
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return ar.conectar(elegido);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        Estilo.info(FrmInicio.this, "Arduino conectado en " + elegido);
                    } else {
                        Estilo.error(FrmInicio.this, "No se pudo abrir " + elegido
                                + ". Cierre el Monitor Serie del Arduino IDE.");
                    }
                } catch (Exception ignored) {
                    // sin accion
                }
                actualizarEstado();
            }
        }.execute();
    }
}
