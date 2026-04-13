package com.reydi.tienda.dto;

import com.reydi.tienda.model.EstadoPedido;
import com.reydi.tienda.model.Pago;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PedidoResponseDTO {
    private Integer id;
    private Integer clienteId;
    private String clienteNombre;
    private String clienteEmail;
    private String clienteTelefono;
    private String clienteDocumento;
    private String clienteDireccion;
    private BigDecimal total;
    private Pago.MetodoPago metodoPago;
    private EstadoPedido estado;
    private LocalDateTime fecha;
    private List<DetallePedidoResponseDTO> detalles;
    private PagoResponseDTO pago;
    private EnvioResponseDTO envio;
}