package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.databinding.ActivityInicioSesionBinding;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.viewmodels.AuthViewModel;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class InicioSesionActivity extends AppCompatActivity {

    private static final String TAG = "InicioSesionActivity";

    private ActivityInicioSesionBinding binding;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ CORRECCIÓN: Inicializar SessionManager con manejo de excepciones
        try {
            sessionManager = SessionManager.getInstance(this);
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "❌ Error inicializando SessionManager: " + e.getMessage(), e);
            Toast.makeText(this, "Error de seguridad en la aplicación", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loginManager = new LoginManager(this);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // ✅ Validar si ya hay sesión activa
        if (hasActiveSession()) {
            Log.d(TAG, "✅ Sesión activa detectada, redirigiendo a MainActivity...");
            navigateToMain();
            return;
        }

        // ✅ No hay sesión, continuar con el login
        binding = ActivityInicioSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        observeViewModel();
        setupTextWatchers();
    }

    // ✅ MÉTODO MEJORADO: Verificar sesión activa con manejo de errores
    private boolean hasActiveSession() {
        try {
            return loginManager.hasActiveSession() ||
                    (sessionManager.isLoggedIn() &&
                            sessionManager.hasValidToken() &&
                            !sessionManager.isSessionExpired());
        } catch (Exception e) {
            Log.e(TAG, "Error verificando sesión activa: " + e.getMessage());
            return false;
        }
    }

    private void setupViews() {
        binding.buttonLogin.setOnClickListener(v -> performLogin());

        binding.textViewRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));

        binding.textViewForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, correo_Recuperar_Contrasena_Activity.class)));

        handleIntent();
    }

    private void observeViewModel() {
        authViewModel.emailError.observe(this, error ->
                binding.textInputLayoutEmail.setError(error));

        authViewModel.passwordError.observe(this, error ->
                binding.textInputLayoutPassword.setError(error));

        authViewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        authViewModel.navigationEvent.observe(this, destination -> {
            if ("main".equals(destination)) {
                navigateToMain();
            }
        });
    }

    private void setupTextWatchers() {
        binding.editTextEmail.addTextChangedListener(clearErrorWatcher(binding.textInputLayoutEmail));
        binding.editTextPassword.addTextChangedListener(clearErrorWatcher(binding.textInputLayoutPassword));
    }

    private TextWatcher clearErrorWatcher(final View layout) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout instanceof com.google.android.material.textfield.TextInputLayout) {
                    ((com.google.android.material.textfield.TextInputLayout) layout).setError(null);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        };
    }

    private void performLogin() {
        String email = binding.editTextEmail.getText() != null ?
                binding.editTextEmail.getText().toString().trim() : "";

        String password = binding.editTextPassword.getText() != null ?
                binding.editTextPassword.getText().toString() : "";

        // Validación básica
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.setError("El email es requerido");
            return;
        }

        if (password.isEmpty()) {
            binding.textInputLayoutPassword.setError("La contraseña es requerida");
            return;
        }

        authViewModel.clearErrors();

        authViewModel.login(email, password).observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        handleLoginSuccess(resource.data, email, password);
                    } else {
                        Toast.makeText(this, "Error: respuesta vacía del servidor", Toast.LENGTH_LONG).show();
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    String errorMessage = resource.message != null ?
                            resource.message : "Error al iniciar sesión";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error en login: " + errorMessage);
                    break;
            }
        });
    }

    private void handleLoginSuccess(LoginResponse loginResponse, String email, String password) {
        try {
            Log.d(TAG, "🚀 Login exitoso, guardando datos...");

            // Verificar que tenemos un token válido
            if (loginResponse.getToken() == null || loginResponse.getToken().trim().isEmpty()) {
                Toast.makeText(this, "Error: no se recibió token de autenticación", Toast.LENGTH_LONG).show();
                return;
            }

            if (loginResponse.getUser() != null) {
                LoginResponse.UserData user = loginResponse.getUser();

                // Guardar sesión completa
                sessionManager.saveCompleteUserSession(
                        email,
                        password,
                        loginResponse.getToken(),
                        String.valueOf(user.getId()),
                        user.getNombre(),
                        user.getFoto(),
                        user.getTipo_usuario(),
                        user.isVerified()
                );
                Log.d(TAG, "✅ Guardado en SessionManager - Usuario: " + user.getNombre());

                // Guardar en LoginManager
                loginManager.saveToken(loginResponse.getToken());
                loginManager.saveUser(user);
                Log.d(TAG, "✅ Guardado en LoginManager");

                // Debug: imprimir datos del usuario (opcional)
                // loginManager.debugPrintUserData();
            } else {
                // Sesión básica sin datos del usuario
                sessionManager.saveUserSession(email, password, loginResponse.getToken());
                loginManager.saveToken(loginResponse.getToken());
                Log.d(TAG, "✅ Guardado login básico sin user info");
            }

            // Mostrar mensaje de bienvenida
            String welcomeMessage = loginResponse.getMessage() != null ?
                    loginResponse.getMessage() : "¡Bienvenido!";
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();

            // Navegar a la pantalla principal
            navigateToMain();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al guardar sesión: " + e.getMessage(), e);
            Toast.makeText(this, "Error al guardar la sesión: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        binding.buttonLogin.setEnabled(!show);
        binding.buttonLogin.setText(show ? "Ingresando..." : "Iniciar sesión");

        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EMAIL")) {
            String email = intent.getStringExtra("EMAIL");
            if (email != null && !email.trim().isEmpty()) {
                binding.editTextEmail.setText(email);
                Log.d(TAG, "Email pre-cargado desde intent: " + email);
            }
        }
    }

    private void navigateToMain() {
        Log.d(TAG, "🏠 Navegando a MainActivity...");
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment_to_show", "inicio");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navegando a MainActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error al navegar a la pantalla principal", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verificar sesión al volver a la activity
        if (sessionManager != null && hasActiveSession()) {
            Log.d(TAG, "Sesión detectada en onResume, navegando a MainActivity");
            navigateToMain();
        }
    }
}