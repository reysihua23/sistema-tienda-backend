package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Devolucion;
import com.reydi.tienda.model.EstadoDevolucion;
import com.reydi.tienda.repository.DevolucionRepository;
import com.reydi.tienda.service.DevolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DevolucionServiceImpl implements DevolucionService {

    private final DevolucionRepository devolucionRepository;

    @Override
    public List<Devolucion> listarTodos() {
        return devolucionRepository.findAll();
    }

    @Override
    public Optional<Devolucion> buscarPorId(Long id) {
        return devolucionRepository.findById(id);
    }

    @Override
    public Optional<Devolucion> buscarPorReclamo(Long reclamoId) {
        return devolucionRepository.findByReclamoId(reclamoId);
    }

    @Override
    public List<Devolucion> buscarPorEstado(EstadoDevolucion estado) {
        return devolucionRepository.findByEstado(estado);
    }

    @Override
    public List<Devolucion> buscarPorPedido(Long pedidoId) {
        return devolucionRepository.findByPedidoId(pedidoId);
    }

    @Override
    public Devolucion guardar(Devolucion devolucion) {
        return devolucionRepository.save(devolucion);
    }

    @Override
    public Devolucion actualizar(Devolucion devolucion) {
        if (!devolucionRepository.existsById(devolucion.getId())) {
            throw new RuntimeException("Devolución no encontrada");
        }
        return devolucionRepository.save(devolucion);
    }

    @Override
    public void eliminar(Long id) {
        if (!devolucionRepository.existsById(id)) {
            throw new RuntimeException("Devolución no encontrada");
        }
        devolucionRepository.deleteById(id);
    }

    @Override
    public boolean existePorReclamo(Long reclamoId) {
        return devolucionRepository.existsByReclamoId(reclamoId);
    }
}