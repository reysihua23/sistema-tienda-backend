package com.reydi.tienda.security;

import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioActualHelper {

    private final UsuarioRepository usuarioRepository;

    /**
     * Devuelve el usuario logueado actualmente, o null si no hay sesión.
     */
    public Usuario getUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String correo = auth.getName();
        if (correo == null || "anonymousUser".equals(correo)) return null;
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    /**
     * Devuelve el rol del usuario actual, o null si no hay sesión.
     */
    public TipoRol getRolActual() {
        Usuario u = getUsuarioActual();
        return (u != null && u.getRol() != null) ? u.getRol().getNombre() : null;
    }

    /**
     * Devuelve el ID del usuario actual, o null si no hay sesión.
     */
    public Integer getUsuarioActualId() {
        Usuario u = getUsuarioActual();
        return u != null ? u.getId() : null;
    }

    /**
     * ¿El usuario actual tiene uno de estos roles?
     */
    public boolean tieneRol(TipoRol... roles) {
        TipoRol actual = getRolActual();
        if (actual == null) return false;
        for (TipoRol r : roles) {
            if (r == actual) return true;
        }
        return false;
    }
}