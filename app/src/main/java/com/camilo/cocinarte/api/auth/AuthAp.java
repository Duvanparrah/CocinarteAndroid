package com.camilo.cocinarte.api.auth;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthAp {
    @FormUrlEncoded
    @POST("/auth/register")
    Call<ResponseBody> register(
            @Field("email") String email,
            @Field("password") String password,
            @Field("confirmPassword") String confirmPassword
    );
}