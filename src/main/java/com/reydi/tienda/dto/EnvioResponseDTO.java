package com.reydi.tienda.dto;

import com.reydi.tienda.model.EstadoEnvio;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EnvioResponseDTO {
    private Integer id;
    private Integer pedidoId;
    private String metodoEnvio;
    private String direccion;
    private BigDecimal costoEnvio;
    private EstadoEnvio estado;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaEntrega;
    private String codigoSeguimiento;
    private LocalDateTime fecha;
}