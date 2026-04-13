package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_comprobante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleComprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "igv_item", precision = 10, scale = 2)
    private BigDecimal igvItem;

    @Column(name = "total_item", precision = 10, scale = 2)
    private BigDecimal totalItem;
}