package com.reydi.tienda.service;

import com.reydi.tienda.model.ReclamoEvidencias;

import java.util.List;
import java.util.Optional;

public interface ReclamoEvidenciasService {

    List<ReclamoEvidencias> listarPorReclamo(Integer reclamoId);

    Optional<ReclamoEvidencias> buscarPorId(Integer id);

    ReclamoEvidencias guardar(ReclamoEvidencias evidencia);

    void eliminar(Integer id);

    void eliminarPorReclamo(Integer reclamoId);

    boolean existeEvidenciaParaReclamo(Integer reclamoId);
}