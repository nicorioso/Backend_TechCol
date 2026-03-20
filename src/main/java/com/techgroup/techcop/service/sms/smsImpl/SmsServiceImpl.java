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

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String to, String messageText) {

        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                messageText
        ).create();

        System.out.println("SMS enviado, SID: " + message.getSid());
    }
}