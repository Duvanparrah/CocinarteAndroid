package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.models.ApiResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BanqueteApi {

    // ✅ ENDPOINTS PRINCIPALES PARA BANQUETES

    // Obtener todos los banquetes (público)
    @GET("banquetes/")
    Call<List<Banquete>> obtenerTodosBanquetes();

    // Obtener banquetes con autenticación
    @GET("banquetes/")
    Call<List<Banquete>> obtenerTodosBanquetes(@Header("Authorization") String token);

    // Obtener banquete por ID (público)
    @GET("banquetes/{id}")
    Call<Banquete> obtenerBanquetePorId(@Path("id") int idBanquete);

    // Obtener banquete por ID con autenticación
    @GET("banquetes/{id}")
    Call<Banquete> obtenerBanquetePorId(@Path("id") int idBanquete, @Header("Authorization") String token);

    // ✅ FAVORITOS DE BANQUETES - CORREGIDOS PARA USAR ResponseBody
    @GET("banquetes/favoritos")
    Call<List<Banquete>> obtenerBanquetesFavoritos(@Header("Authorization") String token);

    @POST("banquetes/favoritos/{banquete_id}")
    Call<ResponseBody> agregarBanqueteAFavoritos(@Path("banquete_id") int banqueteId, @Header("Authorization") String token);

    @DELETE("banquetes/favoritos/{banquete_id}")
    Call<ResponseBody> quitarBanqueteDeFavoritos(@Path("banquete_id") int banqueteId, @Header("Authorization") String token);

    @GET("banquetes/favoritos/verificar/{banquete_id}")
    Call<ResponseBody> verificarBanqueteFavorito(@Path("banquete_id") int banqueteId, @Header("Authorization") String token);

    // ✅ REACCIONES DE BANQUETES - CORREGIDOS PARA USAR ResponseBody
    @GET("banquetes/reacciones/{banqueteId}")
    Call<ResponseBody> obtenerReaccionesBanquete(@Path("banqueteId") int banqueteId);

    @GET("banquetes/reacciones/{banqueteId}")
    Call<ResponseBody> obtenerReaccionesBanquete(@Path("banqueteId") int banqueteId, @Header("Authorization") String token);

    @POST("banquetes/reacciones/{banqueteId}/like")
    Call<ResponseBody> toggleLikeBanquete(@Path("banqueteId") int banqueteId, @Header("Authorization") String token);

    @POST("banquetes/reacciones/{banqueteId}/comentario")
    Call<ResponseBody> agregarComentarioBanquete(@Path("banqueteId") int banqueteId, @Header("Authorization") String token);
}