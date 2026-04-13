package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Pago;
import com.reydi.tienda.model.Pago.EstadoPago;
import com.reydi.tienda.model.Pago.MetodoPago;
import com.reydi.tienda.repository.PagoRepository;
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

    @Override
    @Transactional
    public Pago guardar(Pago pago) {
        if (pago.getFecha() == null) {
            pago.setFecha(LocalDateTime.now());
        }
        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PENDIENTE);
        }
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago actualizar(Pago pago) {
        if (!pagoRepository.existsById(pago.getId())) {
            throw new RuntimeException("Pago no encontrado");
        }
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!pagoRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado");
        }
        pagoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Pago aprobarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.APROBADO);
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago rechazarPago(Integer id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado"));
        pago.setEstado(EstadoPago.RECHAZADO);
        return pagoRepository.save(pago);
    }

    @Override
    public boolean existePagoParaPedido(Integer pedidoId) {
        return pagoRepository.existsByPedidoId(pedidoId);
    }
}