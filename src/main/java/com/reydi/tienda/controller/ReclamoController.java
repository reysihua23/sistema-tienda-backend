package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ReclamoRequestDTO;
import com.reydi.tienda.dto.ReclamoResponseDTO;
import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.UsuarioRepository;
import com.reydi.tienda.security.JwtUtil;
import com.reydi.tienda.service.ReclamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reclamos")
@RequiredArgsConstructor
public class ReclamoController {

    private final ReclamoService reclamoService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    // =========================================================
    // ✅ HELPER: obtener clienteId desde el token
    // =========================================================
    private Integer getClienteIdDesdeToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no proporcionado");
        }

        String token = authHeader.substring(7);
        Integer usuarioId = jwtUtil.extraerUsuarioId(token);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getCliente() == null) {
            throw new RuntimeException("El usuario no está asociado a un cliente");
        }

        return usuario.getCliente().getId();
    }

    // =========================================================
    // ✅ LISTADOS Y BÚSQUEDAS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<ReclamoResponseDTO>> listar() {
        return ResponseEntity.ok(reclamoService.listarTodosDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reclamo> buscarPorId(@PathVariable Integer id) {
        return reclamoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<ReclamoResponseDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(reclamoService.buscarPorClienteDTO(clienteId));
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

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ReclamoResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        return ResponseEntity.ok(reclamoService.buscarPorPedido(pedidoId));
    }


    // =========================================================
    // ✅ CREAR RECLAMO (con clienteId y pedidoId)
    // =========================================================

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ReclamoRequestDTO dto) {
        try {
            ReclamoResponseDTO response = reclamoService.crearDesdeDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    // ✅ CANCELAR RECLAMO (solo el cliente dueño)
    // =========================================================

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Integer clienteId = getClienteIdDesdeToken(authHeader);
            ReclamoResponseDTO response = reclamoService.cancelar(id, clienteId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================
    // ✅ ACTUALIZAR / CAMBIAR ESTADO / ELIMINAR
    // =========================================================

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
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoReclamo estado) {
        try {
            return ResponseEntity.ok(reclamoService.cambiarEstadoDTO(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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