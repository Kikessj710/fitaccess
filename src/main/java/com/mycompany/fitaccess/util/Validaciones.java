package com.mycompany.fitaccess.util;

/** Validaciones de los formularios. */
public final class Validaciones {

    private Validaciones() {
    }

    /** Cedula ecuatoriana: 10 digitos, provincia valida y digito verificador (modulo 10). */
    public static boolean cedulaValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            return false;
        }
        if (cedula.charAt(2) - '0' >= 6) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int d = (cedula.charAt(i) - '0') * (i % 2 == 0 ? 2 : 1);
            if (d > 9) {
                d -= 9;
            }
            suma += d;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    public static boolean correoValido(String correo) {
        return correo != null && correo.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    /** Celular ecuatoriano: 09 + 8 digitos. Vacio tambien se acepta. */
    public static boolean telefonoValido(String tel) {
        return tel == null || tel.isBlank() || tel.trim().matches("09\\d{8}");
    }

    public static boolean soloLetras(String texto) {
        return texto != null && texto.trim().matches("[\\p{L} ]{2,60}");
    }

    /** Convierte "70,5" o "70.5" a numero. Devuelve null si no es numero. */
    public static Double decimal(String texto) {
        try {
            return Double.parseDouble(texto.trim().replace(',', '.'));
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean enRango(Double valor, double min, double max) {
        return valor != null && valor >= min && valor <= max;
    }
}
