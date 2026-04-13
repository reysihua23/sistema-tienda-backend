package com.reydi.tienda.controller;

import com.reydi.tienda.dto.PagoResponseDTO;
import com.reydi.tienda.model.Pago;
import com.reydi.tienda.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    /**
     * Listar todos los pagos (solo ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagoResponseDTO>> listar() {
        List<Pago> pagos = pagoService.listarTodos();
        List<PagoResponseDTO> dtos = pagos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar pago por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return pagoService.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Buscar pagos por pedido
     */
    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PagoResponseDTO>> buscarPorPedido(@PathVariable Integer pedidoId) {
        List<Pago> pagos = pagoService.buscarPorPedido(pedidoId);
        List<PagoResponseDTO> dtos = pagos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Aprobar pago
     */
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponseDTO> aprobarPago(@PathVariable Integer id) {
        try {
            Pago pago = pagoService.aprobarPago(id);
            return ResponseEntity.ok(convertirADTO(pago));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rechazar pago
     */
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoResponseDTO> rechazarPago(@PathVariable Integer id) {
        try {
            Pago pago = pagoService.rechazarPago(id);
            return ResponseEntity.ok(convertirADTO(pago));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private PagoResponseDTO convertirADTO(Pago pago) {
        PagoResponseDTO dto = new PagoResponseDTO();
        dto.setId(pago.getId());
        dto.setMetodo(pago.getMetodo());
        dto.setMonto(pago.getMonto());
        dto.setEstado(pago.getEstado());
        dto.setReferenciaPasarela(pago.getReferenciaPasarela());
        dto.setFecha(pago.getFecha());
        return dto;
    }
}