package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Envio;
import com.reydi.tienda.model.EstadoEnvio;
import com.reydi.tienda.repository.EnvioRepository;
import com.reydi.tienda.service.EnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioService {

    private final EnvioRepository envioRepository;

    @Override
    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    @Override
    public Optional<Envio> buscarPorId(Integer id) {
        return envioRepository.findById(id);
    }

    @Override
    public List<Envio> buscarPorPedido(Integer pedidoId) {
        return envioRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<Envio> buscarPorEstado(EstadoEnvio estado) {
        return envioRepository.findByEstado(estado);
    }

    @Override
    public Optional<Envio> buscarPorCodigoSeguimiento(String codigo) {
        return envioRepository.findByCodigoSeguimiento(codigo);
    }

    @Override
    @Transactional
    public Envio guardar(Envio envio) {
        if (envio.getEstado() == null) {
            envio.setEstado(EstadoEnvio.PENDIENTE);
        }
        return envioRepository.save(envio);
    }

    @Override
    @Transactional
    public Envio actualizar(Envio envio) {
        if (!envioRepository.existsById(envio.getId())) {
            throw new RuntimeException("Envío no encontrado con ID: " + envio.getId());
        }
        return envioRepository.save(envio);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!envioRepository.existsById(id)) {
            throw new RuntimeException("Envío no encontrado con ID: " + id);
        }
        envioRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Envio actualizarEstado(Integer id, EstadoEnvio estado) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado con ID: " + id));

        envio.setEstado(estado);

        // Actualizar fechas según el estado
        if (estado == EstadoEnvio.ENVIADO && envio.getFechaEnvio() == null) {
            envio.setFechaEnvio(LocalDateTime.now());
        }
        if (estado == EstadoEnvio.ENTREGADO && envio.getFechaEntrega() == null) {
            envio.setFechaEntrega(LocalDateTime.now());
        }

        return envioRepository.save(envio);
    }
}