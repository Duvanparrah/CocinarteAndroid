package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.Banquete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("banquetes")
    Call<List<Banquete>> getBanquetes();
}
