package com.techgroup.techcop.model.dto;

public class AuthResponse {

    private String accessToken;
    private AuthenticatedUserDto user;

    public AuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public AuthResponse(String accessToken, AuthenticatedUserDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public AuthenticatedUserDto getUser() {
        return user;
    }
}
