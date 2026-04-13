package com.reydi.tienda.repository;

import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {

    List<Reclamo> findByClienteId(Integer clienteId);

    List<Reclamo> findByEstado(EstadoReclamo estado);

    List<Reclamo> findByTipo(TipoReclamo tipo);

    @Query("SELECT r FROM Reclamo r WHERE " +
            "(:estado IS NULL OR r.estado = :estado) AND " +
            "(:clienteId IS NULL OR r.cliente.id = :clienteId) AND " +
            "(:fechaInicio IS NULL OR r.fecha >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR r.fecha <= :fechaFin)")
    List<Reclamo> filtrarAvanzado(
            @Param("estado") EstadoReclamo estado,
            @Param("clienteId") Integer clienteId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}