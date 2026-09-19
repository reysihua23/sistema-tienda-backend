package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProductoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stockMinimo;

    private Boolean activo;
    private String categoria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer stock;  // Cantidad real desde la tabla stock

    private Integer porcentajeDescuento;
    private BigDecimal precioDescuento;
    private Boolean descuentoActivo;
    private LocalDate fechaInicioDescuento;
    private LocalDate fechaFinDescuento;
    private BigDecimal precioActual;
}