package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.LikeResponse;
import com.camilo.cocinarte.models.ReaccionesResponse;
import com.camilo.cocinarte.models.Receta;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecetaApi {

    // --- ENDPOINTS PRINCIPALES PARA PANTALLAS ---

    // ✅ NUEVO: Para pantalla de INICIO (recetas de administradores)
    @GET("inicio/")
    Call<List<Receta>> obtenerRecetasInicio();

    // ✅ NUEVO: Solo administradores (alternativo)
    @GET("inicio/admin-only")
    Call<List<Receta>> obtenerRecetasAdministradores();

    // ✅ EXISTENTE MEJORADO: Para pantalla de COMUNIDAD (recetas de usuarios)
    @GET("recetas/usuarios-only")
    Call<List<Receta>> obtenerRecetasUsuarios();

    // ✅ MÉTODO ORIGINAL CON AUTENTICACIÓN OPCIONAL
    @GET("recetas/")
    Call<List<Receta>> obtenerTodasLasRecetas();

    // ✅ MÉTODO CON AUTENTICACIÓN REQUERIDA
    @GET("recetas/")
    Call<List<Receta>> obtenerTodasLasRecetas(@Header("Authorization") String token);

    // --- ENDPOINTS ANTERIORES (COMPATIBILIDAD) ---

    // ✅ ENDPOINT ORIGINAL: Todas las recetas (incluye administradores)
    @GET("recetas")
    Call<List<Receta>> getRecetas(@Header("Authorization") String token);

    // ✅ NUEVO ENDPOINT PRINCIPAL: Solo recetas de usuarios regulares (NO administradores)
    @GET("recetas/usuarios-only")
    Call<List<Receta>> getRecetasUsuarios(@Header("Authorization") String token);

    // ✅ ENDPOINT ESPECÍFICO: Mis recetas (del usuario autenticado)
    @GET("recetas/usuario")
    Call<List<Receta>> getMisRecetas(@Header("Authorization") String token);

    @GET("recetas/{id}")
    Call<Receta> getRecetaById(@Path("id") int idReceta, @Header("Authorization") String token);

    // ✅ BÚSQUEDA DE RECETAS
    @GET("recetas/buscar")
    Call<List<Receta>> buscarRecetas(
            @Query("q") String query,
            @Header("Authorization") String token
    );

    // ✅ CREAR RECETA - CORREGIDO PARA TU BACKEND
    @Multipart
    @POST("recetas")
    Call<Receta> createReceta(
            @Part MultipartBody.Part foto,                    // imagen
            @Part("nombre") RequestBody titulo,               // título de la receta
            @Part("id_categoria") RequestBody idCategoria,    // ID de categoría
            @Part("seccion") RequestBody seccion,             // "comunidad"
            @Part("ingredientes") RequestBody ingredientes,   // JSON array de IDs
            @Part("preparacion") RequestBody preparacion,     // pasos unidos con \n
            @Part("tiempo_preparacion") RequestBody tiempoPreparacion,
            @Part("dificultad") RequestBody dificultad,
            @Part("descripcion") RequestBody descripcion,     // los pasos también van aquí
            @Header("Authorization") String token
    );

    // ✅ EDITAR RECETA
    @Multipart
    @POST("recetas/{id}")  // Algunos backends usan POST en lugar de PUT para multipart
    Call<Receta> editarReceta(
            @Path("id") int recetaId,
            @Part MultipartBody.Part foto,
            @Part("nombre") RequestBody titulo,
            @Part("id_categoria") RequestBody idCategoria,
            @Part("ingredientes") RequestBody ingredientes,
            @Part("descripcion") RequestBody descripcion,
            @Part("tiempo_preparacion") RequestBody tiempoPreparacion,
            @Part("dificultad") RequestBody dificultad,
            @Header("Authorization") String token
    );

    @DELETE("recetas/{id}")
    Call<Void> deleteReceta(@Path("id") int recetaId, @Header("Authorization") String token);

    // --- Imagen receta independiente ---

    @Multipart
    @POST("recetas/upload/image")
    Call<FotoResponse> subirFotoReceta(
            @Part MultipartBody.Part image,
            @Header("Authorization") String token
    );

    // --- Reacciones (likes, comentarios) ---

    @GET("reacciones/receta/{recetaId}")
    Call<ReaccionesResponse> getReaccionesRecetaById(
            @Path("recetaId") int recetaId,
            @Header("Authorization") String token
    );

    @POST("reacciones/receta/{recetaId}/like")
    Call<LikeResponse> sendLike(
            @Path("recetaId") int recetaId,
            @Header("Authorization") String token
    );

    // ✅ NUEVO: Recalcular nutrición con IA
    @POST("recetas/{id}/recalcular-nutricion")
    Call<ApiResponse> recalcularNutricion(
            @Path("id") int recetaId,
            @Header("Authorization") String token
    );
}