package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.api.MyCookieJar;
import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InicioSesionActivity extends AppCompatActivity {

    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword;
    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    private AuthService authService;
    private SessionManager sessionManager;
    private MyCookieJar cookieJar;

    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        sessionManager = new SessionManager(this);
        cookieJar = new MyCookieJar(this);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cookieJar(cookieJar)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        buttonLogin.setOnClickListener(v -> iniciarSesion());

        findViewById(R.id.textViewRegister).setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesionActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.textViewForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(InicioSesionActivity.this, correo_Recuperar_Contrasena_Activity.class);
            startActivity(intent);
        });

        cargarDatosGuardados();
    }

    private void cargarDatosGuardados() {
        if (sessionManager.isUserExist()) {
            String savedEmail = sessionManager.getEmail();
            if (savedEmail != null) {
                editTextEmail.setText(savedEmail);
            }
        }
    }

    private boolean validarCampos() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim().toLowerCase() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";

        boolean isValid = true;

        if (email.isEmpty()) {
            textInputLayoutEmail.setError("Por favor, ingrese su email");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Por favor, ingrese un email válido");
            isValid = false;
        } else {
            textInputLayoutEmail.setError(null);
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.setError("Por favor, ingrese su contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            textInputLayoutPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return isValid;
    }

    private void iniciarSesion() {
        if (!validarCampos()) return;

        // ✅ Sanitizar email
        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString();

        buttonLogin.setEnabled(false);
        buttonLogin.setText("Ingresando...");

        LoginRequest request = new LoginRequest(email, password);

        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Iniciar sesión");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.getToken() != null) {
                        sessionManager.saveUserSession(email, password, loginResponse.getToken());
                        Toast.makeText(InicioSesionActivity.this,
                                loginResponse.getMessage() != null ? loginResponse.getMessage() : "Bienvenido",
                                Toast.LENGTH_SHORT).show();
                        irAMainActivity();
                        return;
                    }

                    HttpUrl url = HttpUrl.parse(BASE_URL);
                    if (url != null) {
                        List<Cookie> cookies = cookieJar.loadForRequest(url);
                        for (Cookie cookie : cookies) {
                            if (cookie.name().equalsIgnoreCase("token")) {
                                sessionManager.saveUserSession(email, password, cookie.value());
                                Toast.makeText(InicioSesionActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                                irAMainActivity();
                                return;
                            }
                        }
                    }

                    sessionManager.saveUser(email, password);
                    Toast.makeText(InicioSesionActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
                    irAMainActivity();

                } else {
                    String errorMessage = "Credenciales incorrectas";
                    if (response.code() == 401) {
                        errorMessage = "Email o contraseña incorrectos";
                    } else if (response.code() == 404) {
                        errorMessage = "Usuario no encontrado";
                    } else if (response.code() >= 500) {
                        errorMessage = "Error del servidor. Intenta más tarde.";
                    }

                    Toast.makeText(InicioSesionActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Iniciar sesión");

                String errorMessage = "Error de conexión";
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("Unable to resolve host")) {
                        errorMessage = "No se puede conectar al servidor. Verifica que esté ejecutándose.";
                    } else if (t.getMessage().contains("timeout")) {
                        errorMessage = "Tiempo de espera agotado. Intenta de nuevo.";
                    } else {
                        errorMessage = "Error de conexión: " + t.getMessage();
                    }
                }

                Toast.makeText(InicioSesionActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAMainActivity() {
        Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
        intent.putExtra("fragment_to_show", "inicio");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
