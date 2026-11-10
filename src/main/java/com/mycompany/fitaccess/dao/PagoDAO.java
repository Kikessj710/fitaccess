package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Pago;
import java.util.List;

public interface PagoDAO {

    int insertar(Pago p);

    List<Pago> listarPorMes(int mes, int anio);
}
