package com.reydi.tienda.controller;

import com.reydi.tienda.dto.*;
import com.reydi.tienda.model.*;
import com.reydi.tienda.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final DetallePedidoService detallePedidoService;
    private final PagoService pagoService;
    private final EnvioService envioService;
    private final ComprobanteService comprobanteService;
    private final StockService stockService;
    private final ClienteService clienteService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");

    // Lista de métodos de pago válidos
    private static final List<String> METODOS_PAGO_VALIDOS = Arrays.asList(
            "EFECTIVO", "TARJETA", "YAPE", "PLIN", "TRANSFERENCIA", "PAYPAL"
    );

    /**
     * Listar pedidos por cliente
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        List<Pedido> pedidos = pedidoService.listarTodos();
        List<PedidoResponseDTO> dtos = pedidos.stream()
                .map(pedido -> {
                    PedidoResponseDTO dto = convertirADTO(pedido);
                    if (pedido.getCliente() != null) {
                        dto.setClienteNombre(pedido.getCliente().getNombre());
                        dto.setClienteEmail(pedido.getCliente().getEmail());
                        dto.setClienteTelefono(pedido.getCliente().getTelefono());
                        dto.setClienteDocumento(pedido.getCliente().getDocumento());
                        dto.setClienteDireccion(pedido.getCliente().getDireccion());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Listar pedidos por cliente
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PedidoResponseDTO>> buscarPorCliente(@PathVariable Integer clienteId, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean isAdmin = usuario.getRol().getNombre().name().equals("ADMIN");
        boolean isOwner = usuario.getCliente() != null && usuario.getCliente().getId().equals(clienteId);

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Pedido> pedidos = pedidoService.buscarPorCliente(clienteId);
        List<PedidoResponseDTO> dtos = pedidos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar pedido por ID (solo el dueño o ADMIN)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Integer id, Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.buscarPorCorreo(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Pedido pedido = pedidoService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        boolean isAdmin = usuario.getRol().getNombre().name().equals("ADMIN");
        boolean isOwner = pedido.getCliente() != null &&
                usuario.getCliente() != null &&
                pedido.getCliente().getId().equals(usuario.getCliente().getId());

        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertirADTO(pedido));
    }

    /**
     * Crear un pedido completo
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS', 'CLIENTE')")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequestDTO request, Authentication authentication) {
        try {
            System.out.println("=== INICIANDO CREACIÓN DE PEDIDO COMPLETO ===");
            System.out.println("Cliente ID: " + request.getClienteId());
            System.out.println("Método Pago: " + request.getMetodoPago());
            System.out.println("Método Envío: " + request.getMetodoEnvio());
            System.out.println("Productos: " + request.getProductos().size());

            // ==================== 1. VALIDACIONES ====================
            if (request.getClienteId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El clienteId es obligatorio"));
            }
            if (request.getProductos() == null || request.getProductos().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe incluir al menos un producto"));
            }
            if (request.getMetodoPago() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El método de pago es obligatorio"));
            }

            // Validar metodo de pago (incluir PAYPAL)
            String metodoPagoStr = request.getMetodoPago().name();
            if (!METODOS_PAGO_VALIDOS.contains(metodoPagoStr)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Método de pago inválido: " + metodoPagoStr));
            }

            // ==================== 2. OBTENER CLIENTE ====================
            Cliente cliente = clienteService.buscarPorId(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

            System.out.println("Cliente encontrado: " + cliente.getNombre());

            // ==================== 3. VALIDAR PRODUCTOS Y STOCK ====================
            List<DetallePedido> detalles = new ArrayList<>();
            BigDecimal subtotalSinIGV = BigDecimal.ZERO;

            for (PedidoRequestDTO.DetallePedidoRequestDTO detalleReq : request.getProductos()) {
                Producto producto = productoService.buscarPorId(detalleReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalleReq.getProductoId()));

                Stock stock = stockService.buscarPorProductoId(producto.getId())
                        .orElseThrow(() -> new RuntimeException("Stock no encontrado para: " + producto.getNombre()));

                if (stock.getCantidad() < detalleReq.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para " + producto.getNombre() +
                            ". Disponible: " + stock.getCantidad() + ", solicitado: " + detalleReq.getCantidad());
                }

                BigDecimal precioSinIGV = detalleReq.getPrecioUnitario().divide(BigDecimal.ONE.add(IGV_PORCENTAJE), 2, RoundingMode.HALF_UP);
                BigDecimal subtotalItem = precioSinIGV.multiply(BigDecimal.valueOf(detalleReq.getCantidad()));
                subtotalSinIGV = subtotalSinIGV.add(subtotalItem);

                DetallePedido detalle = DetallePedido.builder()
                        .producto(producto)
                        .cantidad(detalleReq.getCantidad())
                        .precio(detalleReq.getPrecioUnitario())
                        .subtotal(detalleReq.getPrecioUnitario().multiply(BigDecimal.valueOf(detalleReq.getCantidad())))
                        .build();

                detalles.add(detalle);
                System.out.println("Producto validado: " + producto.getNombre() + " x" + detalleReq.getCantidad());
            }

            // ==================== 4. CALCULAR TOTALES ====================
            BigDecimal igv = subtotalSinIGV.multiply(IGV_PORCENTAJE);
            BigDecimal subtotalConIGV = subtotalSinIGV.add(igv);
            BigDecimal costoEnvio = calcularCostoEnvio(request.getMetodoEnvio());
            BigDecimal total = subtotalConIGV.add(costoEnvio);

            System.out.println("Subtotal sin IGV: S/ " + subtotalSinIGV);
            System.out.println("IGV (18%): S/ " + igv);
            System.out.println("Costo envío: S/ " + costoEnvio);
            System.out.println("Total: S/ " + total);

            // ==================== 5. CREAR PEDIDO ====================
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setTotal(total);
            pedido.setFecha(LocalDateTime.now());

            // DETERMINAR ORIGEN Y ESTADO
            OrigenPedido origen;
            if (request.getOrigen() != null) {
                origen = OrigenPedido.valueOf(request.getOrigen());
            } else {
                if (request.getMetodoEnvio() != null && !request.getMetodoEnvio().equals("RECOJO_EN_TIENDA")) {
                    origen = OrigenPedido.TIENDA_ONLINE;
                } else {
                    origen = OrigenPedido.TIENDA_FISICA;
                }
            }
            pedido.setOrigen(origen);

            // ESTADO según el origen
            pedido.setEstado(EstadoPedido.PAGADO);

            // ASIGNAR MÉTODO DE PAGO
            if (request.getMetodoPago() != null) {
                pedido.setMetodoPago(request.getMetodoPago().name());
            }

            System.out.println("Origen: " + pedido.getOrigen());
            System.out.println("Estado inicial: " + pedido.getEstado());
            System.out.println("Método de pago: " + pedido.getMetodoPago());

            Pedido nuevoPedido = pedidoService.guardar(pedido);
            System.out.println("Pedido creado ID: " + nuevoPedido.getId());

            // ==================== 6. AGREGAR DETALLES AL PEDIDO ====================
            for (DetallePedido detalle : detalles) {
                detalle.setPedido(nuevoPedido);
                detallePedidoService.guardar(detalle);
                nuevoPedido.getDetalles().add(detalle);
            }

            nuevoPedido.actualizarTotal();
            pedidoService.actualizar(nuevoPedido);
            System.out.println("Detalles agregados: " + detalles.size());

            // ==================== 7. ACTUALIZAR STOCK ====================
            for (PedidoRequestDTO.DetallePedidoRequestDTO detalleReq : request.getProductos()) {
                Stock stock = stockService.buscarPorProductoId(detalleReq.getProductoId())
                        .orElseThrow(() -> new RuntimeException("Stock no encontrado"));
                stock.setCantidad(stock.getCantidad() - detalleReq.getCantidad());
                stockService.actualizar(stock);
                System.out.println("Stock actualizado - Producto ID: " + detalleReq.getProductoId() +
                        ", Nuevo stock: " + stock.getCantidad());
            }

            // ==================== 8. CREAR PAGO ====================
            Pago pago = Pago.builder()
                    .pedido(nuevoPedido)
                    .metodo(request.getMetodoPago())
                    .monto(total)
                    .estado(Pago.EstadoPago.APROBADO)
                    .referenciaPasarela(metodoPagoStr.equals("PAYPAL") ? "PAYPAL_" + System.currentTimeMillis() : "SIM_" + System.currentTimeMillis())
                    .fecha(LocalDateTime.now())
                    .build();

            Pago nuevoPago = pagoService.guardar(pago);
            System.out.println("Pago creado ID: " + nuevoPago.getId());

            // ==================== 9. CREAR ENVÍO ====================
            String codigoSeguimiento = generarCodigoSeguimiento();
            Envio envio = Envio.builder()
                    .pedido(nuevoPedido)
                    .metodoEnvio(request.getMetodoEnvio() != null ? request.getMetodoEnvio() : "RECOJO_EN_TIENDA")
                    .direccion(request.getDireccionEnvio())
                    .costoEnvio(costoEnvio)
                    .estado(EstadoEnvio.PENDIENTE)
                    .codigoSeguimiento(codigoSeguimiento)
                    .fechaEnvio(null)
                    .fechaEntrega(null)
                    .build();

            Envio nuevoEnvio = envioService.guardar(envio);
            nuevoPedido.setEnvio(nuevoEnvio);
            System.out.println("Envío creado ID: " + nuevoEnvio.getId() + ", Código: " + codigoSeguimiento);

            // ==================== 10. CREAR COMPROBANTE ====================
            Comprobante comprobante = comprobanteService.generarComprobante(nuevoPedido, "BOLETA");
            System.out.println("Comprobante creado ID: " + comprobante.getId() + ", Número: " + comprobante.getNumeroComprobante());

            // ==================== 11. CONSTRUIR RESPUESTA ====================
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("pedidoId", nuevoPedido.getId());
            response.put("pagoId", nuevoPago.getId());
            response.put("envioId", nuevoEnvio.getId());
            response.put("comprobanteId", comprobante.getId());
            response.put("codigoSeguimiento", codigoSeguimiento);
            response.put("numeroComprobante", comprobante.getNumeroComprobante());
            response.put("subtotal", subtotalSinIGV);
            response.put("igv", igv);
            response.put("costoEnvio", costoEnvio);
            response.put("total", total);
            response.put("estado", "PAGADO");
            response.put("fecha", LocalDateTime.now());
            response.put("message", "Pedido realizado exitosamente");

            System.out.println("=== PEDIDO COMPLETADO EXITOSAMENTE ===");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            System.err.println("Error al crear pedido: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Calcular costo de envío según método
     */
    private BigDecimal calcularCostoEnvio(String metodoEnvio) {
        if (metodoEnvio == null) return BigDecimal.ZERO;
        switch (metodoEnvio) {
            case "RECOJO_EN_TIENDA":
                return BigDecimal.ZERO;
            case "ENVIO_DOMICILIO":
                return new BigDecimal("15.00");
            case "ENVIO_EXPRESS":
                return new BigDecimal("25.00");
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Generar código de seguimiento único
     */
    private String generarCodigoSeguimiento() {
        return "JIM" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }

    /**
     * Actualizar estado de un pedido (para ADMIN y VENTAS)
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENTAS')")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Integer id,
            @RequestParam String estado,
            Authentication authentication) {

        try {
            System.out.println("=== ACTUALIZANDO ESTADO DEL PEDIDO ===");
            System.out.println("Pedido ID: " + id);
            System.out.println("Nuevo estado: " + estado);
            System.out.println("Usuario: " + authentication.getName());
            System.out.println("Roles: " + authentication.getAuthorities());

            // Validar que el estado sea válido
            EstadoPedido nuevoEstado;
            try {
                nuevoEstado = EstadoPedido.valueOf(estado.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido: " + estado));
            }

            // Buscar el pedido
            Pedido pedido = pedidoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

            // Actualizar estado
            pedido.setEstado(nuevoEstado);
            Pedido pedidoActualizado = pedidoService.actualizar(pedido);

            System.out.println("Estado actualizado a: " + pedidoActualizado.getEstado());

            // Retornar respuesta
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("id", pedidoActualizado.getId());
            response.put("estado", pedidoActualizado.getEstado());
            response.put("message", "Estado actualizado exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("Error al actualizar estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar el estado"));
        }
    }

    /**
     * Obtener todos los estados posibles de pedido
     */
    @GetMapping("/estados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Map<String, String>>> obtenerEstados() {
        List<Map<String, String>> estados = Arrays.stream(EstadoPedido.values())
                .map(estado -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("valor", estado.name());
                    map.put("nombre", estado.name());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(estados);
    }

    /**
     * Convertir Pedido a PedidoResponseDTO
     */
    private PedidoResponseDTO convertirADTO(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setClienteId(pedido.getCliente().getId());
        dto.setClienteNombre(pedido.getCliente().getNombre());
        dto.setTotal(pedido.getTotal());
        dto.setEstado(pedido.getEstado());
        dto.setFecha(pedido.getFecha());

        if (pedido.getCliente() != null) {
            dto.setClienteEmail(pedido.getCliente().getEmail());
            dto.setClienteTelefono(pedido.getCliente().getTelefono());
            dto.setClienteDocumento(pedido.getCliente().getDocumento());
            dto.setClienteDireccion(pedido.getCliente().getDireccion());
        }

        if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            List<DetallePedidoResponseDTO> detalles = pedido.getDetalles().stream()
                    .map(detalle -> {
                        DetallePedidoResponseDTO detalleDTO = new DetallePedidoResponseDTO();
                        detalleDTO.setId(detalle.getId());
                        detalleDTO.setProductoId(detalle.getProducto().getId());
                        detalleDTO.setProductoNombre(detalle.getProducto().getNombre());
                        detalleDTO.setCantidad(detalle.getCantidad());
                        detalleDTO.setPrecio(detalle.getPrecio());
                        detalleDTO.setSubtotal(detalle.getSubtotal());
                        return detalleDTO;
                    })
                    .collect(Collectors.toList());
            dto.setDetalles(detalles);
        }

        if (pedido.getPago() != null) {
            PagoResponseDTO pagoDTO = new PagoResponseDTO();
            pagoDTO.setId(pedido.getPago().getId());
            pagoDTO.setPedidoId(pedido.getPago().getPedido().getId());
            pagoDTO.setMetodo(pedido.getPago().getMetodo());
            pagoDTO.setMonto(pedido.getPago().getMonto());
            pagoDTO.setEstado(pedido.getPago().getEstado());
            pagoDTO.setReferenciaPasarela(pedido.getPago().getReferenciaPasarela());
            pagoDTO.setFecha(pedido.getPago().getFecha());
            dto.setPago(pagoDTO);
        }

        if (pedido.getEnvio() != null) {
            EnvioResponseDTO envioDTO = new EnvioResponseDTO();
            envioDTO.setId(pedido.getEnvio().getId());
            envioDTO.setPedidoId(pedido.getEnvio().getPedido().getId());
            envioDTO.setMetodoEnvio(pedido.getEnvio().getMetodoEnvio());
            envioDTO.setDireccion(pedido.getEnvio().getDireccion());
            envioDTO.setCostoEnvio(pedido.getEnvio().getCostoEnvio());
            envioDTO.setEstado(pedido.getEnvio().getEstado());
            envioDTO.setFechaEnvio(pedido.getEnvio().getFechaEnvio());
            envioDTO.setFechaEntrega(pedido.getEnvio().getFechaEntrega());
            envioDTO.setCodigoSeguimiento(pedido.getEnvio().getCodigoSeguimiento());
            dto.setEnvio(envioDTO);
        }

        return dto;
    }
}