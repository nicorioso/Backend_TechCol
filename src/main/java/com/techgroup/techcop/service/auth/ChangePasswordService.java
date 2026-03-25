package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.security.enums.VerificationPurpose;

public interface ChangePasswordService {
    String changePasswordAuthenticate(String email, String password, String channel, String purpose);
    String changePasswordVerifiCode(String email, String code);
    String changePassword(String email, String newPassword);
}