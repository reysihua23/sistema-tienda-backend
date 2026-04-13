package com.reydi.tienda.repository;

import com.reydi.tienda.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByDocumento(String documento);
    boolean existsByEmail(String email);

}