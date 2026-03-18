package com.techgroup.techcop.service.auth;

public interface ChangePasswordService {
    String changePasswordAuthenticate(String email, String password);
    String changePasswordVerifiCode(String email, String code);
    String changePassword(String email, String newPassword);
}