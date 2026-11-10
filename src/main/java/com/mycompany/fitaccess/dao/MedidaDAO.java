package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Medida;
import java.util.List;

public interface MedidaDAO {

    int insertar(Medida m);

    /** Historial de peso, del mas antiguo al mas reciente. */
    List<Medida> listarPorCliente(int clienteId);

    /** Ultimo peso registrado, o null. */
    Medida ultima(int clienteId);
}
