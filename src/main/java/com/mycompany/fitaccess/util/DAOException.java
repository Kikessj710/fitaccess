package com.mycompany.fitaccess.util;

/** Error de base de datos que las ventanas pueden mostrar con un mensaje claro. */
public class DAOException extends RuntimeException {

    public DAOException(String mensaje, Throwable causa) {
        super(mensaje + ": " + causa.getMessage(), causa);
    }
}
