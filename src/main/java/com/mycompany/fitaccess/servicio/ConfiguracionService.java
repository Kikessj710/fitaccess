package com.mycompany.fitaccess.servicio;

import com.mycompany.fitaccess.dao.impl.ConfiguracionDAOImpl;
import com.mycompany.fitaccess.modelo.ConfiguracionGym;
import com.mycompany.fitaccess.util.Config;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** Acceso centralizado a la configuración comercial y de horarios. */
public final class ConfiguracionService {
    private static volatile ConfiguracionGym actual = new ConfiguracionGym();

    private ConfiguracionService() { }

    public static void cargar() {
        try { aplicar(new ConfiguracionDAOImpl().obtener()); }
        catch (RuntimeException e) { System.err.println("Se usaran horarios locales: " + e.getMessage()); }
    }

    public static ConfiguracionGym obtener() { return actual; }

    public static void guardar(ConfiguracionGym configuracion) {
        new ConfiguracionDAOImpl().guardar(configuracion);
        aplicar(configuracion);
    }

    public static void aplicar(ConfiguracionGym configuracion) {
        if (configuracion == null) return;
        actual = configuracion;
        Config.aplicarHorario(configuracion.getHoraCierre(), configuracion.getDiasAtencion(), configuracion.getFeriados());
    }

    public static String nombreGimnasio() { return actual.getNombre(); }
    public static LocalTime horaApertura() { return actual.getHoraApertura(); }
    public static LocalTime horaCierre() { return actual.getHoraCierre(); }
    public static boolean atiende(LocalDate fecha) { return actual.atiende(fecha); }
    public static String diasTexto() {
        return actual.getDiasAtencion().stream().sorted().map(DayOfWeek::toString).reduce((a, b) -> a + ", " + b).orElse("Ninguno");
    }
}
