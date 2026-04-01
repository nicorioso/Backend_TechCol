package com.techgroup.techcop.model.dto;

public class GoogleClientConfigResponse {

    private final String clientId;

    public GoogleClientConfigResponse(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
