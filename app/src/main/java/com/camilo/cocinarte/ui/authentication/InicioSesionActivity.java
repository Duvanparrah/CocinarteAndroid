package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.ActivityInicioSesionBinding;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InicioSesionActivity extends AppCompatActivity {

    private ActivityInicioSesionBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                        String message = resource.data.getMessage() != null ?
                                resource.data.getMessage() : "Bienvenido";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        navigateToMain();
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