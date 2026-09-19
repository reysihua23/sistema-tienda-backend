package com.reydi.tienda.repository;

import com.reydi.tienda.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.usuario.id = :usuarioId AND t.usado = false")
    void invalidarTokensAnteriores(@Param("usuarioId") Integer usuarioId);

    /**
     * ✅ NUEVO: Último token creado para un usuario.
     * Sirve para rate limiting.
     */
    Optional<PasswordResetToken> findTopByUsuarioIdOrderByCreadoEnDesc(Integer usuarioId);

    /**
     * ✅ NUEVO: Borra todos los tokens que ya expiraron.
     * Se usa desde un @Scheduled.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiraEn < :ahora")
    int eliminarTokensExpirados(@Param("ahora") LocalDateTime ahora);
}