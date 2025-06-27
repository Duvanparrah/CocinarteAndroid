package com.camilo.cocinarte.models;

public class LikeResponse {

    private String mensaje;
    private boolean isLiked;
    private int totalLikes;
    private int recetaId;
    private int userId;

    // Getters
    public String getMensaje() {
        return mensaje;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public int getRecetaId() {
        return recetaId;
    }

    public int getUserId() {
        return userId;
    }

    // Setters (opcional, si necesitás modificar el objeto)
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void setTotalLikes(int totalLikes) {
        this.totalLikes = totalLikes;
    }

    public void setRecetaId(int recetaId) {
        this.recetaId = recetaId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
