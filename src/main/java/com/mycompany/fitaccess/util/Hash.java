package com.mycompany.fitaccess.util;

import org.mindrot.jbcrypt.BCrypt;

/** Cifrado de contrasenas con BCrypt (incluye "sal", mas seguro que SHA-256 solo). */
public final class Hash {

    private Hash() {
    }

    public static String generar(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt(10));
    }

    public static boolean verificar(String clave, String hash) {
        try {
            return hash != null && BCrypt.checkpw(clave, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
