package com.reydi.tienda.repository;

import com.reydi.tienda.model.ProductoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoImagenRepository extends JpaRepository<ProductoImagen, Integer> {

    List<ProductoImagen> findByProductoId(Integer productoId);

    Optional<ProductoImagen> findByProductoIdAndPrincipalTrue(Integer productoId);

    List<ProductoImagen> findByPrincipalTrue();

    void deleteByProductoId(Integer productoId);
}