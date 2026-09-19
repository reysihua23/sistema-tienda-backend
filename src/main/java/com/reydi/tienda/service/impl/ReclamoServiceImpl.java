package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import com.reydi.tienda.repository.ReclamoRepository;
import com.reydi.tienda.service.NotificacionService;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.ReclamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final NotificacionService notificacionService;
    private final NotificationRecipientResolver recipientResolver;

    @Override
    public List<Reclamo> listarTodos() {
        return reclamoRepository.findAll();
    }

    @Override
    public Optional<Reclamo> buscarPorId(Integer id) {
        return reclamoRepository.findById(id);
    }

    @Override
    public List<Reclamo> buscarPorCliente(Integer clienteId) {
        return reclamoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Reclamo> buscarPorEstado(EstadoReclamo estado) {
        return reclamoRepository.findByEstado(estado);
    }

    @Override
    public List<Reclamo> buscarPorTipo(TipoReclamo tipo) {
        return reclamoRepository.findByTipo(tipo);
    }

    // =========================================================
    // ✅ GUARDAR
    // =========================================================
    @Override
    @Transactional
    public Reclamo guardar(Reclamo reclamo) {
        if (reclamo.getEstado() == null) {
            reclamo.setEstado(EstadoReclamo.REGISTRADO);
        }
        Reclamo saved = reclamoRepository.save(reclamo);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "RECLAMO",
                "📢 Nuevo reclamo #" + saved.getId() +
                        (saved.getTipo() != null ? " (" + saved.getTipo() + ")" : "")
        );

        // ✅ CLIENTE
        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "RECLAMO",
                        "📢 Tu reclamo #" + saved.getId() + " fue registrado"
                );
            }
        }

        return saved;
    }

    // =========================================================
    // ✅ ACTUALIZAR
    // =========================================================
    @Override
    @Transactional
    public Reclamo actualizar(Reclamo reclamo) {
        if (!reclamoRepository.existsById(reclamo.getId())) {
            throw new RuntimeException("Reclamo no encontrado");
        }
        Reclamo saved = reclamoRepository.save(reclamo);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "RECLAMO",
                "✏️ Reclamo #" + saved.getId() + " actualizado"
        );

        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "RECLAMO",
                        "✏️ Tu reclamo #" + saved.getId() + " fue actualizado"
                );
            }
        }

        return saved;
    }

    // =========================================================
    // ✅ ELIMINAR
    // =========================================================
    @Override
    @Transactional
    public void eliminar(Integer id) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        reclamoRepository.deleteById(id);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "RECLAMO",
                "🗑️ Reclamo eliminado #" + id
        );

        if (reclamo.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(reclamo.getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "RECLAMO",
                        "🗑️ Tu reclamo #" + id + " fue eliminado"
                );
            }
        }
    }

    // =========================================================
    // ✅ CAMBIAR ESTADO
    // =========================================================
    @Override
    @Transactional
    public Reclamo cambiarEstado(Integer id, EstadoReclamo nuevoEstado) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        reclamo.setEstado(nuevoEstado);
        Reclamo saved = reclamoRepository.save(reclamo);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "RECLAMO",
                "🔄 Reclamo #" + id + " cambió a " + nuevoEstado.name()
        );

        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "RECLAMO",
                        "🔄 Tu reclamo #" + id + " cambió a " + nuevoEstado.name()
                );
            }
        }

        return saved;
    }

    @Override
    public List<Reclamo> filtrarAvanzado(EstadoReclamo estado, Integer clienteId,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reclamoRepository.filtrarAvanzado(estado, clienteId, fechaInicio, fechaFin);
    }
}