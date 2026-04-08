package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.dto.RegisterCustomerRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.auth.impl.RegistrationServiceImpl;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private AuthIdentityService authIdentityService;

    @Test
    void shouldRegisterAndSendSmsVerificationCode() {
        RegistrationServiceImpl service = buildService();
        RegisterCustomerRequest request = new RegisterCustomerRequest();
        request.setCustomerName("Test");
        request.setCustomerLastName("User");
        request.setCustomerEmail("USER@TECHCOL.COM");
        request.setCustomerPassword("Secret123!");
        request.setCustomerPhoneNumber("3001112233");
        request.setChannel("SMS");

        Role role = new Role(1, "ROLE_CLIENTE");

        when(authIdentityService.parseChannel("SMS", VerificationChannel.EMAIL)).thenReturn(VerificationChannel.SMS);
        when(authIdentityService.normalizeEmail("USER@TECHCOL.COM")).thenReturn("user@techcol.com");
        when(authIdentityService.normalizePhone("3001112233")).thenReturn("+573001112233");
        when(customerRepository.existsByCustomerEmail("user@techcol.com")).thenReturn(false);
        when(customerRepository.existsByCustomerPhoneNumber("+573001112233")).thenReturn(false);
        when(passwordHashingService.hashNewPassword("Secret123!")).thenReturn("hashed-password");
        when(roleRepository.findByRoleName("ROLE_CLIENTE")).thenReturn(java.util.Optional.of(role));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = service.registerRequest(request);

        assertThat(result).isEqualTo("Verification code sent");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getCustomerEmail()).isEqualTo("user@techcol.com");
        assertThat(savedCustomer.getCustomerPhoneNumber()).isEqualTo("+573001112233");
        assertThat(savedCustomer.isAccountVerified()).isFalse();
        verify(verificationCodeService).generateAndSendCode(
                eq(savedCustomer),
                eq(VerificationChannel.SMS),
                eq(VerificationPurpose.REGISTER)
        );
    }

    @Test
    void shouldVerifyRegisterCodeByRequestedChannelAndMarkAccountAsVerified() {
        RegistrationServiceImpl service = buildService();
        Customer customer = new Customer();
        customer.setAccountVerified(false);
        customer.setCustomerPhoneNumber("+573001112233");

        when(authIdentityService.parseChannel("SMS", VerificationChannel.EMAIL)).thenReturn(VerificationChannel.SMS);
        when(authIdentityService.getCustomerByIdentifier("+573001112233", VerificationChannel.SMS)).thenReturn(customer);
        when(verificationCodeService.verifyCode(
                customer,
                "123456",
                VerificationChannel.SMS,
                VerificationPurpose.REGISTER
        )).thenReturn(true);

        String result = service.verifyRegister("+573001112233", "SMS", "123456");

        assertThat(result).isEqualTo("Account verified successfully");
        assertThat(customer.isAccountVerified()).isTrue();
        verify(customerRepository).save(customer);
    }

    private RegistrationServiceImpl buildService() {
        return new RegistrationServiceImpl(
                customerRepository,
                passwordHashingService,
                roleRepository,
                verificationCodeService,
                authIdentityService
        );
    }
}
