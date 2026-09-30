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

    // ✅ Crear — MÉTODO VIEJO (mantiene compatibilidad con las 43 llamadas)
    NotificacionDTO crearNotificacion(Integer usuarioId, String tipo, String mensaje);

    // ✅ Crear — MÉTODO NUEVO (con referenciaId)
    NotificacionDTO crearNotificacion(Integer usuarioId, String tipo, String mensaje, Integer referenciaId);

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