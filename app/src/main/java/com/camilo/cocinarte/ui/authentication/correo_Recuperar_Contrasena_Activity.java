package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.ForgotPasswordRequest;
import com.camilo.cocinarte.models.ApiResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class correo_Recuperar_Contrasena_Activity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend;
    private ImageButton btnBack;

    private AuthService authService;

    private static final String BASE_URL = "http://192.168.18.7:5000/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correo_recuperar_contrasena);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        btnSend.setOnClickListener(v -> enviarCorreoRecuperacion());
    }

    private void enviarCorreoRecuperacion() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Por favor ingresa un correo electrónico válido");
            return;
        }

        btnSend.setEnabled(false);
        btnSend.setText("Enviando...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // ✅ CORREGIDO: Cambiar Callback<Void> por Callback<ApiResponse>
        authService.forgotPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Enviar");

                if (response.isSuccessful() && response.body() != null) {
                    // Usar el mensaje del servidor si está disponible
                    ApiResponse apiResponse = response.body();
                    String message = apiResponse.getMessage();

                    if (message != null && !message.isEmpty()) {
                        Toast.makeText(correo_Recuperar_Contrasena_Activity.this, message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(correo_Recuperar_Contrasena_Activity.this,
                                "Se ha enviado un correo a " + email + " para restablecer tu contraseña",
                                Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(correo_Recuperar_Contrasena_Activity.this, codigo_recuperacionActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                    // finish(); // Descomenta si no quieres que el usuario vuelva atrás
                } else {
                    Toast.makeText(correo_Recuperar_Contrasena_Activity.this,
                            "No se pudo enviar el correo. Intenta más tarde.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                btnSend.setText("Enviar");
                Toast.makeText(correo_Recuperar_Contrasena_Activity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}