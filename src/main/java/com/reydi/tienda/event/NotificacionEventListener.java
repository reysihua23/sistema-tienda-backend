package com.reydi.tienda.event;

import com.reydi.tienda.service.NotificacionWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final NotificacionWebSocketService webSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificacionCreada(NotificacionEvent event) {
        System.out.println("📡 [EVENT] afterCommit → enviando a usuario " + event.getUsuarioId());
        webSocketService.enviarNotificacion(event.getUsuarioId(), event.getNotificacion());
    }
}
