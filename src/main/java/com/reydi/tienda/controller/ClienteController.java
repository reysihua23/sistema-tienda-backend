package com.reydi.tienda.controller;

import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<Cliente>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Integer id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(@PathVariable String email) {
        Optional<Cliente> cliente = clienteService.buscarPorEmail(email);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Cliente no encontrado con email: " + email));
        }
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String documento) {
        Optional<Cliente> cliente = clienteService.buscarPorDocumento(documento);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Cliente no encontrado con documento: " + documento));
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Cliente cliente) {
        try {
            // ========== VALIDACIONES ANTES DE GUARDAR ==========

            // 1. Validar que el nombre no esté vacío
            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El nombre del cliente es obligatorio");
                error.put("campo", "nombre");
                return ResponseEntity.badRequest().body(error);
            }

            // 2. Validar formato de email si se proporciona
            if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
                String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                if (!cliente.getEmail().matches(emailRegex)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Formato de correo electrónico inválido");
                    error.put("campo", "email");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 3. Validar formato de teléfono (9 dígitos)
            if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
                if (!cliente.getTelefono().matches("^[0-9]{9}$")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "El teléfono debe tener 9 dígitos");
                    error.put("campo", "telefono");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 4. Validar formato de documento (DNI: 8, RUC: 11)
            if (cliente.getDocumento() != null && !cliente.getDocumento().isEmpty()) {
                if (!cliente.getDocumento().matches("^[0-9]{8}$") && !cliente.getDocumento().matches("^[0-9]{11}$")) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "El documento debe tener 8 dígitos (DNI) o 11 dígitos (RUC)");
                    error.put("campo", "documento");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 5. VERIFICAR SI EL EMAIL YA EXISTE (EVITA EL AUTOINCREMENTO)
            if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
                if (clienteService.existePorEmail(cliente.getEmail())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Ya existe un cliente registrado con el correo: " + cliente.getEmail());
                    error.put("campo", "email");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // 6. VERIFICAR SI EL DOCUMENTO YA EXISTE (EVITA EL AUTOINCREMENTO)
            if (cliente.getDocumento() != null && !cliente.getDocumento().isEmpty()) {
                if (clienteService.existePorDocumento(cliente.getDocumento())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Ya existe un cliente registrado con el documento: " + cliente.getDocumento());
                    error.put("campo", "documento");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Si todas las validaciones pasan, guardar el cliente
            Cliente nuevoCliente = clienteService.guardar(cliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al crear el cliente: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Cliente cliente) {
        try {
            // Verificar si el cliente existe
            if (!clienteService.buscarPorId(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Validar formato de email si se proporciona
            if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
                String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                if (!cliente.getEmail().matches(emailRegex)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Formato de correo electrónico inválido"));
                }
            }

            // Validar formato de teléfono
            if (cliente.getTelefono() != null && !cliente.getTelefono().isEmpty()) {
                if (!cliente.getTelefono().matches("^[0-9]{9}$")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El teléfono debe tener 9 dígitos"));
                }
            }

            // Validar formato de documento
            if (cliente.getDocumento() != null && !cliente.getDocumento().isEmpty()) {
                if (!cliente.getDocumento().matches("^[0-9]{8}$") && !cliente.getDocumento().matches("^[0-9]{11}$")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El documento debe tener 8 dígitos (DNI) o 11 dígitos (RUC)"));
                }
            }

            // Verificar si el email ya está en uso por otro cliente
            if (cliente.getEmail() != null && !cliente.getEmail().isEmpty()) {
                if (clienteService.existePorEmailYIdDistinto(cliente.getEmail(), id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ya existe otro cliente con el correo: " + cliente.getEmail()));
                }
            }

            // Verificar si el documento ya está en uso por otro cliente
            if (cliente.getDocumento() != null && !cliente.getDocumento().isEmpty()) {
                if (clienteService.existePorDocumentoYIdDistinto(cliente.getDocumento(), id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Ya existe otro cliente con el documento: " + cliente.getDocumento()));
                }
            }

            cliente.setId(id);
            Cliente clienteActualizado = clienteService.actualizar(cliente);
            return ResponseEntity.ok(clienteActualizado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            clienteService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}