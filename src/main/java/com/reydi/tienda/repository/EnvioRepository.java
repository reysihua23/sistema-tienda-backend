package com.reydi.tienda.repository;

import com.reydi.tienda.model.Envio;
import com.reydi.tienda.model.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    List<Envio> findByPedidoId(Integer pedidoId);

    List<Envio> findByEstado(EstadoEnvio estado);

    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);
}