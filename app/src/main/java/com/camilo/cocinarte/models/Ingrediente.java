package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

public class Ingrediente {
    @SerializedName("id_ingrediente")
    private int idIngrediente;
    @SerializedName("nombre_ingrediente")
    private String nombreIngrediente;
    @SerializedName("imagen")
    private String imagen;
    @SerializedName("categoria")
    private String categoria;
    @SerializedName("calorias_por_100g")
    private double caloriasPor100g;
    @SerializedName("proteinas_por_100g")
    private double proteinasPor100g;
    @SerializedName("carbohidratos_por_100g")
    private double carbohidratosPor100g;
    @SerializedName("grasas_totales_por_100g")
    private double grasasTotalesPor100g;
    @SerializedName("azucar_por_100g")
    private double azucarPor100g;
    @SerializedName("unidad")
    private String unidad;

    // Getters y Setters
    public int getIdIngrediente() { return idIngrediente; }
    public void setIdIngrediente(int idIngrediente) { this.idIngrediente = idIngrediente; }

    public String getNombreIngrediente() { return nombreIngrediente; }
    public void setNombreIngrediente(String nombreIngrediente) { this.nombreIngrediente = nombreIngrediente; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getCaloriasPor100g() { return caloriasPor100g; }
    public void setCaloriasPor100g(double caloriasPor100g) { this.caloriasPor100g = caloriasPor100g; }

    public double getProteinasPor100g() { return proteinasPor100g; }
    public void setProteinasPor100g(double proteinasPor100g) { this.proteinasPor100g = proteinasPor100g; }

    public double getCarbohidratosPor100g() { return carbohidratosPor100g; }
    public void setCarbohidratosPor100g(double carbohidratosPor100g) { this.carbohidratosPor100g = carbohidratosPor100g; }

    public double getGrasasTotalesPor100g() { return grasasTotalesPor100g; }
    public void setGrasasTotalesPor100g(double grasasTotalesPor100g) { this.grasasTotalesPor100g = grasasTotalesPor100g; }

    public double getAzucarPor100g() { return azucarPor100g; }
    public void setAzucarPor100g(double azucarPor100g) { this.azucarPor100g = azucarPor100g; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    @SerializedName("receta_ingredientes")
    private RecetaIngrediente recetaIngredientes;

    // Getters y setters...

    public static class RecetaIngrediente {
        @SerializedName("id_receta")
        private int idReceta;

        @SerializedName("id_ingrediente")
        private int idIngrediente;

        // También puedes incluir createdAt/updatedAt si los necesitas
    }
}
