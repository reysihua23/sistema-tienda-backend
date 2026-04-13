package com.reydi.tienda.service;

import com.reydi.tienda.model.ServicioTecnico;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ServicioTecnicoService {
    List<ServicioTecnico> listarTodos();
    Optional<ServicioTecnico> buscarPorId(Integer id);
    List<ServicioTecnico> buscarPorCliente(Integer clienteId);
    List<ServicioTecnico> buscarPorEstado(String estado);
    ServicioTecnico guardar(ServicioTecnico servicio);
    List<ServicioTecnico> buscarPorTecnico(Integer tecnicoId);  // ✅ AGREGAR ESTE MÉTODO
    ServicioTecnico actualizar(ServicioTecnico servicio);
    void eliminar(Integer id);
    ServicioTecnico cambiarEstado(Integer id, String nuevoEstado);
    ServicioTecnico agregarDiagnostico(Integer id, String diagnostico, BigDecimal costo);
}