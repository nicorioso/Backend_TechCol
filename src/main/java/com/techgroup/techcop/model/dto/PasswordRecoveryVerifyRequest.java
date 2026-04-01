package com.techgroup.techcop.model.dto;

public class PasswordRecoveryVerifyRequest {

    private String identifier;
    private String channel;
    private String code;

    public PasswordRecoveryVerifyRequest() {
    }

    public PasswordRecoveryVerifyRequest(String identifier, String channel, String code) {
        this.identifier = identifier;
        this.channel = channel;
        this.code = code;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
