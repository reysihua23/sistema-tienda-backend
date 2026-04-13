// src/test/java/com/reydi/tienda/service/PedidoServiceTest.java
package com.reydi.tienda.service;

import com.reydi.tienda.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PedidoServiceTest {

    // ==================== PRUEBAS DE CREACIÓN DE PEDIDO ====================

    @Test
    void testCrearPedido() {
        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setId(1);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTotal(BigDecimal.ZERO);

        // Verificaciones
        assertNotNull(pedido);
        assertEquals(1, pedido.getId());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()));
    }

    @Test
    void testPedidoConCliente() {
        // Crear cliente
        Cliente cliente = new Cliente();
        cliente.setId(1);
        cliente.setNombre("Juan Perez");

        // Crear pedido con cliente
        Pedido pedido = new Pedido();
        pedido.setId(1);
        pedido.setCliente(cliente);

        assertNotNull(pedido.getCliente());
        assertEquals(1, pedido.getCliente().getId());
        assertEquals("Juan Perez", pedido.getCliente().getNombre());
    }

    // ==================== PRUEBAS DE ESTADOS DEL PEDIDO ====================

    @Test
    void testEstadoPendiente() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.PENDIENTE);

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertNotEquals(EstadoPedido.PAGADO, pedido.getEstado());
    }

    @Test
    void testEstadoPagado() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.PAGADO);

        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
    }

    @Test
    void testEstadoEnviado() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.ENVIADO);

        assertEquals(EstadoPedido.ENVIADO, pedido.getEstado());
    }

    @Test
    void testEstadoEntregado() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.ENTREGADO);

        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    @Test
    void testEstadoCancelado() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.CANCELADO);

        assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
    }

    @Test
    void testCambiarEstado() {
        Pedido pedido = new Pedido();
        pedido.setEstado(EstadoPedido.PENDIENTE);

        // Cambiar estado a PAGADO
        pedido.setEstado(EstadoPedido.PAGADO);

        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
    }

    // ==================== PRUEBAS DE CÁLCULOS ====================

    @Test
    void testCalcularTotalPedido() {
        Pedido pedido = new Pedido();
        List<DetallePedido> detalles = new ArrayList<>();

        // Crear detalles
        DetallePedido detalle1 = new DetallePedido();
        detalle1.setSubtotal(new BigDecimal("2999.00"));
        detalles.add(detalle1);

        DetallePedido detalle2 = new DetallePedido();
        detalle2.setSubtotal(new BigDecimal("199.00"));
        detalles.add(detalle2);

        pedido.setDetalles(detalles);

        // Calcular total
        BigDecimal total = pedido.getDetalles().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("3198.00").compareTo(total));
    }

    @Test
    void testTotalCeroSinDetalles() {
        Pedido pedido = new Pedido();
        pedido.setDetalles(new ArrayList<>());

        BigDecimal total = pedido.getDetalles().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, BigDecimal.ZERO.compareTo(total));
    }

    // ==================== PRUEBAS DE DETALLES DEL PEDIDO ====================

    @Test
    void testAgregarDetalleAPedido() {
        Pedido pedido = new Pedido();
        pedido.setId(1);
        pedido.setDetalles(new ArrayList<>());

        // Crear detalle
        DetallePedido detalle = new DetallePedido();
        detalle.setId(1);
        detalle.setCantidad(2);
        detalle.setPrecio(new BigDecimal("2999.00"));
        detalle.calcularSubtotal();

        // Agregar al pedido
        pedido.getDetalles().add(detalle);

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(2, pedido.getDetalles().get(0).getCantidad());
        assertEquals(0, new BigDecimal("5998.00").compareTo(detalle.getSubtotal()));
    }

    @Test
    void testEliminarDetalleDePedido() {
        Pedido pedido = new Pedido();
        pedido.setDetalles(new ArrayList<>());

        DetallePedido detalle1 = new DetallePedido();
        detalle1.setId(1);

        DetallePedido detalle2 = new DetallePedido();
        detalle2.setId(2);

        pedido.getDetalles().add(detalle1);
        pedido.getDetalles().add(detalle2);

        assertEquals(2, pedido.getDetalles().size());

        // Eliminar detalle con ID 1
        pedido.getDetalles().removeIf(d -> d.getId().equals(1));

        assertEquals(1, pedido.getDetalles().size());
        assertEquals(2, pedido.getDetalles().get(0).getId());
    }

    // ==================== PRUEBAS DE FECHAS ====================

    @Test
    void testFechaCreacionPedido() {
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = new Pedido();
        pedido.setFecha(ahora);

        assertNotNull(pedido.getFecha());
        assertEquals(ahora, pedido.getFecha());
    }

    @Test
    void testPedidoFechaFutura() {
        LocalDateTime futuro = LocalDateTime.now().plusDays(1);
        Pedido pedido = new Pedido();
        pedido.setFecha(futuro);

        assertTrue(pedido.getFecha().isAfter(LocalDateTime.now()));
    }

    // ==================== PRUEBAS DE LISTAS ====================

    @Test
    void testListaPedidosVacia() {
        List<Pedido> pedidos = new ArrayList<>();

        assertTrue(pedidos.isEmpty());
        assertEquals(0, pedidos.size());
    }

    @Test
    void testAgregarPedidoALista() {
        List<Pedido> pedidos = new ArrayList<>();

        Pedido pedido = new Pedido();
        pedido.setId(1);
        pedido.setTotal(new BigDecimal("2999.00"));

        pedidos.add(pedido);

        assertFalse(pedidos.isEmpty());
        assertEquals(1, pedidos.size());
        assertEquals(0, new BigDecimal("2999.00").compareTo(pedidos.get(0).getTotal()));
    }

    // ==================== PRUEBAS DE VALIDACIONES ====================

    @Test
    void testPedidoConTotalNegativo() {
        Pedido pedido = new Pedido();
        pedido.setTotal(new BigDecimal("-100.00"));

        assertTrue(pedido.getTotal().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void testPedidoConTotalCero() {
        Pedido pedido = new Pedido();
        pedido.setTotal(BigDecimal.ZERO);

        assertEquals(0, BigDecimal.ZERO.compareTo(pedido.getTotal()));
    }

    @Test
    void testPedidoConTotalPositivo() {
        Pedido pedido = new Pedido();
        pedido.setTotal(new BigDecimal("1500.00"));

        assertTrue(pedido.getTotal().compareTo(BigDecimal.ZERO) > 0);
    }
}