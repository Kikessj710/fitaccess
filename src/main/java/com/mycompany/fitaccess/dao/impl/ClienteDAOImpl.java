package com.mycompany.fitaccess.dao.impl;
import com.mycompany.fitaccess.dao.ClienteDAO;
import com.mycompany.fitaccess.modelo.Cliente;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl extends BaseDAO implements ClienteDAO {

    @Override
    public int insertar(Cliente c) {
        String sql = "INSERT INTO cliente(cedula, nombres, apellidos, fecha_nacimiento, telefono, correo, "
                + "altura_cm, consentimiento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCedula());
            ps.setString(2, c.getNombres());
            ps.setString(3, c.getApellidos());
            ps.setDate(4, sql(c.getFechaNacimiento()));
            ps.setString(5, c.getTelefono());
            ps.setString(6, c.getCorreo());
            ps.setDouble(7, c.getAlturaCm());
            ps.setBoolean(8, c.isConsentimiento());
            ps.executeUpdate();
            c.setId(idGenerado(ps));
            return c.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo registrar el cliente", e);
        }
    }

    @Override
    public void actualizar(Cliente c) {
        String sql = "UPDATE cliente SET nombres=?, apellidos=?, fecha_nacimiento=?, telefono=?, correo=?, "
                + "altura_cm=?, consentimiento=? WHERE id=?";
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getNombres());
            ps.setString(2, c.getApellidos());
            ps.setDate(3, sql(c.getFechaNacimiento()));
            ps.setString(4, c.getTelefono());
            ps.setString(5, c.getCorreo());
            ps.setDouble(6, c.getAlturaCm());
            ps.setBoolean(7, c.isConsentimiento());
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el cliente", e);
        }
    }

    @Override
    public void eliminar(int id) {
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement("UPDATE cliente SET activo=FALSE WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el cliente", e);
        }
    }

    @Override
    public Cliente buscarPorId(int id) {
        return buscarUno("SELECT * FROM cliente WHERE id=?", String.valueOf(id));
    }

    @Override
    public Cliente buscarPorCedula(String cedula) {
        return buscarUno("SELECT * FROM cliente WHERE cedula=?", cedula);
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT * FROM cliente WHERE activo=TRUE ORDER BY apellidos, nombres");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los clientes", e);
        }
        return lista;
    }

    private Cliente buscarUno(String sql, String valor) {
        try (Connection cn = Conexion.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setCedula(rs.getString("cedula"));
        c.setNombres(rs.getString("nombres"));
        c.setApellidos(rs.getString("apellidos"));
        c.setFechaNacimiento(fecha(rs, "fecha_nacimiento"));
        c.setTelefono(rs.getString("telefono"));
        c.setCorreo(rs.getString("correo"));
        c.setAlturaCm(rs.getDouble("altura_cm"));
        c.setConsentimiento(rs.getBoolean("consentimiento"));
        c.setFechaRegistro(fechaHora(rs, "fecha_registro"));
        c.setActivo(rs.getBoolean("activo"));
        return c;
    }
}
