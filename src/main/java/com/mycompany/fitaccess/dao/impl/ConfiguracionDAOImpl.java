package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.ConfiguracionDAO;
import com.mycompany.fitaccess.modelo.ConfiguracionGym;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfiguracionDAOImpl implements ConfiguracionDAO {
    private static final int ID = 1;

    @Override
    public ConfiguracionGym obtener() {
        String sql = "SELECT * FROM configuracion_gimnasio WHERE id=?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new ConfiguracionGym();
                ConfiguracionGym c = new ConfiguracionGym();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDireccion(rs.getString("direccion"));
                c.setTelefono(rs.getString("telefono"));
                c.setCorreo(rs.getString("correo"));
                c.setLogoRuta(rs.getString("logo_ruta"));
                c.setMensajeBienvenida(rs.getString("mensaje_bienvenida"));
                c.setHoraApertura(LocalTime.parse(rs.getString("hora_apertura")));
                c.setHoraCierre(LocalTime.parse(rs.getString("hora_cierre")));
                c.setDiasAtencion(parseDias(rs.getString("dias_atencion")));
                c.setFeriados(parseFeriados(rs.getString("feriados")));
                return c;
            }
        } catch (Exception e) {
            throw new DAOException("No se pudo cargar la configuracion del gimnasio", e);
        }
    }

    @Override
    public void guardar(ConfiguracionGym c) {
        String sql = "UPDATE configuracion_gimnasio SET nombre=?, direccion=?, telefono=?, correo=?, "
                + "logo_ruta=?, mensaje_bienvenida=?, hora_apertura=?, hora_cierre=?, dias_atencion=?, feriados=? WHERE id=?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDireccion());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getCorreo());
            ps.setString(5, c.getLogoRuta());
            ps.setString(6, c.getMensajeBienvenida());
            ps.setString(7, c.getHoraApertura().toString());
            ps.setString(8, c.getHoraCierre().toString());
            ps.setString(9, diasTexto(c.getDiasAtencion()));
            ps.setString(10, feriadosTexto(c.getFeriados()));
            ps.setInt(11, ID);
            if (ps.executeUpdate() == 0) throw new DAOException("No existe la configuracion inicial del gimnasio", null);
        } catch (SQLException e) {
            throw new DAOException("No se pudo guardar la configuracion del gimnasio", e);
        }
    }

    private static Set<DayOfWeek> parseDias(String texto) {
        Set<DayOfWeek> dias = EnumSet.noneOf(DayOfWeek.class);
        if (texto != null) for (String valor : texto.split(",")) {
            try { dias.add(DayOfWeek.of(Integer.parseInt(valor.trim()))); } catch (Exception ignored) { }
        }
        return dias;
    }

    private static Set<LocalDate> parseFeriados(String texto) {
        Set<LocalDate> fechas = new java.util.HashSet<>();
        if (texto != null && !texto.isBlank()) for (String valor : texto.split(",")) {
            try { fechas.add(LocalDate.parse(valor.trim())); } catch (Exception ignored) { }
        }
        return fechas;
    }

    private static String diasTexto(Set<DayOfWeek> dias) {
        return dias.stream().sorted().map(d -> String.valueOf(d.getValue())).collect(Collectors.joining(","));
    }

    private static String feriadosTexto(Set<LocalDate> fechas) {
        return fechas.stream().sorted().map(LocalDate::toString).collect(Collectors.joining(","));
    }
}
