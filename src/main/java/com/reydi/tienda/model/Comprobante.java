package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(name = "tipo_comprobante", nullable = false)
    private String tipoComprobante;

    @Column(name = "serie", length = 4)
    private String serie;

    @Column(name = "numero", length = 8)
    private String numero;

    @Column(name = "numero_comprobante", unique = true)
    private String numeroComprobante;

    // Datos del cliente
    @Column(name = "cliente_nombre")
    private String clienteNombre;

    @Column(name = "cliente_documento")
    private String clienteDocumento;

    @Column(name = "cliente_direccion")
    private String clienteDireccion;

    @Column(name = "cliente_telefono")
    private String clienteTelefono;

    @Column(name = "cliente_email")
    private String clienteEmail;

    // Datos de la empresa
    @Column(name = "empresa_ruc")
    private String empresaRuc;

    @Column(name = "empresa_nombre")
    private String empresaNombre;

    @Column(name = "empresa_direccion")
    private String empresaDireccion;

    @Column(name = "empresa_telefono")
    private String empresaTelefono;

    // Totales
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    private String estado;

    @PrePersist
    public void prePersist() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "EMITIDO";
        }
    }
}