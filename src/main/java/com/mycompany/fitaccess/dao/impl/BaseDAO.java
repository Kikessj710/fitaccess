package com.mycompany.fitaccess.dao.impl;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Ayudas comunes para todos los DAO (conversion de fechas e id generado). */
abstract class BaseDAO {

    protected static LocalDate fecha(ResultSet rs, String columna) throws SQLException {
        Date d = rs.getDate(columna);
        return d == null ? null : d.toLocalDate();
    }

    protected static LocalDateTime fechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }

    protected static Date sql(LocalDate f) {
        return f == null ? null : Date.valueOf(f);
    }

    protected static Timestamp sql(LocalDateTime f) {
        return f == null ? null : Timestamp.valueOf(f);
    }

    /** Lee el id AUTO_INCREMENT despues de un INSERT. */
    protected static int idGenerado(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
