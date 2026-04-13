package com.reydi.tienda.service;

import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.model.EstadoPedido;
import com.reydi.tienda.model.DetallePedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoService {
    List<Pedido> listarTodos();
    Optional<Pedido> buscarPorId(Integer id);
    List<Pedido> buscarPorCliente(Integer clienteId);
    List<Pedido> buscarPorEstado(EstadoPedido estado);
    List<Pedido> buscarPorFechas(LocalDateTime inicio, LocalDateTime fin);
    List<Pedido> buscarUltimosPedidosPorCliente(Integer clienteId);
    Long contarPedidosPorCliente(Integer clienteId);
    Pedido guardar(Pedido pedido);
    Pedido actualizar(Pedido pedido);
    void eliminar(Integer id);
    Pedido cambiarEstado(Integer id, EstadoPedido nuevoEstado);
    BigDecimal calcularTotal(Pedido pedido);
    Pedido agregarDetalle(Integer pedidoId, DetallePedido detalle);
    Pedido eliminarDetalle(Integer pedidoId, Integer detalleId);
}