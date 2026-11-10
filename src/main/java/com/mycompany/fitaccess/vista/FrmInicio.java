package com.mycompany.fitaccess.vista;

import com.mycompany.fitaccess.util.Estilo;
import java.awt.GridBagLayout;
import javax.swing.JFrame;

/** ESQUELETO: Sebastian reemplaza este archivo completo con el codigo real. */
public class FrmInicio extends JFrame {

    public FrmInicio() {
        Estilo.ventana(this, "Inicio (en construccion)", 900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        add(Estilo.logo(56));
    }
}
