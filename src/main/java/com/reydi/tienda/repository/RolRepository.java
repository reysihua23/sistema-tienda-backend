package com.reydi.tienda.repository;

import com.reydi.tienda.model.Rol;
import com.reydi.tienda.model.TipoRol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(TipoRol nombre);

    boolean existsByNombre(TipoRol nombre);
}