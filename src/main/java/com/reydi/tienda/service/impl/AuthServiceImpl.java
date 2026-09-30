package com.reydi.tienda.service.impl;

import com.reydi.tienda.dto.LoginRequest;
import com.reydi.tienda.dto.LoginResponse;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.security.JwtUtil;
import com.reydi.tienda.service.AuthService;
import com.reydi.tienda.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioService usuarioService;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        // 1. Autenticar (lanza RuntimeException si falla)
        Usuario usuario = usuarioService.autenticar(
                request.getCorreo(),
                request.getPassword()
        );

        // 2. Generar token JWT
        String token = jwtUtil.generarToken(
                usuario.getCorreo(),
                usuario.getRol().getNombre().name(),
                usuario.getId()
        );

        // 3. Determinar nombre con prioridad correcta
        String nombre = obtenerNombre(usuario);

        // 4. Cliente ID (puede ser null si es empleado)
        Integer clienteId = usuario.getCliente() != null
                ? usuario.getCliente().getId()
                : null;

        // 5. Construir respuesta
        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre().name())
                .usuarioId(usuario.getId())
                .clienteId(clienteId)
                .nombre(nombre)
                .build();
    }

    /**
     * 🎯 Prioridad del nombre:
     * 1. usuario.nombre (empleados: ADMIN, VENTAS, TECNICO) ← FIX DEL BUG
     * 2. usuario.cliente.nombre (clientes registrados)
     * 3. usuario.correo (fallback final)
     */
    private String obtenerNombre(Usuario usuario) {
        // 1. Nombre del usuario (empleados)
        if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) {
            return usuario.getNombre();
        }

        // 2. Nombre del cliente (usuarios con cliente asociado)
        if (usuario.getCliente() != null
                && usuario.getCliente().getNombre() != null
                && !usuario.getCliente().getNombre().trim().isEmpty()) {
            return usuario.getCliente().getNombre();
        }

        // 3. Fallback: correo
        return usuario.getCorreo();
    }
}