package com.reydi.tienda.service;

import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationRecipientResolver {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve el ID del primer ADMIN activo.
     */
    public Integer adminId() {
        List<Usuario> admins = usuarioRepository.findByRolNombre(TipoRol.ADMIN);
        return admins.stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .findFirst()
                .map(Usuario::getId)
                .orElseThrow(() -> new RuntimeException("No hay usuario ADMIN activo para notificar"));
    }

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