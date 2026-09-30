package com.reydi.tienda.service.impl;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.reydi.tienda.dto.ComprobanteResponseDTO;
import com.reydi.tienda.model.*;
import com.reydi.tienda.repository.ComprobanteRepository;
import com.reydi.tienda.repository.DetalleComprobanteRepository;
import com.reydi.tienda.service.ComprobanteService;
import com.reydi.tienda.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.time.format.DateTimeFormatter;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final DetalleComprobanteRepository detalleComprobanteRepository;
    private final EmailService emailService;

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
                .clienteDocumento(cliente.getDocumento() != null && !cliente.getDocumento().isBlank()
                        ? cliente.getDocumento()
                        : "-")
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

        // ✅ NUEVO: Enviar comprobante automáticamente por correo
        enviarComprobanteAutomatico(comprobante, pedido);
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

    /**@Override
    public byte[] generarPDFComprobante(Integer pedidoId) {
        // TODO: Implementar generación de PDF con iText o JasperReports
        return new byte[0];
    }*/
    @Override
    public byte[] generarPDFComprobante(Integer pedidoId) {
        try {
            Comprobante comprobante = comprobanteRepository.findByPedidoId(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Comprobante no encontrado para pedido: " + pedidoId));

            List<DetalleComprobante> detalles = detalleComprobanteRepository
                    .findByComprobanteId(comprobante.getId());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            // 👇 Márgenes más pequeños para que quepa todo
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 60, 30);  // arriba, derecha, abajo, izquierda

            // ==================== LOGO ====================
            try {
                ClassPathResource logoResource = new ClassPathResource("static/logo.png");
                if (logoResource.exists()) {
                    try (InputStream is = logoResource.getInputStream()) {
                        Image logo = new Image(ImageDataFactory.create(is.readAllBytes()));
                        logo.setWidth(160);  // Ajusta el tamaño aquí
                        logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                        logo.setMarginBottom(10);
                        document.add(logo);
                    }
                } else {
                    System.out.println("ℹ️ Logo no encontrado");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error al cargar el logo: " + e.getMessage());
            }


            // ==================== HEADER ====================
            document.add(new Paragraph("COMPROBANTE DE PAGO")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16)
                    .setFontColor(new DeviceRgb(26, 26, 46))
                    .setMarginBottom(2));

            document.add(new Paragraph("N° " + comprobante.getNumeroComprobante())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(8));

            // ==================== DATOS DE LA EMPRESA ====================
            document.add(new Paragraph(comprobante.getEmpresaNombre())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(91, 78, 255))
                    .setMarginBottom(2));

            document.add(new Paragraph("RUC: " + comprobante.getEmpresaRuc() +
                    "  |  Tel: " + comprobante.getEmpresaTelefono())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(1));

            document.add(new Paragraph(comprobante.getEmpresaDireccion())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginBottom(10));

            // ==================== DATOS DEL CLIENTE ====================
            document.add(new Paragraph("DATOS DEL CLIENTE")
                    .setBold()
                    .setFontSize(10)
                    .setBackgroundColor(new DeviceRgb(245, 245, 245))
                    .setPadding(4)
                    .setMarginBottom(4));

            // Cliente en 2 columnas para ahorrar espacio
            Table clienteTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            clienteTable.setWidth(UnitValue.createPercentValue(100));
            clienteTable.setMarginBottom(8);

            clienteTable.addCell(new Cell().add(new Paragraph("Cliente: " + comprobante.getClienteNombre()).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));

            String docTexto = (comprobante.getClienteDocumento() != null
                    && !comprobante.getClienteDocumento().isBlank()
                    && !comprobante.getClienteDocumento().equals("00000000"))
                    ? "Documento: " + comprobante.getClienteDocumento()
                    : "Documento: -";

            clienteTable.addCell(new Cell().add(new Paragraph(docTexto).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));

            clienteTable.addCell(new Cell().add(new Paragraph("Dirección: " + comprobante.getClienteDireccion()).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));
            clienteTable.addCell(new Cell().add(new Paragraph("Teléfono: " + (comprobante.getClienteTelefono() != null ? comprobante.getClienteTelefono() : "-")).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));
            clienteTable.addCell(new Cell().add(new Paragraph("Email: " + (comprobante.getClienteEmail() != null ? comprobante.getClienteEmail() : "-")).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));
            clienteTable.addCell(new Cell().add(new Paragraph("Fecha: " + comprobante.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setFontSize(8))
                    .setBorder(Border.NO_BORDER).setPadding(2));

            document.add(clienteTable);

            // ==================== DETALLE DE PRODUCTOS ====================
            document.add(new Paragraph("DETALLE DE PRODUCTOS")
                    .setBold()
                    .setFontSize(10)
                    .setBackgroundColor(new DeviceRgb(245, 245, 245))
                    .setPadding(4)
                    .setMarginBottom(4));

            float[] columnWidths = {5, 1, 2, 2};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginBottom(8);

            // Cabecera con font más pequeño
            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Producto").setBold().setFontSize(8).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(91, 78, 255))
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.LEFT));

            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Cant.").setBold().setFontSize(8).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(91, 78, 255))
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.CENTER));

            table.addHeaderCell(new Cell()
                    .add(new Paragraph("P. Unit.").setBold().setFontSize(8).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(91, 78, 255))
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.RIGHT));

            table.addHeaderCell(new Cell()
                    .add(new Paragraph("Total").setBold().setFontSize(8).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(new DeviceRgb(91, 78, 255))
                    .setPadding(4)
                    .setTextAlignment(TextAlignment.RIGHT));

            for (DetalleComprobante d : detalles) {
                table.addCell(new Cell()
                        .add(new Paragraph(d.getProducto().getNombre()).setFontSize(8))
                        .setPadding(4));
                table.addCell(new Cell()
                        .add(new Paragraph(String.valueOf(d.getCantidad())).setFontSize(8))
                        .setPadding(4)
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell()
                        .add(new Paragraph("S/ " + d.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP)).setFontSize(8))
                        .setPadding(4)
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell()
                        .add(new Paragraph("S/ " + d.getTotalItem().setScale(2, RoundingMode.HALF_UP)).setFontSize(8).setBold())
                        .setPadding(4)
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);

            // ==================== TOTALES ====================
            Table totales = new Table(UnitValue.createPercentArray(new float[]{3, 1}));
            totales.setWidth(UnitValue.createPercentValue(50));
            totales.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            totales.setMarginBottom(20);

            totales.addCell(new Cell().add(new Paragraph("Subtotal:").setFontSize(9))
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            totales.addCell(new Cell().add(new Paragraph("S/ " + comprobante.getSubtotal().setScale(2, RoundingMode.HALF_UP)).setFontSize(9))
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));

            totales.addCell(new Cell().add(new Paragraph("IGV (18%):").setFontSize(9))
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            totales.addCell(new Cell().add(new Paragraph("S/ " + comprobante.getIgv().setScale(2, RoundingMode.HALF_UP)).setFontSize(9))
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));

            totales.addCell(new Cell().add(new Paragraph("Total:").setFontSize(11).setBold())
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            totales.addCell(new Cell().add(new Paragraph("S/ " + comprobante.getTotal().setScale(2, RoundingMode.HALF_UP))
                            .setFontSize(11).setBold().setFontColor(new DeviceRgb(91, 78, 255)))
                    .setBorder(Border.NO_BORDER).setPadding(2).setTextAlignment(TextAlignment.RIGHT));

            document.add(totales);

            // ==================== ✅ FOOTER FIJO (no se desborda) ====================
            // Este footer SIEMPRE se queda al fondo de la página
            // No empuja el contenido a otra página
            document.add(new Paragraph("Este documento es una representación digital de su comprobante de pago")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setFixedPosition(30, 45, PageSize.A4.getWidth() - 60));

            document.add(new Paragraph("© " + java.time.Year.now().getValue() + " JIMENEZ - Todos los derechos reservados")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setFixedPosition(30, 30, PageSize.A4.getWidth() - 60));

            // Cerrar
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            System.err.println("❌ Error al generar PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ✅ Envía el comprobante por correo de forma automática.
     * Se ejecuta después de generar el comprobante.
     * Si falla, solo se loguea el error (no rompe la venta).
     */
    private void enviarComprobanteAutomatico(Comprobante comprobante, Pedido pedido) {
        try {
            // 1. Verificar que el cliente tenga correo
            String emailCliente = comprobante.getClienteEmail();
            if (emailCliente == null || emailCliente.isBlank()) {
                System.out.println("ℹ️ Cliente sin correo, no se envía comprobante automático");
                return;
            }

            // 2. Preparar la lista de productos para el template del correo
            List<Map<String, Object>> productos = new ArrayList<>();
            if (pedido.getDetalles() != null) {
                for (DetallePedido d : pedido.getDetalles()) {
                    Map<String, Object> item = new HashMap<>();
                    String nombreProducto = d.getProducto() != null
                            ? d.getProducto().getNombre()
                            : "Producto";
                    item.put("nombre", nombreProducto);
                    item.put("cantidad", d.getCantidad());
                    item.put("precio", "S/ " + d.getPrecio());
                    item.put("subtotal", "S/ " + d.getSubtotal());
                    productos.add(item);
                }
            }

            // 3. Método de pago
            String metodoPago = pedido.getMetodoPago() != null
                    ? pedido.getMetodoPago()
                    : "EFECTIVO";

            // 4. Generar PDF (opcional, si lo tienes implementado)
            byte[] pdfAdjunto = null;
            try {
                pdfAdjunto = generarPDFComprobante(pedido.getId());
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo generar el PDF: " + e.getMessage());
            }

            // 5. Enviar el correo
            emailService.enviarComprobante(
                    emailCliente,
                    comprobante.getClienteNombre() != null ? comprobante.getClienteNombre() : "Cliente",
                    comprobante.getNumeroComprobante() != null ? comprobante.getNumeroComprobante() : "S/N",
                    comprobante.getFechaEmision() != null ? comprobante.getFechaEmision().toString() : "",
                    "S/ " + comprobante.getTotal(),
                    productos,
                    metodoPago,
                    pdfAdjunto
            );

            System.out.println("✅ Comprobante enviado automáticamente a " + emailCliente);

        } catch (Exception e) {
            // ⚠️ NO lanzar excepción para no romper la venta si falla el correo
            System.err.println("❌ Error al enviar comprobante automático: " + e.getMessage());
            e.printStackTrace();
        }
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