package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.SuscripcionDAO;
import com.mycompany.fitaccess.modelo.Suscripcion;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SuscripcionDAOImpl extends BaseDAO implements SuscripcionDAO {

    @Override
    public int insertar(Suscripcion s) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO suscripcion(cliente_id, plan_id, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getClienteId());
            ps.setInt(2, s.getPlanId());
            ps.setDate(3, sql(s.getFechaInicio()));
            ps.setDate(4, sql(s.getFechaFin()));
            ps.setString(5, s.getEstado());
            ps.executeUpdate();
            s.setId(idGenerado(ps));
            return s.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear la suscripcion", e);
        }
    }

    @Override
    public void actualizar(Suscripcion s) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE suscripcion SET plan_id=?, fecha_inicio=?, fecha_fin=?, estado=? WHERE id=?")) {
            ps.setInt(1, s.getPlanId());
            ps.setDate(2, sql(s.getFechaInicio()));
            ps.setDate(3, sql(s.getFechaFin()));
            ps.setString(4, s.getEstado());
            ps.setInt(5, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar la suscripcion", e);
        }
    }

    @Override
    public Suscripcion buscarVigente(int clienteId) {
        return buscarUna("SELECT * FROM suscripcion WHERE cliente_id=? AND estado='ACTIVA' "
                + "AND CURDATE() BETWEEN fecha_inicio AND fecha_fin ORDER BY fecha_fin DESC LIMIT 1", clienteId);
    }

    @Override
    public Suscripcion buscarUltima(int clienteId) {
        return buscarUna("SELECT * FROM suscripcion WHERE cliente_id=? ORDER BY fecha_fin DESC, id DESC LIMIT 1",
                clienteId);
    }

    @Override
    public List<Suscripcion> listarPorVencer(int dias) {
        List<Suscripcion> lista = new ArrayList<>();
        String sql = "SELECT s.* FROM suscripcion s JOIN cliente c ON c.id = s.cliente_id "
                + "WHERE c.activo=TRUE AND s.estado='ACTIVA' AND s.fecha_fin BETWEEN CURDATE() AND ? "
                + "AND NOT EXISTS (SELECT 1 FROM suscripcion s2 WHERE s2.cliente_id = s.cliente_id "
                + "AND s2.fecha_inicio > s.fecha_fin) ORDER BY s.fecha_fin";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, sql(LocalDate.now().plusDays(dias)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar las suscripciones por vencer", e);
        }
        return lista;
    }

    private Suscripcion buscarUna(String sql, int clienteId) {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar la suscripcion", e);
        }
    }

    private Suscripcion mapear(ResultSet rs) throws SQLException {
        Suscripcion s = new Suscripcion();
        s.setId(rs.getInt("id"));
        s.setClienteId(rs.getInt("cliente_id"));
        s.setPlanId(rs.getInt("plan_id"));
        s.setFechaInicio(fecha(rs, "fecha_inicio"));
        s.setFechaFin(fecha(rs, "fecha_fin"));
        s.setEstado(rs.getString("estado"));
        return s;
    }
}
