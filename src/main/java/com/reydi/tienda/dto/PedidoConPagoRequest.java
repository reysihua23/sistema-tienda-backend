package com.reydi.tienda.dto;

import com.reydi.tienda.model.EstadoPedido;
import com.reydi.tienda.model.Pago;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PedidoConPagoRequest {
    private Integer clienteId;
    private BigDecimal total;
    private EstadoPedido estado;
    private Pago.MetodoPago metodoPago;
}