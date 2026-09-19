package com.reydi.tienda.repository;

import com.reydi.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // =========================================================
    // CONSULTAS CON JOIN FETCH (para cargar el stock)
    // =========================================================

    /**
     * Obtiene todos los productos con su stock (LEFT JOIN FETCH)
     * LEFT JOIN asegura que también se incluyan productos sin stock
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock")
    List<Producto> findAll();

    /**
     * Obtiene solo productos activos con su stock
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE p.activo = true")
    List<Producto> findByActivoTrue();

    /**
     * Busca productos por nombre (coincidencia parcial, insensible a mayúsculas)
     * Ejemplo: "iphone" encontrará "iPhone 15 Pro"
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Lista productos con stock bajo (stock real <= stockMinimo)
     * Útil para alertas de inventario
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE p.stock.cantidad <= :stockMinimo")
    List<Producto> findByStockLessThanEqual(@Param("stockMinimo") Integer stockMinimo);

    /**
     * Busca un producto por su ID y carga su stock
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE p.id = :id")
    Optional<Producto> findById(@Param("id") Integer id);

    /**
     * Filtro avanzado con múltiples criterios
     * - nombre: búsqueda parcial (opcional)
     * - activo: estado del producto (opcional)
     * - stockMinimo: stock real menor o igual (opcional)
     * - precioMinimo: precio mayor o igual (opcional)
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE " +
            "(:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:activo IS NULL OR p.activo = :activo) AND " +
            "(:stockMinimo IS NULL OR p.stock.cantidad <= :stockMinimo) AND " +
            "(:precioMinimo IS NULL OR p.precio >= :precioMinimo)")
    List<Producto> filtrarProductosAvanzado(
            @Param("nombre") String nombre,
            @Param("activo") Boolean activo,
            @Param("stockMinimo") Integer stockMinimo,
            @Param("precioMinimo") Double precioMinimo);

    // =========================================================
    // MÉTODOS PARA VALIDAR PRODUCTOS DUPLICADOS
    // =========================================================

    /**
     * Verifica si existe un producto con el mismo nombre (ignorando mayúsculas/minúsculas)
     * Útil para validación simple antes de crear un producto
     *
     * @param nombre Nombre del producto a verificar
     * @return true si ya existe, false si no
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Busca un producto por nombre exacto (ignorando mayúsculas/minúsculas)
     *
     * @param nombre Nombre del producto
     * @return Producto si existe, Optional vacío si no
     */
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe un producto duplicado con el mismo nombre Y la misma descripción
     * Previene creación de productos idénticos
     *
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @return true si ya existe un producto idéntico, false si no
     */
    @Query("SELECT COUNT(p) > 0 FROM Producto p WHERE LOWER(p.nombre) = LOWER(:nombre) AND LOWER(p.descripcion) = LOWER(:descripcion)")
    boolean existeProductoDuplicado(@Param("nombre") String nombre, @Param("descripcion") String descripcion);

    // =========================================================
    // ✅ NUEVOS MÉTODOS PARA DESCUENTOS
    // =========================================================

    /**
     * Lista productos en oferta (descuento activo y dentro del rango de fechas).
     *
     * @param fechaInicio Fecha de inicio para comparar (hoy)
     * @param fechaFin Fecha de fin para comparar (hoy)
     * @return Lista de productos en oferta
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE " +
            "p.descuentoActivo = true AND " +
            "p.porcentajeDescuento > 0 AND " +
            "p.fechaInicioDescuento IS NOT NULL AND " +
            "p.fechaFinDescuento IS NOT NULL AND " +
            "p.fechaInicioDescuento <= :fechaInicio AND " +
            "p.fechaFinDescuento >= :fechaFin")
    List<Producto> findByDescuentoActivoTrueAndFechaInicioDescuentoLessThanEqualAndFechaFinDescuentoGreaterThanEqual(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Lista productos con descuento activo (sin importar fechas).
     * Útil para el panel de administración.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE p.descuentoActivo = true")
    List<Producto> findByDescuentoActivoTrue();

    /**
     * Lista productos cuyo descuento ha expirado (fecha fin < hoy).
     * Útil para desactivar descuentos vencidos automáticamente.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE " +
            "p.descuentoActivo = true AND " +
            "p.fechaFinDescuento < :hoy")
    List<Producto> findDescuentosExpirados(@Param("hoy") LocalDate hoy);

    /**
     * Lista productos cuyo descuento está próximo a expirar (en los próximos 3 días).
     * Útil para enviar notificaciones.
     */
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.stock WHERE " +
            "p.descuentoActivo = true AND " +
            "p.fechaFinDescuento BETWEEN :hoy AND :limite")
    List<Producto> findDescuentosPorExpiracion(
            @Param("hoy") LocalDate hoy,
            @Param("limite") LocalDate limite
    );
}