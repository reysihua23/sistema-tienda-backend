package com.reydi.tienda.repository;

import com.reydi.tienda.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

    // Un pedido tiene un solo comprobante - retorna Optional
    Optional<Comprobante> findByPedidoId(Integer pedidoId);

    List<Comprobante> findByTipoComprobante(String tipoComprobante);

    @Query("SELECT MAX(c.numero) FROM Comprobante c WHERE c.serie = :serie")
    String findLastNumeroBySerie(@Param("serie") String serie);
}