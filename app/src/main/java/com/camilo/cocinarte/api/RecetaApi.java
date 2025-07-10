package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.FotoResponse;
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
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecetaApi {

    // ✅ ENDPOINTS PRINCIPALES EXISTENTES

    /**
     * ✅ OBTENER TODAS LAS RECETAS - Sin autenticación
     * Endpoint: GET /api/recetas
     * Devuelve todas las recetas CON valores nutricionales
     */
    @GET("api/recetas")
    Call<List<Receta>> obtenerTodasLasRecetas();

    /**
     * ✅ OBTENER TODAS LAS RECETAS - Con autenticación
     * Endpoint: GET /api/recetas (con token)
     * Devuelve todas las recetas CON valores nutricionales + datos de reacciones
     */
    @GET("api/recetas")
    Call<List<Receta>> obtenerTodasLasRecetasConAuth(@Header("Authorization") String token);

    /**
     * ✅ OBTENER RECETAS DE COMUNIDAD (usuarios regulares) - EXISTENTE
     * Endpoint: GET /api/recetas/usuarios-only
     * Devuelve SOLO recetas de usuarios regulares
     */
    @GET("api/recetas/usuarios-only")
    Call<List<Receta>> obtenerRecetasComunidad();

    // ✅ ENDPOINTS INDIVIDUALES

    /**
     * ✅ OBTENER RECETA POR ID - Sin autenticación
     * Endpoint: GET /api/recetas/{id}
     */
    @GET("api/recetas/{id}")
    Call<Receta> obtenerRecetaPorId(@Path("id") int recetaId);

    /**
     * ✅ OBTENER RECETA POR ID - Con autenticación
     * Endpoint: GET /api/recetas/{id} (con token)
     */
    @GET("api/recetas/{id}")
    Call<Receta> obtenerRecetaPorIdConAuth(@Path("id") int recetaId, @Header("Authorization") String token);

    // ✅ ENDPOINTS DE USUARIO AUTENTICADO

    /**
     * ✅ MIS RECETAS: Recetas del usuario autenticado
     * Endpoint: GET /api/recetas/usuario
     */
    @GET("api/recetas/usuario")
    Call<List<Receta>> obtenerMisRecetas(@Header("Authorization") String token);

    // ✅ BÚSQUEDA DE RECETAS

    /**
     * ✅ BUSCAR RECETAS - Sin autenticación
     * Endpoint: GET /api/recetas/buscar?q={query}
     */
    @GET("api/recetas/buscar")
    Call<List<Receta>> buscarRecetas(@Query("q") String query);

    /**
     * ✅ BUSCAR RECETAS - Con autenticación
     * Endpoint: GET /api/recetas/buscar?q={query} (con token)
     */
    @GET("api/recetas/buscar")
    Call<List<Receta>> buscarRecetasConAuth(@Query("q") String query, @Header("Authorization") String token);

    // ✅ CREAR Y EDITAR RECETAS

    /**
     * ✅ CREAR NUEVA RECETA - ENDPOINT EXISTENTE
     * Endpoint: POST /api/recetas
     * Crea receta con cálculo nutricional automático
     */
    @Multipart
    @POST("api/recetas")
    Call<Receta> createReceta(
            @Part MultipartBody.Part foto,                    // imagen (requerida)
            @Part("nombre") RequestBody titulo,               // título de la receta
            @Part("id_categoria") RequestBody idCategoria,    // ID de categoría (1=general)
            @Part("seccion") RequestBody seccion,             // "comunidad" para recetas de usuarios
            @Part("ingredientes") RequestBody ingredientes,   // JSON array de IDs de ingredientes
            @Part("preparacion") RequestBody preparacion,     // pasos de preparación
            @Part("tiempo_preparacion") RequestBody tiempoPreparacion, // "30 minutos"
            @Part("dificultad") RequestBody dificultad,       // "Fácil", "Media", "Difícil"
            @Part("descripcion") RequestBody descripcion,     // descripción/pasos adicionales
            @Header("Authorization") String token
    );

    /**
     * ✅ EDITAR RECETA EXISTENTE
     * Endpoint: PUT /api/recetas/{id}
     */
    @Multipart
    @PUT("api/recetas/{id}")
    Call<Receta> editarReceta(
            @Path("id") int recetaId,
            @Part MultipartBody.Part foto,
            @Part("nombre") RequestBody titulo,
            @Part("id_categoria") RequestBody idCategoria,
            @Part("ingredientes") RequestBody ingredientes,
            @Part("preparacion") RequestBody preparacion,
            @Part("tiempo_preparacion") RequestBody tiempoPreparacion,
            @Part("dificultad") RequestBody dificultad,
            @Part("descripcion") RequestBody descripcion,
            @Header("Authorization") String token
    );

    /**
     * ✅ ELIMINAR RECETA - ENDPOINT EXISTENTE
     * Endpoint: DELETE /api/recetas/{id}
     */
    @DELETE("api/recetas/{id}")
    Call<Void> deleteReceta(@Path("id") int recetaId, @Header("Authorization") String token);

    // ✅ GESTIÓN DE IMÁGENES

    /**
     * ✅ SUBIR IMAGEN DE RECETA
     * Endpoint: POST /api/recetas/upload/image
     */
    @Multipart
    @POST("api/recetas/upload/image")
    Call<FotoResponse> subirImagenReceta(
            @Part MultipartBody.Part image,
            @Header("Authorization") String token
    );

    // ✅ FUNCIONES AVANZADAS

    /**
     * ✅ RECALCULAR NUTRICIÓN CON IA
     * Endpoint: POST /api/recetas/{id}/recalcular-nutricion
     */
    @POST("api/recetas/{id}/recalcular-nutricion")
    Call<ApiResponse> recalcularNutricionConIA(
            @Path("id") int recetaId,
            @Header("Authorization") String token
    );

    /**
     * ✅ CALCULAR NUTRICIÓN PREVIA
     * Endpoint: POST /api/recetas/calcular-nutricion-previa
     */
    @POST("api/recetas/calcular-nutricion-previa")
    Call<ApiResponse> calcularNutricionPrevia(
            @Body RequestBody ingredientesJson,
            @Header("Authorization") String token
    );

    // ✅ MÉTODOS LEGACY PARA COMPATIBILIDAD

    @Deprecated
    @GET("api/recetas")
    Call<List<Receta>> getRecetas(@Header("Authorization") String token);

    @Deprecated
    @GET("api/recetas/{id}")
    Call<Receta> getRecetaById(@Path("id") int idReceta, @Header("Authorization") String token);

    @Deprecated
    @GET("api/recetas/usuario")
    Call<List<Receta>> getMisRecetas(@Header("Authorization") String token);

    Call<Receta> crearRecetaUsuario(MultipartBody.Part imagenPart, RequestBody nombre, RequestBody ingredientes, RequestBody preparacion, RequestBody tiempo, RequestBody dificultad, RequestBody descripcion, String s);

    Call<Void> eliminarRecetaUsuario(int idReceta, String s);
}