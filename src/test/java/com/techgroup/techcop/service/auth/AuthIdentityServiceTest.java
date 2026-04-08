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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
        customer.setCustomerPhoneNumber("3042241681");

        when(customerRepository.findByCustomerPhoneNumber("+573042241681")).thenReturn(Optional.empty());
        when(customerRepository.findByCustomerPhoneNumber("573042241681")).thenReturn(Optional.empty());
        when(customerRepository.findByCustomerPhoneNumber("3042241681")).thenReturn(Optional.of(customer));

        Customer result = authIdentityService.getCustomerByIdentifier("3042241681", VerificationChannel.SMS);

        assertThat(result).isSameAs(customer);
        verify(customerRepository).findByCustomerPhoneNumber("+573042241681");
        verify(customerRepository).findByCustomerPhoneNumber("573042241681");
        verify(customerRepository).findByCustomerPhoneNumber("3042241681");
    }

    @Test
    void shouldReportExistingSmsAccountForLegacyStoredPhone() {
        when(customerRepository.findByCustomerPhoneNumber("+573042241681")).thenReturn(Optional.empty());
        when(customerRepository.findByCustomerPhoneNumber("573042241681")).thenReturn(Optional.empty());
        when(customerRepository.findByCustomerPhoneNumber("3042241681"))
                .thenReturn(Optional.of(new Customer()));

        boolean exists = authIdentityService.accountExists("3042241681", VerificationChannel.SMS);

        assertThat(exists).isTrue();
    }
}
