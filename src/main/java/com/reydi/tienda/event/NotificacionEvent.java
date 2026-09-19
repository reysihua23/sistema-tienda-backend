package com.reydi.tienda.event;

import com.reydi.tienda.dto.NotificacionDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificacionEvent extends ApplicationEvent {
    private final Integer usuarioId;
    private final NotificacionDTO notificacion;

    public NotificacionEvent(Object source, Integer usuarioId, NotificacionDTO notificacion) {
        super(source);
        this.usuarioId = usuarioId;
        this.notificacion = notificacion;
    }
}