package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.DetallePedido;
import com.reydi.tienda.model.EstadoPedido;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.repository.DetallePedidoRepository;
import com.reydi.tienda.repository.PedidoRepository;
import com.reydi.tienda.service.PedidoService;
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
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido actualizar(Pedido pedido) {
        if (!pedidoRepository.existsById(pedido.getId())) {
            throw new RuntimeException("Pedido no encontrado");
        }
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado");
        }
        pedidoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Integer id, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
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