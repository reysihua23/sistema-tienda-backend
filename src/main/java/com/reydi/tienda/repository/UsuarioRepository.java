package com.reydi.tienda.repository;

import com.reydi.tienda.model.TipoRol;
import com.reydi.tienda.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // para recuperar contraseña
    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByActivo(Boolean activo);

    List<Usuario> findByRolId(Integer rolId);

    boolean existsByCorreo(String correo);

    @Query("SELECT u FROM Usuario u WHERE u.cliente.id = :clienteId")
    Optional<Usuario> findByClienteId(@Param("clienteId") Integer clienteId);
    // dentro de la interfaz:
    @Query("SELECT u FROM Usuario u WHERE u.rol.nombre = :rol")
    List<Usuario> findByRolNombre(@Param("rol") TipoRol rol);

    @Query("SELECT u FROM Usuario u WHERE " +
            "(:correo IS NULL OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :correo, '%'))) AND " +
            "(:rolId IS NULL OR u.rol.id = :rolId) AND " +
            "(:activo IS NULL OR u.activo = :activo) AND " +
            "(:fechaInicio IS NULL OR u.fechaCreacion >= :fechaInicio) AND " +
            "(:fechaFin IS NULL OR u.fechaCreacion <= :fechaFin)")
    List<Usuario> filtrarUsuarios(
            @Param("correo") String correo,
            @Param("rolId") Integer rolId,
            @Param("activo") Boolean activo,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}