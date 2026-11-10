package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.PlanDAO;
import com.mycompany.fitaccess.modelo.Plan;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlanDAOImpl extends BaseDAO implements PlanDAO {

    @Override
    public int insertar(Plan p) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO plan(nombre, dias, precio, activo) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDias());
            ps.setDouble(3, p.getPrecio());
            ps.setBoolean(4, p.isActivo());
            ps.executeUpdate();
            p.setId(idGenerado(ps));
            return p.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear el plan", e);
        }
    }

    @Override
    public void actualizar(Plan p) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE plan SET nombre=?, dias=?, precio=?, activo=? WHERE id=?")) {
            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getDias());
            ps.setDouble(3, p.getPrecio());
            ps.setBoolean(4, p.isActivo());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el plan", e);
        }
    }

    @Override
    public Plan buscarPorId(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM plan WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el plan", e);
        }
    }

    @Override
    public List<Plan> listarActivos() {
        List<Plan> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM plan WHERE activo=TRUE ORDER BY dias");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los planes", e);
        }
        return lista;
    }

    private Plan mapear(ResultSet rs) throws SQLException {
        Plan p = new Plan();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setDias(rs.getInt("dias"));
        p.setPrecio(rs.getDouble("precio"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
