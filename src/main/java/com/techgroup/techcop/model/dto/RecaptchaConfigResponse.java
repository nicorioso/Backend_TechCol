package com.techgroup.techcop.model.dto;

public class RecaptchaConfigResponse {

    private final String siteKey;

    public RecaptchaConfigResponse(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getSiteKey() {
        return siteKey;
    }
}
