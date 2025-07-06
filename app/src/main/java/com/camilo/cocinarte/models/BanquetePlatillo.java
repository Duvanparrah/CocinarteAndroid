package com.camilo.cocinarte.models;

import java.io.Serializable;

public class BanquetePlatillo implements Serializable {
    private int id_platillo;
    private int id_banquete;
    private String nombre_platillo;
    private String nombre; // Alias
    private String descripcion;
    private String preparacion; // Alias
    private String imagen;

    // ✅ CONSTRUCTORES
    public BanquetePlatillo() {}

    public BanquetePlatillo(String nombre, String preparacion, String imagen) {
        this.nombre_platillo = nombre;
        this.nombre = nombre;
        this.descripcion = preparacion;
        this.preparacion = preparacion;
        this.imagen = imagen;
    }

    // ✅ GETTERS Y SETTERS
    public int getId_platillo() { return id_platillo; }
    public void setId_platillo(int id_platillo) { this.id_platillo = id_platillo; }

    public int getId_banquete() { return id_banquete; }
    public void setId_banquete(int id_banquete) { this.id_banquete = id_banquete; }

    public String getNombre_platillo() { return nombre_platillo; }
    public void setNombre_platillo(String nombre_platillo) {
        this.nombre_platillo = nombre_platillo;
        this.nombre = nombre_platillo; // Sincronizar alias
    }

    public String getNombre() { return nombre != null ? nombre : nombre_platillo; }
    public void setNombre(String nombre) {
        this.nombre = nombre;
        this.nombre_platillo = nombre; // Sincronizar
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.preparacion = descripcion; // Sincronizar alias
    }

    public String getPreparacion() { return preparacion != null ? preparacion : descripcion; }
    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
        this.descripcion = preparacion; // Sincronizar
    }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

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
        return "BanquetePlatillo{" +
                "id=" + id_platillo +
                ", nombre='" + getNombre() + '\'' +
                ", descripcion='" + getDescripcion() + '\'' +
                ", tieneImagen=" + tieneImagen() +
                '}';
    }
}