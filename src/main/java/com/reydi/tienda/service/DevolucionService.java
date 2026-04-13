package com.reydi.tienda.service;

import com.reydi.tienda.model.Devolucion;
import com.reydi.tienda.model.EstadoDevolucion;
import java.util.List;
import java.util.Optional;

public interface DevolucionService {
    List<Devolucion> listarTodos();
    Optional<Devolucion> buscarPorId(Long id);
    Optional<Devolucion> buscarPorReclamo(Long reclamoId);
    List<Devolucion> buscarPorEstado(EstadoDevolucion estado);
    List<Devolucion> buscarPorPedido(Long pedidoId);
    Devolucion guardar(Devolucion devolucion);
    Devolucion actualizar(Devolucion devolucion);
    void eliminar(Long id);
    boolean existePorReclamo(Long reclamoId);
}