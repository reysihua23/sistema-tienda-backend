package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ComprobanteResponseDTO;
import com.reydi.tienda.model.Comprobante;
import com.reydi.tienda.model.TipoComprobante;
import com.reydi.tienda.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

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
}