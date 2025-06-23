package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.LoginRequest;
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

public interface RecetaApi {

    @GET("recetas")
    Call<List<Receta>> getRecetas(@Header("Authorization") String token);

    @GET("recetas/{id}")
    Call<Receta> getRecetaById(@Path("id") int idReceta, @Header("Authorization") String token);

    @Multipart
    @POST("recetas")
    Call<Receta> createReceta(
            @Part MultipartBody.Part foto,
            @Part("nombre") RequestBody titulo,
            @Part("id_categoria") RequestBody idCategoria,
            @Part("seccion") RequestBody seccion,
            @Part("ingredientes") RequestBody ingredientes,
            @Part("preparacion") RequestBody preparacion,
            @Part("tiempo_preparacion") RequestBody tiempoPreparacion,
            @Part("dificultad") RequestBody dificultad,
            @Part("descripcion") RequestBody descripcion,
            @Header("Authorization") String token
    );

    @DELETE("recetas/{id}")
    Call<Void> deleteReceta(@Path("id") int recetaId, @Header("Authorization") String token);

    @Multipart
    @POST("recetas/upload/image")
    Call<FotoResponse> subirFotoReceta(
            @Part MultipartBody.Part image,
            @Header("Authorization") String token
    );
}
