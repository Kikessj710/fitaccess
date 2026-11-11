package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.ConfiguracionGym;

public interface ConfiguracionDAO {
    ConfiguracionGym obtener();
    void guardar(ConfiguracionGym configuracion);
}
