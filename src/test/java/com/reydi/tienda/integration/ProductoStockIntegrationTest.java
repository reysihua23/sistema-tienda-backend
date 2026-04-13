package com.reydi.tienda.integration;

import com.reydi.tienda.model.*;
import com.reydi.tienda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductoStockIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockRepository stockRepository;

    private Producto productoTest;
    private Stock stockTest;

    @BeforeEach
    void setUp() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PRUEBAS INTEGRALES - PRODUCTOS Y STOCK");
        System.out.println("=".repeat(60));
    }

    // ==================== PRUEBA 1: CREAR PRODUCTO CON STOCK ====================

    @Test
    @Order(1)
    void testCrearProductoConStock() {
        System.out.println("\n📦 PRUEBA 1: CREAR PRODUCTO CON STOCK");
        System.out.println("-".repeat(40));

        // Crear producto
        Producto producto = new Producto();
        producto.setNombre("Xiaomi Note 13 Pro");
        producto.setDescripcion("Smartphone Xiaomi 5G");
        producto.setPrecio(new BigDecimal("1299.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        // Crear stock para el producto
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(50);
        stock.setFechaActualizacion(LocalDateTime.now());
        stock = stockRepository.save(stock);

        productoTest = producto;
        stockTest = stock;

        // Verificaciones
        assertNotNull(producto.getId());
        assertEquals("Xiaomi Note 13 Pro", producto.getNombre());
        assertEquals(0, new BigDecimal("1299.00").compareTo(producto.getPrecio()));
        assertEquals(50, stock.getCantidad());

        System.out.println("✅ Producto creado - ID: " + producto.getId());
        System.out.println("✅ Nombre: " + producto.getNombre());
        System.out.println("✅ Precio: S/ " + producto.getPrecio());
        System.out.println("✅ Stock inicial: " + stock.getCantidad() + " unidades");
    }

    // ==================== PRUEBA 2: ACTUALIZAR STOCK ====================

    @Test
    @Order(2)
    void testActualizarStock() {
        System.out.println("\n📦 PRUEBA 2: ACTUALIZAR STOCK");
        System.out.println("-".repeat(40));

        // Primero crear producto y stock
        Producto producto = new Producto();
        producto.setNombre("Samsung Galaxy A54");
        producto.setDescripcion("Smartphone Samsung gama media");
        producto.setPrecio(new BigDecimal("1599.00"));
        producto.setStockMinimo(3);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(30);
        stock.setFechaActualizacion(LocalDateTime.now());
        stock = stockRepository.save(stock);

        System.out.println("📊 Stock inicial: " + stock.getCantidad() + " unidades");

        // Actualizar stock (aumentar)
        stock.setCantidad(45);
        stock.setFechaActualizacion(LocalDateTime.now());
        stock = stockRepository.save(stock);

        assertEquals(45, stock.getCantidad());
        System.out.println("✅ Stock después de aumentar: " + stock.getCantidad() + " unidades");

        // Actualizar stock (disminuir)
        stock.setCantidad(20);
        stock = stockRepository.save(stock);

        assertEquals(20, stock.getCantidad());
        System.out.println("✅ Stock después de disminuir: " + stock.getCantidad() + " unidades");
    }

    // ==================== PRUEBA 3: VALIDAR STOCK BAJO ====================

    @Test
    @Order(3)
    void testValidarStockBajo() {
        System.out.println("\n📦 PRUEBA 3: VALIDAR STOCK BAJO");
        System.out.println("-".repeat(40));

        // Crear producto con stock mínimo
        Producto producto = new Producto();
        producto.setNombre("Funda para iPhone");
        producto.setDescripcion("Funda protectora");
        producto.setPrecio(new BigDecimal("49.00"));
        producto.setStockMinimo(10);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        // Stock bajo (menor al mínimo)
        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(3); // Stock bajo (3 < 10)
        stock = stockRepository.save(stock);

        boolean esStockBajo = stock.getCantidad() <= producto.getStockMinimo();

        assertTrue(esStockBajo);
        System.out.println("✅ Stock actual: " + stock.getCantidad() + " unidades");
        System.out.println("✅ Stock mínimo: " + producto.getStockMinimo() + " unidades");
        System.out.println("✅ Alerta de stock bajo: " + (esStockBajo ? "SÍ" : "NO"));
    }

    // ==================== PRUEBA 4: BUSCAR PRODUCTOS POR NOMBRE ====================

    @Test
    @Order(4)
    void testBuscarProductosPorNombre() {
        System.out.println("\n📦 PRUEBA 4: BUSCAR PRODUCTOS POR NOMBRE");
        System.out.println("-".repeat(40));

        // Crear varios productos
        Producto p1 = new Producto();
        p1.setNombre("iPhone 15 Pro");
        p1.setDescripcion("Apple iPhone");
        p1.setPrecio(new BigDecimal("3999.00"));
        p1.setStockMinimo(5);
        p1.setActivo(true);
        productoRepository.save(p1);

        Producto p2 = new Producto();
        p2.setNombre("iPhone 15 Plus");
        p2.setDescripcion("Apple iPhone grande");
        p2.setPrecio(new BigDecimal("4299.00"));
        p2.setStockMinimo(5);
        p2.setActivo(true);
        productoRepository.save(p2);

        // Buscar por nombre que contenga "iPhone"
        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCase("iPhone");

        assertNotNull(productos);
        assertTrue(productos.size() >= 2);
        System.out.println("✅ Búsqueda: 'iPhone'");
        System.out.println("✅ Productos encontrados: " + productos.size());

        for (Producto p : productos) {
            System.out.println("   📱 " + p.getNombre());
        }
    }

    // ==================== PRUEBA 5: ACTUALIZAR PRECIO DE PRODUCTO ====================

    @Test
    @Order(5)
    void testActualizarPrecioProducto() {
        System.out.println("\n📦 PRUEBA 5: ACTUALIZAR PRECIO DE PRODUCTO");
        System.out.println("-".repeat(40));

        // Crear producto
        Producto producto = new Producto();
        producto.setNombre("Auriculares Bluetooth");
        producto.setDescripcion("Auriculares inalámbricos");
        producto.setPrecio(new BigDecimal("89.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        System.out.println("💰 Precio original: S/ " + producto.getPrecio());

        // Actualizar precio
        producto.setPrecio(new BigDecimal("69.00"));
        producto = productoRepository.save(producto);

        assertEquals(0, new BigDecimal("69.00").compareTo(producto.getPrecio()));
        System.out.println("✅ Precio actualizado: S/ " + producto.getPrecio());

        double descuento = (89.00 - 69.00) / 89.00 * 100;
        System.out.println("✅ Descuento aplicado: " + String.format("%.2f", descuento) + "%");
    }

    // ==================== PRUEBA 6: ACTIVAR/DESACTIVAR PRODUCTO ====================

    @Test
    @Order(6)
    void testActivarDesactivarProducto() {
        System.out.println("\n📦 PRUEBA 6: ACTIVAR/DESACTIVAR PRODUCTO");
        System.out.println("-".repeat(40));

        // Crear producto
        Producto producto = new Producto();
        producto.setNombre("Cargador Rápido");
        producto.setDescripcion("Cargador 65W USB-C");
        producto.setPrecio(new BigDecimal("45.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        System.out.println("🔘 Estado inicial: " + (producto.getActivo() ? "ACTIVO" : "INACTIVO"));

        // Desactivar producto
        producto.setActivo(false);
        producto = productoRepository.save(producto);
        assertFalse(producto.getActivo());
        System.out.println("🔘 Después de desactivar: " + (producto.getActivo() ? "ACTIVO" : "INACTIVO"));

        // Activar producto nuevamente
        producto.setActivo(true);
        producto = productoRepository.save(producto);
        assertTrue(producto.getActivo());
        System.out.println("🔘 Después de activar: " + (producto.getActivo() ? "ACTIVO" : "INACTIVO"));
    }

    // ==================== PRUEBA 7: ELIMINAR PRODUCTO ====================

    @Test
    @Order(7)
    void testEliminarProducto() {
        System.out.println("\n📦 PRUEBA 7: ELIMINAR PRODUCTO");
        System.out.println("-".repeat(40));

        // Crear producto temporal
        Producto producto = new Producto();
        producto.setNombre("Producto a Eliminar");
        producto.setDescripcion("Este producto será eliminado");
        producto.setPrecio(new BigDecimal("999.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        System.out.println("✅ Producto creado - ID: " + producto.getId());

        // Eliminar producto
        productoRepository.delete(producto);

        // Verificar que ya no existe
        boolean existe = productoRepository.findById(producto.getId()).isPresent();
        assertFalse(existe);
        System.out.println("✅ Producto eliminado correctamente");
    }

    // ==================== PRUEBA 8: LISTAR TODOS LOS PRODUCTOS ====================

    @Test
    @Order(8)
    void testListarTodosLosProductos() {
        System.out.println("\n📦 PRUEBA 8: LISTAR TODOS LOS PRODUCTOS");
        System.out.println("-".repeat(40));

        List<Producto> productos = productoRepository.findAll();

        assertNotNull(productos);
        System.out.println("✅ Total de productos en BD: " + productos.size());

        if (productos.isEmpty()) {
            System.out.println("   ⚠️ No hay productos registrados aún");
        } else {
            System.out.println("\n📱 Lista de productos:");
            for (Producto p : productos) {
                System.out.println("   • " + p.getNombre() + " - S/ " + p.getPrecio());
            }
        }
    }

    // ==================== PRUEBA 9: VALIDAR STOCK SUFICIENTE ====================

    @Test
    @Order(9)
    void testValidarStockSuficiente() {
        System.out.println("\n📦 PRUEBA 9: VALIDAR STOCK SUFICIENTE");
        System.out.println("-".repeat(40));

        // Crear producto con stock
        Producto producto = new Producto();
        producto.setNombre("Producto Stock Test");
        producto.setDescripcion("Para validar stock");
        producto.setPrecio(new BigDecimal("100.00"));
        producto.setStockMinimo(5);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        Stock stock = new Stock();
        stock.setProducto(producto);
        stock.setCantidad(10);
        stock = stockRepository.save(stock);

        int cantidadSolicitada = 3;
        boolean hayStock = stock.getCantidad() >= cantidadSolicitada;

        assertTrue(hayStock);
        System.out.println("✅ Stock disponible: " + stock.getCantidad() + " unidades");
        System.out.println("✅ Cantidad solicitada: " + cantidadSolicitada);
        System.out.println("✅ Stock suficiente: " + (hayStock ? "SÍ" : "NO"));
    }
}