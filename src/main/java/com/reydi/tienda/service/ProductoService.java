package com.reydi.tienda.service;

import com.reydi.tienda.model.Producto;
import java.util.List;
import java.util.Optional;

public interface
ProductoService {

    /**
     * Lista todos los productos
     */
    List<Producto> listarTodos();

    /**
     * Busca un producto por su ID
     */
    Optional<Producto> buscarPorId(Integer id);

    /**
     * Lista solo productos activos
     */
    List<Producto> listarActivos();

    /**
     * Busca productos por nombre (coincidencia parcial, insensible a mayúsculas)
     */
    List<Producto> buscarPorNombre(String nombre);

    /**
     * Lista productos con stock bajo (stock real <= stockMinimo)
     * El stock real viene de la relación con la tabla stock
     */
    List<Producto> listarStockBajo(Integer stockMinimo);

    /**
     * Filtro avanzado con múltiples criterios
     * @param nombre Nombre del producto (opcional)
     * @param activo Estado activo/inactivo (opcional)
     * @param stockMinimo Stock mínimo para filtrar (opcional)
     * @param precioMinimo Precio mínimo (opcional)
     */
    List<Producto> filtrarAvanzado(String nombre, Boolean activo, Integer stockMinimo, Double precioMinimo);

    /**
     * Guarda un nuevo producto
     * También crea automáticamente el registro en la tabla stock con cantidad 0
     */
    Producto guardar(Producto producto);

    /**
     * Actualiza un producto existente
     */
    Producto actualizar(Producto producto);

    /**
     * Elimina un producto por su ID
     * También elimina su registro en la tabla stock (por CASCADE)
     */
    void eliminar(Integer id);

    // =========================================================
    // ✅ NUEVO MÉTODO PARA DESCUENTOS - AGREGAR ESTO
    // =========================================================

    /**
     * Lista productos en oferta (con descuento activo y vigente)
     *
     * @return Lista de productos que actualmente están en oferta
     */
    List<Producto> listarEnOferta();
}