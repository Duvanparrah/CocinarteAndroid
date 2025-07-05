package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.FavoritosResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritosApi {

    @GET("favoritos")
    Call<FavoritosResponse> getFavoritos(@Header("Authorization") String token);

    @POST("favoritos/{recetaId}")
    Call<ResponseBody> agregarFavorito(@Header("Authorization") String token, @Path("recetaId") int recetaId);

    @DELETE("favoritos/{recetaId}")
    Call<ResponseBody> quitarFavorito(@Header("Authorization") String token, @Path("recetaId") int recetaId);

    @GET("favoritos/check/{recetaId}")
    Call<ResponseBody> verificarFavorito(@Header("Authorization") String token, @Path("recetaId") int recetaId);
}