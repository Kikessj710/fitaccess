package com.mycompany.fitaccess.dao;

import com.mycompany.fitaccess.modelo.Admin;

public interface AdminDAO {

    int insertar(Admin a);

    Admin buscarPorUsuario(String usuario);

    int contar();
}
