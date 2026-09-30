package com.reydi.tienda.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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

    // =========================================================
// ✅ NUEVO: Enviar comprobante por correo
// =========================================================
    public void enviarComprobante(
            String destinatario,
            String clienteNombre,
            String numeroComprobante,
            String fecha,
            String total,
            List<Map<String, Object>> productos,   // [{nombre, cantidad, precio, subtotal}, ...]
            String metodoPago,
            byte[] pdfAdjunto                       // opcional: si no tienes PDF, pásalo como null
    )

    {
        String asunto = "Tu comprobante Nº " + numeroComprobante + " - JIMENEZ";

        String html = construirHtmlComprobante(
                clienteNombre,
                numeroComprobante,
                fecha,
                total,
                productos,
                metodoPago
        );

        boolean modoSimulado = "simulado".equalsIgnoreCase(emailMode) || mailSender == null;

        if (modoSimulado) {
            System.out.println("════════════════════════════════════════════════════════");
            System.out.println("📧 COMPROBANTE (MODO SIMULADO)");
            System.out.println("Para: " + destinatario);
            System.out.println("Comprobante: " + numeroComprobante);
            System.out.println("Total: " + total);
            System.out.println("════════════════════════════════════════════════════════");
            return;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,       // multipart (para adjunto)
                    "UTF-8"
            );

            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            // Si hay PDF adjunto
            if (pdfAdjunto != null && pdfAdjunto.length > 0) {
                helper.addAttachment(
                        "comprobante-" + numeroComprobante + ".pdf",
                        new org.springframework.core.io.ByteArrayResource(pdfAdjunto)
                );
            }

            mailSender.send(mensaje);
            System.out.println("📧 Comprobante enviado a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar comprobante: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo enviar el comprobante por correo");
        }
    }

    // =========================================================
// ✅ Plantilla HTML del correo de comprobante
// =========================================================
    private String construirHtmlComprobante(
            String clienteNombre,
            String numeroComprobante,
            String fecha,
            String total,
            List<Map<String, Object>> productos,
            String metodoPago
    ) {
        StringBuilder filasProductos = new StringBuilder();
        for (Map<String, Object> p : productos) {
            filasProductos.append("""
            <tr>
                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #333;">%s</td>
                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #333; text-align: center;">%s</td>
                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #333; text-align: right;">%s</td>
                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #333; text-align: right; font-weight: bold;">%s</td>
            </tr>
            """.formatted(
                    p.get("nombre"),
                    p.get("cantidad"),
                    p.get("precio"),
                    p.get("subtotal")
            ));
        }

        return """
        <div style="font-family: Arial, sans-serif; max-width: 640px; margin: auto;
                    padding: 24px; background: #f9f9f9; border-radius: 12px;">

            <!-- Header con logo -->
            <div style="text-align: center; margin-bottom: 24px;">
                <h1 style="font-size: 32px; font-weight: 900; font-style: italic;
                           color: #0d0c1e; margin: 0;">
                    Jimenez<span style="color: #5b4eff;">.</span>
                </h1>
                <p style="color: #888; font-size: 12px; margin: 4px 0;">Tu tienda de confianza</p>
            </div>

            <!-- Título -->
            <h2 style="color: #5b4eff; margin-bottom: 8px;">¡Gracias por tu compra, %s!</h2>
            <p style="color: #333;">Adjunto encontrarás tu comprobante electrónico.</p>

            <!-- Datos del comprobante -->
            <div style="background: white; border-radius: 8px; padding: 16px; margin: 20px 0;">
                <table style="width: 100%%; font-size: 14px;">
                    <tr>
                        <td style="color: #888; padding: 4px 0;">Comprobante:</td>
                        <td style="color: #333; font-weight: bold; text-align: right;">Nº %s</td>
                    </tr>
                    <tr>
                        <td style="color: #888; padding: 4px 0;">Fecha:</td>
                        <td style="color: #333; text-align: right;">%s</td>
                    </tr>
                    <tr>
                        <td style="color: #888; padding: 4px 0;">Método de pago:</td>
                        <td style="color: #333; text-align: right;">%s</td>
                    </tr>
                </table>
            </div>

            <!-- Detalle de productos -->
            <div style="background: white; border-radius: 8px; padding: 16px; margin: 20px 0;">
                <h3 style="color: #0d0c1e; margin: 0 0 12px 0; font-size: 16px;">Detalle</h3>
                <table style="width: 100%%; font-size: 13px; border-collapse: collapse;">
                    <thead>
                        <tr style="background: #f4f7fe;">
                            <th style="padding: 8px; text-align: left; color: #5b4eff; font-size: 11px; text-transform: uppercase;">Producto</th>
                            <th style="padding: 8px; text-align: center; color: #5b4eff; font-size: 11px; text-transform: uppercase;">Cant.</th>
                            <th style="padding: 8px; text-align: right; color: #5b4eff; font-size: 11px; text-transform: uppercase;">Precio</th>
                            <th style="padding: 8px; text-align: right; color: #5b4eff; font-size: 11px; text-transform: uppercase;">Subtotal</th>
                        </tr>
                    </thead>
                    <tbody>
                        %s
                    </tbody>
                </table>
            </div>

            <!-- Total -->
            <div style="background: #5b4eff; color: white; border-radius: 8px; padding: 16px; margin: 20px 0;
                        display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 14px; opacity: 0.9;">Total pagado:</span>
                <span style="font-size: 24px; font-weight: 900;">%s</span>
            </div>

            <!-- Footer -->
            <p style="color: #666; font-size: 13px; text-align: center; margin-top: 24px;">
                Si tienes alguna consulta, contáctanos al <b>997 863 112</b><br>
                o escríbenos a <a href="mailto:soporte@jimenez.com" style="color: #5b4eff;">soporte@jimenez.com</a>
            </p>

            <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />

            <p style="color: #999; font-size: 11px; text-align: center; margin: 0;">
                © %d JIMENEZ. Todos los derechos reservados.<br>
                RUC: 20601234567 · Amazonas, Písac 08106 - Cusco - Perú
            </p>
        </div>
        """.formatted(
                clienteNombre,
                numeroComprobante,
                fecha,
                metodoPago,
                filasProductos.toString(),
                total,
                java.time.Year.now().getValue()
        );
    }

    /**
     * ✅ NUEVO: Envía el comprobante de forma asíncrona (no bloquea la respuesta).
     * Se llama automáticamente después de generar un comprobante.
     */
    @Async
    public void enviarComprobanteAutomatico(
            String destinatario,
            String clienteNombre,
            String numeroComprobante,
            String fecha,
            String total,
            List<Map<String, Object>> productos,
            String metodoPago,
            byte[] pdfAdjunto
    ) {
        try {
            System.out.println("📧 Iniciando envío automático de comprobante a " + destinatario);
            enviarComprobante(
                    destinatario,
                    clienteNombre,
                    numeroComprobante,
                    fecha,
                    total,
                    productos,
                    metodoPago,
                    pdfAdjunto
            );
            System.out.println("✅ Comprobante enviado automáticamente a " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error en envío automático: " + e.getMessage());
            e.printStackTrace();
            // No relanzamos la excepción para no afectar el flujo de la venta
        }
    }
}