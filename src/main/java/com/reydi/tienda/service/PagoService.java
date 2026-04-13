package com.reydi.tienda.service;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pago.EstadoPago;
import com.reydi.tienda.model.Pago.MetodoPago;
import java.util.List;
import java.util.Optional;

public interface PagoService {
    List<Pago> listarTodos();
    Optional<Pago> buscarPorId(Integer id);
    List<Pago> buscarPorPedido(Integer pedidoId);
    Optional<Pago> buscarPorPedidoId(Integer pedidoId);
    List<Pago> buscarPorEstado(EstadoPago estado);
    List<Pago> buscarPorMetodo(MetodoPago metodo);
    Optional<Pago> buscarPorReferencia(String referenciaPasarela);
    Pago guardar(Pago pago);
    Pago actualizar(Pago pago);
    void eliminar(Integer id);
    Pago aprobarPago(Integer id);
    Pago rechazarPago(Integer id);
    boolean existePagoParaPedido(Integer pedidoId);
}