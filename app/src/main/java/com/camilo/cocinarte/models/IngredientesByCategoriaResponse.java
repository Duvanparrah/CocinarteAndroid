package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IngredientesByCategoriaResponse {

    @SerializedName("ingredientes")
    private List<Ingrediente> ingredientes;

    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }
}

