package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.MedidaDAO;
import com.mycompany.fitaccess.modelo.Medida;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedidaDAOImpl extends BaseDAO implements MedidaDAO {

    @Override
    public int insertar(Medida m) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO medida(cliente_id, fecha, peso_kg) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getClienteId());
            ps.setDate(2, sql(m.getFecha()));
            ps.setDouble(3, m.getPesoKg());
            ps.executeUpdate();
            m.setId(idGenerado(ps));
            return m.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo guardar el peso", e);
        }
    }

    @Override
    public List<Medida> listarPorCliente(int clienteId) {
        List<Medida> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT * FROM medida WHERE cliente_id=? ORDER BY fecha, id")) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo leer el historial de peso", e);
        }
        return lista;
    }

    @Override
    public Medida ultima(int clienteId) {
        List<Medida> l = listarPorCliente(clienteId);
        return l.isEmpty() ? null : l.get(l.size() - 1);
    }

    private Medida mapear(ResultSet rs) throws SQLException {
        Medida m = new Medida();
        m.setId(rs.getInt("id"));
        m.setClienteId(rs.getInt("cliente_id"));
        m.setFecha(fecha(rs, "fecha"));
        m.setPesoKg(rs.getDouble("peso_kg"));
        return m;
    }
}
