package com.reydi.tienda.service;

import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class NotificationRecipientResolver {

    private final UsuarioRepository usuarioRepository;

    // =========================================================
    // ✅ ADMIN
    // =========================================================

    /**
     * Primer ADMIN activo (para cuando solo quieres notificar a uno).
     */
    public Integer adminId() {
        return usuarioRepository.findByRolNombre(TipoRol.ADMIN).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException("No hay usuario ADMIN activo para notificar"));
    }

    // =========================================================
    // ✅ LISTA DE IDs POR ROL
    // =========================================================

    /**
     * Todos los IDs de usuarios activos con un rol específico.
     */
    public List<Integer> idsPorRol(TipoRol rol) {
        return usuarioRepository.findByRolNombre(rol).stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .map(Usuario::getId)
                .toList();
    }

    /**
     * IDs de ADMIN + VENTAS.
     * Se usa para notificaciones de gestión (pedidos, pagos, reclamos, productos, stock).
     */
    public List<Integer> idsAdminYVentas() {
        return Stream.concat(
                idsPorRol(TipoRol.ADMIN).stream(),
                idsPorRol(TipoRol.VENTAS).stream()
        ).distinct().toList();
    }

    /**
     * IDs de ADMIN + TECNICO.
     * Se usa para notificaciones de servicios técnicos.
     */
    public List<Integer> idsAdminYTecnico() {
        return Stream.concat(
                idsPorRol(TipoRol.ADMIN).stream(),
                idsPorRol(TipoRol.TECNICO).stream()
        ).distinct().toList();
    }

    // =========================================================
    // ✅ CLIENTE
    // =========================================================

    /**
     * Devuelve el usuarioId asociado al cliente. Lanza excepción si no existe.
     */
    public Integer clienteUsuarioId(Cliente cliente) {
        if (cliente == null) {
            throw new RuntimeException("Cliente nulo");
        }
        return usuarioRepository.findByClienteId(cliente.getId())
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException(
                        "No hay usuario asociado al cliente id=" + cliente.getId()
                ));
    }

    /**
     * Variante segura: devuelve null si no encuentra usuario (no rompe el flujo).
     */
    public Integer clienteUsuarioIdOrNull(Cliente cliente) {
        if (cliente == null) return null;
        return usuarioRepository.findByClienteId(cliente.getId())
                .map(Usuario::getId)
                .orElse(null);
    }
}