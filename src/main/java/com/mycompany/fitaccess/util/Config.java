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
import java.util.Properties;
import java.util.Set;

/** Lee config.properties (en la carpeta del proyecto). Si falta, usa valores por defecto. */
public final class Config {

    private static final Properties P = new Properties();

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

    private Config() {
    }

    public static String get(String clave, String porDefecto) {
        String v = P.getProperty(clave);
        return (v == null || v.isBlank()) ? porDefecto : v.trim();
    }

    public static int getInt(String clave, int porDefecto) {
        try {
            return Integer.parseInt(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    public static double getDouble(String clave, double porDefecto) {
        try {
            return Double.parseDouble(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    /** Hora de cierre del gimnasio (por defecto 22:00). */
    public static LocalTime horaCierre() {
        try {
            return LocalTime.parse(get("gimnasio.hora.cierre", "22:00"));
        } catch (Exception e) {
            return LocalTime.of(22, 0);
        }
    }

    /** Dias que abre el gimnasio (por defecto lunes a sabado). */
    public static Set<DayOfWeek> diasApertura() {
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        for (String d : get("gimnasio.dias.apertura", "1,2,3,4,5,6").split(",")) {
            try {
                dias.add(DayOfWeek.of(Integer.parseInt(d.trim())));
            } catch (Exception ignored) {
                // valor invalido: se ignora
            }
        }
        return dias.isEmpty() ? EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY) : dias;
    }

    public static boolean abre(LocalDate fecha) {
        return diasApertura().contains(fecha.getDayOfWeek());
    }
}
