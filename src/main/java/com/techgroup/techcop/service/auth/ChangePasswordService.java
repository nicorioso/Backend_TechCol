package com.techgroup.techcop.service.auth;

public interface ChangePasswordService {
    String requestAuthenticatedPasswordChange(String channel);
    String verifyAuthenticatedPasswordChange(String channel, String code);
    String confirmAuthenticatedPasswordChange(String newPassword);
    String changePasswordAuthenticate(String email, String password, String channel);
    String changePasswordVerifiCode(String email, String code, String channel);
    String changePassword(String email, String newPassword);
    String requestPasswordRecovery(String identifier, String channel);
    String verifyPasswordRecoveryCode(String identifier, String channel, String code);
    String resetPasswordByRecovery(String identifier, String channel, String newPassword);
}
