package com.reydi.tienda.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UsuarioDTO {
    private Integer id;
    private String correo;
    private String nombre;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private String rol;
    private String nombreMostrar;

    // Datos del cliente (solo si tiene cliente asociado)
    private Integer clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private LocalDateTime clienteFechaRegistro;
}