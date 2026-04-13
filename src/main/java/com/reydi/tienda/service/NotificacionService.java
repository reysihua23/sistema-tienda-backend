package com.reydi.tienda.service;

import com.reydi.tienda.model.Notificacion;
import java.util.List;
import java.util.Optional;

public interface NotificacionService {
    List<Notificacion> listarTodos();
    Optional<Notificacion> buscarPorId(Integer id);
    List<Notificacion> listarNoLeidas();
    List<Notificacion> listarPorTipo(String tipo);
    Notificacion guardar(Notificacion notificacion);
    Notificacion actualizar(Notificacion notificacion);
    void eliminar(Integer id);
    void marcarComoLeida(Integer id);
    void marcarTodasComoLeidas();
}