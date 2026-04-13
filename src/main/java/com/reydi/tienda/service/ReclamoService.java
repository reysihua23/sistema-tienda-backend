package com.reydi.tienda.service;

import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReclamoService {
    List<Reclamo> listarTodos();
    Optional<Reclamo> buscarPorId(Integer id);
    List<Reclamo> buscarPorCliente(Integer clienteId);
    List<Reclamo> buscarPorEstado(EstadoReclamo estado);
    List<Reclamo> buscarPorTipo(TipoReclamo tipo);
    Reclamo guardar(Reclamo reclamo);
    Reclamo actualizar(Reclamo reclamo);
    void eliminar(Integer id);
    Reclamo cambiarEstado(Integer id, EstadoReclamo nuevoEstado);
    List<Reclamo> filtrarAvanzado(EstadoReclamo estado, Integer clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}