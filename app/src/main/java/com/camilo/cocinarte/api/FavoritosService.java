package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.FavoritosResponse;
import com.camilo.cocinarte.models.VerificarFavoritoResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoritosService {
    @GET("favoritos")
    Call<FavoritosResponse> getFavoritos(@Header("Authorization") String token);

    @POST("favoritos/{id}")
    Call<ApiResponse> setFavorito(@Path("id") int id, @Header("Authorization") String token);

    @DELETE("favoritos/{id}")
    Call<ApiResponse> deleteFavorito(@Path("id") int id, @Header("Authorization") String token);


    @GET("favoritos/verificar/{id}")
    Call<VerificarFavoritoResponse> verificarFavorito(@Path("id") int id, @Header("Authorization") String token);
}
