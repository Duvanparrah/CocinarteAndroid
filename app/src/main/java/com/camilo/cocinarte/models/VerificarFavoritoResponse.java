package com.camilo.cocinarte.models;

public class VerificarFavoritoResponse {

    private boolean success;
    private boolean esFavorito;
    private int id_receta;
    private String fecha_agregado;
    private String titulo;
    private String tiempo_desde_agregado;

    public boolean isSuccess() {
        return success;
    }

    public boolean isEsFavorito() {
        return esFavorito;
    }

    public int getId_receta() {
        return id_receta;
    }

    public String getFecha_agregado() {
        return fecha_agregado;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTiempo_desde_agregado() {
        return tiempo_desde_agregado;
    }
}
