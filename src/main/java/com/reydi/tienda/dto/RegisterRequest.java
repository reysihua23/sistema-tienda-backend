package com.reydi.tienda.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String correo;
    private String password;
    private String nombre;
    private String telefono;
    private String documento;
    private String direccion;
}