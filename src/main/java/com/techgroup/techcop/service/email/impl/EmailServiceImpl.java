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
        <h2>%s</h2>
        <p>%s</p>
        <h1>%s</h1>
        <p>Expira en 10 minutos</p>
    """.formatted(title, message, code);
    }
}
