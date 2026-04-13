package com.reydi.tienda.service;

import com.reydi.tienda.model.Envio;
import com.reydi.tienda.model.EstadoEnvio;
import java.util.List;
import java.util.Optional;

public interface EnvioService {
    List<Envio> listarTodos();
    Optional<Envio> buscarPorId(Integer id);
    List<Envio> buscarPorPedido(Integer pedidoId);
    List<Envio> buscarPorEstado(EstadoEnvio estado);
    Optional<Envio> buscarPorCodigoSeguimiento(String codigo);
    Envio guardar(Envio envio);
    Envio actualizar(Envio envio);
    void eliminar(Integer id);
    Envio actualizarEstado(Integer id, EstadoEnvio estado);
}