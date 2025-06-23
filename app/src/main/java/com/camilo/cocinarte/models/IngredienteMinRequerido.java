package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class IngredienteMinRequerido {
    private int id;
    private String cantidad;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }
}
