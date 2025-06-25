package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.FavoritosResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface FavoritosService {
    @GET("favoritos")
    Call<FavoritosResponse> getFavoritos(@Header("Authorization") String token);
}
