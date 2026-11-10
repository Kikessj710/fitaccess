package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.AsistenciaDAO;
import com.mycompany.fitaccess.modelo.Asistencia;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AsistenciaDAOImpl extends BaseDAO implements AsistenciaDAO {

    @Override
    public int insertar(Asistencia a) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO asistencia(cliente_id, fecha, hora_entrada) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getClienteId());
            ps.setDate(2, sql(a.getFecha()));
            ps.setTimestamp(3, sql(a.getHoraEntrada()));
            ps.executeUpdate();
            a.setId(idGenerado(ps));
            return a.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo registrar la entrada", e);
        }
    }

    @Override
    public void registrarSalida(int id, LocalDateTime salida, int minutos, boolean automatico) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE asistencia SET hora_salida=?, duracion_minutos=?, cierre_automatico=? WHERE id=?")) {
            ps.setTimestamp(1, sql(salida));
            ps.setInt(2, minutos);
            ps.setBoolean(3, automatico);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo registrar la salida", e);
        }
    }

    @Override
    public Asistencia buscarAbierta(int clienteId) {
        List<Asistencia> l = consultar("SELECT * FROM asistencia WHERE cliente_id=? AND hora_salida IS NULL "
                + "ORDER BY hora_entrada DESC LIMIT 1", clienteId);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public Asistencia buscarUltima(int clienteId) {
        List<Asistencia> l = consultar("SELECT * FROM asistencia WHERE cliente_id=? "
                + "ORDER BY hora_entrada DESC LIMIT 1", clienteId);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override
    public List<Asistencia> listarAbiertas() {
        return consultar("SELECT * FROM asistencia WHERE hora_salida IS NULL ORDER BY hora_entrada");
    }

    @Override
    public List<Asistencia> listarPorFecha(LocalDate fecha) {
        return consultar("SELECT * FROM asistencia WHERE fecha=? ORDER BY hora_entrada", sql(fecha));
    }

    @Override
    public List<Asistencia> listarPorCliente(int clienteId, LocalDate desde, LocalDate hasta) {
        return consultar("SELECT * FROM asistencia WHERE cliente_id=? AND fecha BETWEEN ? AND ? "
                + "ORDER BY hora_entrada", clienteId, sql(desde), sql(hasta));
    }

    @Override
    public Set<LocalDate> fechasAsistidas(int clienteId, LocalDate desde, LocalDate hasta) {
        Set<LocalDate> fechas = new TreeSet<>();
        for (Asistencia a : listarPorCliente(clienteId, desde, hasta)) {
            fechas.add(a.getFecha());
        }
        return fechas;
    }

    @Override
    public int minutosDelDia(int clienteId, LocalDate fecha) {
        int total = 0;
        for (Asistencia a : listarPorCliente(clienteId, fecha, fecha)) {
            if (a.getDuracionMinutos() != null) {
                total += a.getDuracionMinutos();
            } else if (a.estaAbierta()) {
                total += (int) Duration.between(a.getHoraEntrada(), LocalDateTime.now()).toMinutes();
            }
        }
        return total;
    }

    @Override
    public int[] entradasPorHora(int mes, int anio) {
        int[] horas = new int[24];
        String sql = "SELECT HOUR(hora_entrada) AS h, COUNT(*) AS n FROM asistencia "
                + "WHERE MONTH(fecha)=? AND YEAR(fecha)=? GROUP BY HOUR(hora_entrada)";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    horas[rs.getInt("h")] = rs.getInt("n");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo calcular las horas pico", e);
        }
        return horas;
    }

    /** Ejecuta un SELECT con parametros y convierte cada fila en Asistencia. */
    private List<Asistencia> consultar(String sql, Object... parametros) {
        List<Asistencia> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo consultar la asistencia", e);
        }
        return lista;
    }

    private Asistencia mapear(ResultSet rs) throws SQLException {
        Asistencia a = new Asistencia();
        a.setId(rs.getInt("id"));
        a.setClienteId(rs.getInt("cliente_id"));
        a.setFecha(fecha(rs, "fecha"));
        a.setHoraEntrada(fechaHora(rs, "hora_entrada"));
        a.setHoraSalida(fechaHora(rs, "hora_salida"));
        int min = rs.getInt("duracion_minutos");
        a.setDuracionMinutos(rs.wasNull() ? null : min);
        a.setCierreAutomatico(rs.getBoolean("cierre_automatico"));
        return a;
    }
}
