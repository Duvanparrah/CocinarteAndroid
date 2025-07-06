package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface IngredientesService {

    // ✅ NUEVO MÉTODO: Obtener todos los ingredientes (EL QUE FALTABA)
    @GET("ingredientes")
    Call<IngredientesByCategoriaResponse> obtenerTodosLosIngredientes(
            @Header("Authorization") String token
    );

    // Método existente para buscar por categoría
    @GET("ingredientes/buscar/categoria")
    Call<IngredientesByCategoriaResponse> obtenerIngredientesPorCategoria(
            @Query("categoria") String categoria,
            @Header("Authorization") String token
    );

    // ✅ OPCIONAL: Método para buscar por nombre
    @GET("ingredientes/buscar")
    Call<IngredientesByCategoriaResponse> buscarIngredientesPorNombre(
            @Query("nombre") String nombre,
            @Header("Authorization") String token
    );

    // ✅ OPCIONAL: Método para obtener ingrediente por ID
    @GET("ingredientes/{id}")
    Call<IngredientesByCategoriaResponse> obtenerIngredientePorId(
            @retrofit2.http.Path("id") int idIngrediente,
            @Header("Authorization") String token
    );
}