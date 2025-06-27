package com.camilo.cocinarte.models;

import java.util.List;

public class ReaccionesResponse {
    private int receta_id;
    private Likes likes;
    private List<Comentario> comentarios;
    private int total_comentarios;
    private boolean usuario_autenticado;

    // Getters
    public int getReceta_id() { return receta_id; }
    public Likes getLikes() { return likes; }
    public List<Comentario> getComentarios() { return comentarios; }
    public int getTotal_comentarios() { return total_comentarios; }
    public boolean isUsuario_autenticado() { return usuario_autenticado; }

    // Submodelo: Likes
    public static class Likes {
        private int total;
        private boolean user_liked;

        public int getTotal() { return total; }
        public boolean isUser_liked() { return user_liked; }
    }

    // Submodelo: Comentario
    public static class Comentario {
        private int id;
        private String contenido;
        private String fecha_creacion;
        private String fecha_edicion;
        private boolean editado;
        private Usuario usuario;

        public int getId() { return id; }
        public String getContenido() { return contenido; }
        public String getFecha_creacion() { return fecha_creacion; }
        public String getFecha_edicion() { return fecha_edicion; }
        public boolean isEditado() { return editado; }
        public Usuario getUsuario() { return usuario; }
    }

    // Submodelo: Usuario
    public static class Usuario {
        private int id;
        private String nombre;
        private String foto_perfil;

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getFoto_perfil() { return foto_perfil; }
    }
}
