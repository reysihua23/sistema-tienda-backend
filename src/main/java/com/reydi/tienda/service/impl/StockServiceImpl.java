package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Stock;
import com.reydi.tienda.repository.StockRepository;
import com.reydi.tienda.service.NotificacionService;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final NotificacionService notificacionService;
    private final NotificationRecipientResolver recipientResolver;

    private static final int UMBRAL_STOCK_BAJO = 5;

    @Override
    public List<Stock> listarTodos() {
        return stockRepository.findAll();
    }

    @Override
    public Optional<Stock> buscarPorId(Integer id) {
        return stockRepository.findById(id);
    }

    @Override
    public Optional<Stock> buscarPorProductoId(Integer productoId) {
        return stockRepository.findByProductoId(productoId);
    }

    @Override
    public List<Stock> listarStockBajo(Integer cantidad) {
        return stockRepository.findByStockBajo(cantidad);
    }

    @Override
    @Transactional
    public Stock guardar(Stock stock) {
        if (stock.getFechaActualizacion() == null) {
            stock.setFechaActualizacion(LocalDateTime.now());
        }
        Stock saved = stockRepository.save(stock);
        verificarYNotificarStockBajo(saved);
        return saved;
    }

    @Override
    @Transactional
    public Stock actualizar(Stock stock) {
        System.out.println("StockService.actualizar - ID: " + stock.getId());

        if (!stockRepository.existsById(stock.getId())) {
            throw new RuntimeException("Stock no encontrado con ID: " + stock.getId());
        }

        Stock existente = stockRepository.findById(stock.getId()).get();
        stock.setProducto(existente.getProducto());
        stock.setFechaActualizacion(LocalDateTime.now());

        Stock saved = stockRepository.save(stock);
        verificarYNotificarStockBajo(saved);
        return saved;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!stockRepository.existsById(id)) {
            throw new RuntimeException("Stock no encontrado");
        }
        stockRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Stock incrementarStock(Integer productoId, Integer cantidad) {
        Stock stock = stockRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado para el producto"));

        stock.setCantidad(stock.getCantidad() + cantidad);
        stock.setFechaActualizacion(LocalDateTime.now());

        System.out.println("Stock incrementado - Producto ID: " + productoId +
                ", Nueva cantidad: " + stock.getCantidad());

        Stock saved = stockRepository.save(stock);
        verificarYNotificarStockBajo(saved);
        return saved;
    }

    @Override
    @Transactional
    public Stock decrementarStock(Integer productoId, Integer cantidad) {
        Stock stock = stockRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado para el producto"));

        if (stock.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente. Stock actual: " + stock.getCantidad() +
                    ", solicitado: " + cantidad);
        }

        stock.setCantidad(stock.getCantidad() - cantidad);
        stock.setFechaActualizacion(LocalDateTime.now());

        System.out.println("Stock decrementado - Producto ID: " + productoId +
                ", Nueva cantidad: " + stock.getCantidad());

        Stock saved = stockRepository.save(stock);
        verificarYNotificarStockBajo(saved);
        return saved;
    }

    @Override
    @Transactional
    public void actualizarStockPorCompra(Integer productoId, Integer cantidad) {
        System.out.println("=== ACTUALIZANDO STOCK POR COMPRA ===");
        System.out.println("Producto ID: " + productoId);
        System.out.println("Cantidad a reducir: " + cantidad);

        Stock stock = stockRepository.findByProductoId(productoId)
                .orElseThrow(() -> new RuntimeException("Stock no encontrado para el producto ID: " + productoId));

        if (stock.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto. " +
                    "Disponible: " + stock.getCantidad() + ", solicitado: " + cantidad);
        }

        int nuevaCantidad = stock.getCantidad() - cantidad;
        stock.setCantidad(nuevaCantidad);
        stock.setFechaActualizacion(LocalDateTime.now());

        stockRepository.save(stock);

        System.out.println("Stock actualizado - Nueva cantidad: " + nuevaCantidad);

        verificarYNotificarStockBajo(stock);
    }

    @Override
    public boolean existeStockParaProducto(Integer productoId) {
        return stockRepository.existsByProductoId(productoId);
    }

    // =========================================================
    // ✅ Helper: notifica si el stock queda por debajo del umbral
    // =========================================================
    private void verificarYNotificarStockBajo(Stock stock) {
        if (stock == null || stock.getCantidad() == null) return;

        if (stock.getCantidad() <= UMBRAL_STOCK_BAJO) {
            String nombre = (stock.getProducto() != null && stock.getProducto().getNombre() != null)
                    ? stock.getProducto().getNombre()
                    : "Producto #" + (stock.getProducto() != null ? stock.getProducto().getId() : "?");

            notificacionService.crearNotificacion(
                    recipientResolver.adminId(),
                    "STOCK",
                    "⚠️ Stock bajo: " + nombre + " (" + stock.getCantidad() + " uds.)"
            );
        }
    }
}