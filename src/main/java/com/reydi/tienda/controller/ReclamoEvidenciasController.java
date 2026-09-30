package com.reydi.tienda.controller;

import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.ReclamoEvidencias;
import com.reydi.tienda.service.ReclamoEvidenciasService;
import com.reydi.tienda.service.ReclamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReclamoEvidenciasController {

    private final ReclamoEvidenciasService evidenciaService;
    private final ReclamoService reclamoService;

    private final String UPLOAD_DIR = "uploads/reclamos/";

    // Extensiones permitidas (imágenes)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // =========================================================
    // ✅ SUBIR EVIDENCIA (1 archivo)
    // POST /api/reclamos/{reclamoId}/evidencias
    // =========================================================
    @PostMapping("/reclamos/{reclamoId}/evidencias")
    public ResponseEntity<?> subirEvidencia(
            @PathVariable Integer reclamoId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ Validar archivo
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El archivo está vacío"));
            }

            // ✅ Validar que el reclamo exista
            Reclamo reclamo = reclamoService.buscarPorId(reclamoId)
                    .orElse(null);
            if (reclamo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Reclamo no encontrado con ID: " + reclamoId));
            }

            // ✅ Validar extensión
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Formato no permitido. Solo: " + String.join(", ", ALLOWED_EXTENSIONS)));
            }

            // ✅ Validar tamaño
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La imagen no puede superar los 5MB"));
            }

            // ✅ Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ✅ Generar nombre único
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String urlImagen = "/" + UPLOAD_DIR + fileName;

            // ✅ Guardar en BD
            ReclamoEvidencias evidencia = new ReclamoEvidencias();
            evidencia.setUrlImagen(urlImagen);
            evidencia.setReclamo(reclamo);

            ReclamoEvidencias saved = evidenciaService.guardar(evidencia);

            if (saved == null || saved.getId() == null) {
                Files.deleteIfExists(filePath);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Error al guardar en base de datos"));
            }

            response.put("success", true);
            response.put("message", "Evidencia subida exitosamente");
            response.put("id", saved.getId());
            response.put("url", urlImagen);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al subir evidencia: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error inesperado: " + e.getMessage()));
        }
    }

    // =========================================================
    // ✅ SUBIR MÚLTIPLES EVIDENCIAS
    // POST /api/reclamos/{reclamoId}/evidencias-multiple
    // =========================================================
    @PostMapping("/reclamos/{reclamoId}/evidencias-multiple")
    public ResponseEntity<?> subirMultiplesEvidencias(
            @PathVariable Integer reclamoId,
            @RequestParam("files") List<MultipartFile> files) {

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> subidas = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try {
            // ✅ Validar reclamo
            Reclamo reclamo = reclamoService.buscarPorId(reclamoId)
                    .orElse(null);
            if (reclamo == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Reclamo no encontrado con ID: " + reclamoId));
            }

            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se enviaron archivos"));
            }

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                try {
                    if (file == null || file.isEmpty()) {
                        errores.add("Archivo " + (i + 1) + " está vacío");
                        continue;
                    }

                    String originalFilename = file.getOriginalFilename();
                    String extension = "";
                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                    }

                    if (!ALLOWED_EXTENSIONS.contains(extension)) {
                        errores.add("Archivo " + (i + 1) + " formato no permitido");
                        continue;
                    }

                    if (file.getSize() > MAX_FILE_SIZE) {
                        errores.add("Archivo " + (i + 1) + " excede el tamaño máximo de 5MB");
                        continue;
                    }

                    String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "_" + i + "." + extension;
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath);

                    String urlImagen = "/" + UPLOAD_DIR + fileName;

                    ReclamoEvidencias evidencia = new ReclamoEvidencias();
                    evidencia.setUrlImagen(urlImagen);
                    evidencia.setReclamo(reclamo);

                    ReclamoEvidencias saved = evidenciaService.guardar(evidencia);

                    if (saved != null && saved.getId() != null) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", saved.getId());
                        item.put("url", urlImagen);
                        subidas.add(item);
                    } else {
                        Files.deleteIfExists(filePath);
                        errores.add("Archivo " + (i + 1) + " no se pudo guardar en BD");
                    }

                } catch (IOException e) {
                    errores.add("Archivo " + (i + 1) + " error: " + e.getMessage());
                }
            }

            response.put("success", !subidas.isEmpty());
            response.put("message", subidas.size() + " evidencia(s) subida(s) exitosamente");
            response.put("evidencias", subidas);
            if (!errores.isEmpty()) {
                response.put("errores", errores);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir evidencias: " + e.getMessage()));
        }
    }

    // =========================================================
    // ✅ LISTAR EVIDENCIAS DE UN RECLAMO
    // GET /api/reclamos/{reclamoId}/evidencias
    // =========================================================
    @GetMapping("/reclamos/{reclamoId}/evidencias")
    public ResponseEntity<?> listarEvidencias(@PathVariable Integer reclamoId) {
        try {
            List<ReclamoEvidencias> evidencias = evidenciaService.listarPorReclamo(reclamoId);

            // Convertir a DTO simple
            List<Map<String, Object>> lista = evidencias.stream()
                    .map(ev -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", ev.getId());
                        item.put("urlImagen", ev.getUrlImagen());
                        item.put("fecha", ev.getFecha());
                        item.put("reclamoId", ev.getReclamo() != null ? ev.getReclamo().getId() : null);
                        return item;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(lista);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al listar evidencias: " + e.getMessage()));
        }
    }

    // =========================================================
    // ✅ OBTENER UNA EVIDENCIA
    // GET /api/reclamo-evidencias/{id}
    // =========================================================
    @GetMapping("/reclamo-evidencias/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        return evidenciaService.buscarPorId(id)
                .map(ev -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", ev.getId());
                    item.put("urlImagen", ev.getUrlImagen());
                    item.put("fecha", ev.getFecha());
                    return ResponseEntity.ok(item);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // ✅ ELIMINAR EVIDENCIA
    // DELETE /api/reclamo-evidencias/{id}
    // =========================================================
    @DeleteMapping("/reclamo-evidencias/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            Optional<ReclamoEvidencias> evidenciaOpt = evidenciaService.buscarPorId(id);

            if (evidenciaOpt.isPresent()) {
                ReclamoEvidencias evidencia = evidenciaOpt.get();

                // ✅ Eliminar archivo físico
                if (evidencia.getUrlImagen() != null) {
                    String fileName = evidencia.getUrlImagen().replace("/" + UPLOAD_DIR, "");
                    Path filePath = Paths.get(UPLOAD_DIR + fileName);
                    Files.deleteIfExists(filePath);
                }
            }

            evidenciaService.eliminar(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evidencia eliminada correctamente"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evidencia eliminada de BD (archivo no encontrado)"
            ));
        }
    }

    // =========================================================
    // ✅ ELIMINAR TODAS LAS EVIDENCIAS DE UN RECLAMO
    // DELETE /api/reclamos/{reclamoId}/evidencias
    // =========================================================
    @DeleteMapping("/reclamos/{reclamoId}/evidencias")
    public ResponseEntity<?> eliminarPorReclamo(@PathVariable Integer reclamoId) {
        try {
            List<ReclamoEvidencias> evidencias = evidenciaService.listarPorReclamo(reclamoId);

            // Eliminar archivos físicos
            for (ReclamoEvidencias ev : evidencias) {
                try {
                    if (ev.getUrlImagen() != null) {
                        String fileName = ev.getUrlImagen().replace("/" + UPLOAD_DIR, "");
                        Path filePath = Paths.get(UPLOAD_DIR + fileName);
                        Files.deleteIfExists(filePath);
                    }
                } catch (IOException e) {
                    // Ignorar error de archivo
                }
            }

            evidenciaService.eliminarPorReclamo(reclamoId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Evidencias eliminadas correctamente"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}