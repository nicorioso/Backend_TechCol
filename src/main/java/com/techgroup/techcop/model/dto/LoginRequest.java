package com.techgroup.techcop.model.dto;

public class LoginRequest {

    private String email;
    private String password;
    private String channel;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password, String channel) {
        this.email = email;
        this.password = password;
        this.channel = channel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

}
