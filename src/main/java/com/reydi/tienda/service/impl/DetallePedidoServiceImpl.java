package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.DetallePedido;
import com.reydi.tienda.repository.DetallePedidoRepository;
import com.reydi.tienda.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetallePedidoServiceImpl implements DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;

    @Override
    public List<DetallePedido> listarTodos() {
        return detallePedidoRepository.findAll();
    }

    @Override
    public Optional<DetallePedido> buscarPorId(Integer id) {
        return detallePedidoRepository.findById(id);
    }

    @Override
    public List<DetallePedido> buscarPorPedido(Integer pedidoId) {
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<DetallePedido> buscarPorProducto(Integer productoId) {
        return detallePedidoRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional
    public DetallePedido guardar(DetallePedido detalle) {
        detalle.calcularSubtotal();
        return detallePedidoRepository.save(detalle);
    }

    @Override
    @Transactional
    public DetallePedido actualizar(DetallePedido detalle) {
        if (!detallePedidoRepository.existsById(detalle.getId())) {
            throw new RuntimeException("Detalle de pedido no encontrado");
        }
        detalle.calcularSubtotal();
        return detallePedidoRepository.save(detalle);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!detallePedidoRepository.existsById(id)) {
            throw new RuntimeException("Detalle de pedido no encontrado");
        }
        detallePedidoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void eliminarPorPedido(Integer pedidoId) {
        detallePedidoRepository.deleteByPedidoId(pedidoId);
    }
}