package com.reydi.tienda.controller;

import com.reydi.tienda.model.ReclamoEvidencias;
import com.reydi.tienda.service.ReclamoEvidenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reclamo-evidencias")
@RequiredArgsConstructor
public class ReclamoEvidenciasController {

    private final ReclamoEvidenciasService reclamoEvidenciasService;

    @GetMapping
    public ResponseEntity<List<ReclamoEvidencias>> listar() {
        return ResponseEntity.ok(reclamoEvidenciasService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamoEvidencias> buscarPorId(@PathVariable Integer id) {
        return reclamoEvidenciasService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reclamo/{reclamoId}")
    public ResponseEntity<List<ReclamoEvidencias>> buscarPorReclamo(@PathVariable Integer reclamoId) {
        return ResponseEntity.ok(reclamoEvidenciasService.buscarPorReclamo(reclamoId));
    }

    @PostMapping
    public ResponseEntity<ReclamoEvidencias> crear(@RequestBody ReclamoEvidencias evidencia) {
        return ResponseEntity.ok(reclamoEvidenciasService.guardar(evidencia));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReclamoEvidencias> actualizar(@PathVariable Integer id, @RequestBody ReclamoEvidencias evidencia) {
        try {
            evidencia.setId(id);
            return ResponseEntity.ok(reclamoEvidenciasService.actualizar(evidencia));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            reclamoEvidenciasService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/reclamo/{reclamoId}")
    public ResponseEntity<Void> eliminarPorReclamo(@PathVariable Integer reclamoId) {
        reclamoEvidenciasService.eliminarPorReclamo(reclamoId);
        return ResponseEntity.noContent().build();
    }
}