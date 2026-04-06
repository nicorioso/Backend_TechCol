package com.techgroup.techcop.service.sms.smsImpl;

import com.techgroup.techcop.service.sms.SmsService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

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
            throw new IllegalStateException("Twilio is not configured");
        }

        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                messageText
        ).create();

        System.out.println("SMS enviado, SID: " + message.getSid());
    }
}
