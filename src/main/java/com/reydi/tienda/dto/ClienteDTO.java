package com.reydi.tienda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClienteDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private String documento;      // Si lo necesitas
    private String direccion;       // Si lo necesitas
    private LocalDateTime fechaRegistro;
    private LocalDateTime updatedAt; // Si lo necesitas
}