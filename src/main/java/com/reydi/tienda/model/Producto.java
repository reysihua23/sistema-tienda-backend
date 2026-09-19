package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    private Boolean activo = true;

    // ✅ NUEVO CAMPO: CATEGORÍA
    @Column(length = 50)
    private String categoria = "otros";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    //====================================
    // DESCUENTOS
    //====================================

    // Porcentaje de descuento aplicado al productos
    @Column(name = "porcentaje_descuento")
    private Integer porcentajeDescuento = 0;

    // Precio final con el descuento ya aplicado (se calcula automaticamente)
    @Column(name = "precio_descuento", precision = 10, scale = 2)
    private BigDecimal precioDescuento = BigDecimal.ZERO;

    // Indica si el descuento está actualmente activo
    @Column(name = "descuento_activo")
    private Boolean descuentoActivo = false;

    // Fecha inicio del descuento
    @Column(name = "fecha_inicio_descuento")
    private LocalDate fechaInicioDescuento;

    // Fecha fin del descuento
    @Column(name = "fecha_fin_descuento")
    private LocalDate fechaFinDescuento;

    // Relación con Stock
    @OneToOne(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Stock stock;

    // Relación con imágenes (una producto tiene muchas imágenes)
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductoImagen> imagenes;

    // Relación con DetallePedido
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles = new ArrayList<>();

    // Metodos para decuento
    /**
     * Obtiene el precio actual del producto.
     * Si el descuento está activo y vigente, retorna el precio con descuento.
     * Si no, retorna el precio original.
     *
     * @return Precio actual (con descuento si aplica)
     */
    public BigDecimal getPrecioActual() {
        if (isDescuentoVigente() && precioDescuento != null && precioDescuento.compareTo(BigDecimal.ZERO) > 0) {
            return precioDescuento;
        }
        return precio;
    }

    /**
     * Verifica si el descuento está activo y dentro del rango de fechas.
     *
     * @return true si el descuento está vigente, false en caso contrario
     */
    public boolean isDescuentoVigente() {
        // El descuento debe estar activo, tener porcentaje y ser mayor que 0
        if (!Boolean.TRUE.equals(descuentoActivo) || porcentajeDescuento == null || porcentajeDescuento <= 0) {
            return false;
        }
        // Si tiene fechas definidas, verificar que la fecha actual esté dentro del rango
        if (fechaInicioDescuento != null && fechaFinDescuento != null) {
            LocalDate hoy = LocalDate.now();
            return (hoy.isEqual(fechaInicioDescuento) || hoy.isAfter(fechaInicioDescuento)) &&
                    (hoy.isEqual(fechaFinDescuento) || hoy.isBefore(fechaFinDescuento));
        }
        return true;
    }

    /**
     * Calcula el precio con descuento basado en el porcentaje.
     * Fórmula: precio - (precio * porcentaje / 100)
     *
     * @return Precio con descuento aplicado
     */
    public BigDecimal calcularPrecioConDescuento() {
        if (Boolean.TRUE.equals(descuentoActivo) && porcentajeDescuento != null && porcentajeDescuento > 0 && precio != null) {
            BigDecimal descuento = precio.multiply(BigDecimal.valueOf(porcentajeDescuento))
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            return precio.subtract(descuento);
        }
        return precio;
    }

    // =========================================================
    // MÉTODOS DE CICLO DE VIDA
    // =========================================================

    /**
     * Se ejecuta antes de persistir el producto por primera vez.
     * Inicializa valores por defecto para los campos de descuento.
     */
    @PrePersist
    protected void onCreate() {
        if (stockMinimo == null) stockMinimo = 5;
        if (activo == null) activo = true;
        if (porcentajeDescuento == null) porcentajeDescuento = 0;
        if (precioDescuento == null) precioDescuento = BigDecimal.ZERO;
        if (descuentoActivo == null) descuentoActivo = false;
        // Calcular precio con descuento al crear
        this.precioDescuento = calcularPrecioConDescuento();
    }

    /**
     * Se ejecuta antes de actualizar el producto.
     * Recalcula el precio con descuento automáticamente.
     */
    @PreUpdate
    protected void onUpdate() {
        // Recalcular precio con descuento automáticamente al actualizar
        this.precioDescuento = calcularPrecioConDescuento();
    }
}