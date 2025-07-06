package com.camilo.cocinarte.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Banquete implements Serializable {
    private int id_banquete;
    private String nombre;
    private String imagen;
    private int cantidad_personas;
    private String descripcion_preparacion;
    private String tiempo_preparacion;
    private String dificultad;
    private Date fecha_creacion;
    private Date fecha_edicion;
    private boolean editado;
    private int id_administrador;
    private String creador; // Nombre del administrador

    // Relaciones
    private List<BanquetePlatillo> platillos;
    private List<BanqueteIngrediente> ingredientes;

    // ✅ CONSTRUCTORES
    public Banquete() {}

    public Banquete(int id_banquete, String nombre, String imagen, int cantidad_personas,
                    String descripcion_preparacion, String tiempo_preparacion, String dificultad) {
        this.id_banquete = id_banquete;
        this.nombre = nombre;
        this.imagen = imagen;
        this.cantidad_personas = cantidad_personas;
        this.descripcion_preparacion = descripcion_preparacion;
        this.tiempo_preparacion = tiempo_preparacion;
        this.dificultad = dificultad;
    }

    // ✅ GETTERS Y SETTERS
    public int getId_banquete() { return id_banquete; }
    public void setId_banquete(int id_banquete) { this.id_banquete = id_banquete; }

    public int getIdBanquete() { return id_banquete; } // Alias
    public void setIdBanquete(int idBanquete) { this.id_banquete = idBanquete; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getImagenUrl() {
        // Si la imagen ya es una URL completa, devolverla tal como está
        if (imagen != null && (imagen.startsWith("http://") || imagen.startsWith("https://"))) {
            return imagen;
        }
        // Si no, construir la URL base (ajustar según tu configuración)
        return imagen != null ? imagen : null;
    }

    public int getCantidad_personas() { return cantidad_personas; }
    public void setCantidad_personas(int cantidad_personas) { this.cantidad_personas = cantidad_personas; }

    public int getCantidadPersonas() { return cantidad_personas; } // Alias
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidad_personas = cantidadPersonas; }

    public String getDescripcion_preparacion() { return descripcion_preparacion; }
    public void setDescripcion_preparacion(String descripcion_preparacion) { this.descripcion_preparacion = descripcion_preparacion; }

    public String getDescripcionPreparacion() { return descripcion_preparacion; } // Alias
    public void setDescripcionPreparacion(String descripcionPreparacion) { this.descripcion_preparacion = descripcionPreparacion; }

    public String getTiempo_preparacion() { return tiempo_preparacion; }
    public void setTiempo_preparacion(String tiempo_preparacion) { this.tiempo_preparacion = tiempo_preparacion; }

    public String getTiempoPreparacion() { return tiempo_preparacion; } // Alias
    public void setTiempoPreparacion(String tiempoPreparacion) { this.tiempo_preparacion = tiempoPreparacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public Date getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(Date fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public Date getFechaCreacion() { return fecha_creacion; } // Alias
    public void setFechaCreacion(Date fechaCreacion) { this.fecha_creacion = fechaCreacion; }

    public Date getFecha_edicion() { return fecha_edicion; }
    public void setFecha_edicion(Date fecha_edicion) { this.fecha_edicion = fecha_edicion; }

    public boolean isEditado() { return editado; }
    public void setEditado(boolean editado) { this.editado = editado; }

    public int getId_administrador() { return id_administrador; }
    public void setId_administrador(int id_administrador) { this.id_administrador = id_administrador; }

    public String getCreador() { return creador; }
    public void setCreador(String creador) { this.creador = creador; }

    public List<BanquetePlatillo> getPlatillos() { return platillos; }
    public void setPlatillos(List<BanquetePlatillo> platillos) { this.platillos = platillos; }

    public List<BanqueteIngrediente> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<BanqueteIngrediente> ingredientes) { this.ingredientes = ingredientes; }

    // ✅ MÉTODOS AUXILIARES
    public boolean tieneImagen() {
        return imagen != null && !imagen.isEmpty() && !imagen.equals("null");
    }

    public boolean tienePlatillos() {
        return platillos != null && !platillos.isEmpty();
    }

    public boolean tieneIngredientes() {
        return ingredientes != null && !ingredientes.isEmpty();
    }

    public int getCantidadPlatillos() {
        return platillos != null ? platillos.size() : 0;
    }

    public int getCantidadIngredientes() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    @Override
    public String toString() {
        return "Banquete{" +
                "id=" + id_banquete +
                ", nombre='" + nombre + '\'' +
                ", cantidad_personas=" + cantidad_personas +
                ", dificultad='" + dificultad + '\'' +
                ", tiempo='" + tiempo_preparacion + '\'' +
                ", platillos=" + getCantidadPlatillos() +
                ", ingredientes=" + getCantidadIngredientes() +
                '}';
    }
}