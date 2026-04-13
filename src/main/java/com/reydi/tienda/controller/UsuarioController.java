package com.reydi.tienda.controller;

import com.reydi.tienda.dto.UsuarioDTO;
import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.Rol;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.service.ClienteService;
import com.reydi.tienda.service.RolService;
import com.reydi.tienda.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final ClienteService clienteService;

    /**
     * Listar todos los usuarios
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listar() {
        List<Usuario> usuarios = usuarioService.listarTodos();
        List<UsuarioDTO> dtos = usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Integer id) {
        return usuarioService.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Buscar usuario por correo
     */
    @GetMapping("/correo/{correo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> buscarPorCorreo(@PathVariable String correo) {
        return usuarioService.buscarPorCorreo(correo)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear usuario (solo ADMIN)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> crear(@RequestBody Map<String, Object> request) {
        try {

            // LOGS PARA VER QUÉ DATOS LLEGAN
            System.out.println("=== DATOS RECIBIDOS EN CREAR USUARIO ===");
            System.out.println("Request completo: " + request);
            System.out.println("Correo: " + request.get("correo"));
            System.out.println("Password: " + request.get("password"));
            System.out.println("Nombre: " + request.get("nombre"));
            System.out.println("RolId: " + request.get("rolId"));
            System.out.println("Activo: " + request.get("activo"));
            System.out.println("Teléfono: " + request.get("telefono"));
            System.out.println("Documento: " + request.get("documento"));
            System.out.println("Dirección: " + request.get("direccion"));

            String correo = (String) request.get("correo");
            String password = (String) request.get("password");
            String nombre = (String) request.get("nombre");
            Integer rolId = (Integer) request.get("rolId");
            Boolean activo = (Boolean) request.get("activo");

            Rol rol = rolService.buscarPorId(rolId)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            // Crear usuario
            Usuario usuario = new Usuario();
            usuario.setCorreo(correo);
            usuario.setNombre(nombre);
            usuario.setPasswordHash(new BCryptPasswordEncoder().encode(password));
            usuario.setRol(rol);
            usuario.setActivo(activo != null ? activo : true);

            // Si es CLIENTE, crear y asignar cliente
            if (rol.getNombre().name().equals("CLIENTE")) {
                // Verificar si ya existe un cliente con ese email
                Optional<Cliente> clienteExistente = clienteService.buscarPorEmail(correo);

                if (clienteExistente.isPresent()) {
                    usuario.setCliente(clienteExistente.get());
                    System.out.println("Cliente existente encontrado - ID: " + clienteExistente.get().getId());
                } else {
                    // Crear nuevo cliente
                    Cliente cliente = new Cliente();
                    cliente.setNombre(nombre);
                    cliente.setEmail(correo);

                    if (request.containsKey("telefono")) {
                        cliente.setTelefono((String) request.get("telefono"));
                    }
                    if (request.containsKey("documento")) {
                        cliente.setDocumento((String) request.get("documento"));
                    }
                    if (request.containsKey("direccion")) {
                        cliente.setDireccion((String) request.get("direccion"));
                    }

                    Cliente nuevoCliente = clienteService.guardar(cliente);
                    usuario.setCliente(nuevoCliente);
                    System.out.println("Nuevo cliente creado - ID: " + nuevoCliente.getId());
                }
            }

            Usuario nuevoUsuario = usuarioService.guardar(usuario);
            System.out.println("Usuario creado - ID: " + nuevoUsuario.getId() +
                    ", Cliente ID: " + (nuevoUsuario.getCliente() != null ? nuevoUsuario.getCliente().getId() : "null"));

            return ResponseEntity.status(201).body(convertirADTO(nuevoUsuario));

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar usuario (solo ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> actualizar(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            Usuario usuarioExistente = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            if (request.containsKey("correo")) {
                usuarioExistente.setCorreo((String) request.get("correo"));
            }

            if (request.containsKey("nombre")) {
                usuarioExistente.setNombre((String) request.get("nombre"));
            }

            if (request.containsKey("activo")) {
                usuarioExistente.setActivo((Boolean) request.get("activo"));
            }

            if (request.containsKey("rolId")) {
                Integer rolId = (Integer) request.get("rolId");
                Rol rol = rolService.buscarPorId(rolId)
                        .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
                usuarioExistente.setRol(rol);
            }

            Usuario actualizado = usuarioService.actualizar(usuarioExistente);
            return ResponseEntity.ok(convertirADTO(actualizado));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambiar estado del usuario (solo ADMIN)
     */
    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> actualizarActivo(@PathVariable Integer id, @RequestParam Boolean activo) {
        try {
            Usuario usuario = usuarioService.actualizarActivo(id, activo);
            return ResponseEntity.ok(convertirADTO(usuario));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Filtrar usuarios (solo ADMIN)
     */
    @GetMapping("/filtrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> filtrar(
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) Integer rolId,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<Usuario> usuarios = usuarioService.filtrarUsuarios(correo, rolId, activo, fechaInicio, fechaFin);
        List<UsuarioDTO> dtos = usuarios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Eliminar usuario (solo ADMIN)
     */
    /**
     * Eliminar usuario (solo ADMIN)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            // Buscar el usuario antes de eliminarlo
            Usuario usuario = usuarioService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            //  Si el usuario tiene un cliente asociado, eliminarlo también
            if (usuario.getCliente() != null) {
                Integer clienteId = usuario.getCliente().getId();
                System.out.println("Usuario es CLIENTE - Cliente ID: " + clienteId);

                //  IMPORTANTE: Desasociar el cliente del usuario antes de eliminarlo
                usuario.setCliente(null);
                usuarioService.guardar(usuario);  // Guardar cambios para desasociar

                // Ahora eliminar el cliente
                clienteService.eliminar(clienteId);
                System.out.println("Cliente eliminado - ID: " + clienteId);
            }

            // Eliminar el usuario
            usuarioService.eliminar(id);
            System.out.println("Usuario eliminado - ID: " + id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambiar contraseña (el propio usuario)
     */
    @PostMapping("/cambiar-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            Integer usuarioId = (Integer) request.get("usuarioId");
            String passwordActual = (String) request.get("passwordActual");
            String passwordNueva = (String) request.get("passwordNueva");

            Usuario usuarioAutenticado = usuarioService.buscarPorCorreo(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (!usuarioAutenticado.getId().equals(usuarioId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No puedes cambiar la contraseña de otro usuario");
            }

            Usuario usuario = usuarioService.buscarPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            if (!encoder.matches(passwordActual, usuario.getPasswordHash())) {
                return ResponseEntity.badRequest().body("Contraseña actual incorrecta");
            }

            usuario.setPasswordHash(encoder.encode(passwordNueva));
            usuarioService.guardar(usuario);

            return ResponseEntity.ok("Contraseña actualizada exitosamente");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtener perfil del usuario autenticado
     */
    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPerfil(Authentication authentication) {
        try {
            String email = authentication.getName();
            Usuario usuario = usuarioService.buscarPorCorreo(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("correo", usuario.getCorreo());
            response.put("rol", usuario.getRol().getNombre().name());

            if (usuario.getCliente() != null) {
                response.put("nombre", usuario.getCliente().getNombre());
                response.put("telefono", usuario.getCliente().getTelefono());
                response.put("documento", usuario.getCliente().getDocumento());
                response.put("direccion", usuario.getCliente().getDireccion());
            } else {
                response.put("nombre", usuario.getNombre() != null ? usuario.getNombre() : usuario.getCorreo());
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Convertir Usuario a UsuarioDTO
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setCorreo(usuario.getCorreo());
        dto.setNombre(usuario.getNombre());
        dto.setActivo(usuario.getActivo());
        dto.setFechaCreacion(usuario.getFechaCreacion());

        if (usuario.getRol() != null) {
            dto.setRol(usuario.getRol().getNombre().name());
        } else {
            dto.setRol("SIN ROL");
        }

        if (usuario.getCliente() != null) {
            dto.setClienteId(usuario.getCliente().getId());
            dto.setClienteNombre(usuario.getCliente().getNombre());
            dto.setClienteEmail(usuario.getCliente().getEmail());
            dto.setClienteTelefono(usuario.getCliente().getTelefono());
            dto.setClienteFechaRegistro(usuario.getCliente().getFechaRegistro());
            dto.setNombreMostrar(usuario.getCliente().getNombre());
        } else {
            dto.setNombreMostrar(usuario.getNombre() != null ? usuario.getNombre() : usuario.getCorreo());
        }

        return dto;
    }
}