package com.reydi.tienda.controller;

import com.reydi.tienda.model.Devolucion;
import com.reydi.tienda.model.EstadoDevolucion;
import com.reydi.tienda.service.DevolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/devoluciones")
@RequiredArgsConstructor
public class DevolucionController {

    private final DevolucionService devolucionService;

    @GetMapping
    public ResponseEntity<List<Devolucion>> listar() {
        return ResponseEntity.ok(devolucionService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devolucion> buscarPorId(@PathVariable Long id) {
        return devolucionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reclamo/{reclamoId}")
    public ResponseEntity<Devolucion> buscarPorReclamo(@PathVariable Long reclamoId) {
        return devolucionService.buscarPorReclamo(reclamoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Devolucion>> buscarPorEstado(@PathVariable EstadoDevolucion estado) {
        return ResponseEntity.ok(devolucionService.buscarPorEstado(estado));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<Devolucion>> buscarPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(devolucionService.buscarPorPedido(pedidoId));
    }

    @PostMapping
    public ResponseEntity<Devolucion> crear(@RequestBody Devolucion devolucion) {
        return ResponseEntity.ok(devolucionService.guardar(devolucion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devolucion> actualizar(@PathVariable Long id, @RequestBody Devolucion devolucion) {
        try {
            devolucion.setId(id);
            return ResponseEntity.ok(devolucionService.actualizar(devolucion));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            devolucionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}