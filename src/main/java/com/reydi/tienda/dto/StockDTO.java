package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDTO {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private Integer cantidad;
    private LocalDateTime fechaActualizacion;

}