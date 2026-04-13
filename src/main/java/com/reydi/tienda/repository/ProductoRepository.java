package com.reydi.tienda.repository;

import com.reydi.tienda.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}