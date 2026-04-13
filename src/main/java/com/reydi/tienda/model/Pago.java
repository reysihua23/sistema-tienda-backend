package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagos")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MetodoPago metodo;

    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EstadoPago estado;

    @Column(name = "referencia_pasarela", length = 255)
    private String referenciaPasarela;

    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    // Enumeraciones
    public enum MetodoPago {
        TARJETA, YAPE, PLIN, TRANSFERENCIA, EFECTIVO,PAYPAL
    }

    public enum EstadoPago {
        PENDIENTE, APROBADO, RECHAZADO
    }

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPago.PENDIENTE;
        }
    }
}