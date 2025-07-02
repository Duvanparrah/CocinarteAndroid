package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface IngredientesService {
    @GET("ingredientes/buscar/categoria")
    Call<IngredientesByCategoriaResponse> obtenerIngredientesPorCategoria(
            @Query("categoria") String categoria,
            @Header("Authorization") String token
    );

    @GET("ingredientes")
    Call<IngredientesByCategoriaResponse> obtenerTodosLosIngredientes(
            @Header("Authorization") String token
    );


}