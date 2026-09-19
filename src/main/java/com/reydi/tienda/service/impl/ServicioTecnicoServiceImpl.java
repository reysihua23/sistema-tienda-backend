package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.EstadoServicio;
import com.reydi.tienda.model.ServicioTecnico;
import com.reydi.tienda.repository.ServicioTecnicoRepository;
import com.reydi.tienda.service.NotificacionService;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.ServicioTecnicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicioTecnicoServiceImpl implements ServicioTecnicoService {

    private final ServicioTecnicoRepository repository;
    private final NotificacionService notificacionService;
    private final NotificationRecipientResolver recipientResolver;

    @Override
    public List<ServicioTecnico> listarTodos() {
        return repository.findAll();
    }

    @Override
    public Optional<ServicioTecnico> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    @Override
    public List<ServicioTecnico> buscarPorCliente(Integer clienteId) {
        return repository.findByClienteId(clienteId);
    }

    @Override
    public List<ServicioTecnico> buscarPorEstado(String estado) {
        return repository.findByEstado(estado);
    }

    @Override
    public List<ServicioTecnico> buscarPorTecnico(Integer tecnicoId) {
        return repository.findByTecnicoId(tecnicoId);
    }

    // =========================================================
    // ✅ GUARDAR
    // =========================================================
    @Override
    @Transactional
    public ServicioTecnico guardar(ServicioTecnico servicio) {
        if (servicio.getFecha() == null) {
            servicio.setFecha(LocalDateTime.now());
        }
        if (servicio.getEstado() == null) {
            servicio.setEstado(EstadoServicio.RECIBIDO);
        }
        if (servicio.getCosto() == null) {
            servicio.setCosto(BigDecimal.ZERO);
        }
        ServicioTecnico saved = repository.save(servicio);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "SERVICIO",
                "🔧 Nuevo servicio técnico #" + saved.getId()
        );

        // ✅ CLIENTE
        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "SERVICIO",
                    "🔧 Tu servicio #" + saved.getId() + " fue registrado"
            );
        } else {
            System.out.println("⚠️ Cliente sin usuario asociado: " +
                    (saved.getCliente() != null ? saved.getCliente().getId() : "null"));
        }

        return saved;
    }

    // =========================================================
    // ✅ ACTUALIZAR
    // =========================================================
    @Override
    @Transactional
    public ServicioTecnico actualizar(ServicioTecnico servicio) {
        if (!repository.existsById(servicio.getId())) {
            throw new RuntimeException("Servicio no encontrado");
        }
        ServicioTecnico saved = repository.save(servicio);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "SERVICIO",
                "✏️ Servicio #" + saved.getId() + " actualizado"
        );

        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "SERVICIO",
                    "✏️ Tu servicio #" + saved.getId() + " fue actualizado"
            );
        }

        return saved;
    }

    // =========================================================
    // ✅ ELIMINAR
    // =========================================================
    @Override
    @Transactional
    public void eliminar(Integer id) {
        ServicioTecnico servicio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        repository.deleteById(id);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "SERVICIO",
                "🗑️ Servicio eliminado #" + id
        );

        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(servicio.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "SERVICIO",
                    "🗑️ Tu servicio #" + id + " fue eliminado"
            );
        }
    }

    // =========================================================
    // ✅ CAMBIAR ESTADO (el caso que reportaste)
    // =========================================================
    @Override
    @Transactional
    public ServicioTecnico cambiarEstado(Integer id, String nuevoEstado) {
        ServicioTecnico servicio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setEstado(EstadoServicio.valueOf(nuevoEstado));
        ServicioTecnico saved = repository.save(servicio);

        // ✅ 1. ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "SERVICIO",
                "🔄 Servicio #" + id + " cambió a " + nuevoEstado
        );

        // ✅ 2. CLIENTE
        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "SERVICIO",
                    "🔄 Tu servicio #" + id + " cambió a " + nuevoEstado
            );
        } else {
            System.out.println("⚠️ Cliente sin usuario asociado: " +
                    (saved.getCliente() != null ? saved.getCliente().getId() : "null"));
        }

        return saved;
    }

    // =========================================================
    // ✅ AGREGAR DIAGNÓSTICO
    // =========================================================
    @Override
    @Transactional
    public ServicioTecnico agregarDiagnostico(Integer id, String diagnostico, BigDecimal costo) {
        ServicioTecnico servicio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setDiagnostico(diagnostico);
        servicio.setCosto(costo);
        ServicioTecnico saved = repository.save(servicio);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "SERVICIO",
                "🩺 Diagnóstico agregado al servicio #" + id + " (S/ " + costo + ")"
        );

        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "SERVICIO",
                    "🩺 Diagnóstico de tu servicio #" + id + ": " + diagnostico
            );
        }

        return saved;
    }
}