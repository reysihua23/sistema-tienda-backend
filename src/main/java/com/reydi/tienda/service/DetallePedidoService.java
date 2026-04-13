package com.reydi.tienda.service;

import com.reydi.tienda.model.DetallePedido;
import java.util.List;
import java.util.Optional;

public interface DetallePedidoService {
    List<DetallePedido> listarTodos();
    Optional<DetallePedido> buscarPorId(Integer id);
    List<DetallePedido> buscarPorPedido(Integer pedidoId);
    List<DetallePedido> buscarPorProducto(Integer productoId);
    DetallePedido guardar(DetallePedido detalle);
    DetallePedido actualizar(DetallePedido detalle);
    void eliminar(Integer id);
    void eliminarPorPedido(Integer pedidoId);
}