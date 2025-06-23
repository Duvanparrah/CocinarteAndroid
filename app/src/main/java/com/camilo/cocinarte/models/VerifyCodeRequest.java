package com.camilo.cocinarte.models;

public class VerifyCodeRequest {
    private String email;
    private String code;

    public VerifyCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }
}
