package com.reydi.tienda.repository;

import com.reydi.tienda.model.Devolucion;
import com.reydi.tienda.model.EstadoDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    Optional<Devolucion> findByReclamoId(Long reclamoId);

    List<Devolucion> findByEstado(EstadoDevolucion estado);

    List<Devolucion> findByPedidoId(Long pedidoId);

    boolean existsByReclamoId(Long reclamoId);
}