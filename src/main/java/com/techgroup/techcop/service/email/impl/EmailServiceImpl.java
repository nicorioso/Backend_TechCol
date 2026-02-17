package com.techgroup.techcop.service.email.impl;

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
    public void sendVerificationCode(String to, String code) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Verificación de cuenta - TechCop");

            String htmlContent = buildEmailTemplate(code);

            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando el correo", e);
        }
    }

    private String buildEmailTemplate(String code) {

        return """
                <div style="font-family: Arial, sans-serif; 
                            background-color: #f4f6f9; 
                            padding: 40px;">
                            
                    <div style="max-width: 500px; 
                                margin: auto; 
                                background: white; 
                                padding: 30px; 
                                border-radius: 10px;
                                box-shadow: 0 5px 15px rgba(0,0,0,0.1);">
                                
                        <h2 style="color: #2c3e50; text-align: center;">
                            Verificación de Cuenta
                        </h2>
                        
                        <p style="color: #555; font-size: 15px;">
                            Hola 👋,
                        </p>
                        
                        <p style="color: #555; font-size: 15px;">
                            Gracias por registrarte en <b>TechCop</b>. 
                            Usa el siguiente código para verificar tu cuenta:
                        </p>
                        
                        <div style="text-align: center; margin: 30px 0;">
                            <span style="display: inline-block;
                                         padding: 15px 30px;
                                         font-size: 22px;
                                         letter-spacing: 4px;
                                         font-weight: bold;
                                         color: white;
                                         background-color: #007bff;
                                         border-radius: 8px;">
                                """ + code + """
                            </span>
                        </div>
                        
                        <p style="color: #777; font-size: 13px;">
                            Este código expirará en 10 minutos.
                        </p>
                        
                        <hr style="margin: 30px 0;">
                        
                        <p style="font-size: 12px; color: #999; text-align: center;">
                            © 2026 TechCop - Todos los derechos reservados
                        </p>
                        
                    </div>
                </div>
                """;
    }
}
