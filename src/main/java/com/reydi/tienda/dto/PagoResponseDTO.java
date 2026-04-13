package com.reydi.tienda.dto;

import com.reydi.tienda.model.Pago;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoResponseDTO {
    private Integer id;
    private Integer pedidoId;
    private Pago.MetodoPago metodo;
    private BigDecimal monto;
    private Pago.EstadoPago estado;
    private String referenciaPasarela;
    private LocalDateTime fecha;
}
