package com.reydi.tienda.service.impl;

import com.reydi.tienda.model.Rol;
import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.repository.RolRepository;
import com.reydi.tienda.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Override
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    // Buscar por ID
    @Override
    public Optional<Rol> buscarPorId(Integer id) {
        return rolRepository.findById(id);
    }

    // Buscar Rol por nombre
    @Override
    public Optional<Rol> buscarPorNombre(TipoRol nombre) {
        return rolRepository.findByNombre(nombre);
    }


    @Override
    public Rol guardar(Rol rol) {
        return rolRepository.save(rol);
    }

    @Override
    public Rol actualizar(Rol rol) {
        if (!rolRepository.existsById(rol.getId())) {
            throw new RuntimeException("Rol no encontrado");
        }
        return rolRepository.save(rol);
    }

    @Override
    public void eliminar(Integer id) {
        if (!rolRepository.existsById(id)) {
            throw new RuntimeException("Rol no encontrado");
        }
        rolRepository.deleteById(id);
    }
}