package com.reydi.tienda.dto;

import com.reydi.tienda.model.EstadoPedido;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PedidoDTO {
    private Integer id;
    private BigDecimal total;
    private EstadoPedido estado;
    private LocalDateTime fecha;
    private Integer clienteId;
    private String clienteNombre;
}