package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.UsuarioUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UsuarioService {

    // Actualizar usuario
    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @PUT("usuarios/editar/{id}")
    Call<ApiResponse> actualizarUsuario(
            @Path("id") String id,
            @Body UsuarioUpdateRequest request,
            @Header("Authorization") String token
    );
}
