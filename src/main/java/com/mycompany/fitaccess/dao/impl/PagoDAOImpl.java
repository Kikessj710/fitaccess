package com.mycompany.fitaccess.dao.impl;

import com.mycompany.fitaccess.dao.PagoDAO;
import com.mycompany.fitaccess.modelo.Pago;
import com.mycompany.fitaccess.util.Conexion;
import com.mycompany.fitaccess.util.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PagoDAOImpl extends BaseDAO implements PagoDAO {

    @Override
    public int insertar(Pago p) {
        String sql = "INSERT INTO pago(cliente_id, suscripcion_id, fecha, monto_base, porcentaje_descuento, "
                + "monto_final, metodo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getClienteId());
            ps.setInt(2, p.getSuscripcionId());
            ps.setTimestamp(3, sql(p.getFecha()));
            ps.setDouble(4, p.getMontoBase());
            ps.setDouble(5, p.getPorcentajeDescuento());
            ps.setDouble(6, p.getMontoFinal());
            ps.setString(7, p.getMetodo());
            ps.executeUpdate();
            p.setId(idGenerado(ps));
            return p.getId();
        } catch (SQLException e) {
            throw new DAOException("No se pudo registrar el pago", e);
        }
    }

    @Override
    public List<Pago> listarPorMes(int mes, int anio) {
        List<Pago> lista = new ArrayList<>();
        try (Connection cn = Conexion.getConexion();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT * FROM pago WHERE MONTH(fecha)=? AND YEAR(fecha)=? ORDER BY fecha DESC")) {
            ps.setInt(1, mes);
            ps.setInt(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago p = new Pago();
                    p.setId(rs.getInt("id"));
                    p.setClienteId(rs.getInt("cliente_id"));
                    p.setSuscripcionId(rs.getInt("suscripcion_id"));
                    p.setFecha(fechaHora(rs, "fecha"));
                    p.setMontoBase(rs.getDouble("monto_base"));
                    p.setPorcentajeDescuento(rs.getDouble("porcentaje_descuento"));
                    p.setMontoFinal(rs.getDouble("monto_final"));
                    p.setMetodo(rs.getString("metodo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los pagos", e);
        }
        return lista;
    }
}
