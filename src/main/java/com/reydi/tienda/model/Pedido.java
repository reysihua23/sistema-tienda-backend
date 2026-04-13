package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false)
    private OrigenPedido origen = OrigenPedido.TIENDA_ONLINE;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Pago pago;


    // AGREGAR RELACIÓN CON ENVÍO
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL)
    private Envio envio;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoPedido.PENDIENTE;
        }
        if (this.total == null) {
            this.total = BigDecimal.ZERO;
        }
    }

    // Método para calcular total desde los detalles
    public BigDecimal calcularTotalDesdeDetalles() {
        if (detalles == null || detalles.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para actualizar el total basado en los detalles
    public void actualizarTotal() {
        this.total = calcularTotalDesdeDetalles();
    }
}