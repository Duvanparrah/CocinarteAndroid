package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.Receta;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecetaApi {
    @GET("recetas/recetas")
    Call<List<Receta>> getRecetas(@Header("Authorization") String token);

    @POST("recetas/recetas")
    Call<Receta> createReceta(@Body Receta receta, @Header("Authorization") String token);

    @DELETE("recetas/{id}")
    Call<Void> deleteReceta(@Path("id") int recetaId, @Header("Authorization") String token);
}
