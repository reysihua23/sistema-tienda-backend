package com.reydi.tienda.service.impl;

import com.reydi.tienda.dto.ComprobanteResponseDTO;
import com.reydi.tienda.model.*;
import com.reydi.tienda.repository.ComprobanteRepository;
import com.reydi.tienda.repository.DetalleComprobanteRepository;
import com.reydi.tienda.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final DetalleComprobanteRepository detalleComprobanteRepository;

    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("0.18");
    private static final String EMPRESA_RUC = "20601234567";
    private static final String EMPRESA_NOMBRE = "JIMENEZ ";
    private static final String EMPRESA_DIRECCION = "Amazonas, Písac 08106- Cusco - Perú";
    private static final String EMPRESA_TELEFONO = "997863112";

    // ==================== MÉTODOS CRUD BÁSICOS ====================

    @Override
    public List<Comprobante> listarTodos() {
        return comprobanteRepository.findAll();
    }

    @Override
    public Optional<Comprobante> buscarPorId(Integer id) {
        return comprobanteRepository.findById(id);
    }

    @Override
    public Optional<Comprobante> buscarPorPedido(Integer pedidoId) {
        return comprobanteRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<Comprobante> buscarPorServicio(Integer servicioId) {
        // Si tienes servicios, implementa; si no, retorna lista vacía
        return List.of();
    }

    @Override
    public List<Comprobante> buscarPorTipo(TipoComprobante tipo) {
        return comprobanteRepository.findByTipoComprobante(tipo.name());
    }

    @Override
    @Transactional
    public Comprobante guardar(Comprobante comprobante) {
        if (comprobante.getFechaEmision() == null) {
            comprobante.setFechaEmision(LocalDateTime.now());
        }
        if (comprobante.getEstado() == null) {
            comprobante.setEstado("EMITIDO");
        }
        return comprobanteRepository.save(comprobante);
    }

    @Override
    @Transactional
    public Comprobante actualizar(Comprobante comprobante) {
        if (!comprobanteRepository.existsById(comprobante.getId())) {
            throw new RuntimeException("Comprobante no encontrado con ID: " + comprobante.getId());
        }
        return comprobanteRepository.save(comprobante);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!comprobanteRepository.existsById(id)) {
            throw new RuntimeException("Comprobante no encontrado con ID: " + id);
        }
        comprobanteRepository.deleteById(id);
    }

    // ==================== MÉTODOS ESPECÍFICOS ====================

    @Override
    @Transactional
    public Comprobante generarComprobante(Pedido pedido, String tipoComprobante) {
        Cliente cliente = pedido.getCliente();

        // Calcular subtotal (sin IGV)
        BigDecimal subtotal = pedido.getTotal().divide(BigDecimal.ONE.add(IGV_PORCENTAJE), 2, RoundingMode.HALF_UP);
        BigDecimal igv = pedido.getTotal().subtract(subtotal);

        // Generar número de comprobante
        String serie = tipoComprobante.equals("FACTURA") ? "F001" : "B001";
        String numero = generarNumeroComprobante(serie);
        String numeroComprobante = serie + "-" + numero;

        // Crear comprobante
        Comprobante comprobante = Comprobante.builder()
                .pedido(pedido)
                .tipoComprobante(tipoComprobante)
                .serie(serie)
                .numero(numero)
                .numeroComprobante(numeroComprobante)
                .clienteNombre(cliente.getNombre())
                .clienteDocumento(cliente.getDocumento() != null ? cliente.getDocumento() : "00000000")
                .clienteDireccion(cliente.getDireccion() != null ? cliente.getDireccion() : "No especificada")
                .clienteTelefono(cliente.getTelefono())
                .clienteEmail(cliente.getEmail())
                .empresaRuc(EMPRESA_RUC)
                .empresaNombre(EMPRESA_NOMBRE)
                .empresaDireccion(EMPRESA_DIRECCION)
                .empresaTelefono(EMPRESA_TELEFONO)
                .subtotal(subtotal)
                .igv(igv)
                .total(pedido.getTotal())
                .fechaEmision(LocalDateTime.now())
                .estado("EMITIDO")
                .build();

        comprobante = comprobanteRepository.save(comprobante);

        // Crear detalles del comprobante
        for (DetallePedido detalle : pedido.getDetalles()) {
            BigDecimal precioSinIGV = detalle.getPrecio().divide(BigDecimal.ONE.add(IGV_PORCENTAJE), 2, RoundingMode.HALF_UP);
            BigDecimal subtotalItem = precioSinIGV.multiply(BigDecimal.valueOf(detalle.getCantidad())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igvItem = subtotalItem.multiply(IGV_PORCENTAJE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalItem = subtotalItem.add(igvItem).setScale(2, RoundingMode.HALF_UP);

            DetalleComprobante detalleComprobante = DetalleComprobante.builder()
                    .comprobante(comprobante)
                    .producto(detalle.getProducto())
                    .cantidad(detalle.getCantidad())
                    .precioUnitario(precioSinIGV)
                    .subtotal(subtotalItem)
                    .igvItem(igvItem)
                    .totalItem(totalItem)
                    .build();

            detalleComprobanteRepository.save(detalleComprobante);
        }

        return comprobante;
    }

    // En ComprobanteServiceImpl.java - método obtenerComprobantePorPedido
    @Override
    public ComprobanteResponseDTO obtenerComprobantePorPedido(Integer pedidoId) {
        Comprobante comprobante = comprobanteRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado para el pedido: " + pedidoId));

        List<DetalleComprobante> detalles = detalleComprobanteRepository.findByComprobanteId(comprobante.getId());

        ComprobanteResponseDTO dto = new ComprobanteResponseDTO();
        dto.setId(comprobante.getId());
        dto.setTipoComprobante(comprobante.getTipoComprobante());
        dto.setSerie(comprobante.getSerie());
        dto.setNumero(comprobante.getNumero());
        dto.setNumeroComprobante(comprobante.getNumeroComprobante());
        dto.setPedidoId(comprobante.getPedido().getId());
        dto.setClienteNombre(comprobante.getClienteNombre());
        dto.setClienteDocumento(comprobante.getClienteDocumento());
        dto.setClienteDireccion(comprobante.getClienteDireccion());
        dto.setClienteTelefono(comprobante.getClienteTelefono());
        dto.setClienteEmail(comprobante.getClienteEmail());
        dto.setEmpresaRuc(comprobante.getEmpresaRuc());
        dto.setEmpresaNombre(comprobante.getEmpresaNombre());
        dto.setEmpresaDireccion(comprobante.getEmpresaDireccion());
        dto.setEmpresaTelefono(comprobante.getEmpresaTelefono());
        dto.setSubtotal(comprobante.getSubtotal());
        dto.setIgv(comprobante.getIgv());
        dto.setTotal(comprobante.getTotal());
        dto.setFechaEmision(comprobante.getFechaEmision());
        dto.setEstado(comprobante.getEstado());

        List<ComprobanteResponseDTO.DetalleComprobanteResponseDTO> detallesDTO = detalles.stream()
                .map(det -> {
                    ComprobanteResponseDTO.DetalleComprobanteResponseDTO detDTO = new ComprobanteResponseDTO.DetalleComprobanteResponseDTO();
                    detDTO.setId(det.getId());
                    detDTO.setProductoId(det.getProducto().getId());
                    detDTO.setProductoNombre(det.getProducto().getNombre());
                    detDTO.setCantidad(det.getCantidad());
                    detDTO.setPrecioUnitario(det.getPrecioUnitario());
                    detDTO.setSubtotal(det.getSubtotal());
                    detDTO.setIgvItem(det.getIgvItem());
                    detDTO.setTotalItem(det.getTotalItem());
                    return detDTO;
                })
                .collect(Collectors.toList());

        dto.setDetalles(detallesDTO);
        return dto;
    }

    @Override
    public byte[] generarPDFComprobante(Integer pedidoId) {
        // TODO: Implementar generación de PDF con iText o JasperReports
        return new byte[0];
    }

    private String generarNumeroComprobante(String serie) {
        String lastNumber = comprobanteRepository.findLastNumeroBySerie(serie);
        int nextNumber = 1;
        if (lastNumber != null && !lastNumber.isEmpty()) {
            nextNumber = Integer.parseInt(lastNumber) + 1;
        }
        return String.format("%08d", nextNumber);
    }
}