package com.reydi.tienda.controller;

import com.reydi.tienda.dto.EnvioResponseDTO;
import com.reydi.tienda.model.Envio;
import com.reydi.tienda.model.EstadoEnvio;
import com.reydi.tienda.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/envios")
@RequiredArgsConstructor
public class EnvioController {

    private final EnvioService envioService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<List<Envio>> listar() {
        return ResponseEntity.ok(envioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Envio> buscarPorId(@PathVariable Integer id) {
        return envioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EnvioResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        List<Envio> envios = envioService.buscarPorPedido(pedidoId);
        List<EnvioResponseDTO> dtos = envios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<List<Envio>> buscarPorEstado(@PathVariable EstadoEnvio estado) {
        return ResponseEntity.ok(envioService.buscarPorEstado(estado));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EnvioResponseDTO> buscarPorCodigo(@PathVariable String codigo) {
        return envioService.buscarPorCodigoSeguimiento(codigo)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Envio> crear(@RequestBody Envio envio) {
        try {
            return ResponseEntity.ok(envioService.guardar(envio));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Envio> actualizar(@PathVariable Integer id, @RequestBody Envio envio) {
        try {
            envio.setId(id);
            return ResponseEntity.ok(envioService.actualizar(envio));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<Envio> cambiarEstado(@PathVariable Integer id, @RequestParam EstadoEnvio estado) {
        try {
            Envio envio = envioService.actualizarEstado(id, estado);
            return ResponseEntity.ok(envio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            envioService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private EnvioResponseDTO convertirADTO(Envio envio) {
        EnvioResponseDTO dto = new EnvioResponseDTO();
        dto.setId(envio.getId());
        dto.setPedidoId(envio.getPedido().getId());
        dto.setMetodoEnvio(envio.getMetodoEnvio());
        dto.setDireccion(envio.getDireccion());
        dto.setCostoEnvio(envio.getCostoEnvio());
        dto.setEstado(envio.getEstado());
        dto.setFechaEnvio(envio.getFechaEnvio());
        dto.setFechaEntrega(envio.getFechaEntrega());
        dto.setCodigoSeguimiento(envio.getCodigoSeguimiento());
        //dto.setFechaEmision(envio.getFechaEmision());
        return dto;
    }
}