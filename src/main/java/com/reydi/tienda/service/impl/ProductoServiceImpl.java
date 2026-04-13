package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Producto;
import com.reydi.tienda.model.Stock;
import com.reydi.tienda.repository.ProductoRepository;
import com.reydi.tienda.repository.StockRepository;
import com.reydi.tienda.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> buscarPorId(Integer id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Lista productos con stock bajo
     * El stock real viene de la tabla stock, no del campo stock_minimo
     */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarStockBajo(Integer stockMinimo) {
        return productoRepository.findByStockLessThanEqual(stockMinimo);
    }

    /**
     * Filtro avanzado que incluye stock real
     */
    @Override
    @Transactional(readOnly = true)
    public List<Producto> filtrarAvanzado(String nombre, Boolean activo, Integer stockMinimo, Double precioMinimo) {
        return productoRepository.filtrarProductosAvanzado(nombre, activo, stockMinimo, precioMinimo);
    }

    /**
     * Guarda un nuevo producto
     * IMPORTANTE: También crea automáticamente el registro en la tabla stock
     */
    @Override
    @Transactional
    public Producto guardar(Producto producto) {
        // 1. Guardar el producto primero
        Producto saved = productoRepository.save(producto);

        // 2. Crear el registro de stock para este producto (stock inicial = 0)
        Stock stock = new Stock();
        stock.setProducto(saved);
        stock.setCantidad(0);
        stockRepository.save(stock);

        return saved;
    }

    /**
     * Actualiza un producto existente
     * Nota: El stock se actualiza por separado a través de StockService
     */
    @Override
    @Transactional
    public Producto actualizar(Producto producto) {
        if (!productoRepository.existsById(producto.getId())) {
            throw new RuntimeException("Producto no encontrado con ID: " + producto.getId());
        }
        return productoRepository.save(producto);
    }

    /**
     * Elimina un producto
     * El stock se eliminará automáticamente por CASCADE en la base de datos
     */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoRepository.deleteById(id);
    }
}