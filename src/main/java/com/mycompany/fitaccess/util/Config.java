package com.mycompany.fitaccess.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** Lee ajustes tecnicos locales y mantiene el horario comercial cargado desde la BD. */
public final class Config {
    private static final Properties P = new Properties();
    private static volatile LocalTime horaCierreConfigurada;
    private static volatile Set<DayOfWeek> diasConfigurados;
    private static volatile Set<LocalDate> feriadosConfigurados = Set.of();

    static {
        File archivo = new File("config.properties");
        if (archivo.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8)) {
                P.load(r);
            } catch (IOException e) {
                System.err.println("No se pudo leer config.properties: " + e.getMessage());
            }
        } else {
            System.err.println("Aviso: no existe config.properties, se usan valores por defecto");
        }
    }

    private Config() { }

    public static String get(String clave, String porDefecto) {
        String v = P.getProperty(clave);
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }

    public static int getInt(String clave, int porDefecto) {
        try { return Integer.parseInt(get(clave, String.valueOf(porDefecto))); }
        catch (NumberFormatException e) { return porDefecto; }
    }

    public static double getDouble(String clave, double porDefecto) {
        try { return Double.parseDouble(get(clave, String.valueOf(porDefecto))); }
        catch (NumberFormatException e) { return porDefecto; }
    }

    public static LocalTime horaCierre() {
        LocalTime cargada = horaCierreConfigurada;
        if (cargada != null) return cargada;
        try { return LocalTime.parse(get("gimnasio.hora.cierre", "22:00")); }
        catch (Exception e) { return LocalTime.of(22, 0); }
    }

    public static Set<DayOfWeek> diasApertura() {
        Set<DayOfWeek> cargados = diasConfigurados;
        if (cargados != null) return EnumSet.copyOf(cargados);
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        for (String d : get("gimnasio.dias.apertura", "1,2,3,4,5,6").split(",")) {
            try { dias.add(DayOfWeek.of(Integer.parseInt(d.trim()))); } catch (Exception ignored) { }
        }
        return dias.isEmpty() ? EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY) : dias;
    }

    public static boolean abre(LocalDate fecha) {
        return diasApertura().contains(fecha.getDayOfWeek()) && !feriadosConfigurados.contains(fecha);
    }

    public static void aplicarHorario(LocalTime cierre, Set<DayOfWeek> dias, Set<LocalDate> feriados) {
        horaCierreConfigurada = cierre;
        if (dias == null || dias.isEmpty()) {
            diasConfigurados = null;
        } else {
            diasConfigurados = EnumSet.noneOf(DayOfWeek.class);
            diasConfigurados.addAll(dias);
        }
        feriadosConfigurados = feriados == null ? Set.of() : new HashSet<>(feriados);
    }
}
