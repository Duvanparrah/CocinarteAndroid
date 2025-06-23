package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RecetaRequest {

    @SerializedName("id_receta")
    private int idReceta;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("id_categoria")
    private int idCategoria;

    @SerializedName("tiempo_preparacion")
    private String tiempoPreparacion;

    @SerializedName("dificultad")
    private String dificultad;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("imagen")
    private String imagen;

    @SerializedName("fecha_creacion")
    private String fechaCreacion;

    @SerializedName("fecha_edicion")
    private String fechaEdicion;

    @SerializedName("seccion")
    private String seccion;

    @SerializedName("editado")
    private Boolean editado;

    @SerializedName("calorias")
    private int calorias;

    @SerializedName("proteinas")
    private int proteinas;

    @SerializedName("carbohidratos")
    private int carbohidratos;

    @SerializedName("grasas")
    private int grasas;

    @SerializedName("azucar")
    private Double azucar;

    @SerializedName("pasos")
    private List<String> pasos;

    @SerializedName("preparacion")
    private String preparacion;

    // Este es el campo que se envía al backend
    @SerializedName("ingredientes")
    private String ingredientesJson;

    // Este es el campo que puedes manipular internamente (lista de IDs)
    private List<Integer> ingredientesIds;

    // ==== Getters y Setters ====

    public int getIdReceta() { return idReceta; }
    public void setIdReceta(int idReceta) { this.idReceta = idReceta; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(String tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaEdicion() { return fechaEdicion; }
    public void setFechaEdicion(String fechaEdicion) { this.fechaEdicion = fechaEdicion; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }

    public Boolean getEditado() { return editado; }
    public void setEditado(Boolean editado) { this.editado = editado; }

    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }

    public int getProteinas() { return proteinas; }
    public void setProteinas(int proteinas) { this.proteinas = proteinas; }

    public int getCarbohidratos() { return carbohidratos; }
    public void setCarbohidratos(int carbohidratos) { this.carbohidratos = carbohidratos; }

    public int getGrasas() { return grasas; }
    public void setGrasas(int grasas) { this.grasas = grasas; }

    public Double getAzucar() { return azucar; }
    public void setAzucar(Double azucar) { this.azucar = azucar; }

    public List<String> getPasos() { return pasos; }
    public void setPasos(List<String> pasos) { this.pasos = pasos; }

    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }

    public String getIngredientes() { return ingredientesJson; }
    public void setIngredientes(String ingredientesJson) { this.ingredientesJson = ingredientesJson; }

    public List<Integer> getIngredientesIds() { return ingredientesIds; }
    public void setIngredientesIds(List<Integer> ingredientesIds) {
        this.ingredientesIds = ingredientesIds;
        // Convertimos automáticamente a JSON string para el backend
        this.ingredientesJson = new com.google.gson.Gson().toJson(ingredientesIds);
    }
}
