// src/main/java/com/reydi/tienda/controller/NotificacionController.java
package com.reydi.tienda.controller;

import com.reydi.tienda.dto.NotificacionDTO;
import com.reydi.tienda.security.JwtUtil;
import com.reydi.tienda.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final JwtUtil jwtUtil;

    private Integer getUsuarioIdDesdeToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no proporcionado o inválido");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extraerUsuarioId(token);
    }

    // =========================================================
    // ✅ ENDPOINTS PARA USUARIO AUTENTICADO (sus propias notificaciones)
    // =========================================================

    @GetMapping("/mis-notificaciones")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerMisNotificaciones(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            List<NotificacionDTO> notificaciones = notificacionService.obtenerPorUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/no-leidas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> obtenerNoLeidas(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            List<NotificacionDTO> notificaciones = notificacionService.obtenerNoLeidasPorUsuario(usuarioId);
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/contar-no-leidas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> contarNoLeidas(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            Long count = notificacionService.contarNoLeidasPorUsuario(usuarioId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/leer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer id, @RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            boolean success = notificacionService.marcarComoLeida(id, usuarioId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Notificación marcada como leída"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Notificación no encontrada o no pertenece al usuario"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/marcar-todas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> marcarTodasComoLeidas(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            int count = notificacionService.marcarTodasComoLeidas(usuarioId);
            return ResponseEntity.ok(Map.of("success", true, "message", count + " notificaciones marcadas como leídas"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminarNotificacion(@PathVariable Integer id, @RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            boolean success = notificacionService.eliminarNotificacion(id, usuarioId);
            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Notificación eliminada"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Notificación no encontrada o no pertenece al usuario"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/eliminar-todas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminarTodas(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer usuarioId = getUsuarioIdDesdeToken(authHeader);
            notificacionService.eliminarTodas(usuarioId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Todas las notificaciones eliminadas"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    // ✅ ENDPOINTS PARA ADMIN (gestión de notificaciones de otros usuarios)
    // =========================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearNotificacion(@RequestBody Map<String, Object> request) {
        try {
            Integer usuarioId = (Integer) request.get("usuarioId");
            String tipo = (String) request.get("tipo");
            String mensaje = (String) request.get("mensaje");

            if (usuarioId == null || tipo == null || mensaje == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Faltan campos requeridos: usuarioId, tipo, mensaje"
                ));
            }

            NotificacionDTO notificacion = notificacionService.crearNotificacion(usuarioId, tipo, mensaje);
            return ResponseEntity.status(HttpStatus.CREATED).body(notificacion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> obtenerTodas(@RequestHeader("Authorization") String authHeader) {
        try {
            // Opcional: el admin puede ver todas las notificaciones
            List<NotificacionDTO> notificaciones = notificacionService.obtenerTodas();
            return ResponseEntity.ok(notificaciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}