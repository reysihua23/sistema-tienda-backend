package com.reydi.tienda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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

    // ✅ ENUM MODIFICADO CON @JsonCreator y @JsonValue
    public enum MetodoPago {
        TARJETA("TARJETA"),
        YAPE("YAPE"),
        PLIN("PLIN"),
        TRANSFERENCIA("TRANSFERENCIA"),
        EFECTIVO("EFECTIVO"),
        PAYPAL("PAYPAL");

        private final String value;

        MetodoPago(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static MetodoPago fromString(String value) {
            if (value == null) return null;

            // Buscar por el valor (ej: "PAYPAL" -> PAYPAL)
            for (MetodoPago metodo : MetodoPago.values()) {
                if (metodo.value.equalsIgnoreCase(value)) {
                    return metodo;
                }
            }

            // Si no encuentra, intentar por el nombre del ENUM
            try {
                return MetodoPago.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no encuentra, lanzar excepción con mensaje claro
                throw new IllegalArgumentException("Método de pago inválido: " + value +
                        ". Valores válidos: " + String.join(", ", getValidValues()));
            }
        }

        // Método auxiliar para mostrar valores válidos
        public static String[] getValidValues() {
            MetodoPago[] values = MetodoPago.values();
            String[] validValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                validValues[i] = values[i].value;
            }
            return validValues;
        }
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
