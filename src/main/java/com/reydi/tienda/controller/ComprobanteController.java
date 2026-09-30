package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ComprobanteResponseDTO;
import com.reydi.tienda.model.Comprobante;
import com.reydi.tienda.model.DetallePedido;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.model.TipoComprobante;
import com.reydi.tienda.service.ClienteService;
import com.reydi.tienda.service.ComprobanteService;
import com.reydi.tienda.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;
    // 🔽 Agrega esto al inicio de la clase, junto a las otras @Autowired
    @Autowired
    private EmailService emailService;


    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'CLIENTE')")
    public ResponseEntity<List<Comprobante>> listar() {
        return ResponseEntity.ok(comprobanteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comprobante> buscarPorId(@PathVariable Integer id) {
        return comprobanteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComprobanteResponseDTO> buscarPorPedido(@PathVariable Integer pedidoId) {
        ComprobanteResponseDTO dto = comprobanteService.obtenerComprobantePorPedido(pedidoId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/servicio/{servicioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<List<Comprobante>> buscarPorServicio(@PathVariable Integer servicioId) {
        return ResponseEntity.ok(comprobanteService.buscarPorServicio(servicioId));
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<List<Comprobante>> buscarPorTipo(@PathVariable TipoComprobante tipo) {
        return ResponseEntity.ok(comprobanteService.buscarPorTipo(tipo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Comprobante> crear(@RequestBody Comprobante comprobante) {
        try {
            return ResponseEntity.ok(comprobanteService.guardar(comprobante));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Comprobante> actualizar(@PathVariable Integer id, @RequestBody Comprobante comprobante) {
        try {
            comprobante.setId(id);
            return ResponseEntity.ok(comprobanteService.actualizar(comprobante));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            comprobanteService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================================
    // ✅ NUEVO: Enviar comprobante al correo del cliente
    // =========================================================
    @PostMapping("/{id}/enviar")
    public ResponseEntity<?> enviarComprobantePorCorreo(@PathVariable Integer id) {
        try {
            // 1. Buscar comprobante
            Comprobante comprobante = comprobanteService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Comprobante no encontrado"));

            // 2. Obtener email y nombre del cliente (directo desde el comprobante)
            String emailCliente = comprobante.getClienteEmail();
            String nombreCliente = comprobante.getClienteNombre();

            if (emailCliente == null || emailCliente.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "El comprobante no tiene un correo registrado")
                );
            }

            // 3. Obtener el pedido y sus detalles
            Pedido pedido = comprobante.getPedido();
            if (pedido == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "El comprobante no tiene un pedido asociado")
                );
            }

            List<DetallePedido> detalles = pedido.getDetalles();

            // 4. Mapear productos
            List<Map<String, Object>> productos = new ArrayList<>();
            if (detalles != null) {
                for (DetallePedido d : detalles) {
                    Map<String, Object> item = new HashMap<>();

                    String nombreProducto = d.getProducto() != null
                            ? d.getProducto().getNombre()
                            : "Producto";

                    item.put("nombre", nombreProducto);
                    item.put("cantidad", d.getCantidad());
                    item.put("precio", "S/ " + d.getPrecio());
                    item.put("subtotal", "S/ " + d.getSubtotal());
                    productos.add(item);
                }
            }

            // 5. Método de pago desde el pedido
            String metodoPago = pedido.getMetodoPago() != null
                    ? pedido.getMetodoPago()
                    : "EFECTIVO";

            // 6. Generar PDF adjunto (¡ya tienes el método!)
            byte[] pdfAdjunto = null;
            try {
                pdfAdjunto = comprobanteService.generarPDFComprobante(pedido.getId());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo generar el PDF: " + e.getMessage());
            }

            // 7. Enviar correo
            emailService.enviarComprobante(
                    emailCliente,
                    nombreCliente != null ? nombreCliente : "Cliente",
                    comprobante.getNumeroComprobante() != null
                            ? comprobante.getNumeroComprobante()
                            : "S/N",
                    comprobante.getFechaEmision() != null
                            ? comprobante.getFechaEmision().toString()
                            : "",
                    "S/ " + comprobante.getTotal(),
                    productos,
                    metodoPago,
                    pdfAdjunto
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Comprobante enviado a " + emailCliente
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    Map.of("error", "Error al enviar comprobante: " + e.getMessage())
            );
        }
    }

    // Dentro de ComprobanteController
    private void enviarComprobantePorCorreoAsync(Integer comprobanteId) {
        try {
            Comprobante comprobante = comprobanteService.buscarPorId(comprobanteId).orElse(null);
            if (comprobante == null) {
                System.err.println("⚠️ Comprobante no encontrado: " + comprobanteId);
                return;
            }

            String emailCliente = comprobante.getClienteEmail();
            if (emailCliente == null || emailCliente.isBlank()) {
                System.out.println("ℹ️ Comprobante sin correo, no se envía: " + comprobanteId);
                return;
            }

            Pedido pedido = comprobante.getPedido();
            if (pedido == null) {
                System.err.println("⚠️ Comprobante sin pedido: " + comprobanteId);
                return;
            }

            List<DetallePedido> detalles = pedido.getDetalles();
            List<Map<String, Object>> productos = new ArrayList<>();
            if (detalles != null) {
                for (DetallePedido d : detalles) {
                    Map<String, Object> item = new HashMap<>();
                    String nombreProducto = d.getProducto() != null
                            ? d.getProducto().getNombre()
                            : "Producto";
                    item.put("nombre", nombreProducto);
                    item.put("cantidad", d.getCantidad());
                    item.put("precio", "S/ " + d.getPrecio());
                    item.put("subtotal", "S/ " + d.getSubtotal());
                    productos.add(item);
                }
            }

            String metodoPago = pedido.getMetodoPago() != null
                    ? pedido.getMetodoPago()
                    : "EFECTIVO";

            byte[] pdfAdjunto = null;
            try {
                pdfAdjunto = comprobanteService.generarPDFComprobante(pedido.getId());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo generar el PDF: " + e.getMessage());
            }

            // ✅ Llamada ASÍNCRONA
            emailService.enviarComprobanteAutomatico(
                    emailCliente,
                    comprobante.getClienteNombre() != null ? comprobante.getClienteNombre() : "Cliente",
                    comprobante.getNumeroComprobante() != null ? comprobante.getNumeroComprobante() : "S/N",
                    comprobante.getFechaEmision() != null ? comprobante.getFechaEmision().toString() : "",
                    "S/ " + comprobante.getTotal(),
                    productos,
                    metodoPago,
                    pdfAdjunto
            );

        } catch (Exception e) {
            System.err.println("❌ Error al preparar envío automático: " + e.getMessage());
            e.printStackTrace();
        }
    }

}