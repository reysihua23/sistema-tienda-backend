package com.reydi.tienda.repository;

import com.reydi.tienda.model.ReclamoEvidencias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReclamoEvidenciasRepository extends JpaRepository<ReclamoEvidencias, Integer> {

    List<ReclamoEvidencias> findByReclamoId(Integer reclamoId);

    void deleteByReclamoId(Integer reclamoId);
}