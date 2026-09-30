// src/main/java/com/reydi/tienda/model/Notificacion.java
package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    // Nueva relación con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "mensaje", length = 255)
    private String mensaje;

    // 👇 NUEVO
    /**
     * ID del recurso asociado. El significado depende de {@link #tipo}:
     *   STOCK, PRODUCTO     → producto.id
     *   PEDIDO, PAGO, ENVIO → pedido.id
     *   SERVICIO            → servicio_tecnico.id
     *   RECLAMO             → reclamo.id
     *   (null si no aplica)
     */
    @Column(name = "referencia_id")
    private Integer referenciaId;


    @Column(name = "leido")
    private Boolean leido = false;

    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
        if (leido == null) leido = false;
    }
}