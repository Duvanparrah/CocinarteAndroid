package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.ForgotPasswordRequest;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;
import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.RegisterRequest;
import com.camilo.cocinarte.models.RegisterResponse;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.UpdatePhotoResponse;
import com.camilo.cocinarte.models.VerifyCodeRequest;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface IngredientesService {
    @GET("ingredientes/buscar/categoria")
    Call<IngredientesByCategoriaResponse> obtenerIngredientesPorCategoria(
            @Query("categoria") String categoria,
            @Header("Authorization") String token
    );
}