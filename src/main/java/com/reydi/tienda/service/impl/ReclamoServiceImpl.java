package com.reydi.tienda.service.impl;

import com.reydi.tienda.dto.ReclamoRequestDTO;
import com.reydi.tienda.dto.ReclamoResponseDTO;
import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.model.Pedido;
import com.reydi.tienda.model.Reclamo;
import com.reydi.tienda.model.EstadoReclamo;
import com.reydi.tienda.model.TipoReclamo;
import com.reydi.tienda.repository.ClienteRepository;
import com.reydi.tienda.repository.PedidoRepository;
import com.reydi.tienda.repository.ReclamoRepository;
import com.reydi.tienda.service.NotificacionHelper;
import com.reydi.tienda.service.NotificationRecipientResolver;
import com.reydi.tienda.service.ReclamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamoServiceImpl implements ReclamoService {

    private final ReclamoRepository reclamoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;
    private final NotificacionHelper notificacionHelper;
    private final NotificationRecipientResolver recipientResolver;

    // =========================================================
    // ✅ CONSULTAS
    // =========================================================

    @Override
    public List<ReclamoResponseDTO> listarTodosDTO() {
        return reclamoRepository.findAll().stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reclamo> buscarPorId(Integer id) {
        return reclamoRepository.findById(id);
    }

    @Override
    public List<Reclamo> buscarPorCliente(Integer clienteId) {
        return reclamoRepository.findByClienteId(clienteId);
    }

    /** ✅ NUEVO: DTO de reclamos del cliente — para evitar ciclos infinitos de JSON */
    @Override
    public List<ReclamoResponseDTO> buscarPorClienteDTO(Integer clienteId) {
        return reclamoRepository.findByClienteId(clienteId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ✅ CANCELAR RECLAMO (solo el dueño, solo si REGISTRADO)
    // =========================================================
    @Override
    @Transactional
    public ReclamoResponseDTO cancelar(Integer id, Integer clienteId) {
        // 1. Validar cliente
        if (clienteId == null) {
            throw new RuntimeException("El clienteId es obligatorio");
        }

        // 2. Buscar reclamo
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));

        // 3. Validar que el reclamo pertenece al cliente
        if (reclamo.getCliente() == null ||
                !reclamo.getCliente().getId().equals(clienteId)) {
            throw new RuntimeException("No puedes cancelar un reclamo que no te pertenece");
        }

        // 4. Validar estado (solo REGISTRADO)
        if (reclamo.getEstado() != EstadoReclamo.REGISTRADO) {
            throw new RuntimeException(
                    "Solo puedes cancelar reclamos en estado REGISTRADO. " +
                            "Estado actual: " + reclamo.getEstado());
        }

        // 5. Cambiar estado
        reclamo.setEstado(EstadoReclamo.CANCELADO);
        Reclamo saved = reclamoRepository.save(reclamo);

        // 6. Notificar a ADMIN + VENTAS
        notificacionHelper.notificarA(
                recipientResolver.idsAdminYVentas(),
                "RECLAMO",
                "🚫 Reclamo #" + saved.getId() + " cancelado por el cliente",
                saved.getId()
        );

        return convertirAResponseDTO(saved);
    }

    @Override
    public List<Reclamo> buscarPorEstado(EstadoReclamo estado) {
        return reclamoRepository.findByEstado(estado);
    }

    @Override
    public List<Reclamo> buscarPorTipo(TipoReclamo tipo) {
        return reclamoRepository.findByTipo(tipo);
    }

    @Override
    public List<Reclamo> filtrarAvanzado(EstadoReclamo estado, Integer clienteId,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reclamoRepository.filtrarAvanzado(estado, clienteId, fechaInicio, fechaFin);
    }

    /** ✅ reclamos de un pedido específico */
    @Override
    public List<ReclamoResponseDTO> buscarPorPedido(Integer pedidoId) {
        return reclamoRepository.findByPedidoId(pedidoId).stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // =========================================================
    // ✅ CREAR RECLAMO (desde DTO)
    // =========================================================

    @Override
    @Transactional
    public ReclamoResponseDTO crearDesdeDTO(ReclamoRequestDTO dto) {
        if (dto.getTipo() == null) {
            throw new RuntimeException("El tipo de reclamo es obligatorio");
        }
        if (dto.getDescripcion() == null || dto.getDescripcion().isBlank()) {
            throw new RuntimeException("La descripción es obligatoria");
        }
        if (dto.getClienteId() == null) {
            throw new RuntimeException("El clienteId es obligatorio");
        }

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException(
                        "Cliente no encontrado con ID: " + dto.getClienteId()));

        Pedido pedido = null;
        if (dto.getPedidoId() != null) {
            pedido = pedidoRepository.findById(dto.getPedidoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Pedido no encontrado con ID: " + dto.getPedidoId()));

            if (!pedido.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("El pedido no pertenece al cliente indicado");
            }

            if (reclamoRepository.existsByPedidoId(dto.getPedidoId())) {
                throw new RuntimeException("Este pedido ya tiene un reclamo registrado");
            }
        }

        Reclamo reclamo = new Reclamo();
        reclamo.setTipo(dto.getTipo());
        reclamo.setDescripcion(dto.getDescripcion());
        reclamo.setEstado(EstadoReclamo.REGISTRADO);
        reclamo.setCliente(cliente);
        reclamo.setPedido(pedido);

        Reclamo saved = reclamoRepository.save(reclamo);

        Integer reclamoId = saved.getId();

        String mensaje = "📢 Nuevo reclamo #" + saved.getId() +
                " (" + saved.getTipo() + ")" +
                (pedido != null ? " - Pedido #" + pedido.getId() : "");

        notificacionHelper.notificarA(
                recipientResolver.idsAdminYVentas(),
                "RECLAMO",
                mensaje,
                reclamoId
        );

        return convertirAResponseDTO(saved);
    }

    // =========================================================
    // ✅ GUARDAR (legacy)
    // =========================================================

    @Override
    @Transactional
    public Reclamo guardar(Reclamo reclamo) {
        if (reclamo.getEstado() == null) {
            reclamo.setEstado(EstadoReclamo.REGISTRADO);
        }
        Reclamo saved = reclamoRepository.save(reclamo);

        Integer reclamoId = saved.getId();

        notificacionHelper.notificarA(
                recipientResolver.idsAdminYVentas(),
                "RECLAMO",
                "📢 Nuevo reclamo #" + saved.getId() +
                        (saved.getTipo() != null ? " (" + saved.getTipo() + ")" : ""),
                reclamoId
        );

        return saved;
    }

    // =========================================================
    // ✅ ACTUALIZAR
    // =========================================================

    @Override
    @Transactional
    public Reclamo actualizar(Reclamo reclamo) {
        if (!reclamoRepository.existsById(reclamo.getId())) {
            throw new RuntimeException("Reclamo no encontrado");
        }
        Reclamo saved = reclamoRepository.save(reclamo);

        Integer reclamoId = saved.getId();

        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "RECLAMO",
                    "✏️ Tu reclamo #" + saved.getId() + " fue actualizado",
                    reclamoId
            );
        }

        return saved;
    }

    // =========================================================
    // ✅ ELIMINAR
    // =========================================================

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        reclamoRepository.deleteById(id);

        if (reclamo.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(reclamo.getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "RECLAMO",
                    "🗑️ Tu reclamo #" + id + " fue eliminado",
                    id
            );
        }
    }

    // =========================================================
    // ✅ CAMBIAR ESTADO
    // =========================================================

    {/**
    @Override
    @Transactional
    public Reclamo cambiarEstado(Integer id, EstadoReclamo nuevoEstado) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        reclamo.setEstado(nuevoEstado);
        Reclamo saved = reclamoRepository.save(reclamo);

        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "RECLAMO",
                    "🔄 Tu reclamo #" + id + " cambió a " + nuevoEstado.name(),
                    id
            );
        }

        return saved;
    }*/}
    @Override
    @Transactional
    public ReclamoResponseDTO cambiarEstadoDTO(Integer id, EstadoReclamo nuevoEstado) {
        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamo no encontrado"));
        reclamo.setEstado(nuevoEstado);
        Reclamo saved = reclamoRepository.save(reclamo);

        // ✅ Notificar al cliente
        if (saved.getCliente() != null) {
            Integer clienteUsuarioId =
                    recipientResolver.clienteUsuarioIdOrNull(saved.getCliente());
            notificacionHelper.notificarA(
                    clienteUsuarioId,
                    "RECLAMO",
                    "🔄 Tu reclamo #" + id + " cambió a " + nuevoEstado.name(),
                    id
            );
        }

        return convertirAResponseDTO(saved);
    }

    // =========================================================
    // ✅ CONVERSIÓN A DTO
    // =========================================================

    private ReclamoResponseDTO convertirAResponseDTO(Reclamo reclamo) {
        ReclamoResponseDTO dto = new ReclamoResponseDTO();
        dto.setId(reclamo.getId());
        dto.setTipo(reclamo.getTipo());
        dto.setDescripcion(reclamo.getDescripcion());
        dto.setEstado(reclamo.getEstado());
        dto.setFecha(reclamo.getFecha());

        if (reclamo.getCliente() != null) {
            dto.setClienteId(reclamo.getCliente().getId());
            dto.setClienteNombre(reclamo.getCliente().getNombre());
            dto.setClienteEmail(reclamo.getCliente().getEmail());
        }

        if (reclamo.getPedido() != null) {
            dto.setPedidoId(reclamo.getPedido().getId());
            dto.setPedidoEstado(reclamo.getPedido().getEstado() != null
                    ? reclamo.getPedido().getEstado().name()
                    : null);
            dto.setPedidoFecha(reclamo.getPedido().getFecha());
        }

        return dto;
    }
}