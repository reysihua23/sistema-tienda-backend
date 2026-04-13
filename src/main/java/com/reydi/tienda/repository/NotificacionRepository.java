package com.reydi.tienda.repository;

import com.reydi.tienda.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByLeidoFalse();

    List<Notificacion> findByTipo(String tipo);

    List<Notificacion> findByLeidoOrderByFechaDesc(Boolean leido);
}