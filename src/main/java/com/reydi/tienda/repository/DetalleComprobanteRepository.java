package com.reydi.tienda.repository;

import com.reydi.tienda.model.DetalleComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleComprobanteRepository extends JpaRepository<DetalleComprobante, Integer> {
    List<DetalleComprobante> findByComprobanteId(Integer comprobanteId);
}