package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicios_tecnicos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicioTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ser_tec")
    private Integer id;

    @Column(length = 150)
    private String equipo;

    @Column(columnDefinition = "TEXT")
    private String problema;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(precision = 10, scale = 2)
    private BigDecimal costo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoServicio estado;

    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // Relación con el técnico (usuario)
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoServicio.RECIBIDO;
        }
        if (this.costo == null) {
            this.costo = BigDecimal.ZERO;
        }
    }
}