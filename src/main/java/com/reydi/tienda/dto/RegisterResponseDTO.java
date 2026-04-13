package com.reydi.tienda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponseDTO {
    private boolean success;
    private String message;
    private Integer clienteId;
    private Integer usuarioId;
}