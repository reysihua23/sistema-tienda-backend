// src/test/java/com/reydi/tienda/service/ProductoServiceCompleteTest.java
package com.reydi.tienda.service;

import com.reydi.tienda.model.Producto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductoServiceTest {

    @Test
    void testCrearProducto() {
        // Crear producto
        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("iPhone 15 Pro Max");
        producto.setDescripcion("Smartphone Apple de última generación");
        producto.setPrecio(new BigDecimal("4299.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);

        // Verificaciones
        assertNotNull(producto);
        assertEquals(1, producto.getId());
        assertEquals("iPhone 15 Pro Max", producto.getNombre());
        assertEquals(0, new BigDecimal("4299.00").compareTo(producto.getPrecio()));
        assertTrue(producto.getActivo());
    }

    @Test
    void testActualizarPrecioProducto() {
        // Crear producto con precio inicial
        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("Samsung Galaxy S24");
        producto.setPrecio(new BigDecimal("3999.00"));

        // Actualizar precio
        BigDecimal nuevoPrecio = new BigDecimal("3499.00");
        producto.setPrecio(nuevoPrecio);

        // Verificar
        assertEquals(0, nuevoPrecio.compareTo(producto.getPrecio()));
        assertNotEquals(new BigDecimal("3999.00"), producto.getPrecio());
    }

    @Test
    void testCalcularPrecioConDescuento() {
        BigDecimal precioOriginal = new BigDecimal("2999.00");
        double descuento = 0.10; // 10% de descuento

        BigDecimal precioConDescuento = precioOriginal.multiply(BigDecimal.valueOf(1 - descuento));

        assertEquals(0, new BigDecimal("2699.10").compareTo(precioConDescuento));
    }

    @Test
    void testValidarStockMinimo() {
        Producto producto = new Producto();
        producto.setStockMinimo(5);

        int stockActual = 3;
        boolean necesitaAlerta = stockActual <= producto.getStockMinimo();

        assertTrue(necesitaAlerta);
    }

    @Test
    void testValidarStockMinimo_Suficiente() {
        Producto producto = new Producto();
        producto.setStockMinimo(5);

        int stockActual = 10;
        boolean necesitaAlerta = stockActual <= producto.getStockMinimo();

        assertFalse(necesitaAlerta);
    }

    @Test
    void testActivarProducto() {
        Producto producto = new Producto();
        producto.setActivo(false);

        // Activar producto
        producto.setActivo(true);

        assertTrue(producto.getActivo());
    }

    @Test
    void testDesactivarProducto() {
        Producto producto = new Producto();
        producto.setActivo(true);

        // Desactivar producto
        producto.setActivo(false);

        assertFalse(producto.getActivo());
    }

    @Test
    void testListaProductosVacia() {
        List<Producto> productos = new ArrayList<>();

        assertTrue(productos.isEmpty());
        assertEquals(0, productos.size());
    }

    @Test
    void testAgregarProductoLista() {
        List<Producto> productos = new ArrayList<>();

        Producto producto = new Producto();
        producto.setId(1);
        producto.setNombre("Xiaomi Note 13");
        productos.add(producto);

        assertFalse(productos.isEmpty());
        assertEquals(1, productos.size());
        assertEquals("Xiaomi Note 13", productos.get(0).getNombre());
    }
}