package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetallePedidoResponseDTO {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precio;
    private BigDecimal subtotal;
}