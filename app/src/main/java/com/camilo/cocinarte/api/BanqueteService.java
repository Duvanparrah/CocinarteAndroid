package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.models.ApiResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * ✅ SERVICIO API PARA BANQUETES
 * Interface que define todos los endpoints relacionados con banquetes
 */
public interface BanqueteService {

    /**
     * ✅ OBTENER TODOS LOS BANQUETES
     * Endpoint: GET /api/banquetes
     */
    @GET("banquetes")
    Call<List<Banquete>> obtenerTodosBanquetes();

    /**
     * ✅ OBTENER TODOS LOS BANQUETES CON AUTENTICACIÓN
     * Para obtener información de likes y favoritos del usuario
     */
    @GET("banquetes")
    Call<List<Banquete>> obtenerTodosBanquetes(@Header("Authorization") String authToken);

    /**
     * ✅ OBTENER BANQUETE POR ID
     * Endpoint: GET /api/banquetes/{id}
     */
    @GET("banquetes/{id}")
    Call<Banquete> obtenerBanquetePorId(@Path("id") int id);

    /**
     * ✅ OBTENER BANQUETE POR ID CON AUTENTICACIÓN
     * Para obtener información de likes y favoritos del usuario
     */
    @GET("banquetes/{id}")
    Call<Banquete> obtenerBanquetePorId(@Path("id") int id, @Header("Authorization") String authToken);

    /**
     * ✅ BUSCAR BANQUETES POR NOMBRE
     * Endpoint: GET /api/banquetes?search={query}
     */
    @GET("banquetes")
    Call<List<Banquete>> buscarBanquetes(@Query("search") String query);

    /**
     * ✅ BUSCAR BANQUETES CON AUTENTICACIÓN
     */
    @GET("banquetes")
    Call<List<Banquete>> buscarBanquetes(@Query("search") String query, @Header("Authorization") String authToken);

    /**
     * ✅ FILTRAR BANQUETES POR DIFICULTAD
     * Endpoint: GET /api/banquetes?dificultad={dificultad}
     */
    @GET("banquetes")
    Call<List<Banquete>> filtrarPorDificultad(@Query("dificultad") String dificultad);

    /**
     * ✅ FILTRAR BANQUETES POR CANTIDAD DE PERSONAS
     * Endpoint: GET /api/banquetes?min_personas={min}&max_personas={max}
     */
    @GET("banquetes")
    Call<List<Banquete>> filtrarPorPersonas(@Query("min_personas") int minPersonas,
                                            @Query("max_personas") int maxPersonas);

    /**
     * ✅ FILTROS COMBINADOS
     * Endpoint: GET /api/banquetes?dificultad={dificultad}&min_personas={min}&max_personas={max}&search={query}
     */
    @GET("banquetes")
    Call<List<Banquete>> filtrarBanquetes(@Query("search") String query,
                                          @Query("dificultad") String dificultad,
                                          @Query("min_personas") Integer minPersonas,
                                          @Query("max_personas") Integer maxPersonas);

    /**
     * ✅ FILTROS COMBINADOS CON AUTENTICACIÓN
     */
    @GET("banquetes")
    Call<List<Banquete>> filtrarBanquetes(@Query("search") String query,
                                          @Query("dificultad") String dificultad,
                                          @Query("min_personas") Integer minPersonas,
                                          @Query("max_personas") Integer maxPersonas,
                                          @Header("Authorization") String authToken);

    /**
     * ✅ PAGINACIÓN
     * Endpoint: GET /api/banquetes?page={page}&limit={limit}
     */
    @GET("banquetes")
    Call<ApiResponse<List<Banquete>>> obtenerBanquetesPaginados(@Query("page") int page,
                                                                @Query("limit") int limit);

    /**
     * ✅ PAGINACIÓN CON AUTENTICACIÓN
     */
    @GET("banquetes")
    Call<ApiResponse<List<Banquete>>> obtenerBanquetesPaginados(@Query("page") int page,
                                                                @Query("limit") int limit,
                                                                @Header("Authorization") String authToken);

    // ==========================================
    // ENDPOINTS DE INTERACCIÓN (LIKES, FAVORITOS, COMENTARIOS)
    // ==========================================

    /**
     * ✅ OBTENER REACCIONES DE UN BANQUETE
     * Endpoint: GET /api/banquetes/reacciones/{banqueteId}
     */
    @GET("banquetes/reacciones/{banqueteId}")
    Call<ReaccionesBanquete> obtenerReacciones(@Path("banqueteId") int banqueteId);

    /**
     * ✅ OBTENER REACCIONES CON AUTENTICACIÓN
     */
    @GET("banquetes/reacciones/{banqueteId}")
    Call<ReaccionesBanquete> obtenerReacciones(@Path("banqueteId") int banqueteId,
                                               @Header("Authorization") String authToken);

    /**
     * ✅ TOGGLE LIKE
     * Endpoint: POST /api/banquetes/reacciones/{banqueteId}/like
     */
    @POST("banquetes/reacciones/{banqueteId}/like")
    Call<ApiResponse<String>> toggleLike(@Path("banqueteId") int banqueteId,
                                         @Header("Authorization") String authToken);

    /**
     * ✅ OBTENER BANQUETES FAVORITOS DEL USUARIO
     * Endpoint: GET /api/banquetes/favoritos
     */
    @GET("banquetes/favoritos")
    Call<ApiResponse<List<Banquete>>> obtenerBanquetesFavoritos(@Header("Authorization") String authToken);

    /**
     * ✅ VERIFICAR SI UN BANQUETE ES FAVORITO
     * Endpoint: GET /api/banquetes/favoritos/verificar/{banqueteId}
     */
    @GET("banquetes/favoritos/verificar/{banqueteId}")
    Call<FavoritoResponse> verificarFavorito(@Path("banqueteId") int banqueteId,
                                             @Header("Authorization") String authToken);

    /**
     * ✅ AGREGAR A FAVORITOS
     * Endpoint: POST /api/banquetes/favoritos/{banqueteId}
     */
    @POST("banquetes/favoritos/{banqueteId}")
    Call<ApiResponse<String>> agregarAFavoritos(@Path("banqueteId") int banqueteId,
                                                @Header("Authorization") String authToken);

    /**
     * ✅ QUITAR DE FAVORITOS
     * Endpoint: DELETE /api/banquetes/favoritos/{banqueteId}
     */
    @DELETE("banquetes/favoritos/{banqueteId}")
    Call<ApiResponse<String>> quitarDeFavoritos(@Path("banqueteId") int banqueteId,
                                                @Header("Authorization") String authToken);

    // ==========================================
    // CLASES DE RESPUESTA
    // ==========================================

    /**
     * ✅ CLASE PARA RESPUESTAS DE REACCIONES
     */
    class ReaccionesBanquete {
        private int banquete_id;
        private LikesInfo likes;
        private List<ComentarioBanquete> comentarios;
        private int total_comentarios;
        private boolean usuario_autenticado;

        // Getters y setters
        public int getBanquete_id() { return banquete_id; }
        public void setBanquete_id(int banquete_id) { this.banquete_id = banquete_id; }

        public LikesInfo getLikes() { return likes; }
        public void setLikes(LikesInfo likes) { this.likes = likes; }

        public List<ComentarioBanquete> getComentarios() { return comentarios; }
        public void setComentarios(List<ComentarioBanquete> comentarios) { this.comentarios = comentarios; }

        public int getTotal_comentarios() { return total_comentarios; }
        public void setTotal_comentarios(int total_comentarios) { this.total_comentarios = total_comentarios; }

        public boolean isUsuario_autenticado() { return usuario_autenticado; }
        public void setUsuario_autenticado(boolean usuario_autenticado) { this.usuario_autenticado = usuario_autenticado; }

        // ✅ CLASE INTERNA PARA LIKES
        public static class LikesInfo {
            private int total;
            private boolean user_liked;

            public int getTotal() { return total; }
            public void setTotal(int total) { this.total = total; }

            public boolean isUser_liked() { return user_liked; }
            public void setUser_liked(boolean user_liked) { this.user_liked = user_liked; }
        }

        // ✅ CLASE INTERNA PARA COMENTARIOS
        public static class ComentarioBanquete {
            private int id;
            private String contenido;
            private String fecha_creacion;
            private String fecha_edicion;
            private boolean editado;
            private UsuarioComentario usuario;

            public int getId() { return id; }
            public void setId(int id) { this.id = id; }

            public String getContenido() { return contenido; }
            public void setContenido(String contenido) { this.contenido = contenido; }

            public String getFecha_creacion() { return fecha_creacion; }
            public void setFecha_creacion(String fecha_creacion) { this.fecha_creacion = fecha_creacion; }

            public String getFecha_edicion() { return fecha_edicion; }
            public void setFecha_edicion(String fecha_edicion) { this.fecha_edicion = fecha_edicion; }

            public boolean isEditado() { return editado; }
            public void setEditado(boolean editado) { this.editado = editado; }

            public UsuarioComentario getUsuario() { return usuario; }
            public void setUsuario(UsuarioComentario usuario) { this.usuario = usuario; }

            // ✅ CLASE PARA USUARIO DEL COMENTARIO
            public static class UsuarioComentario {
                private int id;
                private String nombre;
                private String foto_perfil;

                public int getId() { return id; }
                public void setId(int id) { this.id = id; }

                public String getNombre() { return nombre; }
                public void setNombre(String nombre) { this.nombre = nombre; }

                public String getFoto_perfil() { return foto_perfil; }
                public void setFoto_perfil(String foto_perfil) { this.foto_perfil = foto_perfil; }
            }
        }
    }

    /**
     * ✅ CLASE PARA RESPUESTA DE FAVORITOS
     */
    class FavoritoResponse {
        private boolean success;
        private boolean esFavorito;
        private int banquete_id;
        private String fecha_agregado;
        private String nombre;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public boolean isEsFavorito() { return esFavorito; }
        public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }

        public int getBanquete_id() { return banquete_id; }
        public void setBanquete_id(int banquete_id) { this.banquete_id = banquete_id; }

        public String getFecha_agregado() { return fecha_agregado; }
        public void setFecha_agregado(String fecha_agregado) { this.fecha_agregado = fecha_agregado; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    /**
     * ✅ CLASE PARA RESPUESTA DE LIKE
     */
    class LikeResponse {
        private boolean success;
        private String mensaje;
        private boolean isLiked;
        private int totalLikes;
        private int banqueteId;
        private int userId;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }

        public boolean isLiked() { return isLiked; }
        public void setLiked(boolean liked) { isLiked = liked; }

        public int getTotalLikes() { return totalLikes; }
        public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }

        public int getBanqueteId() { return banqueteId; }
        public void setBanqueteId(int banqueteId) { this.banqueteId = banqueteId; }

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
    }
}