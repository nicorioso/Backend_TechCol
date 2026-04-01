package com.techgroup.techcop.model.dto;

public class PasswordRecoveryResetRequest {

    private String identifier;
    private String channel;
    private String newPassword;

    public PasswordRecoveryResetRequest() {
    }

    public PasswordRecoveryResetRequest(String identifier, String channel, String newPassword) {
        this.identifier = identifier;
        this.channel = channel;
        this.newPassword = newPassword;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
