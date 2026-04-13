package com.reydi.tienda.service;

import com.reydi.tienda.model.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteService {
    List<Cliente> listarTodos();
    Optional<Cliente> buscarPorId(Integer id);
    Cliente guardar(Cliente cliente);
    Cliente actualizar(Cliente cliente);
    Optional<Cliente> buscarPorEmail(String email);
    // ClienteService.java
    Optional<Cliente> buscarPorDocumento(String documento);
    void eliminar(Integer id);

    boolean existePorEmail(String email);
    boolean existePorDocumento(String documento);
    boolean existePorEmailYIdDistinto(String email, Integer id);
    boolean existePorDocumentoYIdDistinto(String documento, Integer id);

}