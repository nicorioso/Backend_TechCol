package com.techgroup.techcop.service.customer;

import com.techgroup.techcop.model.dto.CustomerProfileUpdateRequest;
import com.techgroup.techcop.model.dto.CustomerResponse;
import com.techgroup.techcop.model.dto.CustomerRoleUpdateRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.access.ResourceAuthorizationService;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import com.techgroup.techcop.service.customer.impl.CustomerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ResourceAuthorizationService resourceAuthorizationService;

    @Mock
    private PasswordHashingService passwordHashingService;

    @Mock
    private AuthIdentityService authIdentityService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void shouldUpdateCustomerProfile() {
        Customer existing = buildCustomer(1, "old@techcol.com");
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest();
        request.setCustomerName(" Ana ");
        request.setCustomerLastName(" Lopez ");
        request.setCustomerEmail(" ANA@TECHCOL.COM ");
        request.setCustomerPhoneNumber(" 3001112233 ");

        doNothing().when(resourceAuthorizationService).assertCurrentCustomer(1);
        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findByCustomerEmail("ana@techcol.com")).thenReturn(Optional.empty());
        when(authIdentityService.normalizePhone("3001112233")).thenReturn("+573001112233");
        when(authIdentityService.findCustomerByIdentifier("+573001112233", VerificationChannel.SMS))
                .thenReturn(Optional.empty());
        when(passwordHashingService.encodeIfNeeded("legacy-password")).thenReturn("encoded-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.updateCustomer(1, request);

        assertThat(response.getCustomerName()).isEqualTo("Ana");
        assertThat(response.getCustomerLastName()).isEqualTo("Lopez");
        assertThat(response.getCustomerEmail()).isEqualTo("ana@techcol.com");
        assertThat(response.getCustomerPhoneNumber()).isEqualTo("+573001112233");
        assertThat(existing.getCustomerPassword()).isEqualTo("encoded-password");
        verify(customerRepository).save(existing);
    }

    @Test
    void shouldRejectDuplicatePhoneWhenUpdatingCustomer() {
        Customer existing = buildCustomer(1, "old@techcol.com");
        Customer other = buildCustomer(2, "other@techcol.com");
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest();
        request.setCustomerEmail("old@techcol.com");
        request.setCustomerPhoneNumber("3001112233");

        doNothing().when(resourceAuthorizationService).assertCurrentCustomer(1);
        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findByCustomerEmail("old@techcol.com")).thenReturn(Optional.of(existing));
        when(authIdentityService.normalizePhone("3001112233")).thenReturn("+573001112233");
        when(authIdentityService.findCustomerByIdentifier("+573001112233", VerificationChannel.SMS))
                .thenReturn(Optional.of(other));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.updateCustomer(1, request)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Phone is already in use");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldRejectDuplicateEmailWhenUpdatingCustomer() {
        Customer existing = buildCustomer(1, "old@techcol.com");
        Customer other = buildCustomer(2, "taken@techcol.com");
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest();
        request.setCustomerEmail("taken@techcol.com");

        doNothing().when(resourceAuthorizationService).assertCurrentCustomer(1);
        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(customerRepository.findByCustomerEmail("taken@techcol.com")).thenReturn(Optional.of(other));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.updateCustomer(1, request)
        );

        assertThat(exception.getStatusCode().value()).isEqualTo(400);
        assertThat(exception.getReason()).isEqualTo("Email is already in use");
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void shouldUpdateRoleForAdminUsers() {
        Customer existing = buildCustomer(1, "user@techcol.com");
        Role adminRole = new Role(2, "ROLE_ADMIN");
        CustomerRoleUpdateRequest request = new CustomerRoleUpdateRequest();
        request.setRole("admin");

        when(resourceAuthorizationService.isAdmin()).thenReturn(true);
        when(customerRepository.findById(1)).thenReturn(Optional.of(existing));
        when(roleRepository.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordHashingService.encodeIfNeeded("legacy-password")).thenReturn("encoded-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.updateRole(1, request);

        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(existing.getRole().getRoleName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void shouldRejectRoleChangesWhenUserIsNotAdmin() {
        CustomerRoleUpdateRequest request = new CustomerRoleUpdateRequest();
        request.setRole("admin");

        when(resourceAuthorizationService.isAdmin()).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> customerService.updateRole(1, request));
    }

    private Customer buildCustomer(Integer id, String email) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerName("Cliente");
        customer.setCustomerLastName("Demo");
        customer.setCustomerEmail(email);
        customer.setCustomerPhoneNumber("+573001112233");
        customer.setCustomerPassword("legacy-password");
        customer.setRole(new Role(1, "ROLE_CLIENTE"));
        return customer;
    }
}
