package com.techgroup.techcop.service.email.impl;

import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.service.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String to, String code, VerificationPurpose purpose) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);

            String subject;
            String htmlContent;

            switch (purpose) {
                case LOGIN -> {
                    subject = "Código de inicio de sesión - TechCop";
                    htmlContent = buildTemplate("Inicio de sesión",
                            "Estás intentando iniciar sesión", code);
                }
                case REGISTER -> {
                    subject = "Verifica tu cuenta - TechCop";
                    htmlContent = buildTemplate("Registro",
                            "Gracias por registrarte", code);
                }
                case CHANGE_PASSWORD -> {
                    subject = "Cambio de contraseña - TechCop";
                    htmlContent = buildTemplate("Seguridad",
                            "Estás cambiando tu contraseña", code);
                }
                default -> throw new RuntimeException("Invalid purpose");
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando el correo", e);
        }
    }

    private String buildTemplate(String title, String message, String code) {
        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: Arial, sans-serif; background-color:#f4f6f8;">
        
        <table width="100%%" cellpadding="0" cellspacing="0" style="padding:20px;">
            <tr>
                <td align="center">
                    
                    <!-- Contenedor -->
                    <table width="500px" style="background:#ffffff; border-radius:10px; padding:30px; box-shadow:0 4px 10px rgba(0,0,0,0.1);">
                        
                        <!-- Header -->
                        <tr>
                            <td align="center" style="padding-bottom:20px;">
                                <h1 style="margin:0; color:#2c3e50;">TechCol</h1>
                                <p style="margin:0; color:#7f8c8d;">Seguridad y confianza</p>
                            </td>
                        </tr>

                        <!-- Título -->
                        <tr>
                            <td>
                                <h2 style="color:#34495e;">%s</h2>
                                <p style="color:#555;">%s</p>
                            </td>
                        </tr>

                        <!-- Código -->
                        <tr>
                            <td align="center" style="padding:20px 0;">
                                <div style="
                                    display:inline-block;
                                    padding:15px 30px;
                                    font-size:28px;
                                    letter-spacing:5px;
                                    font-weight:bold;
                                    color:#ffffff;
                                    background:#3498db;
                                    border-radius:8px;">
                                    %s
                                </div>
                            </td>
                        </tr>

                        <!-- Expiración -->
                        <tr>
                            <td align="center">
                                <p style="color:#e74c3c; font-size:14px;">
                                    ⏳ Este código expira en 5 minutos
                                </p>
                            </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                            <td style="padding-top:20px;">
                                <hr style="border:none; border-top:1px solid #eee;">
                                <p style="font-size:12px; color:#999; text-align:center;">
                                    Si no solicitaste este código, puedes ignorar este mensaje.<br>
                                    © 2026 TechCol. Todos los derechos reservados.
                                </p>
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """.formatted(title, message, code);
    }
}
