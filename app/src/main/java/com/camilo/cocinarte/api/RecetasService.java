package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.LikeResponse;
import com.camilo.cocinarte.models.ReaccionesResponse;
import com.camilo.cocinarte.models.Receta;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RecetasService {
    @GET("/recetas/{recetaId}")
    Call<Receta> getRecetaById(
            @Path("recetaId") int recetaId,
            @Header("Authorization") String token
    );

    @GET("/reacciones/receta/{recetaId}")
        Call<ReaccionesResponse> getReaccionesRecetaById(
                @Path("recetaId") int recetaId,
                @Header("Authorization") String token
    );


    @POST("/reacciones/receta/{recetaId}/like")
    Call<LikeResponse> sendLike(
            @Path("recetaId") int recetaId,
            @Header("Authorization") String token
    );

}
