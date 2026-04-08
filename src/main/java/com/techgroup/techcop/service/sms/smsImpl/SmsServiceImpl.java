package com.techgroup.techcop.service.sms.smsImpl;

import com.techgroup.techcop.service.sms.SmsService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromNumber;

    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = !accountSid.isBlank() && !authToken.isBlank() && !fromNumber.isBlank();
        if (!enabled) {
            return;
        }
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String messageText) {
        if (!enabled) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El envio de SMS no esta disponible en este momento."
            );
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    messageText
            ).create();

            log.info("SMS enviado correctamente. sid={}, to={}", message.getSid(), to);
        } catch (ApiException ex) {
            log.warn("Twilio rechazo el envio de SMS hacia {}", to, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    resolveDeliveryMessage(ex),
                    ex
            );
        }
    }

    private String resolveDeliveryMessage(ApiException ex) {
        String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();

        if (message.contains("trial") && message.contains("unverified")) {
            return "No se pudo enviar el codigo por SMS. La cuenta de Twilio esta en modo trial y solo permite numeros verificados.";
        }

        if (message.contains("not a valid phone number") || message.contains("invalid")) {
            return "No se pudo enviar el codigo por SMS porque el numero no es valido para Twilio.";
        }

        return "No se pudo enviar el codigo por SMS en este momento.";
    }
}
