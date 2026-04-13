package com.reydi.tienda.repository;

import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByClienteId(Integer clienteId);

    List<Pedido> findByEstado(EstadoPedido estado);

    List<Pedido> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    List<Pedido> findByClienteIdAndEstado(Integer clienteId, EstadoPedido estado);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.fecha DESC")
    List<Pedido> findUltimosPedidosPorCliente(@Param("clienteId") Integer clienteId);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.cliente.id = :clienteId")
    Long countPedidosByCliente(@Param("clienteId") Integer clienteId);

    @Query("SELECT p FROM Pedido p WHERE p.cliente.id = :clienteId AND p.fecha BETWEEN :inicio AND :fin")
    List<Pedido> findPedidosByClienteAndFechas(@Param("clienteId") Integer clienteId,
                                               @Param("inicio") LocalDateTime inicio,
                                               @Param("fin") LocalDateTime fin);
}