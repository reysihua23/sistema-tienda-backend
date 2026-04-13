package com.reydi.tienda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductoImagenDTO {
    private Integer id;
    private String urlImagen;
    private Boolean principal;
    private LocalDateTime fecha;
    private Integer productoId;
    private String productoNombre;
}