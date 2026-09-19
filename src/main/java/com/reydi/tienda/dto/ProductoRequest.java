//para recibir producto con stock
package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stockMinimo;
    private Boolean activo;
    private Integer stock;
    private String categoria;

    //Campos para descuentos
    private Integer porcentajeDescuento;
    private  BigDecimal precioDescuento;
    private Boolean descuentoActivo;
    private LocalDate fechaInicioDescuento;
    private LocalDate fechaFinDescuento;

}