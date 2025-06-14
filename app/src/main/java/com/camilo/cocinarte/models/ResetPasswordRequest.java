package com.camilo.cocinarte.models;

public class ResetPasswordRequest {
    private String email;
    private String password;

    public ResetPasswordRequest(String email, String newPassword) {
        this.email = email;
        this.password = newPassword;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return password;
    }

    public void setNewPassword(String newPassword) {
        this.password = newPassword;
    }
}
