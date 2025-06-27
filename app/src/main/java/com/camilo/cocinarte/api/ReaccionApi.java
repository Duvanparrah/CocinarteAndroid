package com.camilo.cocinarte.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ReaccionApi {

    // 🔄 Obtener todas las reacciones (likes y comentarios) de una receta
    @GET("reacciones/receta/{id}")
    Call<ResponseBody> getReaccionesPorReceta(
            @Path("id") int recetaId
    );

    // ❤️ Dar o quitar like a una receta
    @POST("reacciones/receta/{id}/like")
    Call<ResponseBody> toggleLike(
            @Header("Authorization") String token,
            @Path("id") int recetaId
    );

    // 💬 Enviar nuevo comentario
    @POST("reacciones/receta/{id}/comentario")
    @FormUrlEncoded
    Call<ResponseBody> enviarComentario(
            @Header("Authorization") String token,
            @Path("id") int recetaId,
            @Field("contenido") String contenido
    );

    // ✏️ Editar comentario
    @PUT("reacciones/comentario/{id}")
    @FormUrlEncoded
    Call<ResponseBody> editarComentario(
            @Header("Authorization") String token,
            @Path("id") int comentarioId,
            @Field("contenido") String nuevoContenido
    );

    // 🗑️ Eliminar comentario
    @DELETE("reacciones/comentario/{id}")
    Call<ResponseBody> eliminarComentario(
            @Header("Authorization") String token,
            @Path("id") int comentarioId
    );
}
