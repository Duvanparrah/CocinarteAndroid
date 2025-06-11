package com.camilo.cocinarte.models;

public class CodeVerificationRequest {
    private String email;
    private String code;

    public CodeVerificationRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }


}
