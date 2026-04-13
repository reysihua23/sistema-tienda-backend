//para recibir producto con stock
package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoRequest {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stockMinimo;
    private Boolean activo;
    private Integer stock;
}