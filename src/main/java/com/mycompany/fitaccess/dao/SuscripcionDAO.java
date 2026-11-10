package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Suscripcion;
import java.util.List;

public interface SuscripcionDAO {

    int insertar(Suscripcion s);

    void actualizar(Suscripcion s);

    /** La suscripcion ACTIVA que cubre el dia de hoy, o null si esta vencido. */
    Suscripcion buscarVigente(int clienteId);

    /** La ultima suscripcion del cliente (vigente o no), o null si nunca tuvo. */
    Suscripcion buscarUltima(int clienteId);

    /** Suscripciones activas que vencen dentro de los proximos "dias" dias. */
    List<Suscripcion> listarPorVencer(int dias);
}
