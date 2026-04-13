package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.ReclamoEvidencias;
import com.reydi.tienda.repository.ReclamoEvidenciasRepository;
import com.reydi.tienda.service.ReclamoEvidenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReclamoEvidenciasServiceImpl implements ReclamoEvidenciasService {

    private final ReclamoEvidenciasRepository reclamoEvidenciasRepository;

    @Override
    public List<ReclamoEvidencias> listarTodos() {
        return reclamoEvidenciasRepository.findAll();
    }

    @Override
    public Optional<ReclamoEvidencias> buscarPorId(Integer id) {
        return reclamoEvidenciasRepository.findById(id);
    }

    @Override
    public List<ReclamoEvidencias> buscarPorReclamo(Integer reclamoId) {
        return reclamoEvidenciasRepository.findByReclamoId(reclamoId);
    }

    @Override
    public ReclamoEvidencias guardar(ReclamoEvidencias evidencia) {
        return reclamoEvidenciasRepository.save(evidencia);
    }

    @Override
    public ReclamoEvidencias actualizar(ReclamoEvidencias evidencia) {
        if (!reclamoEvidenciasRepository.existsById(evidencia.getId())) {
            throw new RuntimeException("Evidencia no encontrada");
        }
        return reclamoEvidenciasRepository.save(evidencia);
    }

    @Override
    public void eliminar(Integer id) {
        if (!reclamoEvidenciasRepository.existsById(id)) {
            throw new RuntimeException("Evidencia no encontrada");
        }
        reclamoEvidenciasRepository.deleteById(id);
    }

    @Override
    public void eliminarPorReclamo(Integer reclamoId) {
        reclamoEvidenciasRepository.deleteByReclamoId(reclamoId);
    }
}