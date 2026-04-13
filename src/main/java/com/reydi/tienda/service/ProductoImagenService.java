package com.reydi.tienda.service;

import com.reydi.tienda.model.ProductoImagen;
import java.util.List;
import java.util.Optional;

public interface ProductoImagenService {
    List<ProductoImagen> listarTodos();
    Optional<ProductoImagen> buscarPorId(Integer id);
    List<ProductoImagen> buscarPorProducto(Integer productoId);
    List<ProductoImagen> buscarTodas();
    Optional<ProductoImagen> buscarImagenPrincipal(Integer productoId);
    ProductoImagen guardar(ProductoImagen productoImagen);
    ProductoImagen actualizar(ProductoImagen productoImagen);
    void eliminar(Integer id);
    void eliminarPorProducto(Integer productoId);
    void marcarComoPrincipal(Integer id, Integer productoId);
}