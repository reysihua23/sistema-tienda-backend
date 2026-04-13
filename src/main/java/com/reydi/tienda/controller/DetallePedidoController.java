package com.reydi.tienda.controller;

import com.reydi.tienda.dto.DetallePedidoResponseDTO;
import com.reydi.tienda.model.DetallePedido;
import com.reydi.tienda.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/detalle-pedido")
@RequiredArgsConstructor
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DetallePedidoResponseDTO>> listar() {
        List<DetallePedido> detalles = detallePedidoService.listarTodos();
        List<DetallePedidoResponseDTO> dtos = detalles.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DetallePedidoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return detallePedidoService.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DetallePedidoResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        List<DetallePedido> detalles = detallePedidoService.buscarPorPedido(pedidoId);
        List<DetallePedidoResponseDTO> dtos = detalles.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DetallePedidoResponseDTO>> buscarPorProducto(@PathVariable Integer productoId) {
        List<DetallePedido> detalles = detallePedidoService.buscarPorProducto(productoId);
        List<DetallePedidoResponseDTO> dtos = detalles.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DetallePedidoResponseDTO> crear(@RequestBody DetallePedido detallePedido) {
        try {
            DetallePedido nuevoDetalle = detallePedidoService.guardar(detallePedido);
            return ResponseEntity.ok(convertirADTO(nuevoDetalle));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DetallePedidoResponseDTO> actualizar(@PathVariable Integer id, @RequestBody DetallePedido detallePedido) {
        try {
            detallePedido.setId(id);
            DetallePedido actualizado = detallePedidoService.actualizar(detallePedido);
            return ResponseEntity.ok(convertirADTO(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            detallePedidoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarPorPedido(@PathVariable Integer pedidoId) {
        detallePedidoService.eliminarPorPedido(pedidoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convertir DetallePedido a DetallePedidoResponseDTO
     */
    private DetallePedidoResponseDTO convertirADTO(DetallePedido detalle) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecio(detalle.getPrecio());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}