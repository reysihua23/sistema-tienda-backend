package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.ProductoImagen;
import com.reydi.tienda.repository.ProductoImagenRepository;
import com.reydi.tienda.service.ProductoImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductoImagenServiceImpl implements ProductoImagenService {

    private final ProductoImagenRepository productoImagenRepository;

    @Override
    public List<ProductoImagen> listarTodos() {
        return productoImagenRepository.findAll();
    }

    @Override
    public Optional<ProductoImagen> buscarPorId(Integer id) {
        return productoImagenRepository.findById(id);
    }

    @Override
    public List<ProductoImagen> buscarPorProducto(Integer productoId) {
        return productoImagenRepository.findByProductoId(productoId);
    }

    // AGREGAR ESTE MÉTODO - Obtener todas las imágenes
    @Override
    public List<ProductoImagen> buscarTodas() {
        return productoImagenRepository.findAll();
    }

    @Override
    public Optional<ProductoImagen> buscarImagenPrincipal(Integer productoId) {
        return productoImagenRepository.findByProductoIdAndPrincipalTrue(productoId);
    }

    @Override
    public ProductoImagen guardar(ProductoImagen productoImagen) {
        // Si es la imagen principal, quitar principal de otras imágenes del mismo producto
        if (productoImagen.getPrincipal()) {
            List<ProductoImagen> imagenes = productoImagenRepository.findByProductoId(productoImagen.getProducto().getId());
            for (ProductoImagen img : imagenes) {
                img.setPrincipal(false);
                productoImagenRepository.save(img);
            }
        }
        return productoImagenRepository.save(productoImagen);
    }

    @Override
    public ProductoImagen actualizar(ProductoImagen productoImagen) {
        if (!productoImagenRepository.existsById(productoImagen.getId())) {
            throw new RuntimeException("Imagen no encontrada");
        }

        // Si se marca como principal, quitar principal de otras imágenes del mismo producto
        if (productoImagen.getPrincipal()) {
            List<ProductoImagen> imagenes = productoImagenRepository.findByProductoId(productoImagen.getProducto().getId());
            for (ProductoImagen img : imagenes) {
                if (!img.getId().equals(productoImagen.getId())) {
                    img.setPrincipal(false);
                    productoImagenRepository.save(img);
                }
            }
        }

        return productoImagenRepository.save(productoImagen);
    }

    @Override
    public void eliminar(Integer id) {
        if (!productoImagenRepository.existsById(id)) {
            throw new RuntimeException("Imagen no encontrada");
        }
        productoImagenRepository.deleteById(id);
    }

    @Override
    public void eliminarPorProducto(Integer productoId) {
        productoImagenRepository.deleteByProductoId(productoId);
    }

    @Override
    public void marcarComoPrincipal(Integer id, Integer productoId) {
        // Quitar principal de todas las imágenes del producto
        List<ProductoImagen> imagenes = productoImagenRepository.findByProductoId(productoId);
        for (ProductoImagen img : imagenes) {
            img.setPrincipal(false);
            productoImagenRepository.save(img);
        }

        // Marcar la imagen seleccionada como principal
        ProductoImagen imagen = productoImagenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
        imagen.setPrincipal(true);
        productoImagenRepository.save(imagen);
    }
}