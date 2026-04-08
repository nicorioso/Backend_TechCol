package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthIdentityServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AuthIdentityService authIdentityService;

    @Test
    void shouldFindLegacySmsCustomerStoredWithoutCountryCode() {
        Customer customer = new Customer();
        customer.setCustomerId(7);
        customer.setCustomerPhoneNumber("3042241681");

        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("+573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("3042241681"))
                .thenReturn(List.of(customer));

        Customer result = authIdentityService.getCustomerByIdentifier("3042241681", VerificationChannel.SMS);

        assertThat(result).isSameAs(customer);
        verify(customerRepository).findAllByCustomerPhoneNumberOrderByCustomerIdAsc("+573042241681");
        verify(customerRepository).findAllByCustomerPhoneNumberOrderByCustomerIdAsc("573042241681");
        verify(customerRepository).findAllByCustomerPhoneNumberOrderByCustomerIdAsc("3042241681");
    }

    @Test
    void shouldReportExistingSmsAccountForLegacyStoredPhone() {
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("+573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("3042241681"))
                .thenReturn(List.of(new Customer()));

        boolean exists = authIdentityService.accountExists("3042241681", VerificationChannel.SMS);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldRejectSmsLookupWhenPhoneBelongsToMultipleAccounts() {
        Customer first = new Customer();
        first.setCustomerId(1);
        first.setCustomerPhoneNumber("3042241681");

        Customer second = new Customer();
        second.setCustomerId(2);
        second.setCustomerPhoneNumber("3042241681");

        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("+573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("573042241681"))
                .thenReturn(List.of());
        when(customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc("3042241681"))
                .thenReturn(List.of(first, second));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authIdentityService.getCustomerByIdentifier("3042241681", VerificationChannel.SMS)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(409);
        assertThat(exception.getReason())
                .isEqualTo("El numero de telefono esta asociado a varias cuentas. Inicia sesion con correo o contacta soporte.");
    }
}
