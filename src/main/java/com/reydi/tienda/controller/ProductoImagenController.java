package com.reydi.tienda.controller;

import com.reydi.tienda.dto.ProductoImagenDTO;
import com.reydi.tienda.model.Producto;
import com.reydi.tienda.model.ProductoImagen;
import com.reydi.tienda.service.ProductoImagenService;
import com.reydi.tienda.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/producto-imagenes")
@RequiredArgsConstructor
public class ProductoImagenController {

    private final ProductoImagenService productoImagenService;
    private final ProductoService productoService;

    private final String UPLOAD_DIR = "uploads/";

    // Extensiones permitidas
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Funciona!");
    }

    // SUBIR UNA SOLA IMAGEN
    @PostMapping("/upload")
    public ResponseEntity<?> subirImagen(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productoId") Integer productoId,
            @RequestParam(value = "principal", defaultValue = "false") Boolean principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            // VALIDACIÓN Archivo no vacío
            if (file == null || file.isEmpty()) {
                response.put("success", false);
                response.put("error", "El archivo está vacío");
                return ResponseEntity.badRequest().body(response);
            }

            // VALIDACIÓN Producto existe
            Producto producto = productoService.buscarPorId(productoId)
                    .orElse(null);

            if (producto == null) {
                response.put("success", false);
                response.put("error", "Producto no encontrado con ID: " + productoId);
                return ResponseEntity.badRequest().body(response);
            }

            // VALIDACIÓN Tipo de archivo válido
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                response.put("success", false);
                response.put("error", "Formato no permitido. Solo: " + String.join(", ", ALLOWED_EXTENSIONS));
                return ResponseEntity.badRequest().body(response);
            }

            // VALIDACIÓN Tamaño del archivo
            if (file.getSize() > MAX_FILE_SIZE) {
                response.put("success", false);
                response.put("error", "La imagen no puede superar los 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // VALIDACIÓN Verificar si ya hay imágenes principales
            if (principal) {
                // Si ya hay una imagen principal, desmarcarla
                productoImagenService.buscarImagenPrincipal(productoId).ifPresent(img -> {
                    img.setPrincipal(false);
                    productoImagenService.guardar(img);
                });
            } else if (productoImagenService.buscarPorProducto(productoId).isEmpty()) {
                // Si es la primera imagen y no se marcó como principal, forzar como principal
                principal = true;
            }

            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String urlImagen = "/uploads/" + fileName;

            // Guardar en base de datos SOLO si todo salió bien
            ProductoImagen imagen = new ProductoImagen();
            imagen.setUrlImagen(urlImagen);
            imagen.setPrincipal(principal);
            imagen.setProducto(producto);

            ProductoImagen saved = productoImagenService.guardar(imagen);

            // Verificar que se guardó correctamente
            if (saved == null || saved.getId() == null) {
                // Si falló el guardado, eliminar el archivo físico
                Files.deleteIfExists(filePath);
                response.put("success", false);
                response.put("error", "Error al guardar en base de datos");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            response.put("success", true);
            response.put("message", "Imagen subida exitosamente");
            response.put("url", urlImagen);
            response.put("id", saved.getId());
            response.put("principal", principal);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Error al subir imagen: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error inesperado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // SUBIR MÚLTIPLES IMÁGENES - CORREGIDO
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> subirMultiplesImagenes(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productoId") Integer productoId) {

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> imagenesSubidas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        System.out.println("=== SUBIENDO MÚLTIPLES IMÁGENES ===");
        System.out.println("Producto ID: " + productoId);
        System.out.println("Número de archivos recibidos: " + (files != null ? files.size() : 0));

        try {
            // VALIDACIÓN: Producto existe
            Producto producto = productoService.buscarPorId(productoId)
                    .orElse(null);

            if (producto == null) {
                response.put("success", false);
                response.put("error", "Producto no encontrado con ID: " + productoId);
                return ResponseEntity.badRequest().body(response);
            }

            if (files == null || files.isEmpty()) {
                response.put("success", false);
                response.put("error", "No se enviaron archivos");
                return ResponseEntity.badRequest().body(response);
            }

            // Contar cuántas imágenes ya tiene el producto
            long imagenesExistentes = productoImagenService.buscarPorProducto(productoId).size();
            boolean primeraImagen = (imagenesExistentes == 0);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                Map<String, Object> imgResult = new HashMap<>();

                try {
                    // Validar archivo
                    if (file == null || file.isEmpty()) {
                        errores.add("Archivo " + (i+1) + " está vacío");
                        continue;
                    }

                    // Validar extensión
                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                    }

                    if (!ALLOWED_EXTENSIONS.contains(extension)) {
                        errores.add("Archivo " + (i+1) + " (" + originalFilename + ") formato no permitido");
                        continue;
                    }

                    // Validar tamaño
                    if (file.getSize() > MAX_FILE_SIZE) {
                        errores.add("Archivo " + (i+1) + " excede el tamaño máximo de 5MB");
                        continue;
                    }

                    // La primera imagen del producto o la primera del lote es principal
                    boolean isPrincipal = primeraImagen && (i == 0);

                    // Crear directorio
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // Generar nombre único
                    String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "_" + i + "." + extension;
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath);

                    String urlImagen = "/uploads/" + fileName;

                    // Guardar en BD
                    ProductoImagen imagen = new ProductoImagen();
                    imagen.setUrlImagen(urlImagen);
                    imagen.setPrincipal(isPrincipal);
                    imagen.setProducto(producto);

                    ProductoImagen saved = productoImagenService.guardar(imagen);

                    if (saved != null && saved.getId() != null) {
                        imgResult.put("id", saved.getId());
                        imgResult.put("url", urlImagen);
                        imgResult.put("principal", isPrincipal);
                        imagenesSubidas.add(imgResult);
                        System.out.println("Imagen " + (i+1) + " subida: " + urlImagen);
                    } else {
                        Files.deleteIfExists(filePath);
                        errores.add("Archivo " + (i+1) + " no se pudo guardar en BD");
                    }

                } catch (IOException e) {
                    errores.add("Archivo " + (i+1) + " error: " + e.getMessage());
                }
            }

            response.put("success", imagenesSubidas.size() > 0);
            response.put("message", imagenesSubidas.size() + " imagen(es) subida(s) exitosamente");
            if (!errores.isEmpty()) {
                response.put("errores", errores);
            }
            response.put("imagenes", imagenesSubidas);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error al subir imágenes: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // OBTENER TODAS LAS IMÁGENES DE UN PRODUCTO - FILTRANDO INVÁLIDAS
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ProductoImagenDTO>> listarPorProducto(@PathVariable Integer productoId) {
        List<ProductoImagen> imagenes = productoImagenService.buscarPorProducto(productoId);

        // FILTRAR imágenes inválidas (con URL nula o vacía)
        List<ProductoImagenDTO> imagenesDTO = imagenes.stream()
                .filter(imagen -> imagen.getUrlImagen() != null
                        && !imagen.getUrlImagen().trim().isEmpty()
                        && !imagen.getUrlImagen().contains("null"))
                .map(imagen -> {
                    ProductoImagenDTO dto = new ProductoImagenDTO();
                    dto.setId(imagen.getId());
                    dto.setUrlImagen(imagen.getUrlImagen());
                    dto.setPrincipal(imagen.getPrincipal());
                    dto.setFecha(imagen.getFecha());
                    dto.setProductoId(imagen.getProducto().getId());
                    dto.setProductoNombre(imagen.getProducto().getNombre());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(imagenesDTO);
    }

    // OBTENER IMAGEN PRINCIPAL DE UN PRODUCTO
    @GetMapping("/producto/{productoId}/principal")
    public ResponseEntity<ProductoImagenDTO> obtenerImagenPrincipal(@PathVariable Integer productoId) {
        return productoImagenService.buscarImagenPrincipal(productoId)
                .map(imagen -> {
                    ProductoImagenDTO dto = new ProductoImagenDTO();
                    dto.setId(imagen.getId());
                    dto.setUrlImagen(imagen.getUrlImagen());
                    dto.setPrincipal(imagen.getPrincipal());
                    dto.setFecha(imagen.getFecha());
                    dto.setProductoId(imagen.getProducto().getId());
                    dto.setProductoNombre(imagen.getProducto().getNombre());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //  MARCAR UNA IMAGEN COMO PRINCIPAL
    @PatchMapping("/{id}/principal")
    public ResponseEntity<?> marcarComoPrincipal(@PathVariable Integer id, @RequestParam Integer productoId) {
        try {
            productoImagenService.marcarComoPrincipal(id, productoId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Imagen marcada como principal"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ELIMINAR UNA IMAGEN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            // Buscar la imagen antes de eliminar
            Optional<ProductoImagen> imagenOpt = productoImagenService.buscarPorId(id);
            if (imagenOpt.isPresent()) {
                ProductoImagen imagen = imagenOpt.get();
                // Eliminar archivo físico
                if (imagen.getUrlImagen() != null) {
                    String fileName = imagen.getUrlImagen().replace("/uploads/", "");
                    Path filePath = Paths.get(UPLOAD_DIR + fileName);
                    Files.deleteIfExists(filePath);
                }
            }
            productoImagenService.eliminar(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Imagen eliminada correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Imagen eliminada de BD (archivo no encontrado)"));
        }
    }

    // ELIMINAR TODAS LAS IMÁGENES DE UN PRODUCTO
    @DeleteMapping("/producto/{productoId}")
    public ResponseEntity<?> eliminarPorProducto(@PathVariable Integer productoId) {
        List<ProductoImagen> imagenes = productoImagenService.buscarPorProducto(productoId);
        for (ProductoImagen img : imagenes) {
            try {
                if (img.getUrlImagen() != null) {
                    String fileName = img.getUrlImagen().replace("/uploads/", "");
                    Path filePath = Paths.get(UPLOAD_DIR + fileName);
                    Files.deleteIfExists(filePath);
                }
            } catch (IOException e) {
                // Ignorar error de archivo
            }
        }
        productoImagenService.eliminarPorProducto(productoId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Imágenes eliminadas correctamente"));
    }

    // LIMPIAR IMÁGENES INVÁLIDAS (endpoint para admin)
    @DeleteMapping("/limpiar-invalidas")
    public ResponseEntity<?> limpiarImagenesInvalidas() {
        try {
            List<ProductoImagen> todas = productoImagenService.buscarTodas();
            int eliminadas = 0;

            for (ProductoImagen img : todas) {
                boolean esInvalida = img.getUrlImagen() == null
                        || img.getUrlImagen().trim().isEmpty()
                        || img.getUrlImagen().contains("null")
                        || (!img.getUrlImagen().endsWith(".jpg")
                        && !img.getUrlImagen().endsWith(".jpeg")
                        && !img.getUrlImagen().endsWith(".png")
                        && !img.getUrlImagen().endsWith(".webp"));

                if (esInvalida) {
                    // Eliminar archivo físico si existe
                    try {
                        String fileName = img.getUrlImagen().replace("/uploads/", "");
                        Path filePath = Paths.get(UPLOAD_DIR + fileName);
                        Files.deleteIfExists(filePath);
                    } catch (Exception e) {

                    }
                    productoImagenService.eliminar(img.getId());
                    eliminadas++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Se eliminaron " + eliminadas + " imágenes inválidas"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<?> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "El archivo excede el tamaño máximo permitido. Límite: 5MB"));
        }
    }
}