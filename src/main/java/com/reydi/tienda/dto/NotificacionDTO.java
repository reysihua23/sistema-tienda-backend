// src/main/java/com/reydi/tienda/dto/NotificacionDTO.java
package com.reydi.tienda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private Integer id;
    private Integer usuarioId;
    private String usuarioNombre;   // ← nuevo (opcional)
    private String tipo;
    private String mensaje;
    private Boolean leido;
    private LocalDateTime fecha;
}