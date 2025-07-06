package com.camilo.cocinarte.models;

import java.io.Serializable;

public class BanqueteIngrediente implements Serializable {
    private int id;
    private int id_banquete;
    private int id_ingrediente;
    private String cantidad;

    // Información del ingrediente (desde el JOIN)
    private String nombre_ingrediente;
    private String imagen;
    private String categoria;
    private double calorias_por_100g;
    private double proteinas_por_100g;
    private double carbohidratos_por_100g;
    private double grasas_totales_por_100g;
    private double azucar_por_100g;

    // ✅ CONSTRUCTORES
    public BanqueteIngrediente() {}

    public BanqueteIngrediente(int id_ingrediente, String nombre_ingrediente, String cantidad) {
        this.id_ingrediente = id_ingrediente;
        this.nombre_ingrediente = nombre_ingrediente;
        this.cantidad = cantidad;
    }

    // ✅ GETTERS Y SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getId_banquete() { return id_banquete; }
    public void setId_banquete(int id_banquete) { this.id_banquete = id_banquete; }

    public int getId_ingrediente() { return id_ingrediente; }
    public void setId_ingrediente(int id_ingrediente) { this.id_ingrediente = id_ingrediente; }

    public String getCantidad() { return cantidad != null ? cantidad : "1 unidad"; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    public String getNombre_ingrediente() { return nombre_ingrediente; }
    public void setNombre_ingrediente(String nombre_ingrediente) { this.nombre_ingrediente = nombre_ingrediente; }

    public String getNombreIngrediente() { return nombre_ingrediente; } // Alias
    public void setNombreIngrediente(String nombreIngrediente) { this.nombre_ingrediente = nombreIngrediente; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getCalorias_por_100g() { return calorias_por_100g; }
    public void setCalorias_por_100g(double calorias_por_100g) { this.calorias_por_100g = calorias_por_100g; }

    public double getProteinas_por_100g() { return proteinas_por_100g; }
    public void setProteinas_por_100g(double proteinas_por_100g) { this.proteinas_por_100g = proteinas_por_100g; }

    public double getCarbohidratos_por_100g() { return carbohidratos_por_100g; }
    public void setCarbohidratos_por_100g(double carbohidratos_por_100g) { this.carbohidratos_por_100g = carbohidratos_por_100g; }

    public double getGrasas_totales_por_100g() { return grasas_totales_por_100g; }
    public void setGrasas_totales_por_100g(double grasas_totales_por_100g) { this.grasas_totales_por_100g = grasas_totales_por_100g; }

    public double getAzucar_por_100g() { return azucar_por_100g; }
    public void setAzucar_por_100g(double azucar_por_100g) { this.azucar_por_100g = azucar_por_100g; }

    // ✅ MÉTODOS AUXILIARES
    public boolean tieneImagen() {
        return imagen != null && !imagen.isEmpty() && !imagen.equals("null");
    }

    public String getImagenUrl() {
        if (imagen != null && (imagen.startsWith("http://") || imagen.startsWith("https://"))) {
            return imagen;
        }
        return imagen;
    }

    @Override
    public String toString() {
        return "BanqueteIngrediente{" +
                "id_ingrediente=" + id_ingrediente +
                ", nombre='" + nombre_ingrediente + '\'' +
                ", cantidad='" + cantidad + '\'' +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}