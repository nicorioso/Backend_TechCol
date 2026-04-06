package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.audit.AuditLogService;
import com.techgroup.techcop.service.auth.impl.AuthenticationServiceImpl;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private AuditLogService auditLogService;

    @Test
    void shouldLoginSuccessfullyAndSendVerificationCode() {
        AuthenticationServiceImpl service = buildService(false);
        Customer customer = buildCustomer();

        when(customerRepository.findByCustomerEmail("user@techcol.com")).thenReturn(Optional.of(customer));
        when(passwordHashingService.isBcryptHash("stored-hash")).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));

        String result = service.login("USER@TECHCOL.COM", "secret123", "EMAIL");

        assertThat(result).isEqualTo("Verification code sent via EMAIL");
        verify(verificationCodeService).generateAndSendCode(
                customer,
                VerificationChannel.EMAIL,
                VerificationPurpose.LOGIN
        );
    }

    @Test
    void shouldRejectLoginWhenCredentialsAreInvalid() {
        AuthenticationServiceImpl service = buildService(false);
        Customer customer = buildCustomer();

        when(customerRepository.findByCustomerEmail("user@techcol.com")).thenReturn(Optional.of(customer));
        when(passwordHashingService.isBcryptHash("stored-hash")).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login("user@techcol.com", "wrong-pass", "EMAIL")
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(401);
        assertThat(exception.getReason()).isEqualTo("Correo o contrasena incorrectos.");
        verify(verificationCodeService, never()).generateAndSendCode(any(), any(), any());
    }

    @Test
    void shouldRejectRefreshWhenTokenIsInvalid() {
        AuthenticationServiceImpl service = buildService(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refreshToken", "expired-token"));

        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.refresh(request)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(401);
        assertThat(exception.getReason()).isEqualTo("Invalid refresh token");
    }

    @Test
    void shouldWriteSecureCookieWhenLogoutIsConfiguredForSecureMode() {
        AuthenticationServiceImpl service = buildService(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.logout(response);

        String setCookieHeader = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("refreshToken=");
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("Path=/");
        assertThat(setCookieHeader).contains("Max-Age=0");
    }

    private AuthenticationServiceImpl buildService(boolean secureCookie) {
        return new AuthenticationServiceImpl(
                customerRepository,
                roleRepository,
                jwtService,
                authenticationManager,
                verificationCodeService,
                restTemplate,
                passwordHashingService,
                auditLogService,
                "google-client-id",
                secureCookie,
                new MockEnvironment()
        );
    }

    private Customer buildCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId(1);
        customer.setCustomerName("User");
        customer.setCustomerLastName("Demo");
        customer.setCustomerEmail("user@techcol.com");
        customer.setCustomerPassword("stored-hash");
        customer.setRole(new Role(1, "ROLE_CLIENTE"));
        return customer;
    }
}
