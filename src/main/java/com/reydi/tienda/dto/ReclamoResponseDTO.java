package com.reydi.tienda.dto;

import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReclamoResponseDTO {

    private Integer id;
    private TipoReclamo tipo;
    private String descripcion;
    private EstadoReclamo estado;
    private LocalDateTime fecha;

    // Datos del cliente
    private Integer clienteId;
    private String clienteNombre;
    private String clienteEmail;

    // Datos del pedido (si existe)
    private Integer pedidoId;
    private String pedidoEstado;
    private LocalDateTime pedidoFecha;
}