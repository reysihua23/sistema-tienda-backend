package com.reydi.tienda.service.impl;

import com.reydi.tienda.dto.ClienteDTO;
import com.reydi.tienda.model.Cliente;
import com.reydi.tienda.repository.ClienteRepository;
import com.reydi.tienda.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public List<Cliente> listarTodos() {
        // Obtener todos los clientes
        List<Cliente> clientes = clienteRepository.findAll();

        // Convertir cada cliente a DTO y luego de vuelta a Cliente (sin pedidos)
        return clientes.stream()
                .map(this::convertirADTO)
                .map(this::convertirAEntidad)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        return clienteRepository.findById(id)
                .map(this::convertirADTO)
                .map(this::convertirAEntidad);
    }

    // ClienteServiceImpl.java
    @Override
    public Optional<Cliente> buscarPorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento);
    }
    @Override
    public Cliente guardar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente actualizar(Cliente cliente) {
        if (!clienteRepository.existsById(cliente.getId())) {
            throw new RuntimeException("Cliente no encontrado");
        }
        return clienteRepository.save(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .map(this::convertirADTO)
                .map(this::convertirAEntidad);
    }

    @Override
    public void eliminar(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        clienteRepository.deleteById(id);
    }
    // Agregar estos métodos al final de la clase

    @Override
    public boolean existePorEmail(String email) {
        return clienteRepository.findByEmail(email).isPresent();
    }

    @Override
    public boolean existePorDocumento(String documento) {
        return clienteRepository.findByDocumento(documento).isPresent();
    }

    @Override
    public boolean existePorEmailYIdDistinto(String email, Integer id) {
        return clienteRepository.findByEmail(email)
                .map(cliente -> !cliente.getId().equals(id))
                .orElse(false);
    }

    @Override
    public boolean existePorDocumentoYIdDistinto(String documento, Integer id) {
        return clienteRepository.findByDocumento(documento)
                .map(cliente -> !cliente.getId().equals(id))
                .orElse(false);
    }

    // Convertir Entidad a DTO
    private ClienteDTO convertirADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDocumento(cliente.getDocumento());
        dto.setDireccion(cliente.getDireccion());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setUpdatedAt(cliente.getUpdatedAt());
        // No incluimos la lista de pedidos
        return dto;
    }

    // Convertir de DTO a Entidad (crea una nueva entidad solo con los datos básicos)
    private Cliente convertirAEntidad(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDocumento(dto.getDocumento());
        cliente.setDireccion(dto.getDireccion());
        cliente.setFechaRegistro(dto.getFechaRegistro());
        cliente.setUpdatedAt(dto.getUpdatedAt());
        // No asignamos la lista de pedidos (será null)
        return cliente;
    }
}