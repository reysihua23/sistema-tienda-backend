package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pago.EstadoPago;
import com.reydi.tienda.model.Pago.MetodoPago;
import com.reydi.tienda.repository.PagoRepository;
import com.reydi.tienda.service.NotificacionHelper;
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
    private final NotificacionHelper notificacionHelper;
    private final NotificationRecipientResolver recipientResolver;

    // =========================================================
    // ✅ CONSULTAS
    // =========================================================
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
    // Regla: Cliente paga → notificar a ADMIN + VENTAS
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

        Integer pedidoId = saved.getPedido() != null ? saved.getPedido().getId() : null;

        // ✅ Notificar a ADMIN + VENTAS (gestión debe validar el pago)
        notificacionHelper.notificarA(
                recipientResolver.idsAdminYVentas(),
                "PAGO",
                "💰 Pago #" + saved.getId() + " registrado" +
                        (saved.getPedido() != null ? " (Pedido #" + saved.getPedido().getId() + ")" : ""),
                pedidoId
        );

        return saved;
    }

    // =========================================================
    // ✅ ACTUALIZAR
    // Regla: Admin/Vendedor actualiza → notificar al CLIENTE dueño
    // =========================================================
    @Override
    @Transactional
    public Pago actualizar(Pago pago) {
        if (!pagoRepository.existsById(pago.getId())) {
            throw new RuntimeException("Pago no encontrado");
        }
        Pago saved = pagoRepository.save(pago);

        Integer pedidoId = saved.getPedido() != null ? saved.getPedido().getId() : null;

        // ✅ Notificar al CLIENTE dueño del pedido
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "PAGO",
                    "✏️ Tu pago #" + saved.getId() + " fue actualizado",
                    pedidoId
            );
        }

        return saved;
    }

    // =========================================================
    // ✅ ELIMINAR
    // Regla: Admin/Vendedor elimina → notificar al CLIENTE dueño
    // =========================================================
    @Override
    @Transactional
    public void eliminar(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pagoRepository.deleteById(id);

        Integer pedidoId = pago.getPedido() != null ? pago.getPedido().getId() : null;

        // ✅ Notificar al CLIENTE dueño
        if (pago.getPedido() != null && pago.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(pago.getPedido().getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "PAGO",
                    "🗑️ Tu pago #" + id + " fue eliminado",
                    pedidoId
            );
        }
    }

    // =========================================================
    // ✅ APROBAR
    // Regla: Admin aprueba → notificar al CLIENTE dueño
    // =========================================================
    @Override
    @Transactional
    public Pago aprobarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.APROBADO);
        Pago saved = pagoRepository.save(pago);

        Integer pedidoId = saved.getPedido() != null ? saved.getPedido().getId() : null;

        // ✅ Notificar al CLIENTE dueño del pedido
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "PAGO",
                    "✅ Tu pago #" + id + " fue APROBADO",
                    pedidoId
            );
        }

        return saved;
    }

    // =========================================================
    // ✅ RECHAZAR
    // Regla: Admin rechaza → notificar al CLIENTE dueño
    // =========================================================
    @Override
    @Transactional
    public Pago rechazarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.RECHAZADO);
        Pago saved = pagoRepository.save(pago);

        Integer pedidoId = saved.getPedido() != null ? saved.getPedido().getId() : null;

        // ✅ Notificar al CLIENTE dueño del pedido
        if (saved.getPedido() != null && saved.getPedido().getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getPedido().getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "PAGO",
                    "❌ Tu pago #" + id + " fue RECHAZADO",
                    pedidoId
            );
        }

        return saved;
    }

    @Override
    public boolean existePagoParaPedido(Integer pedidoId) {
        return pagoRepository.existsByPedidoId(pedidoId);
    }
}