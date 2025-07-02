package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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

    // URL local para emulador Android Studio (usa 10.0.2.2 en vez de localhost)
    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correo_recuperar_contrasena);

        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // Botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        // Botón enviar correo
        btnSend.setOnClickListener(v -> enviarCorreoRecuperacion());
    }

    private void enviarCorreoRecuperacion() {
        String email = etEmail.getText().toString().trim();

        // Validaciones
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Por favor ingresa un correo electrónico válido");
            return;
        }

        btnSend.setEnabled(false);
        btnSend.setText("Enviando...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        authService.forgotPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnSend.setEnabled(true);
                btnSend.setText("Enviar");

                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();

                    Toast.makeText(correo_Recuperar_Contrasena_Activity.this,
                            message != null && !message.isEmpty() ? message :
                                    "Correo enviado correctamente a " + email,
                            Toast.LENGTH_LONG).show();

                    // Redirigir a la pantalla de verificación de código
                    Intent intent = new Intent(correo_Recuperar_Contrasena_Activity.this, codigo_recuperacionActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                    finish(); // Opcional: cierra esta actividad para que no vuelva al presionar "Atrás"
                } else {
                    Toast.makeText(correo_Recuperar_Contrasena_Activity.this,
                            "No se pudo enviar el correo. Verifica el correo o intenta más tarde.",
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
