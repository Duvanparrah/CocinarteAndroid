package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecetaRequest {
    @SerializedName("id_receta")
    private int idReceta;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("nombre")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("imagen")
    private String imagen;

    @SerializedName("tiempo")
    private String tiempoPreparacion;

    @SerializedName("dificultad")
    private String dificultad;

    @SerializedName("calorias")
    private int calorias;

    @SerializedName("proteinas")
    private int proteinas;

    @SerializedName("carbohidratos")
    private int carbohidratos;

    @SerializedName("grasas")
    private int grasas;

    @SerializedName("azucar")
    private Double azucar; // puede venir null

    @SerializedName("seccion")
    private String seccion;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("fecha_creacion")
    private String fechaCreacion;

    @SerializedName("fecha_edicion")
    private String fechaEdicion;

    @SerializedName("editado")
    private Boolean editado;

    @SerializedName("pasos")
    private List<String> pasos;

    @SerializedName("preparacion")
    private String preparacion;

    @SerializedName("Usuario")
    private Usuario usuario; // Clase que debes crear con los campos del usuario

    /*@SerializedName("ingredientes")
    private List<Ingrediente> ingredientes;*/

    @SerializedName("ingredientes")
    private String ingredientes;


    // Getters y setters...

    /*public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes;
    }*/

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public int getIdReceta() {
        return idReceta;
    }

    public void setIdReceta(int idReceta) {
        this.idReceta = idReceta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return titulo;
    }

    public void setNombre(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(String tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public Integer getCalorias() {
        return calorias;
    }

    public void setCalorias(Integer calorias) {
        this.calorias = calorias;
    }

    public Integer getProteinas() {
        return proteinas;
    }

    public void setProteinas(Integer proteinas) {
        this.proteinas = proteinas;
    }

    public Integer getCarbohidratos() {
        return carbohidratos;
    }

    public void setCarbohidratos(Integer carbohidratos) {
        this.carbohidratos = carbohidratos;
    }

    public Integer getGrasas() {
        return grasas;
    }

    public void setGrasas(Integer grasas) {
        this.grasas = grasas;
    }

    public Double getAzucar() {
        return azucar;
    }

    public void setAzucar(Double azucar) {
        this.azucar = azucar;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    /*public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }*/

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaEdicion() {
        return fechaEdicion;
    }

    public void setFechaEdicion(String fechaEdicion) {
        this.fechaEdicion = fechaEdicion;
    }

    public Boolean getEditado() {
        return editado;
    }

    public void setEditado(Boolean editado) {
        this.editado = editado;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public void setPasos(List<String> pasos) {
        this.pasos = pasos;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPreparacion() {
        return preparacion;
    }

    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
    }
}
