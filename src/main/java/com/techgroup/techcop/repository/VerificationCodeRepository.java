package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.VerificationCode;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Integer> {

    Optional<VerificationCode> findTopByCustomerAndPurposeAndChannelAndUsedFalseOrderByExpirationTimeDesc(
            Customer customer,
            VerificationPurpose purpose,
            VerificationChannel channel
    );

    List<VerificationCode> findByCustomerAndPurposeAndChannelAndUsedFalse(
            Customer customer,
            VerificationPurpose purpose,
            VerificationChannel channel
    );

}
