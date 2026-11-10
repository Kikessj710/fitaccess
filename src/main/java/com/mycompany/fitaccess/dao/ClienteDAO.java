package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Cliente;
import java.util.List;

public interface ClienteDAO {

    /** Inserta y devuelve el id generado. */
    int insertar(Cliente c);

    void actualizar(Cliente c);

    /** Borrado logico (activo = false): se conserva su historial de asistencia y pagos. */
    void eliminar(int id);

    Cliente buscarPorId(int id);

    Cliente buscarPorCedula(String cedula);

    /** Solo clientes activos. */
    List<Cliente> listar();
}
