package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.EstadoServicio;  // ✅ Importar enum externo
import com.reydi.tienda.model.ServicioTecnico;
import com.reydi.tienda.repository.ServicioTecnicoRepository;
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
        return repository.save(servicio);
    }

    @Override
    @Transactional
    public ServicioTecnico actualizar(ServicioTecnico servicio) {
        if (!repository.existsById(servicio.getId())) {
            throw new RuntimeException("Servicio no encontrado");
        }
        return repository.save(servicio);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Servicio no encontrado");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public ServicioTecnico cambiarEstado(Integer id, String nuevoEstado) {
        ServicioTecnico servicio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setEstado(EstadoServicio.valueOf(nuevoEstado));
        return repository.save(servicio);
    }

    @Override
    @Transactional
    public ServicioTecnico agregarDiagnostico(Integer id, String diagnostico, BigDecimal costo) {
        ServicioTecnico servicio = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        servicio.setDiagnostico(diagnostico);
        servicio.setCosto(costo);
        return repository.save(servicio);
    }
}