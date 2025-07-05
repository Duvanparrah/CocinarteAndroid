package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class FavoritosResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("favoritos")
    private List<Favorito> favoritos;

    @SerializedName("total")
    private int total;

    @SerializedName("mensaje")
    private String mensaje;

    public boolean isSuccess() {
        return success;
    }

    public List<Favorito> getFavoritos() {
        return favoritos;
    }

    public int getTotal() {
        return total;
    }

    public String getMensaje() {
        return mensaje;
    }

    // -------------------- Clase interna Favorito CORREGIDA --------------------
    public static class Favorito {

        @SerializedName("id")
        private int id;

        @SerializedName("id_favorito")
        private int idFavorito;

        @SerializedName("id_receta")
        private int idReceta;

        @SerializedName("receta_id")
        private int recetaId;

        @SerializedName("usuario_id")
        private int usuarioId;

        @SerializedName("fecha")
        private Date fecha;

        @SerializedName("fecha_agregado")
        private Date fechaAgregado;

        @SerializedName("tiempo_desde_agregado")
        private String tiempoDesdeAgregado;

        @SerializedName("titulo")
        private String titulo;

        @SerializedName("descripcion")
        private String descripcion;

        @SerializedName("imagen_url")
        private String imagenUrl;

        @SerializedName("dificultad")
        private String dificultad;

        @SerializedName("tiempo")
        private String tiempo;

        // ✅ CAMBIADOS DE INT A DOUBLE/FLOAT PARA MANEJAR DECIMALES
        @SerializedName("calorias")
        private double calorias;

        @SerializedName("proteinas")
        private double proteinas;

        @SerializedName("carbohidratos")
        private double carbohidratos;

        @SerializedName("grasas")
        private double grasas;

        @SerializedName("autor")
        private String autor;

        @SerializedName("categoria")
        private String categoria;

        @SerializedName("receta")
        private Receta receta;

        // ✅ GETTERS ACTUALIZADOS PARA USAR DOUBLE
        public int getId() {
            return id;
        }

        public int getIdFavorito() {
            return idFavorito;
        }

        public int getIdReceta() {
            return idReceta;
        }

        public int getRecetaId() {
            return recetaId;
        }

        public int getUsuarioId() {
            return usuarioId;
        }

        public Date getFecha() {
            return fecha;
        }

        public Date getFechaAgregado() {
            return fechaAgregado;
        }

        public String getTiempoDesdeAgregado() {
            return tiempoDesdeAgregado;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getImagenUrl() {
            return imagenUrl;
        }

        public String getDificultad() {
            return dificultad;
        }

        public String getTiempo() {
            return tiempo;
        }

        // ✅ GETTERS CORREGIDOS PARA VALORES NUTRICIONALES
        public double getCalorias() {
            return calorias;
        }

        public double getProteinas() {
            return proteinas;
        }

        public double getCarbohidratos() {
            return carbohidratos;
        }

        public double getGrasas() {
            return grasas;
        }

        public String getAutor() {
            return autor;
        }

        public String getCategoria() {
            return categoria;
        }

        public Receta getReceta() {
            return receta;
        }
    }

    // -------------------- Clase interna Receta CORREGIDA --------------------
    public static class Receta {

        @SerializedName("id_receta")
        private int idReceta;

        @SerializedName("titulo")
        private String titulo;

        @SerializedName("descripcion")
        private String descripcion;

        @SerializedName("imagen")
        private String imagen;

        @SerializedName("tiempo_preparacion")
        private String tiempoPreparacion;

        @SerializedName("dificultad")
        private String dificultad;

        // ✅ CAMBIADO DE INT A DOUBLE
        @SerializedName("calorias")
        private double calorias;

        @SerializedName("autor")
        private String autor;

        // ✅ GETTERS CORREGIDOS
        public int getIdReceta() {
            return idReceta;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getImagen() {
            return imagen;
        }

        public String getTiempoPreparacion() {
            return tiempoPreparacion;
        }

        public String getDificultad() {
            return dificultad;
        }

        public double getCalorias() {
            return calorias;
        }

        public String getAutor() {
            return autor;
        }
    }
}