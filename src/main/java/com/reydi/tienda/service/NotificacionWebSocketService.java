package com.reydi.tienda.service;

import com.reydi.tienda.dto.NotificacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacionWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void enviarNotificacion(Integer usuarioId, NotificacionDTO notificacion) {
        String destination = "/topic/notificaciones/" + usuarioId;
        System.out.println("📤 [WebSocketService] Enviando a: " + destination);
        System.out.println("📤 [WebSocketService] Contenido: " + notificacion.getMensaje());

        messagingTemplate.convertAndSend(destination, notificacion);

        System.out.println("✅ [WebSocketService] Enviado correctamente");
    }
}