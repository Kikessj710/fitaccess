package com.mycompany.fitaccess;

import com.mycompany.fitaccess.dao.AdminDAO;
import com.mycompany.fitaccess.dao.impl.AdminDAOImpl;
import com.mycompany.fitaccess.hardware.ArduinoPuerto;
import com.mycompany.fitaccess.hardware.ReconocedorFacial;
import com.mycompany.fitaccess.modelo.Admin;
import com.mycompany.fitaccess.servicio.AccesoService;
import com.mycompany.fitaccess.servicio.ConfiguracionService;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.Estilo;
import com.mycompany.fitaccess.util.Hash;
import com.mycompany.fitaccess.vista.FrmInicio;
import javax.swing.SwingUtilities;

/** Punto de entrada de FitAccess. */
public class App {

    public static void main(String[] args) {
        Estilo.aplicarTema();

        if (Conexion.probar()) {
            crearAdminPorDefecto();
            ConfiguracionService.cargar();
            new AccesoService().iniciarCierreAutomatico();
        } else {
            Estilo.error(null, "No hay conexion con MySQL.\n"
                    + "1) Abra XAMPP y presione Start en MySQL\n"
                    + "2) Ejecute sql/fitaccess.sql en phpMyAdmin\n"
                    + "3) Revise db.user y db.password en config.properties");
        }

        // El hardware se prepara en segundo plano: la ventana abre de inmediato
        new Thread(() -> ReconocedorFacial.get().cargar(), "carga-modelo").start();
        new Thread(() -> ArduinoPuerto.get().conectarAuto(), "conexion-arduino").start();

        SwingUtilities.invokeLater(() -> new FrmInicio().setVisible(true));
    }

    /** La primera vez crea el usuario admin / admin123 (cambiarlo despues). */
    private static void crearAdminPorDefecto() {
        try {
            AdminDAO dao = new AdminDAOImpl();
            if (dao.contar() == 0) {
                Admin a = new Admin();
                a.setUsuario("admin");
                a.setClaveHash(Hash.generar("admin123"));
                a.setNombre("Administrador");
                dao.insertar(a);
                System.out.println("Usuario por defecto creado: admin / admin123");
            }
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
