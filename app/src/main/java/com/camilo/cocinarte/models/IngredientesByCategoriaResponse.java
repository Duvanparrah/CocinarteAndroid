package com.camilo.cocinarte.models;

import java.util.List;

public class IngredientesByCategoriaResponse {
    private String mensaje;
    private String categoria;  // ✅ Puede ser null cuando obtienes todos los ingredientes
    private int total;
    private List<Ingrediente> ingredientes;

    // Constructor vacío
    public IngredientesByCategoriaResponse() {}

    // Constructor completo
    public IngredientesByCategoriaResponse(String mensaje, String categoria, int total, List<Ingrediente> ingredientes) {
        this.mensaje = mensaje;
        this.categoria = categoria;
        this.total = total;
        this.ingredientes = ingredientes;
    }

    // Getters
    public String getMensaje() {
        return mensaje;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getTotal() {
        return total;
    }

    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    // Setters
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }

    // ✅ Método de utilidad para verificar si hay ingredientes
    public boolean hasIngredientes() {
        return ingredientes != null && !ingredientes.isEmpty();
    }

    // ✅ Método de utilidad para obtener el tamaño de forma segura
    public int getIngredientesCount() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    @Override
    public String toString() {
        return "IngredientesByCategoriaResponse{" +
                "mensaje='" + mensaje + '\'' +
                ", categoria='" + categoria + '\'' +
                ", total=" + total +
                ", ingredientes=" + (ingredientes != null ? ingredientes.size() : 0) + " items" +
                '}';
    }
}