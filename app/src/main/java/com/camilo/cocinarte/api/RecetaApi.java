package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.Receta;

import java.util.List;

import okhttp3.MultipartBody;
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
    @GET("recetas/recetas")
    Call<List<Receta>> getRecetas(@Header("Authorization") String token);

    @POST("recetas/recetas")
    Call<Receta> createReceta(@Body Receta receta, @Header("Authorization") String token);

    @DELETE("recetas/{id}")
    Call<Void> deleteReceta(@Path("id") int recetaId, @Header("Authorization") String token);

    @Multipart
    @POST("recetas/foto_receta")
    Call<FotoResponse> subirFotoReceta(
            @Part MultipartBody.Part foto,
            // Si necesitas enviar otros datos, como un ID de receta, puedes hacerlo así:
            // @Part("id_receta") RequestBody idReceta,
            @Header("Authorization") String token
    );
}
