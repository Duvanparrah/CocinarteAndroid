package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.api.MyCookieJar;
import com.camilo.cocinarte.models.RegisterRequest;
import com.camilo.cocinarte.models.RegisterResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private AppCompatButton buttonRegister;

    private AuthService authService;
    private SessionManager sessionManager;

    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // Vinculación de vistas
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        sessionManager = new SessionManager(this);

        // Interceptor para logs
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cookieJar(new MyCookieJar(this))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        buttonRegister.setOnClickListener(v -> registrarUsuario());

        findViewById(R.id.textViewLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, InicioSesionActivity.class));
        });
    }

    private void registrarUsuario() {
        String email = getValue(editTextEmail);
        String password = getValue(editTextPassword);
        String confirmPassword = getValue(editTextConfirmPassword);

        // Validaciones
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonRegister.setEnabled(false);
        buttonRegister.setText("Registrando...");

        RegisterRequest request = new RegisterRequest(email, password);

        authService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("Registrarse");

                if (response.isSuccessful() && response.body() != null) {
                    sessionManager.saveUser(email, password);
                    sessionManager.saveToken(response.body().getToken());

                    Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                    // Puedes redirigir a otra pantalla aquí si lo deseas
                    startActivity(new Intent(RegistroActivity.this, InicioSesionActivity.class));
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Toast.makeText(RegistroActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(RegistroActivity.this, "Error inesperado", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                buttonRegister.setEnabled(true);
                buttonRegister.setText("Registrarse");
                Toast.makeText(RegistroActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getValue(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }
}
