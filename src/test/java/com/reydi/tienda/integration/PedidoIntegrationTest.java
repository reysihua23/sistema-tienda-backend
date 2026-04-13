package com.reydi.tienda.integration;

import com.reydi.tienda.model.*;
import com.reydi.tienda.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PedidoIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private Producto productoTest;
    private Cliente clienteTest;

    @BeforeEach
    void setUp() {
        System.out.println("\n=== CONFIGURACIÓN DE PRUEBAS ===\n");

        // Crear cliente de prueba
        clienteTest = new Cliente();
        clienteTest.setNombre("Juan Perez");
        clienteTest.setEmail("juan@test.com");
        clienteTest.setTelefono("987654321");
        clienteTest.setDocumento("12345678");
        clienteTest.setFechaRegistro(LocalDateTime.now());
        clienteTest = clienteRepository.save(clienteTest);
        System.out.println("Cliente creado - ID: " + clienteTest.getId());

        // Crear producto de prueba
        productoTest = new Producto();
        productoTest.setNombre("iPhone 15");
        productoTest.setDescripcion("Smartphone Apple");
        productoTest.setPrecio(new BigDecimal("2999.00"));
        productoTest.setActivo(true);
        productoTest = productoRepository.save(productoTest);
        System.out.println("Producto creado - ID: " + productoTest.getId());
    }

    // ==================== PRUEBA INTEGRAL 1: CREAR PEDIDO ====================

    @Test
    void testCrearPedido() {
        System.out.println("\n=== PRUEBA INTEGRAL 1: CREAR PEDIDO ===\n");

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteTest);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(new BigDecimal("2999.00"));

        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Verificaciones
        assertNotNull(pedidoGuardado.getId());
        assertEquals(clienteTest.getId(), pedidoGuardado.getCliente().getId());
        assertEquals(EstadoPedido.PENDIENTE, pedidoGuardado.getEstado());

        System.out.println("✅ Pedido creado - ID: " + pedidoGuardado.getId());
        System.out.println("✅ Cliente asociado: " + pedidoGuardado.getCliente().getNombre());
        System.out.println("✅ Estado: " + pedidoGuardado.getEstado());
    }

    // ==================== PRUEBA INTEGRAL 2: BUSCAR PEDIDO POR CLIENTE ====================

    @Test
    void testBuscarPedidoPorCliente() {
        System.out.println("\n=== PRUEBA INTEGRAL 2: BUSCAR PEDIDO POR CLIENTE ===\n");

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteTest);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setTotal(new BigDecimal("2999.00"));
        pedidoRepository.save(pedido);

        // Buscar pedidos por cliente
        java.util.List<Pedido> pedidos = pedidoRepository.findByClienteId(clienteTest.getId());

        assertNotNull(pedidos);
        assertTrue(pedidos.size() > 0);
        assertEquals(clienteTest.getId(), pedidos.get(0).getCliente().getId());

        System.out.println("✅ Pedidos encontrados: " + pedidos.size());
        System.out.println("✅ Cliente: " + pedidos.get(0).getCliente().getNombre());
    }

    // ==================== PRUEBA INTEGRAL 3: ACTUALIZAR ESTADO DEL PEDIDO ====================

    @Test
    void testActualizarEstadoPedido() {
        System.out.println("\n=== PRUEBA INTEGRAL 3: ACTUALIZAR ESTADO DEL PEDIDO ===\n");

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(clienteTest);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(new BigDecimal("2999.00"));
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        System.out.println("1. Estado inicial: " + pedidoGuardado.getEstado());

        // Cambiar estado a PAGADO
        pedidoGuardado.setEstado(EstadoPedido.PAGADO);
        pedidoRepository.save(pedidoGuardado);

        assertEquals(EstadoPedido.PAGADO, pedidoGuardado.getEstado());
        System.out.println("2. Estado después de pago: " + pedidoGuardado.getEstado());

        // Cambiar estado a ENVIADO
        pedidoGuardado.setEstado(EstadoPedido.ENVIADO);
        pedidoRepository.save(pedidoGuardado);

        assertEquals(EstadoPedido.ENVIADO, pedidoGuardado.getEstado());
        System.out.println("3. Estado después de envío: " + pedidoGuardado.getEstado());

        // Cambiar estado a ENTREGADO
        pedidoGuardado.setEstado(EstadoPedido.ENTREGADO);
        pedidoRepository.save(pedidoGuardado);

        assertEquals(EstadoPedido.ENTREGADO, pedidoGuardado.getEstado());
        System.out.println("4. Estado final: " + pedidoGuardado.getEstado());
    }

    // ==================== PRUEBA INTEGRAL 4: VALIDAR PRODUCTO ====================

    @Test
    void testValidarProducto() {
        System.out.println("\n=== PRUEBA INTEGRAL 4: VALIDAR PRODUCTO ===\n");

        // Buscar producto por ID
        Producto encontrado = productoRepository.findById(productoTest.getId()).orElse(null);

        assertNotNull(encontrado);
        assertEquals("iPhone 15", encontrado.getNombre());
        assertEquals(0, new BigDecimal("2999.00").compareTo(encontrado.getPrecio()));

        System.out.println("✅ Producto encontrado: " + encontrado.getNombre());
        System.out.println("✅ Precio: S/ " + encontrado.getPrecio());
        System.out.println("✅ Activo: " + encontrado.getActivo());
    }

    // ==================== PRUEBA INTEGRAL 5: CONTAR PEDIDOS POR CLIENTE ====================

    @Test
    void testContarPedidosPorCliente() {
        System.out.println("\n=== PRUEBA INTEGRAL 5: CONTAR PEDIDOS POR CLIENTE ===\n");

        // Crear varios pedidos
        for (int i = 0; i < 3; i++) {
            Pedido pedido = new Pedido();
            pedido.setCliente(clienteTest);
            pedido.setFecha(LocalDateTime.now());
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setTotal(new BigDecimal("1000.00"));
            pedidoRepository.save(pedido);
        }

        // Contar pedidos
        Long cantidad = pedidoRepository.countPedidosByCliente(clienteTest.getId());

        assertTrue(cantidad >= 3);
        System.out.println("✅ Cliente: " + clienteTest.getNombre());
        System.out.println("✅ Total de pedidos: " + cantidad);
    }
}