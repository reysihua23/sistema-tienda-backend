package com.reydi.tienda.repository;

import com.reydi.tienda.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Integer> {

    Optional<Stock> findByProductoId(Integer productoId);

    @Query("SELECT s FROM Stock s WHERE s.cantidad <= :cantidad")
    List<Stock> findByStockBajo(@Param("cantidad") Integer cantidad);

    boolean existsByProductoId(Integer productoId);

}