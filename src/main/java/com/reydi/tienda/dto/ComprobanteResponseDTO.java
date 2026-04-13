package com.reydi.tienda.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ComprobanteResponseDTO {
    private Integer id;
    private String tipoComprobante;
    private String serie;
    private String numero;
    private String numeroComprobante;
    private Integer pedidoId;
    private String clienteNombre;
    private String clienteDocumento;
    private String clienteDireccion;
    private String clienteTelefono;
    private String clienteEmail;
    private String empresaRuc;
    private String empresaNombre;
    private String empresaDireccion;
    private String empresaTelefono;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private LocalDateTime fechaEmision;
    private String estado;
    private List<DetalleComprobanteResponseDTO> detalles;

    @Data
    public static class DetalleComprobanteResponseDTO {
        private Integer id;
        private Integer productoId;
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private BigDecimal igvItem;
        private BigDecimal totalItem;
    }
}