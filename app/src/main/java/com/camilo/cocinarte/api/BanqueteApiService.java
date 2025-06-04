// api/BanqueteApiService.java
package com.camilo.cocinarte.api;

import com.camilo.cocinarte.models.Banquete;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BanqueteApiService {
    @GET("api/banquetes") // Asegúrate que coincida con tu ruta de backend
    Call<List<Banquete>> obtenerBanquetes();
}
