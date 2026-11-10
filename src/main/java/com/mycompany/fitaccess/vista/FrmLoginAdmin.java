package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.dao.AdminDAO;
import com.mycompany.fitaccess.dao.impl.AdminDAOImpl;
import com.mycompany.fitaccess.modelo.Admin;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Hash;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Login del administrador con usuario y contrasena cifrada (BCrypt). */
public class FrmLoginAdmin extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final AdminDAO adminDAO = new AdminDAOImpl();
    private int intentos = 0;

    public FrmLoginAdmin() {
        Estilo.ventana(this, "Administrador", 520, 460);
        setLayout(new GridBagLayout());

        JPanel tarjeta = Estilo.tarjeta();
        tarjeta.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 10, 6, 10);

        txtUsuario.putClientProperty("JTextField.placeholderText", "usuario");
        txtClave.putClientProperty("JTextField.placeholderText", "contrasena");
        txtClave.putClientProperty("JPasswordField.showRevealButton", true);
        JButton btnIngresar = Estilo.boton("Ingresar");
        JButton btnVolver = Estilo.botonSecundario("Volver");

        g.gridy = 0; tarjeta.add(Estilo.logo(30), g);
        g.gridy = 1; tarjeta.add(Estilo.subtitulo("Acceso de administrador"), g);
        g.gridy = 2; tarjeta.add(Estilo.etiqueta("Usuario"), g);
        g.gridy = 3; tarjeta.add(txtUsuario, g);
        g.gridy = 4; tarjeta.add(Estilo.etiqueta("Contrasena"), g);
        g.gridy = 5; tarjeta.add(txtClave, g);
        g.gridy = 6; g.insets = new Insets(18, 10, 6, 10); tarjeta.add(btnIngresar, g);
        g.gridy = 7; g.insets = new Insets(4, 10, 6, 10); tarjeta.add(btnVolver, g);
        add(tarjeta);

        btnIngresar.addActionListener(e -> ingresar());
        txtClave.addActionListener(e -> ingresar());   // Enter en la contrasena
        btnVolver.addActionListener(e -> volver());
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());
        if (usuario.isEmpty() || clave.isEmpty()) {
            Estilo.error(this, "Ingrese usuario y contrasena");
            return;
        }
        try {
            Admin admin = adminDAO.buscarPorUsuario(usuario);
            if (admin != null && Hash.verificar(clave, admin.getClaveHash())) {
                new FrmMenuAdmin(admin).setVisible(true);
                dispose();
                return;
            }
        } catch (RuntimeException ex) {
            Estilo.error(this, ex.getMessage());
            return;
        }
        intentos++;
        txtClave.setText("");
        if (intentos >= 3) {
            Estilo.error(this, "Demasiados intentos fallidos");
            volver();
        } else {
            Estilo.error(this, "Usuario o contrasena incorrectos (" + intentos + " de 3)");
        }
    }

    private void volver() {
        new FrmInicio().setVisible(true);
        dispose();
    }
}
