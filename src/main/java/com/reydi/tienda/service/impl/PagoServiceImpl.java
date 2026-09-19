package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pago.EstadoPago;
import com.reydi.tienda.model.Pago.MetodoPago;
import com.reydi.tienda.repository.PagoRepository;
import com.reydi.tienda.service.NotificacionService;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final NotificacionService notificacionService;
    private final NotificationRecipientResolver recipientResolver;

    @Override
    public List<Pago> listarTodos() {
        return pagoRepository.findAll();
    }

    @Override
    public Optional<Pago> buscarPorId(Integer id) {
        return pagoRepository.findById(id);
    }

    @Override
    public List<Pago> buscarPorPedido(Integer pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public Optional<Pago> buscarPorPedidoId(Integer pedidoId) {
        return pagoRepository.findByPedidoIdOrderByFechaDesc(pedidoId);
    }

    @Override
    public List<Pago> buscarPorEstado(EstadoPago estado) {
        return pagoRepository.findByEstado(estado);
    }

    @Override
    public List<Pago> buscarPorMetodo(MetodoPago metodo) {
        return pagoRepository.findByMetodo(metodo);
    }

    @Override
    public Optional<Pago> buscarPorReferencia(String referenciaPasarela) {
        return pagoRepository.findByReferenciaPasarela(referenciaPasarela);
    }

    // =========================================================
    // ✅ GUARDAR
    // =========================================================
    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        if (pago.getFecha() == null) {
            pago.setFecha(LocalDateTime.now());
        }
        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PENDIENTE);
        }
        Pago saved = pagoRepository.save(pago);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PAGO",
                "💰 Pago #" + saved.getId() + " registrado" +
                        (saved.getPedido() != null ? " (Pedido #" + saved.getPedido().getId() + ")" : "")
        );

        // ✅ CLIENTE
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PAGO",
                        "💰 Tu pago #" + saved.getId() + " fue registrado"
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
    public Pago actualizar(Pago pago) {
        if (!pagoRepository.existsById(pago.getId())) {
            throw new RuntimeException("Pago no encontrado");
        }
        Pago saved = pagoRepository.save(pago);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PAGO",
                "✏️ Pago #" + saved.getId() + " actualizado"
        );

        // ✅ CLIENTE
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PAGO",
                        "✏️ Tu pago #" + saved.getId() + " fue actualizado"
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
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pagoRepository.deleteById(id);

        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PAGO",
                "🗑️ Pago eliminado #" + id
        );

        if (pago.getPedido() != null && pago.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(pago.getPedido().getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PAGO",
                        "🗑️ Tu pago #" + id + " fue eliminado"
                );
            }
        }
    }

    // =========================================================
    // ✅ APROBAR
    // =========================================================
    @Override
    @Transactional
    public Pago aprobarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.APROBADO);
        Pago saved = pagoRepository.save(pago);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PAGO",
                "✅ Pago #" + id + " APROBADO"
        );

        // ✅ CLIENTE
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PAGO",
                        "✅ Tu pago #" + id + " fue APROBADO"
                );
            }
        }

        return saved;
    }

    // =========================================================
    // ✅ RECHAZAR
    // =========================================================
    @Override
    @Transactional
    public Pago rechazarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.RECHAZADO);
        Pago saved = pagoRepository.save(pago);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PAGO",
                "❌ Pago #" + id + " RECHAZADO"
        );

        // ✅ CLIENTE
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PAGO",
                        "❌ Tu pago #" + id + " fue RECHAZADO"
                );
            }
        }

        return saved;
    }

    @Override
    public boolean existePagoParaPedido(Integer pedidoId) {
        return pagoRepository.existsByPedidoId(pedidoId);
    }
}