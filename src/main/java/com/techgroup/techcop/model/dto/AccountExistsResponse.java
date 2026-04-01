package com.techgroup.techcop.model.dto;

public class AccountExistsResponse {

    private final boolean exists;

    public AccountExistsResponse(boolean exists) {
        this.exists = exists;
    }

    public boolean isExists() {
        return exists;
    }
}
