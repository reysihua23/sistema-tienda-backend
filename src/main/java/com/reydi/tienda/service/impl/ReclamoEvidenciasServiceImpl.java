package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.ReclamoEvidencias;
import com.reydi.tienda.repository.ReclamoEvidenciasRepository;
import com.reydi.tienda.service.ReclamoEvidenciasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReclamoEvidenciasServiceImpl implements ReclamoEvidenciasService {

    private final ReclamoEvidenciasRepository evidenciaRepository;

    @Override
    public List<ReclamoEvidencias> listarPorReclamo(Integer reclamoId) {
        return evidenciaRepository.findByReclamoId(reclamoId);
    }

    @Override
    public Optional<ReclamoEvidencias> buscarPorId(Integer id) {
        return evidenciaRepository.findById(id);
    }

    @Override
    @Transactional
    public ReclamoEvidencias guardar(ReclamoEvidencias evidencia) {
        return evidenciaRepository.save(evidencia);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!evidenciaRepository.existsById(id)) {
            throw new RuntimeException("Evidencia no encontrada con ID: " + id);
        }
        evidenciaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void eliminarPorReclamo(Integer reclamoId) {
        evidenciaRepository.deleteByReclamoId(reclamoId);
    }

    @Override
    public boolean existeEvidenciaParaReclamo(Integer reclamoId) {
        return !evidenciaRepository.findByReclamoId(reclamoId).isEmpty();
    }
}