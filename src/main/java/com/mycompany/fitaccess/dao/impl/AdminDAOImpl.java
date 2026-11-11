package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.AdminDAO;
import com.mycompany.fitaccess.modelo.Admin;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdminDAOImpl extends BaseDAO implements AdminDAO {

    @Override
    public int insertar(Admin a) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "INSERT INTO administrador(usuario, clave_hash, nombre) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getUsuario());
            ps.setString(2, a.getClaveHash());
            ps.setString(3, a.getNombre());
            ps.executeUpdate();
            a.setId(idGenerado(ps));
            return a.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo crear el administrador", e);
        }
    }

    @Override
    public Admin buscarPorUsuario(String usuario) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM administrador WHERE usuario=?")) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Admin a = new Admin();
                a.setId(rs.getInt("id"));
                a.setUsuario(rs.getString("usuario"));
                a.setClaveHash(rs.getString("clave_hash"));
                a.setNombre(rs.getString("nombre"));
                return a;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el administrador", e);
        }
    }

    @Override
    public int contar() {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("SELECT COUNT(*) FROM administrador");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DAOException("No se pudo contar administradores", e);
        }
    }
}
