package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.DetallePedido;
import com.reydi.tienda.model.EstadoPedido;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.repository.DetallePedidoRepository;
import com.reydi.tienda.repository.PedidoRepository;
import com.reydi.tienda.service.NotificacionService;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.PedidoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final NotificacionService notificacionService;
    private final NotificationRecipientResolver recipientResolver;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Override
    public Optional<Pedido> buscarPorId(Integer id) {
        return pedidoRepository.findById(id);
    }

    @Override
    public List<Pedido> buscarPorCliente(Integer clienteId) {
        return pedidoRepository.findByClienteId(clienteId);
    }

    @Override
    public List<Pedido> buscarPorEstado(EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    @Override
    public List<Pedido> buscarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return pedidoRepository.findByFechaBetween(inicio, fin);
    }

    @Override
    public List<Pedido> buscarUltimosPedidosPorCliente(Integer clienteId) {
        return pedidoRepository.findUltimosPedidosPorCliente(clienteId);
    }

    @Override
    public Long contarPedidosPorCliente(Integer clienteId) {
        return pedidoRepository.countPedidosByCliente(clienteId);
    }

    // =========================================================
    // ✅ GUARDAR
    // =========================================================
    @Override
    @Transactional
    public Pedido guardar(Pedido pedido) {
        if (pedido.getFecha() == null) {
            pedido.setFecha(LocalDateTime.now());
        }
        if (pedido.getEstado() == null) {
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }
        if (pedido.getTotal() == null) {
            pedido.setTotal(BigDecimal.ZERO);
        }

        try {
            String sql = "INSERT INTO pedidos (cliente_id, total, estado, fecha, metodo_pago, origen) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, pedido.getCliente().getId());
            query.setParameter(2, pedido.getTotal());
            query.setParameter(3, pedido.getEstado().name());
            query.setParameter(4, LocalDateTime.now());
            query.setParameter(5, pedido.getMetodoPago());
            query.setParameter(6, pedido.getOrigen().name());

            query.executeUpdate();

            String idSql = "SELECT LAST_INSERT_ID()";
            Integer id = ((Number) entityManager.createNativeQuery(idSql).getSingleResult()).intValue();
            pedido.setId(id);

            System.out.println("✅ Pedido guardado con SQL nativo - ID: " + id);

            // ✅ 1. ADMIN
            notificacionService.crearNotificacion(
                    recipientResolver.adminId(),
                    "PEDIDO",
                    "🛒 Nuevo pedido #" + id + " - S/ " + pedido.getTotal()
            );

            // ✅ 2. CLIENTE
            Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(pedido.getCliente());
            if (clienteUsuarioId != null) {
                notificacionService.crearNotificacion(
                        clienteUsuarioId,
                        "PEDIDO",
                        "🛒 Tu pedido #" + id + " fue registrado - S/ " + pedido.getTotal()
                );
            }

            return pedido;

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            throw new RuntimeException("Error al guardar pedido: " + e.getMessage(), e);
        }
    }

    // =========================================================
    // ✅ ACTUALIZAR
    // =========================================================
    @Override
    @Transactional
    public Pedido actualizar(Pedido pedido) {
        if (!pedidoRepository.existsById(pedido.getId())) {
            throw new RuntimeException("Pedido no encontrado");
        }
        Pedido saved = pedidoRepository.save(pedido);

        // ✅ 1. ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PEDIDO",
                "✏️ Pedido #" + saved.getId() + " actualizado"
        );

        // ✅ 2. CLIENTE
        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "PEDIDO",
                    "✏️ Tu pedido #" + saved.getId() + " fue actualizado"
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
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedidoRepository.deleteById(id);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PEDIDO",
                "🗑️ Pedido eliminado #" + id
        );

        // ✅ CLIENTE
        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(pedido.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "PEDIDO",
                    "🗑️ Tu pedido #" + id + " fue eliminado"
            );
        }
    }

    // =========================================================
    // ✅ CAMBIAR ESTADO
    // =========================================================
    @Override
    @Transactional
    public Pedido cambiarEstado(Integer id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(nuevoEstado);
        Pedido saved = pedidoRepository.save(pedido);

        // ✅ ADMIN
        notificacionService.crearNotificacion(
                recipientResolver.adminId(),
                "PEDIDO",
                "🔄 Pedido #" + id + " cambió a " + nuevoEstado.name()
        );

        // ✅ CLIENTE
        Integer clienteUsuarioId = recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
        if (clienteUsuarioId != null) {
            notificacionService.crearNotificacion(
                    clienteUsuarioId,
                    "PEDIDO",
                    "🔄 Tu pedido #" + id + " cambió a " + nuevoEstado.name()
            );
        }

        return saved;
    }

    @Override
    public BigDecimal calcularTotal(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return pedido.getDetalles().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public Pedido agregarDetalle(Integer pedidoId, DetallePedido detalle) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        detalle.setPedido(pedido);
        detalle.calcularSubtotal();
        detallePedidoRepository.save(detalle);

        pedido.getDetalles().add(detalle);
        pedido.actualizarTotal();

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido eliminarDetalle(Integer pedidoId, Integer detalleId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        detallePedidoRepository.deleteById(detalleId);
        pedido.getDetalles().removeIf(d -> d.getId().equals(detalleId));

        pedido.actualizarTotal();

        return pedidoRepository.save(pedido);
    }
}