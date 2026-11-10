package com.mycompany.fitaccess;

import com.mycompany.fitaccess.dao.impl.AsistenciaDAOImpl;
import com.mycompany.fitaccess.dao.impl.ClienteDAOImpl;
import com.mycompany.fitaccess.dao.impl.MedidaDAOImpl;
import com.mycompany.fitaccess.dao.impl.PlanDAOImpl;
import com.mycompany.fitaccess.dao.impl.SuscripcionDAOImpl;
import com.mycompany.fitaccess.modelo.Asistencia;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.modelo.Medida;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Conexion;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Prueba de los DAO sin ventanas. Clic derecho en este archivo > Run File.
 * Crea un cliente de prueba (cedula 1710034065), lo lista, le pone suscripcion,
 * entrada, salida y peso. Al final lo desactiva.
 */
public class PruebaDAO {

    public static void main(String[] args) {
        System.out.println("Conexion: " + (Conexion.probar() ? "OK" : "FALLA"));

        PlanDAOImpl planes = new PlanDAOImpl();
        System.out.println("Planes:");
        for (Plan p : planes.listarActivos()) {
            System.out.println("  " + p);
        }

        ClienteDAOImpl clientes = new ClienteDAOImpl();
        Cliente c = clientes.buscarPorCedula("1710034065");
        if (c == null) {
            c = new Cliente();
            c.setCedula("1710034065");
            c.setNombres("Prueba");
            c.setApellidos("DAO");
            c.setFechaNacimiento(LocalDate.of(2003, 5, 10));
            c.setTelefono("0991234567");
            c.setCorreo("prueba@uta.edu.ec");
            c.setAlturaCm(172);
            c.setConsentimiento(true);
            clientes.insertar(c);
        }
        System.out.println("Cliente: " + c + " id=" + c.getId() + " edad=" + c.getEdad());
        System.out.println("Clientes activos: " + clientes.listar().size());

        SuscripcionDAOImpl suscripciones = new SuscripcionDAOImpl();
        Suscripcion s = new Suscripcion();
        s.setClienteId(c.getId());
        s.setPlanId(planes.listarActivos().get(0).getId());
        s.setFechaInicio(LocalDate.now());
        s.setFechaFin(LocalDate.now().plusDays(29));
        suscripciones.insertar(s);
        System.out.println("Vigente: " + (suscripciones.buscarVigente(c.getId()) != null)
                + "  dias restantes: " + suscripciones.buscarVigente(c.getId()).diasRestantes());

        AsistenciaDAOImpl asistencias = new AsistenciaDAOImpl();
        Asistencia a = new Asistencia();
        a.setClienteId(c.getId());
        a.setFecha(LocalDate.now());
        a.setHoraEntrada(LocalDateTime.now().minusMinutes(95));
        asistencias.insertar(a);
        System.out.println("Entrada abierta: " + (asistencias.buscarAbierta(c.getId()) != null));
        asistencias.registrarSalida(a.getId(), LocalDateTime.now(), 95, false);
        System.out.println("Minutos hoy: " + asistencias.minutosDelDia(c.getId(), LocalDate.now()));

        MedidaDAOImpl medidas = new MedidaDAOImpl();
        medidas.insertar(new Medida(c.getId(), LocalDate.now(), 70.5));
        System.out.println("Ultimo peso: " + medidas.ultima(c.getId()).getPesoKg());

        clientes.eliminar(c.getId());
        System.out.println("Cliente desactivado. Todo OK.");
    }
}
