package com.techgroup.techcop.service.customer;

import com.techgroup.techcop.model.dto.CustomerProfileUpdateRequest;
import com.techgroup.techcop.model.dto.CustomerResponse;
import com.techgroup.techcop.model.dto.CustomerRoleUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    List<CustomerResponse> getCustomer();

    Optional<CustomerResponse> getCustomerById(Integer id);

    CustomerResponse updateCustomer(Integer id, CustomerProfileUpdateRequest customer);

    void deleteCustomer(Integer id);

    CustomerResponse patchCustomer(Integer id, CustomerProfileUpdateRequest customer);

    CustomerResponse updateRole(Integer id, CustomerRoleUpdateRequest request);
}
