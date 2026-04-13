package com.reydi.tienda.service;

import com.reydi.tienda.model.Stock;
import java.util.List;
import java.util.Optional;

public interface StockService {
    List<Stock> listarTodos();
    Optional<Stock> buscarPorId(Integer id);
    Optional<Stock> buscarPorProductoId(Integer productoId);

    List<Stock> listarStockBajo(Integer cantidad);
    Stock guardar(Stock stock);
    Stock actualizar(Stock stock);
    void eliminar(Integer id);
    Stock incrementarStock(Integer productoId, Integer cantidad);
    Stock decrementarStock(Integer productoId, Integer cantidad);
    void actualizarStockPorCompra(Integer productoId, Integer cantidad);
    boolean existeStockParaProducto(Integer productoId);

}

