package com.reydi.tienda.service;

import com.reydi.tienda.model.PasswordResetToken;
import com.reydi.tienda.model.Usuario;
import com.reydi.tienda.repository.PasswordResetTokenRepository;
import com.reydi.tienda.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-minutes}")
    private int expirationMinutes;

    /** ✅ Rate limiting: mínimo de segundos entre solicitudes */
    private static final long RATE_LIMIT_SECONDS = 60;

    /** ✅ Política de contraseña */
    private static final int MIN_PASSWORD_LENGTH = 8;

    // =========================================================
    // ✅ SOLICITAR RECUPERACIÓN (con rate limiting)
    // =========================================================
    @Transactional
    public void solicitarRecuperacion(String correo) {
        Optional<Usuario> optUsuario = usuarioRepository.findByCorreo(correo);

        if (optUsuario.isEmpty()) {
            System.out.println("⚠️ Solicitud para correo no registrado: " + correo);
            return; // No revelamos si existe
        }

        Usuario usuario = optUsuario.get();

        // ✅ RATE LIMITING: evitar que pidan 1000 resets
        Optional<PasswordResetToken> ultimoToken =
                tokenRepository.findTopByUsuarioIdOrderByCreadoEnDesc(usuario.getId());

        if (ultimoToken.isPresent()) {
            LocalDateTime ultimaVez = ultimoToken.get().getCreadoEn();
            long segundos = java.time.Duration.between(ultimaVez, LocalDateTime.now()).getSeconds();

            if (segundos < RATE_LIMIT_SECONDS) {
                long esperar = RATE_LIMIT_SECONDS - segundos;
                System.out.println("🚫 Rate limit: usuario " + usuario.getId() +
                        " debe esperar " + esperar + "s");
                throw new RuntimeException(
                        "Debes esperar " + esperar + " segundos antes de solicitar otro enlace"
                );
            }
        }

        // Invalidar tokens previos no usados
        tokenRepository.invalidarTokensAnteriores(usuario.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(expirationMinutes))
                .usado(false)
                .build();

        tokenRepository.save(resetToken);
        System.out.println("🔑 Token generado para usuario " + usuario.getId());

        emailService.enviarEmailRecuperacion(usuario.getCorreo(), token);
    }

    // =========================================================
    // ✅ RESTABLECER CONTRASEÑA (con validación fuerte y confirmación)
    // =========================================================
    @Transactional
    public void restablecerPassword(String token, String nuevaPassword) {
        // ✅ VALIDACIÓN FUERTE DE CONTRASEÑA
        validarPassword(nuevaPassword);

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            throw new RuntimeException("Este enlace ya fue utilizado");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("El enlace ha expirado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        System.out.println("✅ Contraseña actualizada para usuario " + usuario.getId());

        // ✅ NOTIFICAR AL USUARIO QUE SU CONTRASEÑA CAMBIÓ
        emailService.enviarEmailPasswordCambiada(usuario.getCorreo());
    }

    // =========================================================
    // ✅ VALIDACIÓN DE CONTRASEÑA FUERTE
    // =========================================================
    private void validarPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new RuntimeException(
                    "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres"
            );
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException("La contraseña debe tener al menos una mayúscula");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException("La contraseña debe tener al menos una minúscula");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException("La contraseña debe tener al menos un número");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            throw new RuntimeException("La contraseña debe tener al menos un símbolo (!@#$%...)");
        }
    }

    // =========================================================
    // ✅ LIMPIEZA AUTOMÁTICA DE TOKENS EXPIRADOS
    //    Se ejecuta cada 1 hora (3600000 ms)
    // =========================================================
    @Scheduled(fixedRate = 3600000L)
    @Transactional
    public void limpiarTokensExpirados() {
        int eliminados = tokenRepository.eliminarTokensExpirados(LocalDateTime.now());
        if (eliminados > 0) {
            System.out.println("🧹 Tokens expirados eliminados: " + eliminados);
        }
    }
}