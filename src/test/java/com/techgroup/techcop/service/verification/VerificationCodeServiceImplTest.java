package com.techgroup.techcop.service.verification;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.VerificationCode;
import com.techgroup.techcop.repository.VerificationCodeRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import com.techgroup.techcop.service.email.EmailService;
import com.techgroup.techcop.service.sms.SmsService;
import com.techgroup.techcop.service.verification.impl.VerificationCodeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceImplTest {

    @Mock
    private VerificationCodeRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private SmsService smsService;

    @Mock
    private AuthIdentityService authIdentityService;

    @Test
    void shouldPersistPurposeAndChannelWhenGeneratingSmsCode() {
        VerificationCodeServiceImpl service = new VerificationCodeServiceImpl(
                repository,
                emailService,
                smsService,
                authIdentityService
        );
        Customer customer = new Customer();
        customer.setCustomerPhoneNumber("3001112233");

        when(repository.findByCustomerAndPurposeAndChannelAndUsedFalse(
                customer,
                VerificationPurpose.LOGIN,
                VerificationChannel.SMS
        )).thenReturn(List.of());
        when(authIdentityService.normalizePhone("3001112233")).thenReturn("+573001112233");

        service.generateAndSendCode(customer, VerificationChannel.SMS, VerificationPurpose.LOGIN);

        ArgumentCaptor<VerificationCode> codeCaptor = ArgumentCaptor.forClass(VerificationCode.class);
        verify(repository).save(codeCaptor.capture());

        VerificationCode savedCode = codeCaptor.getValue();
        assertThat(savedCode.getChannel()).isEqualTo(VerificationChannel.SMS);
        assertThat(savedCode.getPurpose()).isEqualTo(VerificationPurpose.LOGIN);
        assertThat(savedCode.getCustomer()).isEqualTo(customer);
        verify(smsService).sendSms(eq("+573001112233"), contains("inicio de sesion"));
    }

    @Test
    void shouldSurfaceFriendlyMessageWhenSmsDeliveryFails() {
        VerificationCodeServiceImpl service = new VerificationCodeServiceImpl(
                repository,
                emailService,
                smsService,
                authIdentityService
        );
        Customer customer = new Customer();
        customer.setCustomerPhoneNumber("3001112233");

        when(repository.findByCustomerAndPurposeAndChannelAndUsedFalse(
                customer,
                VerificationPurpose.LOGIN,
                VerificationChannel.SMS
        )).thenReturn(List.of());
        when(authIdentityService.normalizePhone("3001112233")).thenReturn("+573001112233");
        doThrow(new ResponseStatusException(
                org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                "No se pudo enviar el codigo por SMS. La cuenta de Twilio esta en modo trial y solo permite numeros verificados."
        )).when(smsService).sendSms(eq("+573001112233"), contains("inicio de sesion"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.generateAndSendCode(customer, VerificationChannel.SMS, VerificationPurpose.LOGIN)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(503);
        assertThat(exception.getReason()).isEqualTo(
                "No se pudo enviar el codigo por SMS. La cuenta de Twilio esta en modo trial y solo permite numeros verificados."
        );
    }

    @Test
    void shouldVerifyCodeUsingMatchingPurposeAndChannel() {
        VerificationCodeServiceImpl service = new VerificationCodeServiceImpl(
                repository,
                emailService,
                smsService,
                authIdentityService
        );
        Customer customer = new Customer();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCustomer(customer);
        verificationCode.setCode("123456");
        verificationCode.setExpirationTime(LocalDateTime.now().plusMinutes(2));
        verificationCode.setUsed(false);

        when(repository.findTopByCustomerAndPurposeAndChannelAndUsedFalseOrderByExpirationTimeDesc(
                customer,
                VerificationPurpose.LOGIN,
                VerificationChannel.SMS
        )).thenReturn(Optional.of(verificationCode));

        boolean valid = service.verifyCode(customer, "123456", VerificationChannel.SMS, VerificationPurpose.LOGIN);

        assertThat(valid).isTrue();
        assertThat(verificationCode.isUsed()).isTrue();
        verify(repository).save(verificationCode);
    }

    @Test
    void shouldNotMixOtpBetweenDifferentPurposes() {
        VerificationCodeServiceImpl service = new VerificationCodeServiceImpl(
                repository,
                emailService,
                smsService,
                authIdentityService
        );
        Customer customer = new Customer();

        when(repository.findTopByCustomerAndPurposeAndChannelAndUsedFalseOrderByExpirationTimeDesc(
                customer,
                VerificationPurpose.CHANGE_PASSWORD,
                VerificationChannel.SMS
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verifyCode(customer, "123456", VerificationChannel.SMS, VerificationPurpose.CHANGE_PASSWORD)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("No active code");
    }
}
