package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
