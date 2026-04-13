package com.reydi.tienda.service;

import com.reydi.tienda.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    List<Usuario> listarTodos();
    Optional<Usuario> buscarPorId(Integer id);
    Optional<Usuario> buscarPorCorreo(String correo);
    Usuario guardar(Usuario usuario);
    Usuario actualizar(Usuario usuario);

    // Agregar este método a la interfaz
    Usuario autenticar(String correo, String password);

    void eliminar(Integer id);
    Usuario actualizarActivo(Integer id, Boolean activo);
    List<Usuario> filtrarUsuarios(String correo, Integer rolId, Boolean activo,
                                  LocalDateTime fechaInicio, LocalDateTime fechaFin);
    boolean existeCorreo(String correo);

}