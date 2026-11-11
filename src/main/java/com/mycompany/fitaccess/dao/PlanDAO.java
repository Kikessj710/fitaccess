package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Plan;
import java.util.List;

public interface PlanDAO {

    int insertar(Plan p);

    void actualizar(Plan p);

    Plan buscarPorId(int id);

    List<Plan> listarActivos();
}
