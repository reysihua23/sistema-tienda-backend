package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServicioTecnicoResponseDTO {
    private Integer id;
    private String equipo;
    private String problema;
    private String diagnostico;
    private BigDecimal costo;
    private String estado;
    private LocalDateTime fecha;

    // Información del técnico
    private Integer tecnicoId;
    private String tecnicoNombre;
    private String tecnicoEmail;

    // Datos del cliente (solo lo necesario)
    private Integer clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String clienteDireccion;
}