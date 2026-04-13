package com.reydi.tienda.service;

import com.reydi.tienda.model.ReclamoEvidencias;
import java.util.List;
import java.util.Optional;

public interface ReclamoEvidenciasService {
    List<ReclamoEvidencias> listarTodos();
    Optional<ReclamoEvidencias> buscarPorId(Integer id);
    List<ReclamoEvidencias> buscarPorReclamo(Integer reclamoId);
    ReclamoEvidencias guardar(ReclamoEvidencias evidencia);
    ReclamoEvidencias actualizar(ReclamoEvidencias evidencia);
    void eliminar(Integer id);
    void eliminarPorReclamo(Integer reclamoId);
}