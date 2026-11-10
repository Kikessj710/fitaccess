package com.mycompany.fitaccess.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Entrega conexiones a la base de datos usando los datos de config.properties. */
public final class Conexion {

    private Conexion() {
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(
                Config.get("db.url", "jdbc:mysql://localhost:3306/fitaccess?useSSL=false&serverTimezone=America/Guayaquil&allowPublicKeyRetrieval=true"),
                Config.get("db.user", "root"),
                Config.get("db.password", ""));
    }

    /** true si MySQL esta encendido y la base existe. */
    public static boolean probar() {
        try (Connection c = getConexion()) {
            return c.isValid(2);
        } catch (SQLException e) {
            System.err.println("Sin conexion a la BD: " + e.getMessage());
            return false;
        }
    }
}
