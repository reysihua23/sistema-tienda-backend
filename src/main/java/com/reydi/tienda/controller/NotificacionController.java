package com.reydi.tienda.controller;

import com.reydi.tienda.model.Notificacion;
import com.reydi.tienda.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<Notificacion>> listar() {
        return ResponseEntity.ok(notificacionService.listarTodos());
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> listarNoLeidas() {
        return ResponseEntity.ok(notificacionService.listarNoLeidas());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Notificacion>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(notificacionService.listarPorTipo(tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> buscarPorId(@PathVariable Integer id) {
        return notificacionService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Notificacion> crear(@RequestBody Notificacion notificacion) {
        return ResponseEntity.ok(notificacionService.guardar(notificacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notificacion> actualizar(@PathVariable Integer id, @RequestBody Notificacion notificacion) {
        try {
            notificacion.setId(id);
            return ResponseEntity.ok(notificacionService.actualizar(notificacion));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Integer id) {
        try {
            notificacionService.marcarComoLeida(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        notificacionService.marcarTodasComoLeidas();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            notificacionService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}