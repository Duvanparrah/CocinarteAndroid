package com.camilo.cocinarte.api.auth;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.ForgotPasswordRequest;
import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.RegisterRequest;
import com.camilo.cocinarte.models.RegisterResponse;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.models.UsuarioUpdateRequest;
import com.camilo.cocinarte.models.VerifyCodeRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
