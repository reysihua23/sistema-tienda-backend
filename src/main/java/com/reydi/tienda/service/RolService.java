package com.reydi.tienda.service;

import com.reydi.tienda.model.Rol;
import com.reydi.tienda.model.TipoRol;
import java.util.List;
import java.util.Optional;

public interface RolService {
    List<Rol> listarTodos();
    // Buscar por id
    Optional<Rol> buscarPorId(Integer id);

    //Busca por nombre al rol
    Optional<Rol> buscarPorNombre(TipoRol nombre);
    Rol guardar(Rol rol);
    Rol actualizar(Rol rol);
    void eliminar(Integer id);
}