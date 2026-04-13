package com.reydi.tienda.controller;

import com.reydi.tienda.dto.StockDTO;
import com.reydi.tienda.model.Stock;
import com.reydi.tienda.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Listar todos los stocks
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockDTO>> listar() {
        List<Stock> stocks = stockService.listarTodos();

        List<StockDTO> dtos = stocks.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Buscar stock por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> buscarPorId(@PathVariable Integer id) {
        return stockService.buscarPorId(id)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Buscar stock por ID de producto
     */
    /**
     * Buscar stock por ID de producto (PÚBLICO - para que los clientes vean stock disponible)
     */
    @GetMapping("/producto/{productoId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<StockDTO> buscarPorProducto(@PathVariable Integer productoId) {
        return stockService.buscarPorProductoId(productoId)
                .map(this::convertirADTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Listar stocks con cantidad baja
     */
    @GetMapping("/bajo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockDTO>> listarStockBajo(@RequestParam Integer cantidad) {
        List<Stock> stocks = stockService.listarStockBajo(cantidad);

        List<StockDTO> dtos = stocks.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Actualizar stock por compra (REDUCIR stock)
     * Este endpoint es accesible para clientes autenticados
     */
    @PatchMapping("/comprar/{productoId}")
    @PreAuthorize("isAuthenticated()")  //  Cualquier usuario autenticado puede comprar
    public ResponseEntity<?> reducirStockPorCompra(
            @PathVariable Integer productoId,
            @RequestParam Integer cantidad) {

        try {
            System.out.println("=== REDUCIENDO STOCK POR COMPRA ===");
            System.out.println("Producto ID: " + productoId);
            System.out.println("Cantidad a reducir: " + cantidad);

            // Buscar stock por producto
            Optional<Stock> stockOpt = stockService.buscarPorProductoId(productoId);

            if (stockOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontró stock para el producto"));
            }

            Stock stock = stockOpt.get();

            // Verificar que hay suficiente stock
            if (stock.getCantidad() < cantidad) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Stock insuficiente. Disponible: " + stock.getCantidad()));
            }

            // Reducir stock
            int nuevaCantidad = stock.getCantidad() - cantidad;
            stock.setCantidad(nuevaCantidad);

            Stock actualizado = stockService.actualizar(stock);

            System.out.println("Stock reducido exitosamente - Nueva cantidad: " + actualizado.getCantidad());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stock actualizado correctamente");
            response.put("productoId", productoId);
            response.put("cantidadAnterior", stock.getCantidad() + cantidad);
            response.put("cantidadNueva", actualizado.getCantidad());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar stock: " + e.getMessage()));
        }
    }

    /**
     * Actualizar stock completo (solo ADMIN)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> actualizar(@PathVariable Integer id, @RequestBody Stock stock) {
        try {
            System.out.println("=== ACTUALIZANDO STOCK ===");
            System.out.println("ID recibido: " + id);
            System.out.println("Producto ID: " + (stock.getProducto() != null ? stock.getProducto().getId() : "null"));
            System.out.println("Cantidad recibida: " + stock.getCantidad());

            // Validar que el ID coincida
            if (!id.equals(stock.getId())) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que el stock existe
            if (stockService.buscarPorId(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Stock actualizado = stockService.actualizar(stock);
            System.out.println("Stock actualizado exitosamente - Nueva cantidad: " + actualizado.getCantidad());

            return ResponseEntity.ok(convertirADTO(actualizado));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crear nuevo stock
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> crear(@RequestBody Stock stock) {
        try {
            // Verificar si ya existe stock para este producto
            if (stockService.buscarPorProductoId(stock.getProducto().getId()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null);
            }

            Stock nuevoStock = stockService.guardar(stock);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(nuevoStock));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar stock por producto (sin necesidad de conocer el ID del stock)
     */
    @PatchMapping("/producto/{productoId}/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> actualizarPorProducto(
            @PathVariable Integer productoId,
            @RequestParam Integer nuevaCantidad) {
        try {
            System.out.println("=== ACTUALIZANDO STOCK POR PRODUCTO ===");
            System.out.println("Producto ID: " + productoId);
            System.out.println("Nueva cantidad: " + nuevaCantidad);

            // Buscar stock por producto
            Stock stock = stockService.buscarPorProductoId(productoId)
                    .orElseThrow(() -> new RuntimeException("Stock no encontrado para producto ID: " + productoId));

            // Actualizar cantidad
            stock.setCantidad(nuevaCantidad);
            Stock actualizado = stockService.actualizar(stock);

            System.out.println("Stock actualizado exitosamente - Nueva cantidad: " + actualizado.getCantidad());

            return ResponseEntity.ok(convertirADTO(actualizado));
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Incrementar stock
     */
    @PatchMapping("/producto/{productoId}/incrementar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> incrementarStock(
            @PathVariable Integer productoId,
            @RequestParam Integer cantidad) {
        try {
            if (cantidad <= 0) {
                return ResponseEntity.badRequest().build();
            }
            Stock stock = stockService.incrementarStock(productoId, cantidad);
            return ResponseEntity.ok(convertirADTO(stock));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Decrementar stock (solo ADMIN)
     */
    @PatchMapping("/producto/{productoId}/decrementar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockDTO> decrementarStock(
            @PathVariable Integer productoId,
            @RequestParam Integer cantidad) {
        try {
            if (cantidad <= 0) {
                return ResponseEntity.badRequest().build();
            }
            Stock stock = stockService.decrementarStock(productoId, cantidad);
            return ResponseEntity.ok(convertirADTO(stock));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar stock
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        try {
            stockService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Convierte una entidad Stock a StockDTO sin recursión
     */
    private StockDTO convertirADTO(Stock stock) {
        StockDTO dto = new StockDTO();

        dto.setId(stock.getId());
        dto.setCantidad(stock.getCantidad());
        dto.setFechaActualizacion(stock.getFechaActualizacion());

        if (stock.getProducto() != null) {
            dto.setProductoId(stock.getProducto().getId());
            dto.setProductoNombre(stock.getProducto().getNombre());
        }

        return dto;
    }
}