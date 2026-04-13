package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ServicioTecnicoResponseDTO;
import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.EstadoServicio;
import com.reydi.tienda.model.ServicioTecnico;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.service.ClienteService;
import com.reydi.tienda.service.ServicioTecnicoService;
import com.reydi.tienda.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/servicios-tecnicos")
@RequiredArgsConstructor
public class ServicioTecnicoController {

    private final ServicioTecnicoService service;
    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    /**
     * Listar todos los servicios técnicos (con DTO)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioTecnicoResponseDTO>> listar() {
        List<ServicioTecnico> servicios = service.listarTodos();
        List<ServicioTecnicoResponseDTO> dtos = servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar servicio por ID (con DTO)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServicioTecnicoResponseDTO> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Buscar servicios por cliente (con DTO)
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioTecnicoResponseDTO>> buscarPorCliente(@PathVariable Integer clienteId) {
        List<ServicioTecnico> servicios = service.buscarPorCliente(clienteId);
        List<ServicioTecnicoResponseDTO> dtos = servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar servicios por estado (con DTO)
     */
    @GetMapping("/estado/{estado}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioTecnicoResponseDTO>> buscarPorEstado(@PathVariable String estado) {
        List<ServicioTecnico> servicios = service.buscarPorEstado(estado);
        List<ServicioTecnicoResponseDTO> dtos = servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar servicios por técnico asignado (con DTO)
     */
    @GetMapping("/tecnico/{tecnicoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServicioTecnicoResponseDTO>> buscarPorTecnico(@PathVariable Integer tecnicoId) {
        List<ServicioTecnico> servicios = service.buscarPorTecnico(tecnicoId);
        List<ServicioTecnicoResponseDTO> dtos = servicios.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Asignar técnico a un servicio (ADMIN o TECNICO)
     */
    @PatchMapping("/{id}/asignar-tecnico")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECNICO')")
    public ResponseEntity<?> asignarTecnico(@PathVariable Integer id, @RequestParam Integer tecnicoId) {
        try {
            ServicioTecnico servicio = service.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            Usuario tecnico = usuarioService.buscarPorId(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));

            // Validar que el usuario tenga rol TECNICO
            if (!tecnico.getRol().getNombre().name().equals("TECNICO")) {
                return ResponseEntity.badRequest().body(Map.of("error", "El usuario no tiene rol de TÉCNICO"));
            }

            servicio.setTecnico(tecnico);
            service.actualizar(servicio);

            Map<String, Object> response = new HashMap<>();
            response.put("id", servicio.getId());
            response.put("tecnicoId", tecnico.getId());
            response.put("tecnicoNombre", tecnico.getNombre());
            response.put("message", "Técnico asignado correctamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Crear un nuevo servicio técnico (con cliente existente o nuevo)
     * Si el usuario autenticado es TECNICO, se asigna automáticamente
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            System.out.println("=== CREANDO SERVICIO TÉCNICO ===");
            System.out.println("Request recibido: " + request);

            String equipo = (String) request.get("equipo");
            String problema = (String) request.get("problema");
            String diagnostico = (String) request.get("diagnostico");
            BigDecimal costo = request.get("costo") != null
                    ? new BigDecimal(request.get("costo").toString())
                    : BigDecimal.ZERO;
            String estadoStr = (String) request.get("estado");

            // Validar campos obligatorios
            if (equipo == null || equipo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El equipo es obligatorio"));
            }
            if (problema == null || problema.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El problema es obligatorio"));
            }

            Cliente cliente = null;

            // ==================== MANEJAR CLIENTE ====================
            // Caso 1: Cliente existente (viene con clienteId)
            if (request.containsKey("clienteId") && request.get("clienteId") != null) {
                Integer clienteId = (Integer) request.get("clienteId");
                cliente = clienteService.buscarPorId(clienteId)
                        .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));
                System.out.println("Usando cliente existente por ID: " + clienteId);
            }
            // Caso 2: Cliente nuevo (viene con datos del cliente)
            else if (request.containsKey("cliente") && request.get("cliente") != null) {
                Map<String, Object> clienteData = (Map<String, Object>) request.get("cliente");

                String nombre = (String) clienteData.get("nombre");
                String email = (String) clienteData.get("email");
                String telefono = (String) clienteData.get("telefono");
                String documento = (String) clienteData.get("documento");
                String direccion = (String) clienteData.get("direccion");

                System.out.println("Datos del cliente recibido:");
                System.out.println("  Nombre: " + nombre);
                System.out.println("  Email: " + email);
                System.out.println("  Teléfono: " + telefono);
                System.out.println("  Documento: " + documento);
                System.out.println("  Dirección: " + direccion);

                if (nombre == null || nombre.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El nombre del cliente es obligatorio"));
                }

                // Verificar si ya existe un cliente con el mismo email
                boolean clienteEncontrado = false;

                if (email != null && !email.isEmpty()) {
                    Optional<Cliente> clienteExistente = clienteService.buscarPorEmail(email);
                    if (clienteExistente.isPresent()) {
                        cliente = clienteExistente.get();
                        clienteEncontrado = true;
                        System.out.println("Cliente encontrado por email - ID: " + cliente.getId());
                    }
                }

                // Verificar si ya existe un cliente con el mismo documento
                if (!clienteEncontrado && documento != null && !documento.isEmpty()) {
                    Optional<Cliente> clienteExistente = clienteService.buscarPorDocumento(documento);
                    if (clienteExistente.isPresent()) {
                        cliente = clienteExistente.get();
                        clienteEncontrado = true;
                        System.out.println("Cliente encontrado por documento - ID: " + cliente.getId());
                    }
                }

                // Si no existe, crear NUEVO cliente
                if (!clienteEncontrado) {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setNombre(nombre);
                    nuevoCliente.setEmail(email);
                    nuevoCliente.setTelefono(telefono);
                    nuevoCliente.setDocumento(documento);
                    nuevoCliente.setDireccion(direccion);

                    cliente = clienteService.guardar(nuevoCliente);
                    System.out.println("NUEVO cliente creado - ID: " + cliente.getId());
                } else {
                    System.out.println("⚠️ Usando cliente existente - ID: " + cliente.getId());
                }
            }
            else {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar clienteId o datos del cliente"));
            }

            // ==================== OBTENER TÉCNICO AUTENTICADO ====================
            Usuario tecnico = null;
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                tecnico = usuarioService.buscarPorCorreo(email).orElse(null);

                // Validar que el usuario tenga rol TECNICO o ADMIN
                if (tecnico != null && (tecnico.getRol().getNombre().name().equals("TECNICO") ||
                        tecnico.getRol().getNombre().name().equals("ADMIN"))) {
                    System.out.println("Técnico autenticado: " + tecnico.getNombre() + " (ID: " + tecnico.getId() + ")");
                } else {
                    tecnico = null; // No asignar técnico si no es TECNICO o ADMIN
                    System.out.println("⚠️ Usuario no es TÉCNICO o ADMIN, no se asigna técnico");
                }
            }

            // ==================== CREAR SERVICIO ====================
            EstadoServicio estado = EstadoServicio.RECIBIDO;
            if (estadoStr != null) {
                try {
                    estado = EstadoServicio.valueOf(estadoStr);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido: " + estadoStr));
                }
            }

            ServicioTecnico servicio = ServicioTecnico.builder()
                    .cliente(cliente)
                    .tecnico(tecnico)
                    .equipo(equipo)
                    .problema(problema)
                    .diagnostico(diagnostico)
                    .costo(costo)
                    .estado(estado)
                    .build();

            ServicioTecnico nuevoServicio = service.guardar(servicio);
            System.out.println("Servicio creado - ID: " + nuevoServicio.getId());
            if (tecnico != null) {
                System.out.println("Técnico asignado: " + tecnico.getNombre());
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(nuevoServicio));

        } catch (RuntimeException e) {
            System.err.println("Error al crear servicio: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * Actualizar servicio
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            ServicioTecnico servicioExistente = service.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

            if (request.containsKey("equipo")) {
                servicioExistente.setEquipo((String) request.get("equipo"));
            }
            if (request.containsKey("problema")) {
                servicioExistente.setProblema((String) request.get("problema"));
            }
            if (request.containsKey("diagnostico")) {
                servicioExistente.setDiagnostico((String) request.get("diagnostico"));
            }
            if (request.containsKey("costo")) {
                servicioExistente.setCosto(new BigDecimal(request.get("costo").toString()));
            }
            if (request.containsKey("estado")) {
                servicioExistente.setEstado(EstadoServicio.valueOf((String) request.get("estado")));
            }

            ServicioTecnico actualizado = service.actualizar(servicioExistente);
            return ResponseEntity.ok(convertirADTO(actualizado));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Cambiar estado del servicio
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        try {
            EstadoServicio nuevoEstado;
            try {
                nuevoEstado = EstadoServicio.valueOf(estado);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido: " + estado));
            }

            ServicioTecnico servicio = service.cambiarEstado(id, estado);

            Map<String, Object> response = new HashMap<>();
            response.put("id", servicio.getId());
            response.put("estado", servicio.getEstado().name());
            response.put("message", "Estado actualizado a " + servicio.getEstado().name());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Agregar diagnóstico al servicio
     */
    @PatchMapping("/{id}/diagnostico")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> agregarDiagnostico(@PathVariable Integer id,
                                                @RequestBody Map<String, Object> request) {
        try {
            String diagnostico = (String) request.get("diagnostico");
            BigDecimal costo = request.get("costo") != null
                    ? new BigDecimal(request.get("costo").toString())
                    : null;

            if (diagnostico == null || diagnostico.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El diagnóstico es obligatorio"));
            }

            ServicioTecnico servicio = service.agregarDiagnostico(id, diagnostico, costo);

            Map<String, Object> response = new HashMap<>();
            response.put("id", servicio.getId());
            response.put("diagnostico", servicio.getDiagnostico());
            response.put("costo", servicio.getCosto());
            response.put("message", "Diagnóstico agregado correctamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Eliminar servicio
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Convertir ServicioTecnico a DTO (evita recursión)
     */
    private ServicioTecnicoResponseDTO convertirADTO(ServicioTecnico servicio) {
        ServicioTecnicoResponseDTO dto = new ServicioTecnicoResponseDTO();
        dto.setId(servicio.getId());
        dto.setEquipo(servicio.getEquipo());
        dto.setProblema(servicio.getProblema());
        dto.setDiagnostico(servicio.getDiagnostico());
        dto.setCosto(servicio.getCosto());
        dto.setEstado(servicio.getEstado().name());
        dto.setFecha(servicio.getFecha());

        if (servicio.getCliente() != null) {
            dto.setClienteId(servicio.getCliente().getId());
            dto.setClienteNombre(servicio.getCliente().getNombre());
            dto.setClienteEmail(servicio.getCliente().getEmail());
            dto.setClienteTelefono(servicio.getCliente().getTelefono());
            dto.setClienteDocumento(servicio.getCliente().getDocumento());
            dto.setClienteDireccion(servicio.getCliente().getDireccion());
        }

        // Agregar información del técnico
        if (servicio.getTecnico() != null) {
            dto.setTecnicoId(servicio.getTecnico().getId());
            dto.setTecnicoNombre(servicio.getTecnico().getNombre());
            dto.setTecnicoEmail(servicio.getTecnico().getCorreo());
        }

        return dto;
    }
}