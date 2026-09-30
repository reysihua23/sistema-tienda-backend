package com.reydi.tienda.controller;

import com.reydi.tienda.dto.LoginRequest;
import com.reydi.tienda.dto.LoginResponse;
import com.reydi.tienda.dto.RegisterEmployeeRequest;
import com.reydi.tienda.dto.RegisterRequest;
import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.Rol;
import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.service.AuthService;
import com.reydi.tienda.service.ClienteService;
import com.reydi.tienda.service.PasswordResetService;
import com.reydi.tienda.service.RolService;
import com.reydi.tienda.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;         // ✅ NUEVO
    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final ClienteService clienteService;
    private final PasswordResetService passwordResetService;

    // =========================================================
    // 🔐 LOGIN
    // =========================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // ✅ Mensaje genérico (no revela si el correo existe o no)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("error", "Credenciales inválidas")
            );
        }
    }

    // =========================================================
    // 📝 REGISTRO DE CLIENTE
    // =========================================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validar que el correo no exista
            if (usuarioService.existeCorreo(request.getCorreo())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El correo electrónico ya está registrado");
                error.put("campo", "email");
                return ResponseEntity.badRequest().body(error);
            }

            // Validar nombre
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El nombre es obligatorio");
                error.put("campo", "nombre");
                return ResponseEntity.badRequest().body(error);
            }

            // Validar teléfono (9 dígitos)
            if (request.getTelefono() != null && !request.getTelefono().isEmpty()) {
                if (!request.getTelefono().matches("^[0-9]{9}$")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "El teléfono debe tener 9 dígitos");
                    error.put("campo", "telefono");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Validar documento (8 o 11 dígitos)
            if (request.getDocumento() != null && !request.getDocumento().isEmpty()) {
                if (!request.getDocumento().matches("^[0-9]{8}$")
                        && !request.getDocumento().matches("^[0-9]{11}$")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "El documento debe tener 8 dígitos (DNI) o 11 dígitos (RUC)");
                    error.put("campo", "documento");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // ==================== CREAR CLIENTE ====================
            Cliente cliente = new Cliente();
            cliente.setNombre(request.getNombre());
            cliente.setEmail(request.getCorreo());
            cliente.setTelefono(request.getTelefono());
            cliente.setDocumento(request.getDocumento());
            cliente.setDireccion(request.getDireccion());
            cliente.setFechaRegistro(LocalDateTime.now());
            cliente.setUpdatedAt(LocalDateTime.now());

            Cliente clienteGuardado = clienteService.guardar(cliente);

            // ==================== CREAR USUARIO ====================
            Rol rolCliente = rolService.buscarPorNombre(TipoRol.CLIENTE)
                    .orElseThrow(() -> new RuntimeException("Rol CLIENTE no encontrado"));

            Usuario usuario = new Usuario();
            usuario.setCorreo(request.getCorreo());
            usuario.setNombre(request.getNombre());   // ✅ AHORA SÍ GUARDA EL NOMBRE
            usuario.setPasswordHash(new BCryptPasswordEncoder().encode(request.getPassword()));
            usuario.setRol(rolCliente);
            usuario.setCliente(clienteGuardado);
            usuario.setActivo(true);

            usuarioService.guardar(usuario);

            // ==================== RESPUESTA ====================
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cliente registrado exitosamente");
            response.put("clienteId", clienteGuardado.getId());
            response.put("usuarioId", usuario.getId());
            response.put("correo", usuario.getCorreo());
            response.put("nombre", usuario.getNombre());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // =========================================================
    // 👨‍💼 REGISTRO DE EMPLEADO
    // =========================================================
    @PostMapping("/register-employee")
    public ResponseEntity<?> registerEmployee(@RequestBody RegisterEmployeeRequest request) {
        try {
            // Validar que el correo no exista
            if (usuarioService.existeCorreo(request.getCorreo())) {
                return ResponseEntity.badRequest().body("El correo ya está registrado");
            }

            // Validar rol
            TipoRol tipoRol;
            try {
                tipoRol = TipoRol.valueOf(request.getRol().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Rol inválido. Use: ADMIN, VENTAS, TECNICO");
            }

            // Buscar el rol
            Rol rol = rolService.buscarPorNombre(tipoRol)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

            // Crear usuario
            Usuario usuario = new Usuario();
            usuario.setCorreo(request.getCorreo());
            usuario.setNombre(request.getNombreCompleto());   // ✅ USA nombreCompleto
            usuario.setPasswordHash(new BCryptPasswordEncoder().encode(request.getPassword()));
            usuario.setRol(rol);
            usuario.setCliente(null);
            usuario.setActivo(true);

            usuarioService.guardar(usuario);

            return ResponseEntity.status(201).body(
                    "Usuario " + tipoRol + " registrado exitosamente"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // =========================================================
    // 🔑 RECUPERAR CONTRASEÑA
    // =========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Correo requerido"));
        }

        try {
            passwordResetService.solicitarRecuperacion(correo);
            return ResponseEntity.ok(Map.of(
                    "message", "Si el correo está registrado, recibirás un enlace de recuperación."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(429).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String nuevaPassword = body.get("nuevaPassword");

        if (token == null || nuevaPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token y contraseña requeridos"));
        }

        try {
            passwordResetService.restablecerPassword(token, nuevaPassword);
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}