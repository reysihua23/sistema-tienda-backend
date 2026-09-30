package com.reydi.tienda.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Helper para enviar la misma notificación a varios usuarios de una sola vez.
 */
@Component
@RequiredArgsConstructor
public class NotificacionHelper {

    private final NotificacionService notificacionService;

    /**
     * Notifica a varios usuarios.
     */
    public void notificarA(List<Integer> usuarioIds, String tipo, String mensaje, Integer referenciaId) {
        if (usuarioIds == null || usuarioIds.isEmpty()) return;
        for (Integer uid : usuarioIds) {
            notificacionService.crearNotificacion(uid, tipo, mensaje, referenciaId);
        }
    }

    /**
     * Notifica a un solo usuario si no es null.
     */
    public void notificarA(Integer usuarioId, String tipo, String mensaje, Integer referenciaId) {
        if (usuarioId == null) return;
        notificacionService.crearNotificacion(usuarioId, tipo, mensaje, referenciaId);
    }
}