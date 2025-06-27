package com.camilo.cocinarte.models;

import java.util.List;

public class Receta {

    private int id_receta;
    private String titulo;
    private int id_categoria;
    private String tiempo_preparacion;
    private String dificultad;
    private String descripcion;
    private String imagen;
    private String fecha_creacion;
    private String fecha_edicion;
    private int id_usuario;
    private int calorias;
    private int proteinas;
    private int carbohidratos;
    private int grasas;
    private int azucar;
    private String creador_nombre;
    private String creador_correo;
    private String tipo_creador;
    private String categoria_nombre;
    private String categoria;
    private List<Ingrediente> Ingredientes;
    private Creador creador;
    private Nutricion nutricion;

    // Submodelo: Ingrediente
    public static class Ingrediente {
        private int id_ingrediente;
        private String nombre_ingrediente;
        private String imagen;
        private String calorias_por_100g;
        private String proteinas_por_100g;
        private String carbohidratos_por_100g;
        private String grasas_totales_por_100g;
        private String azucar_por_100g;
        private String cantidad;

        public int getId_ingrediente() { return id_ingrediente; }
        public void setId_ingrediente(int id_ingrediente) { this.id_ingrediente = id_ingrediente; }

        public String getNombre_ingrediente() { return nombre_ingrediente; }
        public void setNombre_ingrediente(String nombre_ingrediente) { this.nombre_ingrediente = nombre_ingrediente; }

        public String getImagen() { return imagen; }
        public void setImagen(String imagen) { this.imagen = imagen; }

        public String getCalorias_por_100g() { return calorias_por_100g; }
        public void setCalorias_por_100g(String calorias_por_100g) { this.calorias_por_100g = calorias_por_100g; }

        public String getProteinas_por_100g() { return proteinas_por_100g; }
        public void setProteinas_por_100g(String proteinas_por_100g) { this.proteinas_por_100g = proteinas_por_100g; }

        public String getCarbohidratos_por_100g() { return carbohidratos_por_100g; }
        public void setCarbohidratos_por_100g(String carbohidratos_por_100g) { this.carbohidratos_por_100g = carbohidratos_por_100g; }

        public String getGrasas_totales_por_100g() { return grasas_totales_por_100g; }
        public void setGrasas_totales_por_100g(String grasas_totales_por_100g) { this.grasas_totales_por_100g = grasas_totales_por_100g; }

        public String getAzucar_por_100g() { return azucar_por_100g; }
        public void setAzucar_por_100g(String azucar_por_100g) { this.azucar_por_100g = azucar_por_100g; }

        public String getCantidad() { return cantidad; }
        public void setCantidad(String cantidad) { this.cantidad = cantidad; }
    }

    // Submodelo: Creador
    public static class Creador {
        private int id_usuario;
        private String nombre_usuario;
        private String correo;
        private String tipo_usuario;

        public int getId_usuario() { return id_usuario; }
        public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }

        public String getNombre_usuario() { return nombre_usuario; }
        public void setNombre_usuario(String nombre_usuario) { this.nombre_usuario = nombre_usuario; }

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }

        public String getTipo_usuario() { return tipo_usuario; }
        public void setTipo_usuario(String tipo_usuario) { this.tipo_usuario = tipo_usuario; }
    }

    // Submodelo: Nutricion
    public static class Nutricion {
        private int calorias;
        private int proteinas;
        private int carbohidratos;
        private int grasas;
        private int azucar;

        public int getCalorias() { return calorias; }
        public void setCalorias(int calorias) { this.calorias = calorias; }

        public int getProteinas() { return proteinas; }
        public void setProteinas(int proteinas) { this.proteinas = proteinas; }

        public int getCarbohidratos() { return carbohidratos; }
        public void setCarbohidratos(int carbohidratos) { this.carbohidratos = carbohidratos; }

        public int getGrasas() { return grasas; }
        public void setGrasas(int grasas) { this.grasas = grasas; }

        public int getAzucar() { return azucar; }
        public void setAzucar(int azucar) { this.azucar = azucar; }
    }

    // Getters y Setters principales
    public int getId_receta() { return id_receta; }
    public void setId_receta(int id_receta) { this.id_receta = id_receta; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getId_categoria() { return id_categoria; }
    public void setId_categoria(int id_categoria) { this.id_categoria = id_categoria; }

    public String getTiempo_preparacion() { return tiempo_preparacion; }
    public void setTiempo_preparacion(String tiempo_preparacion) { this.tiempo_preparacion = tiempo_preparacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public String getFecha_creacion() { return fecha_creacion; }
    public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }

    public String getFecha_edicion() { return fecha_edicion; }
    public void setFecha_edicion(String fecha_edicion) { this.fecha_edicion = fecha_edicion; }

    public int getId_usuario() { return id_usuario; }
    public void setId_usuario(int id_usuario) { this.id_usuario = id_usuario; }

    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }

    public int getProteinas() { return proteinas; }
    public void setProteinas(int proteinas) { this.proteinas = proteinas; }

    public int getCarbohidratos() { return carbohidratos; }
    public void setCarbohidratos(int carbohidratos) { this.carbohidratos = carbohidratos; }

    public int getGrasas() { return grasas; }
    public void setGrasas(int grasas) { this.grasas = grasas; }

    public int getAzucar() { return azucar; }
    public void setAzucar(int azucar) { this.azucar = azucar; }

    public String getCreador_nombre() { return creador_nombre; }
    public void setCreador_nombre(String creador_nombre) { this.creador_nombre = creador_nombre; }

    public String getCreador_correo() { return creador_correo; }
    public void setCreador_correo(String creador_correo) { this.creador_correo = creador_correo; }

    public String getTipo_creador() { return tipo_creador; }
    public void setTipo_creador(String tipo_creador) { this.tipo_creador = tipo_creador; }

    public String getCategoria_nombre() { return categoria_nombre; }
    public void setCategoria_nombre(String categoria_nombre) { this.categoria_nombre = categoria_nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<Ingrediente> getIngredientes() { return Ingredientes; }
    public void setIngredientes(List<Ingrediente> ingredientes) { Ingredientes = ingredientes; }

    public Creador getCreador() { return creador; }
    public void setCreador(Creador creador) { this.creador = creador; }

    public Nutricion getNutricion() { return nutricion; }
    public void setNutricion(Nutricion nutricion) { this.nutricion = nutricion; }
}
