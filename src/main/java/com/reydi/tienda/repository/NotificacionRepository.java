// src/main/java/com/reydi/tienda/repository/NotificacionRepository.java
package com.reydi.tienda.repository;

import com.reydi.tienda.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // =========================================================
    // ✅ MÉTODOS POR USUARIO (LOS QUE DEBES USAR)
    // =========================================================

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId ORDER BY n.fecha DESC")
    List<Notificacion> findByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leido = false ORDER BY n.fecha DESC")
    List<Notificacion> findNoLeidasByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.usuario.id = :usuarioId AND n.leido = false")
    Long countNoLeidasByUsuarioId(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.id = :id AND n.usuario.id = :usuarioId")
    int marcarComoLeida(@Param("id") Integer id, @Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.usuario.id = :usuarioId AND n.leido = false")
    int marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n WHERE n.id = :id AND n.usuario.id = :usuarioId")
    void eliminarPorIdYUsuario(@Param("id") Integer id, @Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n WHERE n.usuario.id = :usuarioId")
    void eliminarTodasPorUsuario(@Param("usuarioId") Integer usuarioId);

    // =========================================================
    // ❌ ELIMINA ESTOS MÉTODOS (CAUSAN EL PROBLEMA)
    // =========================================================
    // List<Notificacion> findAllOrderByFechaDesc();
    // List<Notificacion> findNoLeidas();
    // Long countNoLeidas();
    // int marcarComoLeida(Integer id);
    // int marcarTodasComoLeidas();
    // void eliminarPorId(Integer id);
    // void eliminarTodas();
    // ✅ MÉTODOS ANTIGUOS (los puedes eliminar o mantener si los usas para ADMIN)
    // Si quieres mantenerlos para que el ADMIN vea todas:
    @Query("SELECT n FROM Notificacion n ORDER BY n.fecha DESC")
    List<Notificacion> findAllOrderByFechaDesc();

    @Query("SELECT n FROM Notificacion n WHERE n.leido = false ORDER BY n.fecha DESC")
    List<Notificacion> findNoLeidas();

    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.leido = false")
    Long countNoLeidas();

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.id = :id")
    int marcarComoLeida(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("UPDATE Notificacion n SET n.leido = true WHERE n.leido = false")
    int marcarTodasComoLeidas();

    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n WHERE n.id = :id")
    void eliminarPorId(@Param("id") Integer id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Notificacion n")
    void eliminarTodas();
}