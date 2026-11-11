package com.mycompany.fitaccess.util;
import java.time.LocalDate;

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
    
    public static boolean textoObligatorio(String texto) {
    return texto != null && !texto.trim().isEmpty();
}

public static boolean usuarioValido(String usuario) {
    return usuario != null
            && usuario.matches("[A-Za-z0-9_]{4,30}");
}

public static boolean claveValida(String clave) {
    return clave != null && clave.length() >= 8;
}

public static boolean enteroPositivo(String texto) {
    if (texto == null || !texto.trim().matches("\\d+")) {
        return false;
    }

    try {
        return Integer.parseInt(texto.trim()) > 0;
    } catch (NumberFormatException e) {
        return false;
    }
}

public static boolean precioValido(String texto) {
    Double precio = decimal(texto);
    return precio != null && precio > 0 && precio <= 100000;
}

public static boolean fechaNoFutura(LocalDate fecha) {
    return fecha != null && !fecha.isAfter(LocalDate.now());
}

public static boolean rangoFechasValido(
        LocalDate inicio,
        LocalDate fin) {
    return inicio != null
            && fin != null
            && !fin.isBefore(inicio);
}

public static boolean metodoPagoValido(String metodo) {
    return metodo != null
            && (
                metodo.equals("Efectivo")
                || metodo.equals("Transferencia")
                || metodo.equals("Tarjeta")
            );
}

    
}
