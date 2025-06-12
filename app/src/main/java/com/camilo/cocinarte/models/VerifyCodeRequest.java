package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class VerifyCodeRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("code")
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
