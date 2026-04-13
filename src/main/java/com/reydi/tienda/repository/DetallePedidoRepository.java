package com.reydi.tienda.repository;

import com.reydi.tienda.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedidoId(Integer pedidoId);

    List<DetallePedido> findByProductoId(Integer productoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DetallePedido d WHERE d.pedido.id = :pedidoId")
    void deleteByPedidoId(@Param("pedidoId") Integer pedidoId);

    @Query("SELECT SUM(d.cantidad) FROM DetallePedido d WHERE d.producto.id = :productoId")
    Integer sumCantidadVendidaPorProducto(@Param("productoId") Integer productoId);
}