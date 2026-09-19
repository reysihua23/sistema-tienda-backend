package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ProductoDTO;
import com.reydi.tienda.dto.ProductoRequest;
import com.reydi.tienda.model.Producto;
import com.reydi.tienda.model.Stock;
import com.reydi.tienda.repository.ProductoRepository;
import com.reydi.tienda.service.ProductoService;
import com.reydi.tienda.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final StockService stockService;
    private final ProductoRepository productoRepository;

    // ==================== LISTAR TODOS LOS PRODUCTOS ====================
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Producto> productos = productoService.listarTodos();
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al acceder a la base de datos", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado", "message", e.getMessage()));
        }
    }

    // ==================== LISTAR PRODUCTOS ACTIVOS ====================
    @GetMapping("/activos")
    public ResponseEntity<?> listarActivos() {
        try {
            List<Producto> productos = productoService.listarActivos();
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar productos activos", "message", e.getMessage()));
        }
    }

    // =========================================================
    // ✅ LISTAR PRODUCTOS EN OFERTA (NUEVO ENDPOINT)
    // =========================================================

    /**
     * Lista productos que actualmente están en oferta.
     * Filtra productos con descuento activo y dentro del rango de fechas.
     *
     * @return Lista de productos en oferta
     */
    @GetMapping("/en-oferta")
    public ResponseEntity<?> listarEnOferta() {
        try {
            List<Producto> productos = productoService.listarEnOferta();
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al cargar productos en oferta", "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable Integer id) {
        return productoService.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== BUSCAR POR NOMBRE ====================
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        try {
            List<Producto> productos = productoService.buscarPorNombre(nombre);
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error en la búsqueda", "message", e.getMessage()));
        }
    }

    // ==================== LISTAR STOCK BAJO ====================
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarStockBajo(@RequestParam Integer stockMinimo) {
        try {
            List<Producto> productos = productoService.listarStockBajo(stockMinimo);
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar stock bajo", "message", e.getMessage()));
        }
    }

    // ==================== FILTRAR AVANZADO ====================
    @GetMapping("/filtrar")
    public ResponseEntity<?> filtrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer stockMinimo,
            @RequestParam(required = false) Double precioMinimo) {
        try {
            List<Producto> productos = productoService.filtrarAvanzado(nombre, activo, stockMinimo, precioMinimo);
            List<ProductoDTO> dtos = productos.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al filtrar productos", "message", e.getMessage()));
        }
    }

    // ==================== CREAR PRODUCTO (CON MANEJO DE ERRORES MEJORADO) ====================
    @PostMapping("/crear")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> crear(@RequestBody ProductoRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ========== VALIDACIONES DE CAMPO ==========
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "El nombre del producto es obligatorio");
                response.put("field", "nombre");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getPrecio() == null || request.getPrecio().doubleValue() <= 0) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "El precio debe ser mayor a 0");
                response.put("field", "precio");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getStock() != null && request.getStock() < 0) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "El stock no puede ser negativo");
                response.put("field", "stock");
                return ResponseEntity.badRequest().body(response);
            }

            // ========== VALIDAR PRODUCTO DUPLICADO ==========
            boolean existeDuplicado = productoRepository.existeProductoDuplicado(
                    request.getNombre(),
                    request.getDescripcion()
            );

            if (existeDuplicado) {
                response.put("success", false);
                response.put("error", "DUPLICATE_PRODUCT");
                response.put("message", "Ya existe un producto con el nombre '" + request.getNombre() + "'");
                response.put("suggestion", "Verifica el catálogo o utiliza un nombre diferente");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // ========== CREAR PRODUCTO ==========
            Producto producto = new Producto();
            producto.setNombre(request.getNombre().trim());
            producto.setDescripcion(request.getDescripcion());
            producto.setPrecio(request.getPrecio());
            producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5);
            producto.setActivo(request.getActivo() != null ? request.getActivo() : true);

            producto.setCategoria(request.getCategoria() != null && !request.getCategoria().isEmpty()
                    ? request.getCategoria()
                    : "otros");

            // ✅ CAMPOS DE DESCUENTO
            producto.setPorcentajeDescuento(request.getPorcentajeDescuento() != null ? request.getPorcentajeDescuento() : 0);
            producto.setDescuentoActivo(request.getDescuentoActivo() != null ? request.getDescuentoActivo() : false);
            producto.setFechaInicioDescuento(request.getFechaInicioDescuento());
            producto.setFechaFinDescuento(request.getFechaFinDescuento());
            Producto nuevo = productoService.guardar(producto);

            if (nuevo == null || nuevo.getId() == null) {
                throw new RuntimeException("No se pudo guardar el producto en la base de datos");
            }

            // ========== CREAR/ACTUALIZAR STOCK ==========
            Stock stockExistente = stockService.buscarPorProductoId(nuevo.getId()).orElse(null);
            Stock stockGuardado;

            if (stockExistente != null) {
                stockExistente.setCantidad(request.getStock() != null && request.getStock() >= 0 ? request.getStock() : 0);
                stockGuardado = stockService.guardar(stockExistente);
            } else {
                Stock stock = new Stock();
                stock.setProducto(nuevo);
                stock.setCantidad(request.getStock() != null && request.getStock() >= 0 ? request.getStock() : 0);
                stockGuardado = stockService.guardar(stock);
            }

            // ========== RESPUESTA EXITOSA ==========
            ProductoDTO dto = convertirADTO(nuevo);
            dto.setStock(stockGuardado != null ? stockGuardado.getCantidad() : 0);

            response.put("success", true);
            response.put("message", "Producto creado exitosamente");
            response.put("producto", dto);
            response.put("stockInicial", stockGuardado != null ? stockGuardado.getCantidad() : 0);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            // Error de integridad de datos (clave duplicada, restricciones)
            response.put("success", false);
            response.put("error", "DATABASE_INTEGRITY_ERROR");
            response.put("message", "Error de integridad de datos");

            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("stock.producto_id")) {
                response.put("detail", "Ya existe un registro de stock para este producto");
                response.put("suggestion", "Intenta actualizar el producto existente en lugar de crear uno nuevo");
            } else if (e.getMessage().contains("Duplicate entry")) {
                response.put("detail", "Ya existe un registro con estos datos");
            } else {
                response.put("detail", e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (DataAccessException e) {
            response.put("success", false);
            response.put("error", "DATABASE_ERROR");
            response.put("message", "Error al acceder a la base de datos");
            response.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Ocurrió un error inesperado");
            response.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== ACTUALIZAR PRODUCTO ====================
    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody ProductoRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ========== VALIDACIONES ==========
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "El nombre del producto es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getPrecio() == null || request.getPrecio().doubleValue() <= 0) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "El precio debe ser mayor a 0");
                return ResponseEntity.badRequest().body(response);
            }

            // ========== BUSCAR PRODUCTO ==========
            Producto productoExistente = productoService.buscarPorId(id)
                    .orElse(null);

            if (productoExistente == null) {
                response.put("success", false);
                response.put("error", "NOT_FOUND");
                response.put("message", "Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // ========== ACTUALIZAR DATOS ==========
            productoExistente.setNombre(request.getNombre().trim());
            productoExistente.setDescripcion(request.getDescripcion());
            productoExistente.setPrecio(request.getPrecio());
            productoExistente.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5);
            productoExistente.setActivo(request.getActivo() != null ? request.getActivo() : true);

            productoExistente.setCategoria(request.getCategoria() != null && !request.getCategoria().isEmpty()
                    ? request.getCategoria()
                    : "otros");
            // AGREGAR CAMPOS DE DESCUENTO
            productoExistente.setPorcentajeDescuento(request.getPorcentajeDescuento() != null ? request.getPorcentajeDescuento() : 0);
            productoExistente.setDescuentoActivo(request.getDescuentoActivo() != null ? request.getDescuentoActivo() : false);
            productoExistente.setFechaInicioDescuento(request.getFechaInicioDescuento());
            productoExistente.setFechaFinDescuento(request.getFechaFinDescuento());

            // Calcular precio con descuento automáticamente
            if (productoExistente.getDescuentoActivo() && productoExistente.getPorcentajeDescuento() != null && productoExistente.getPorcentajeDescuento() > 0) {
                BigDecimal descuento = productoExistente.getPrecio()
                        .multiply(BigDecimal.valueOf(productoExistente.getPorcentajeDescuento()))
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                productoExistente.setPrecioDescuento(productoExistente.getPrecio().subtract(descuento));
            } else {
                productoExistente.setPrecioDescuento(BigDecimal.ZERO);
            }

            Producto actualizado = productoService.actualizar(productoExistente);

            // ========== ACTUALIZAR STOCK ==========
            if (request.getStock() != null) {
                Stock stock = stockService.buscarPorProductoId(id)
                        .orElse(null);
                if (stock == null) {
                    response.put("warning", "No se encontró stock para este producto, se creará uno nuevo");
                    Stock nuevoStock = new Stock();
                    nuevoStock.setProducto(productoExistente);
                    nuevoStock.setCantidad(request.getStock());
                    stockService.guardar(nuevoStock);
                } else {
                    stock.setCantidad(request.getStock());
                    stockService.guardar(stock);
                }
            }

            // ========== RESPUESTA EXITOSA ==========
            ProductoDTO dto = convertirADTO(actualizado);
            if (request.getStock() != null) {
                dto.setStock(request.getStock());
            }

            response.put("success", true);
            response.put("message", "Producto actualizado exitosamente");
            response.put("producto", dto);
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            response.put("success", false);
            response.put("error", "DATABASE_INTEGRITY_ERROR");
            response.put("message", "Error de integridad de datos al actualizar");
            response.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Error al actualizar el producto");
            response.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== CAMBIAR ESTADO (ACTIVO/INACTIVO) ====================
    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestParam Boolean activo) {
        Map<String, Object> response = new HashMap<>();

        try {
            Producto producto = productoService.buscarPorId(id)
                    .orElse(null);

            if (producto == null) {
                response.put("success", false);
                response.put("error", "NOT_FOUND");
                response.put("message", "Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            producto.setActivo(activo);
            Producto actualizado = productoService.actualizar(producto);

            response.put("success", true);
            response.put("message", activo ? "Producto activado exitosamente" : "Producto desactivado exitosamente");
            response.put("producto", convertirADTO(actualizado));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Error al cambiar el estado del producto");
            response.put("detail", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== VALIDAR STOCK ====================
    @PostMapping("/validar-stock")
    public ResponseEntity<?> validarStock(@RequestBody Map<String, List<Map<String, Object>>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Map<String, Object>> productos = request.get("productos");

            if (productos == null || productos.isEmpty()) {
                response.put("success", false);
                response.put("error", "VALIDATION_ERROR");
                response.put("message", "La lista de productos no puede estar vacía");
                return ResponseEntity.badRequest().body(response);
            }

            List<Map<String, Object>> productosSinStock = new java.util.ArrayList<>();
            boolean stockSuficiente = true;

            for (Map<String, Object> item : productos) {
                Integer id = Integer.valueOf(item.get("id").toString());
                Integer cantidadSolicitada = Integer.valueOf(item.get("cantidad").toString());

                Producto producto = productoService.buscarPorId(id).orElse(null);

                if (producto == null) {
                    Map<String, Object> productoInfo = new HashMap<>();
                    productoInfo.put("id", id);
                    productoInfo.put("nombre", "Producto no encontrado");
                    productoInfo.put("cantidadSolicitada", cantidadSolicitada);
                    productoInfo.put("stockDisponible", 0);
                    productosSinStock.add(productoInfo);
                    stockSuficiente = false;
                } else {
                    Integer stockActual = producto.getStock() != null ? producto.getStock().getCantidad() : 0;

                    if (stockActual < cantidadSolicitada) {
                        Map<String, Object> productoInfo = new HashMap<>();
                        productoInfo.put("id", id);
                        productoInfo.put("nombre", producto.getNombre());
                        productoInfo.put("cantidadSolicitada", cantidadSolicitada);
                        productoInfo.put("stockDisponible", stockActual);
                        productosSinStock.add(productoInfo);
                        stockSuficiente = false;
                    }
                }
            }

            response.put("success", stockSuficiente);
            response.put("message", stockSuficiente ? "Stock disponible para todos los productos" : "Stock insuficiente para algunos productos");
            response.put("productosSinStock", productosSinStock);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "Error al validar stock: " + e.getMessage());
            response.put("productosSinStock", new java.util.ArrayList<>());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ==================== ACTUALIZAR STOCK DESPUÉS DE COMPRA ====================
    @PostMapping("/actualizar-stock")
    public ResponseEntity<?> actualizarStock(@RequestBody Map<String, List<Map<String, Object>>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Map<String, Object>> productos = request.get("productos");
            List<Map<String, Object>> errores = new java.util.ArrayList<>();
            boolean todoOk = true;

            for (Map<String, Object> item : productos) {
                Integer id = Integer.valueOf(item.get("id").toString());
                Integer cantidadRestar = Integer.valueOf(item.get("cantidad").toString());

                Producto producto = productoService.buscarPorId(id).orElse(null);

                if (producto == null) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("id", id);
                    error.put("error", "Producto no encontrado");
                    errores.add(error);
                    todoOk = false;
                } else {
                    Stock stock = producto.getStock();
                    if (stock == null) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("id", id);
                        error.put("nombre", producto.getNombre());
                        error.put("error", "Stock no configurado para este producto");
                        errores.add(error);
                        todoOk = false;
                    } else {
                        Integer nuevoStock = stock.getCantidad() - cantidadRestar;
                        if (nuevoStock < 0) {
                            Map<String, Object> error = new HashMap<>();
                            error.put("id", id);
                            error.put("nombre", producto.getNombre());
                            error.put("error", "Stock insuficiente");
                            error.put("stockActual", stock.getCantidad());
                            error.put("cantidadSolicitada", cantidadRestar);
                            errores.add(error);
                            todoOk = false;
                        } else {
                            stock.setCantidad(nuevoStock);
                            stockService.guardar(stock);
                        }
                    }
                }
            }

            response.put("success", todoOk);
            response.put("message", todoOk ? "Stock actualizado correctamente" : "Error al actualizar stock");
            response.put("errores", errores);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Error al actualizar stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== ELIMINAR PRODUCTO ====================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Producto producto = productoService.buscarPorId(id).orElse(null);

            if (producto == null) {
                response.put("success", false);
                response.put("error", "NOT_FOUND");
                response.put("message", "Producto no encontrado con ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            productoService.eliminar(id);

            response.put("success", true);
            response.put("message", "Producto eliminado correctamente");
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            response.put("success", false);
            response.put("error", "INTEGRITY_ERROR");
            response.put("message", "No se puede eliminar el producto porque tiene registros asociados (pedidos, stock, etc.)");
            response.put("suggestion", "Desactiva el producto en lugar de eliminarlo");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Error al eliminar el producto: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== CONVERTIR A DTO ====================
    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setActivo(producto.getActivo());
        dto.setCategoria(producto.getCategoria());
        dto.setCreatedAt(producto.getCreatedAt());
        dto.setUpdatedAt(producto.getUpdatedAt());

        dto.setPorcentajeDescuento(producto.getPorcentajeDescuento());
        dto.setPrecioDescuento(producto.getPrecioDescuento());
        dto.setDescuentoActivo(producto.getDescuentoActivo());
        dto.setFechaInicioDescuento(producto.getFechaInicioDescuento());
        dto.setFechaFinDescuento(producto.getFechaFinDescuento());
        dto.setPrecioActual(producto.getPrecioActual());

        if (producto.getStock() != null) {
            dto.setStock(producto.getStock().getCantidad());
        } else {
            dto.setStock(0);
        }

        return dto;
    }
}