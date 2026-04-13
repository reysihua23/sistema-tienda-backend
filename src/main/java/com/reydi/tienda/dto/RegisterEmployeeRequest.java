package com.reydi.tienda.dto;

import lombok.Data;

@Data
public class RegisterEmployeeRequest {
    private String correo;
    private String password;
    private String nombreCompleto;
    private String rol;
}