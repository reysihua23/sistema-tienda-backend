package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long id;

    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private EstadoDevolucion estado;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    @OneToOne
    @JoinColumn(name = "reclamo_id", unique = true)
    private Reclamo reclamo;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
}