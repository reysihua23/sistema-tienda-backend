package com.reydi.tienda.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    /**
     * JavaMailSender se inyecta solo si hay config SMTP en application.properties.
     * Si no hay config, queda null y usamos el modo simulado.
     */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.email.mode:simulado}")
    private String emailMode;

    /**
     * Correo "from". Si no hay config, usa un valor por defecto.
     * Con Gmail debe coincidir con spring.mail.username.
     */
    @Value("${spring.mail.username:noreply@gmail.com}")
    private String fromEmail;

    /**
     * Envía (o simula) el email con el link de recuperación.
     */
    public void enviarEmailRecuperacion(String destinatario, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        boolean modoSimulado = "simulado".equalsIgnoreCase(emailMode) || mailSender == null;

        if (modoSimulado) {
            simularEnvio(destinatario, resetLink);
        } else {
            enviarReal(destinatario, resetLink);
        }
    }

    // =========================================================
    // ✅ MODO SIMULADO (solo log en consola)
    // =========================================================
    private void simularEnvio(String destinatario, String resetLink) {
        System.out.println("════════════════════════════════════════════════════════");
        System.out.println("📧 EMAIL DE RECUPERACIÓN (MODO SIMULADO)");
        System.out.println("Para: " + destinatario);
        System.out.println("Link: " + resetLink);
        System.out.println("────────────────────────────────────────────────────────");
        System.out.println("Copia el link y ábrelo en el navegador");
        System.out.println("════════════════════════════════════════════════════════");
    }

    // =========================================================
    // ✅ MODO SMTP REAL (Gmail, Mailtrap, SendGrid, etc.)
    // =========================================================
    private void enviarReal(String destinatario, String resetLink) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("Recuperación de contraseña - Tienda");
            helper.setText(construirHtml(destinatario, resetLink), true);
            helper.setFrom(fromEmail);

            mailSender.send(mensaje);
            System.out.println("📧 Email real enviado a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo enviar el email de recuperación");
        }
    }

    // =========================================================
    // ✅ Plantilla HTML del correo
    // =========================================================
    private String construirHtml(String correo, String link) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                        padding: 24px; background: #f9f9f9; border-radius: 12px;">
                <h2 style="color: #5b4eff; margin-bottom: 8px;">Recuperación de contraseña</h2>
                <p style="color: #333;">Hola,</p>
                <p style="color: #333;">
                    Recibimos una solicitud para restablecer la contraseña de tu cuenta
                    <b>%s</b>.
                </p>
                <p style="color: #333;">
                    Haz clic en el siguiente botón para crear una nueva contraseña:
                </p>
                <p style="text-align: center; margin: 30px 0;">
                    <a href="%s"
                       style="background-color: #5b4eff; color: white; padding: 12px 24px;
                              text-decoration: none; border-radius: 8px; font-weight: bold;
                              display: inline-block;">
                        Restablecer contraseña
                    </a>
                </p>
                <p style="color: #333;">O copia este enlace en tu navegador:</p>
                <p style="word-break: break-all; color: #5b4eff;">%s</p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
                <p style="color: #888; font-size: 12px;">
                    Si no solicitaste este cambio, ignora este correo.
                    El enlace expira en 30 minutos.
                </p>
            </div>
            """.formatted(correo, link, link);
    }

    /**
     * ✅ NUEVO: Notifica al usuario que su contraseña cambió.
     * Se envía después de un reset exitoso, por seguridad.
     */
    public void enviarEmailPasswordCambiada(String destinatario) {
        String asunto = "Tu contraseña fue cambiada - Tienda";

        String html = """
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                    padding: 24px; background: #f9f9f9; border-radius: 12px;">
            <h2 style="color: #5b4eff; margin-bottom: 8px;">Contraseña actualizada</h2>
            <p style="color: #333;">Hola,</p>
            <p style="color: #333;">
                Te confirmamos que la contraseña de tu cuenta <b>%s</b> fue cambiada exitosamente.
            </p>
            <p style="color: #333;">
                Si <b>no fuiste tú</b>, por favor contacta con soporte de inmediato
                y cambia tu contraseña otra vez.
            </p>
            <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;" />
            <p style="color: #888; font-size: 12px;">
                Este es un correo automático. No respondas a este mensaje.
            </p>
        </div>
        """.formatted(destinatario);

        boolean modoSimulado = "simulado".equalsIgnoreCase(emailMode) || mailSender == null;

        if (modoSimulado) {
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("📧 EMAIL 'CONTRASEÑA CAMBIADA' (MODO SIMULADO)");
            System.out.println("Para: " + destinatario);
            System.out.println("════════════════════════════════════════════════════════");
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(mensaje);
            System.out.println("📧 Email 'contraseña cambiada' enviado a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de confirmación: " + e.getMessage());
            // No lanzamos excepción: si esto falla, no queremos romper el flujo de reset
        }
    }
}