package com.reydi.tienda.service.impl;

import com.reydi.tienda.dto.NotificacionDTO;
import com.reydi.tienda.event.NotificacionEvent;
import com.reydi.tienda.model.Notificacion;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.NotificacionRepository;
import com.reydi.tienda.repository.UsuarioRepository;
import com.reydi.tienda.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher; // ✅ publica evento, NO envía directo

    // =========================================================
    // ✅ MÉTODOS POR USUARIO
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerPorUsuario(Integer usuarioId) {
        return notificacionRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNoLeidasPorUsuario(Integer usuarioId) {
        return notificacionRepository.findNoLeidasByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidasPorUsuario(Integer usuarioId) {
        return notificacionRepository.countNoLeidasByUsuarioId(usuarioId);
    }

    @Override
    @Transactional
    public boolean marcarComoLeida(Integer notificacionId, Integer usuarioId) {
        int updated = notificacionRepository.marcarComoLeida(notificacionId, usuarioId);
        return updated > 0;
    }

    @Override
    @Transactional
    public int marcarTodasComoLeidas(Integer usuarioId) {
        return notificacionRepository.marcarTodasComoLeidas(usuarioId);
    }

    @Override
    @Transactional
    public boolean eliminarNotificacion(Integer notificacionId, Integer usuarioId) {
        notificacionRepository.eliminarPorIdYUsuario(notificacionId, usuarioId);
        return true;
    }

    @Override
    @Transactional
    public void eliminarTodas(Integer usuarioId) {
        notificacionRepository.eliminarTodasPorUsuario(usuarioId);
    }

    // =========================================================
    // ✅ MÉTODO PRINCIPAL — crea notificación y publica evento
    // =========================================================

    @Override
    @Transactional
    public NotificacionDTO crearNotificacion(Integer usuarioId, String tipo, String mensaje) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("📝 CREANDO NOTIFICACIÓN");
        System.out.println("👤 Usuario ID: " + usuarioId);
        System.out.println("📋 Tipo: " + tipo);
        System.out.println("💬 Mensaje: " + mensaje);

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .mensaje(mensaje)
                .leido(false)
                .fecha(LocalDateTime.now())
                .build();

        Notificacion saved = notificacionRepository.save(notificacion);
        NotificacionDTO dto = convertirADTO(saved);

        System.out.println("✅ Notificación guardada con ID: " + saved.getId());
        System.out.println("📡 Publicando evento (WebSocket se enviará tras commit)");

        // ✅ Publicar evento: el listener AFTER_COMMIT enviará por WebSocket
        eventPublisher.publishEvent(new NotificacionEvent(this, usuarioId, dto));

        System.out.println("═══════════════════════════════════════");
        return dto;
    }

    // =========================================================
    // ✅ MÉTODOS PARA ADMIN (sin usuarioId)
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerTodas() {
        return notificacionRepository.findAllOrderByFechaDesc()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificacionDTO> obtenerNoLeidas() {
        return notificacionRepository.findNoLeidas()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas() {
        return notificacionRepository.countNoLeidas();
    }

    @Override
    @Transactional
    public boolean marcarComoLeida(Integer id) {
        int updated = notificacionRepository.marcarComoLeida(id);
        return updated > 0;
    }

    @Override
    @Transactional
    public int marcarTodasComoLeidas() {
        return notificacionRepository.marcarTodasComoLeidas();
    }

    @Override
    @Transactional
    public boolean eliminarNotificacion(Integer id) {
        if (notificacionRepository.existsById(id)) {
            notificacionRepository.eliminarPorId(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void eliminarTodas() {
        notificacionRepository.eliminarTodas();
    }

    // =========================================================
    // ✅ Conversión a DTO
    // =========================================================

    @Override
    public NotificacionDTO convertirADTO(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId());
        dto.setUsuarioId(notificacion.getUsuario().getId());
        dto.setUsuarioNombre(notificacion.getUsuario().getNombre());
        dto.setTipo(notificacion.getTipo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setLeido(notificacion.getLeido());
        dto.setFecha(notificacion.getFecha());
        return dto;
    }
}