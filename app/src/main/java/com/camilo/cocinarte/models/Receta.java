package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Receta implements Serializable {

    @SerializedName("id_receta")
    private int idReceta;

    private String titulo;
    private String descripcion;
    private String imagen;

    @SerializedName("tiempo_preparacion")
    private String tiempoPreparacion;

    private String dificultad;
    private String categoria;

    @SerializedName("fecha_creacion")
    private String fechaCreacion;

    @SerializedName("fecha_edicion")
    private String fechaEdicion;

    private boolean editado;

    @SerializedName("id_usuario")
    private int idUsuario;

    @SerializedName("id_categoria")
    private int idCategoria;

    private String seccion;

    // ✅ CAMPOS NUTRICIONALES
    private int calorias;
    private double proteinas;
    private double carbohidratos;
    private double grasas;
    private double azucar;

    // ✅ INFORMACIÓN DEL CREADOR (CLAVE PARA FILTRAR)
    private CreadorInfo creador;

    // ✅ INGREDIENTES
    @SerializedName("Ingredientes")
    private List<Ingrediente> ingredientes;

    // ✅ INFORMACIÓN NUTRICIONAL AGRUPADA
    private NutricionInfo nutricion;

    // Constructores
    public Receta() {}

    public Receta(int idReceta, String titulo, String descripcion, String imagen,
                  String tiempoPreparacion, String dificultad, String categoria) {
        this.idReceta = idReceta;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.categoria = categoria;
    }


    public int getIdReceta() { return idReceta; }
    public void setIdReceta(int idReceta) { this.idReceta = idReceta; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(String tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaEdicion() { return fechaEdicion; }
    public void setFechaEdicion(String fechaEdicion) { this.fechaEdicion = fechaEdicion; }

    public boolean isEditado() { return editado; }
    public void setEditado(boolean editado) { this.editado = editado; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }

    // ✅ GETTERS Y SETTERS NUTRICIONALES
    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }

    public double getProteinas() { return proteinas; }
    public void setProteinas(double proteinas) { this.proteinas = proteinas; }

    public double getCarbohidratos() { return carbohidratos; }
    public void setCarbohidratos(double carbohidratos) { this.carbohidratos = carbohidratos; }

    public double getGrasas() { return grasas; }
    public void setGrasas(double grasas) { this.grasas = grasas; }

    public double getAzucar() { return azucar; }
    public void setAzucar(double azucar) { this.azucar = azucar; }

    // ✅ GETTERS Y SETTERS DEL CREADOR (IMPORTANTE)
    public CreadorInfo getCreador() { return creador; }
    public void setCreador(CreadorInfo creador) { this.creador = creador; }

    // ✅ GETTERS Y SETTERS DE INGREDIENTES
    public List<Ingrediente> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<Ingrediente> ingredientes) { this.ingredientes = ingredientes; }

    // ✅ GETTERS Y SETTERS DE NUTRICIÓN
    public NutricionInfo getNutricion() { return nutricion; }
    public void setNutricion(NutricionInfo nutricion) { this.nutricion = nutricion; }

    // ✅ MÉTODOS DE UTILIDAD

    /**
     * Verifica si la receta fue creada por un usuario regular (no administrador)
     */
    public boolean esDeUsuarioRegular() {
        return creador != null && "usuario".equals(creador.getTipo_usuario());
    }

    /**
     * Verifica si la receta fue creada por un administrador
     */
    public boolean esDeAdministrador() {
        return creador != null &&
                ("administrador".equals(creador.getTipo_usuario()) ||
                        "administrador_lider".equals(creador.getTipo_usuario()));
    }

    /**
     * Obtiene el nombre del creador de forma segura
     */
    public String getNombreCreador() {
        return creador != null ? creador.getNombre_usuario() : "Desconocido";
    }

    /**
     * Obtiene el correo del creador de forma segura
     */
    public String getCorreoCreador() {
        return creador != null ? creador.getCorreo() : "";
    }

    /**
     * Obtiene el tipo de usuario del creador de forma segura
     */
    public String getTipoCreador() {
        return creador != null ? creador.getTipo_usuario() : "usuario";
    }

    // ✅ CLASE INTERNA: INFORMACIÓN DEL CREADOR
    public static class CreadorInfo implements Serializable {
        @SerializedName("id_usuario")
        private int id_usuario;

        @SerializedName("nombre_usuario")
        private String nombre_usuario;

        private String correo;         // ✅ CAMPO CLAVE PARA MOSTRAR

        @SerializedName("tipo_usuario")
        private String tipo_usuario;   // ✅ CAMPO CLAVE PARA FILTRAR

        @SerializedName("foto_perfil")
        private String foto_perfil;

        public CreadorInfo() {}

        public CreadorInfo(int id_usuario, String nombre_usuario, String correo, String tipo_usuario) {
            this.id_usuario = id_usuario;
            this.nombre_usuario = nombre_usuario;
            this.correo = correo;
            this.tipo_usuario = tipo_usuario;
        }

        // Getters y setters
        public int getId_usuario() { return id_usuario; }
        public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }

        public String getNombre_usuario() { return nombre_usuario; }
        public void setNombre_usuario(String nombre_usuario) { this.nombre_usuario = nombre_usuario; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }

        public String getTipo_usuario() { return tipo_usuario; }
        public void setTipo_usuario(String tipo_usuario) { this.tipo_usuario = tipo_usuario; }

        public String getFoto_perfil() { return foto_perfil; }
        public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }

        @Override
        public String toString() {
            return "CreadorInfo{" +
                    "id_usuario=" + id_usuario +
                    ", nombre_usuario='" + nombre_usuario + '\'' +
                    ", correo='" + correo + '\'' +
                    ", tipo_usuario='" + tipo_usuario + '\'' +
                    '}';
        }
    }

    // ✅ CLASE INTERNA: INFORMACIÓN NUTRICIONAL
    public static class NutricionInfo implements Serializable {
        private int calorias;
        private double proteinas;
        private double carbohidratos;
        private double grasas;
        private double azucar;

        public NutricionInfo() {}

        public NutricionInfo(int calorias, double proteinas, double carbohidratos, double grasas, double azucar) {
            this.calorias = calorias;
            this.proteinas = proteinas;
            this.carbohidratos = carbohidratos;
            this.grasas = grasas;
            this.azucar = azucar;
        }

        // Getters y setters
        public int getCalorias() { return calorias; }
        public void setCalorias(int calorias) { this.calorias = calorias; }

        public double getProteinas() { return proteinas; }
        public void setProteinas(double proteinas) { this.proteinas = proteinas; }

        public double getCarbohidratos() { return carbohidratos; }
        public void setCarbohidratos(double carbohidratos) { this.carbohidratos = carbohidratos; }

        public double getGrasas() { return grasas; }
        public void setGrasas(double grasas) { this.grasas = grasas; }

        public double getAzucar() { return azucar; }
        public void setAzucar(double azucar) { this.azucar = azucar; }

        @Override
        public String toString() {
            return "NutricionInfo{" +
                    "calorias=" + calorias +
                    ", proteinas=" + proteinas +
                    ", carbohidratos=" + carbohidratos +
                    ", grasas=" + grasas +
                    ", azucar=" + azucar +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Receta{" +
                "idReceta=" + idReceta +
                ", titulo='" + titulo + '\'' +
                ", creador=" + (creador != null ? creador.toString() : "null") +
                ", esDeUsuarioRegular=" + esDeUsuarioRegular() +
                '}';
    }
}