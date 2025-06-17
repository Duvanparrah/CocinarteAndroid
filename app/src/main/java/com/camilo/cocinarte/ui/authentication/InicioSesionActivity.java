package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.ActivityInicioSesionBinding;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.viewmodels.AuthViewModel;

public class InicioSesionActivity extends AppCompatActivity {

    private ActivityInicioSesionBinding binding;
    private AuthViewModel authViewModel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Inicializar ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Verificar si ya hay sesión activa
        if (authViewModel.isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        // Inicializar View Binding
        binding = ActivityInicioSesionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        observeViewModel();
        setupTextWatchers();
    }

    private void setupViews() {
        // Botón de login
        binding.buttonLogin.setOnClickListener(v -> performLogin());

        // Link de registro
        binding.textViewRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistroActivity.class));
        });

        // Link de olvidé mi contraseña
        binding.textViewForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, correo_Recuperar_Contrasena_Activity.class));
        });

        // Verificar si viene del registro o cambio de contraseña
        handleIntent();
    }

    private void observeViewModel() {
        // Observar errores de email
        authViewModel.emailError.observe(this, error -> {
            binding.textInputLayoutEmail.setError(error);
        });

        // Observar errores de contraseña
        authViewModel.passwordError.observe(this, error -> {
            binding.textInputLayoutPassword.setError(error);
        });

        // Observar mensajes de error generales
        authViewModel.errorMessage.observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        // Observar eventos de navegación
        authViewModel.navigationEvent.observe(this, destination -> {
            if ("main".equals(destination)) {
                navigateToMain();
            }
        });
    }

    private void setupTextWatchers() {
        // Limpiar error al escribir en el campo de email
        binding.editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textInputLayoutEmail.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Limpiar error al escribir en el campo de contraseña
        binding.editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textInputLayoutPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performLogin() {
        // Obtener valores de los campos
        String email = binding.editTextEmail.getText() != null ?
                binding.editTextEmail.getText().toString().trim() : "";
        String password = binding.editTextPassword.getText() != null ?
                binding.editTextPassword.getText().toString() : "";

        // Limpiar errores previos
        authViewModel.clearErrors();

        // Realizar login
        authViewModel.login(email, password).observe(this, resource -> {
            switch (resource.status) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    if (resource.data != null) {
                        handleLoginSuccess(resource.data, email, password);
                    }
                    break;

                case ERROR:
                    showLoading(false);
                    String errorMessage = resource.message != null ?
                            resource.message : "Error al iniciar sesión";
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    /**
     * Maneja el login exitoso guardando todos los datos del usuario
     */
    private void handleLoginSuccess(LoginResponse loginResponse, String email, String password) {
        try {
            // Verificar si la respuesta incluye datos del usuario
            if (loginResponse.getUser() != null) {
                LoginResponse.UserData userData = loginResponse.getUser();

                // Guardar sesión completa con todos los datos del usuario
                sessionManager.saveCompleteUserSession(
                        email,
                        password,
                        loginResponse.getToken(),
                        String.valueOf(userData.getId()),
                        userData.getNombre(),
                        userData.getFoto(),
                        userData.getTipo_usuario(),
                        userData.isVerified()
                );

                android.util.Log.d("Login", "Datos del usuario guardados: " + userData.getNombre());

            } else {
                // Si solo hay token, guardar sesión básica
                sessionManager.saveUserSession(email, password, loginResponse.getToken());
                android.util.Log.d("Login", "Sesión básica guardada, falta información del usuario");
            }

            // Mostrar mensaje de bienvenida
            String welcomeMessage = loginResponse.getMessage() != null ?
                    loginResponse.getMessage() : "¡Bienvenido!";
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();

            // Navegar a MainActivity
            navigateToMain();

        } catch (Exception e) {
            android.util.Log.e("Login", "Error al guardar sesión: " + e.getMessage());
            Toast.makeText(this, "Error al guardar sesión", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        binding.buttonLogin.setEnabled(!show);
        binding.buttonLogin.setText(show ? "Ingresando..." : "Iniciar sesión");

        // Si tienes un ProgressBar en tu layout
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("EMAIL")) {
            String email = intent.getStringExtra("EMAIL");
            if (email != null) {
                binding.editTextEmail.setText(email);
            }
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragment_to_show", "inicio");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}