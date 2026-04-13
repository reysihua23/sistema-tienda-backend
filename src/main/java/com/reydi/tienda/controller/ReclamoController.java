package com.reydi.tienda.controller;

import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import com.reydi.tienda.service.ReclamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
public class ReclamoController {

    private final ReclamoService reclamoService;

    @GetMapping
    public ResponseEntity<List<Reclamo>> listar() {
        return ResponseEntity.ok(reclamoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reclamo> buscarPorId(@PathVariable Integer id) {
        return reclamoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Reclamo>> buscarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(reclamoService.buscarPorCliente(clienteId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<Reclamo>> buscarPorEstado(@PathVariable EstadoReclamo estado) {
        return ResponseEntity.ok(reclamoService.buscarPorEstado(estado));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Reclamo>> buscarPorTipo(@PathVariable TipoReclamo tipo) {
        return ResponseEntity.ok(reclamoService.buscarPorTipo(tipo));
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Reclamo>> filtrar(
            @RequestParam(required = false) EstadoReclamo estado,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        return ResponseEntity.ok(reclamoService.filtrarAvanzado(estado, clienteId, fechaInicio, fechaFin));
    }

    @PostMapping
    public ResponseEntity<Reclamo> crear(@RequestBody Reclamo reclamo) {
        return ResponseEntity.ok(reclamoService.guardar(reclamo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reclamo> actualizar(@PathVariable Integer id, @RequestBody Reclamo reclamo) {
        try {
            reclamo.setId(id);
            return ResponseEntity.ok(reclamoService.actualizar(reclamo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Reclamo> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoReclamo estado) {
        try {
            return ResponseEntity.ok(reclamoService.cambiarEstado(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            reclamoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}