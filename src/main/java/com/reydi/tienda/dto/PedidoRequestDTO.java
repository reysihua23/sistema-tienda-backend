package com.reydi.tienda.dto;

import com.reydi.tienda.model.Pago;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class PedidoRequestDTO {
    private Integer clienteId;
    private Pago.MetodoPago metodoPago;
    private String origen;
    private String metodoEnvio;
    private String direccionEnvio;
    private String emailComprobante;
    private List<DetallePedidoRequestDTO> productos;



    // ✅ AGREGAR ESTOS CAMPOS (son los que envía el frontend)
    private String clienteEmail;           // ← Correo del cliente registrado
    private Map<String, Object> paypalData; // ← Datos de PayPal
    private BigDecimal montoUSD;            // ← Monto en dólares
    private BigDecimal montoPEN;            // ← Monto en soles


    @Data
    public static class DetallePedidoRequestDTO {
        private Integer productoId;
        private Integer cantidad;
        private BigDecimal precioUnitario;
    }
}