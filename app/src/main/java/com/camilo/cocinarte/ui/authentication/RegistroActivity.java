package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.AuthCookieJar;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.RegisterRequest;
import com.camilo.cocinarte.models.RegisterResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {

    private static final String TAG = "RegistroActivity";
    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";

    private TextInputEditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private TextView textViewPasswordError, textViewConfirmPasswordError;
    private AppCompatButton buttonRegister;

    private AuthService authService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        initViews();
        initSessionManager();
        initApiService();
        setupClickListeners();
        agregarTextWatchers();
    }

    private void initViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        textViewPasswordError = findViewById(R.id.textViewPasswordError);
        textViewConfirmPasswordError = findViewById(R.id.textViewConfirmPasswordError);
        buttonRegister = findViewById(R.id.buttonRegister);
    }

    // ✅ CORRECCIÓN: Inicializar SessionManager con manejo de excepciones
    private void initSessionManager() {
        try {
            sessionManager = SessionManager.getInstance(this);
            Log.d(TAG, "SessionManager inicializado correctamente");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "❌ Error inicializando SessionManager: " + e.getMessage(), e);
            Toast.makeText(this, "Error de seguridad en la aplicación", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initApiService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cookieJar(new AuthCookieJar(this))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> registrarUsuario());

        findViewById(R.id.textViewLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegistroActivity.this, InicioSesionActivity.class));
        });
    }

    private void registrarUsuario() {
        ocultarErrores();

        String email = getValue(editTextEmail);
        String password = getValue(editTextPassword);
        String confirmPassword = getValue(editTextConfirmPassword);

        // Validación de campos vacíos
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de contraseñas
        if (!validarContraseñas(password, confirmPassword)) {
            return;
        }

        // Evaluar y mostrar seguridad de la contraseña
        String seguridad = evaluarSeguridad(password);
        Toast.makeText(this, "Seguridad de la contraseña: " + seguridad, Toast.LENGTH_SHORT).show();

        // Proceder con el registro
        realizarRegistro(email, password);
    }

    private boolean validarContraseñas(String password, String confirmPassword) {
        boolean valido = true;

        // Verificar que las contraseñas coincidan
        if (!password.equals(confirmPassword)) {
            textViewConfirmPasswordError.setText("Las contraseñas no coinciden");
            textViewConfirmPasswordError.setVisibility(View.VISIBLE);
            valido = false;
        }

        // Validar complejidad de la contraseña
        if (password.length() < 6) {
            textViewPasswordError.setText("La contraseña debe tener al menos 6 caracteres");
            textViewPasswordError.setVisibility(View.VISIBLE);
            valido = false;
        } else if (!password.matches(".*[A-Z].*")) {
            textViewPasswordError.setText("Debe tener al menos una letra mayúscula");
            textViewPasswordError.setVisibility(View.VISIBLE);
            valido = false;
        } else if (!password.matches(".*[a-z].*")) {
            textViewPasswordError.setText("Debe tener al menos una letra minúscula");
            textViewPasswordError.setVisibility(View.VISIBLE);
            valido = false;
        } else if (!password.matches(".*[!@#$%^&*()_+=\\[\\]{};:<>|./?,-].*")) {
            textViewPasswordError.setText("Debe incluir al menos un carácter especial");
            textViewPasswordError.setVisibility(View.VISIBLE);
            valido = false;
        }

        return valido;
    }

    private void realizarRegistro(String email, String password) {
        // Verificar que SessionManager esté disponible
        if (sessionManager == null) {
            Toast.makeText(this, "Error del sistema. Reinicia la aplicación", Toast.LENGTH_LONG).show();
            return;
        }

        setLoadingState(true);

        RegisterRequest request = new RegisterRequest(email, password);

        authService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleRegistroExitoso(response.body(), email, password);
                } else {
                    handleRegistroError(response);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Error de conexión en registro: " + t.getMessage(), t);
                Toast.makeText(RegistroActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleRegistroExitoso(RegisterResponse response, String email, String password) {
        try {
            Log.d(TAG, "✅ Registro exitoso");

            // Guardar datos del usuario
            sessionManager.saveUser(email, password);

            // Guardar token si existe
            if (response.getToken() != null && !response.getToken().trim().isEmpty()) {
                sessionManager.saveAuthToken(response.getToken());
                Log.d(TAG, "Token guardado en registro");
            }

            Toast.makeText(RegistroActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

            // Navegar a login con el email pre-cargado
            Intent intent = new Intent(RegistroActivity.this, InicioSesionActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error al guardar datos de registro: " + e.getMessage(), e);
            Toast.makeText(this, "Error al guardar los datos. Intenta iniciar sesión manualmente",
                    Toast.LENGTH_LONG).show();

            // Aún así navegar al login
            startActivity(new Intent(RegistroActivity.this, InicioSesionActivity.class));
            finish();
        }
    }

    private void handleRegistroError(Response<RegisterResponse> response) {
        try {
            String errorBody = response.errorBody() != null ?
                    response.errorBody().string() : "Error desconocido";

            Log.e(TAG, "Error en registro: " + response.code() + " - " + errorBody);

            String mensajeError;
            switch (response.code()) {
                case 400:
                    mensajeError = "Datos inválidos. Verifica tu información";
                    break;
                case 409:
                    mensajeError = "Este email ya está registrado";
                    break;
                case 422:
                    mensajeError = "Error de validación. Verifica los campos";
                    break;
                case 500:
                    mensajeError = "Error del servidor. Intenta más tarde";
                    break;
                default:
                    mensajeError = "Error: " + errorBody;
            }

            Toast.makeText(RegistroActivity.this, mensajeError, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e(TAG, "Error procesando respuesta de error: " + e.getMessage());
            Toast.makeText(RegistroActivity.this, "Error inesperado", Toast.LENGTH_LONG).show();
        }
    }

    private void setLoadingState(boolean loading) {
        buttonRegister.setEnabled(!loading);
        buttonRegister.setText(loading ? "Registrando..." : "Registrarse");
    }

    private void ocultarErrores() {
        textViewPasswordError.setVisibility(View.GONE);
        textViewConfirmPasswordError.setVisibility(View.GONE);
    }

    private void agregarTextWatchers() {
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewPasswordError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewConfirmPasswordError.setVisibility(View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private String getValue(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private String evaluarSeguridad(String password) {
        int puntos = 0;

        if (password.length() >= 8) puntos++;
        if (password.matches(".*[A-Z].*")) puntos++;
        if (password.matches(".*[a-z].*")) puntos++;
        if (password.matches(".*\\d.*")) puntos++;
        if (password.matches(".*[!@#$%^&*()_+=\\[\\]{};:<>|./?,-].*")) puntos++;

        if (puntos <= 2) return "Débil";
        else if (puntos <= 4) return "Media";
        else return "Fuerte";
    }
}