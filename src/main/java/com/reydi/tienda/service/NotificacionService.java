package com.reydi.tienda.service;

import com.reydi.tienda.dto.NotificacionDTO;
import com.reydi.tienda.model.Notificacion;

import java.util.List;

public interface NotificacionService {

    // ✅ Por usuario
    List<NotificacionDTO> obtenerPorUsuario(Integer usuarioId);
    List<NotificacionDTO> obtenerNoLeidasPorUsuario(Integer usuarioId);
    Long contarNoLeidasPorUsuario(Integer usuarioId);
    boolean marcarComoLeida(Integer notificacionId, Integer usuarioId);
    int marcarTodasComoLeidas(Integer usuarioId);
    boolean eliminarNotificacion(Integer notificacionId, Integer usuarioId);
    void eliminarTodas(Integer usuarioId);

    // ✅ Crear (único método, con usuarioId)
    NotificacionDTO crearNotificacion(Integer usuarioId, String tipo, String mensaje);

    // ✅ Admin
    List<NotificacionDTO> obtenerTodas();
    List<NotificacionDTO> obtenerNoLeidas();
    Long contarNoLeidas();
    boolean marcarComoLeida(Integer id);
    int marcarTodasComoLeidas();
    boolean eliminarNotificacion(Integer id);
    void eliminarTodas();

    // ✅ Conversión
    NotificacionDTO convertirADTO(Notificacion notificacion);
}