package com.reydi.tienda.dto;

import com.reydi.tienda.model.Pago;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoRequestDTO {
    private Integer clienteId;
    private Pago.MetodoPago metodoPago;
    private String origen;
    private String metodoEnvio;
    private String direccionEnvio;
    private String emailComprobante;
    private List<DetallePedidoRequestDTO> productos;



    @Data
    public static class DetallePedidoRequestDTO {
        private Integer productoId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}