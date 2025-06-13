package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("email")
    private String email;

    // CAMBIO PRINCIPAL: Usar "password" en lugar de "newPassword"
    @SerializedName("password")
    private String password;

    // OPCIONAL: Comentado porque el servidor parece no necesitarlo
    // Si tu servidor SÍ necesita el código, descomenta estas líneas:
    // @SerializedName("resetCode")
    // private String resetCode;

    // Constructor principal (solo email y password)
    public ResetPasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor alternativo si necesitas también el resetCode
    // public ResetPasswordRequest(String email, String resetCode, String password) {
    //     this.email = email;
    //     this.resetCode = resetCode;
    //     this.password = password;
    // }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // public String getResetCode() {
    //     return resetCode;
    // }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // public void setResetCode(String resetCode) {
    //     this.resetCode = resetCode;
    // }

    @Override
    public String toString() {
        return "ResetPasswordRequest{" +
                "email='" + email + '\'' +
                ", password='[HIDDEN]'" +
                // ", resetCode='" + resetCode + '\'' +
                '}';
    }
}