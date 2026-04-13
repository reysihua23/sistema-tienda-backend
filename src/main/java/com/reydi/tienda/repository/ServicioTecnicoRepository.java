package com.reydi.tienda.repository;

import com.reydi.tienda.model.ServicioTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioTecnicoRepository extends JpaRepository<ServicioTecnico, Integer> {

    List<ServicioTecnico> findByClienteId(Integer clienteId);

    List<ServicioTecnico> findByEstado(String estado);

    List<ServicioTecnico> findByTecnicoId(Integer tecnicoId);
}