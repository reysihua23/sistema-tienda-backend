package com.reydi.tienda.repository;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pago.EstadoPago;
import com.reydi.tienda.model.Pago.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByPedidoId(Integer pedidoId);

    Optional<Pago> findByPedidoIdOrderByFechaDesc(Integer pedidoId);

    List<Pago> findByEstado(EstadoPago estado);

    List<Pago> findByMetodo(MetodoPago metodo);

    Optional<Pago> findByReferenciaPasarela(String referenciaPasarela);

    boolean existsByPedidoId(Integer pedidoId);
}