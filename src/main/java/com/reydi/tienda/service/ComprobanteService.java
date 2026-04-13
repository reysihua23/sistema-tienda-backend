package com.reydi.tienda.service;

import com.reydi.tienda.dto.ComprobanteResponseDTO;
import com.reydi.tienda.model.Comprobante;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.model.TipoComprobante;

import java.util.List;
import java.util.Optional;

public interface ComprobanteService {

    // Métodos CRUD básicos
    List<Comprobante> listarTodos();
    Optional<Comprobante> buscarPorId(Integer id);
    Optional<Comprobante> buscarPorPedido(Integer pedidoId);

    List<Comprobante> buscarPorServicio(Integer servicioId);
    List<Comprobante> buscarPorTipo(TipoComprobante tipo);
    Comprobante guardar(Comprobante comprobante);
    Comprobante actualizar(Comprobante comprobante);
    void eliminar(Integer id);

    // Métodos específicos
    Comprobante generarComprobante(Pedido pedido, String tipoComprobante);
    ComprobanteResponseDTO obtenerComprobantePorPedido(Integer pedidoId);
    byte[] generarPDFComprobante(Integer pedidoId);
}