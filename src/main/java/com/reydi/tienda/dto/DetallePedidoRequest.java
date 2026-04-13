package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public  class DetallePedidoRequest {
    private Integer productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}