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

    // ✅ GETTERS Y SETTERS BÁSICOS
    public int getId_platillo() { return id_platillo; }
    public void setId_platillo(int id_platillo) { this.id_platillo = id_platillo; }

    public int getId_banquete() { return id_banquete; }
    public void setId_banquete(int id_banquete) { this.id_banquete = id_banquete; }

    public String getNombre_platillo() { return nombre_platillo; }
    public void setNombre_platillo(String nombre_platillo) {
        this.nombre_platillo = nombre_platillo;
        this.nombre = nombre_platillo; // Sincronizar alias
    }

    public String getNombre() {
        // Priorizar nombre_platillo, luego nombre
        if (nombre_platillo != null && !nombre_platillo.trim().isEmpty()) {
            return nombre_platillo;
        }
        return nombre != null ? nombre : "Sin nombre";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
        if (this.nombre_platillo == null || this.nombre_platillo.trim().isEmpty()) {
            this.nombre_platillo = nombre; // Sincronizar si no existe nombre_platillo
        }
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
        this.preparacion = descripcion; // Sincronizar alias
    }

    public String getPreparacion() {
        // Priorizar descripcion, luego preparacion
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            return descripcion;
        }
        return preparacion != null ? preparacion : "";
    }

    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
        if (this.descripcion == null || this.descripcion.trim().isEmpty()) {
            this.descripcion = preparacion; // Sincronizar si no existe descripcion
        }
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

    public boolean tieneDescripcion() {
        String desc = getDescripcion();
        return desc != null && !desc.trim().isEmpty();
    }

    public boolean tieneNombre() {
        String nom = getNombre();
        return nom != null && !nom.trim().isEmpty() && !nom.equals("Sin nombre");
    }

    // ✅ MÉTODOS DE COMPATIBILIDAD (para evitar errores)

    /**
     * Método de compatibilidad - devuelve descripción como preparación
     */
    public String getTiempoPreparacion() {
        // Como no tienes este campo, devolver null o una descripción por defecto
        return null; // o "No especificado"
    }

    /**
     * Método de compatibilidad - devuelve dificultad genérica
     */
    public String getDificultad() {
        // Como no tienes este campo, devolver null o una dificultad por defecto
        return null; // o "Media"
    }

    /**
     * Método de compatibilidad - devuelve categoría genérica
     */
    public String getCategoria() {
        // Como no tienes este campo, devolver null o una categoría por defecto
        return null; // o "Platillo principal"
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