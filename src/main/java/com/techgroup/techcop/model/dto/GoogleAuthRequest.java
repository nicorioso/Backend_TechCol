package com.techgroup.techcop.model.dto;

public class GoogleAuthRequest {

    private String credential;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String credential) {
        this.credential = credential;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
