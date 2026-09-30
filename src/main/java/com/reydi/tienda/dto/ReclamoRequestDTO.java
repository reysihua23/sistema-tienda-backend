package com.reydi.tienda.dto;

import com.reydi.tienda.model.TipoReclamo;
import lombok.Data;

@Data
public class ReclamoRequestDTO {

    /** Tipo de reclamo: DEFECTO, GARANTIA, DEVOLUCION, NO_CONFORMIDAD */
    private TipoReclamo tipo;

    /** Descripción del problema */
    private String descripcion;

    /** ID del cliente que hace el reclamo */
    private Integer clienteId;

    /** ID del pedido relacionado (opcional pero recomendado) */
    private Integer pedidoId;
}