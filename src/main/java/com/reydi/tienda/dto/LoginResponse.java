package com.reydi.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tipo;
    private String correo;
    private String rol;
    private Integer usuarioId;
    private Integer clienteId;
    private String nombre;  //
}