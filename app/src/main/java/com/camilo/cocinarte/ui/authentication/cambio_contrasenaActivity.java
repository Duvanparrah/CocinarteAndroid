package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class cambio_contrasenaActivity extends AppCompatActivity {

    private static final String TAG = "CambioContrasena";

    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private Button btnSavePassword;
    private ImageButton btnBack;

    private String email = "";
    private String resetCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_contrasena);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        getIntentData();
        initializeViews();
        setupListeners();
        setupPasswordValidation();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("EMAIL");
            resetCode = intent.getStringExtra("RESET_CODE");

            Log.d(TAG, "Email recibido: " + email);
            Log.d(TAG, "Código de reset recibido: " + (resetCode != null ? resetCode : "No"));
        }
    }

    private void initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnSavePassword.setOnClickListener(v -> {
            if (validatePasswords()) {
                saveNewPassword();
            }
        });
    }

    private void setupPasswordValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validatePasswordsInput();
            }
        };
        etNewPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
    }

    private boolean validatePasswordsInput() {
        String password = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean isValid = true;

        if (password.isEmpty()) {
            passwordLayout.setError("La contraseña no puede estar vacía");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Debe tener al menos 6 caracteres");
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            passwordLayout.setError("Debe contener mayúscula, minúscula y número");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }

        if (!confirmPassword.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                confirmPasswordLayout.setError("Las contraseñas no coinciden");
                isValid = false;
            } else {
                confirmPasswordLayout.setError(null);
            }
        }

        return isValid;
    }

    private boolean isPasswordStrong(String password) {
        boolean hasUpper = false, hasLower = false, hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasUpper && hasLower && hasDigit;
    }

    private boolean validatePasswords() {
        return validatePasswordsInput() && !etConfirmPassword.getText().toString().trim().isEmpty();
    }

    private void saveNewPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Guardando...");

        if (email != null && resetCode != null && !email.isEmpty() && !resetCode.isEmpty()) {
            ResetPasswordRequest request = new ResetPasswordRequest(email, resetCode, newPassword);

            AuthService authService = ApiClient.getClient(getApplicationContext()).create(AuthService.class);

            // ✅ CORREGIDO: Cambiar Callback<Void> por Callback<ApiResponse>
            authService.resetPassword(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Opcionalmente usar el mensaje de la respuesta
                        ApiResponse apiResponse = response.body();
                        Log.d(TAG, "Respuesta del servidor: " + apiResponse.getMessage());
                        showSuccessAndNavigate();
                    } else {
                        Toast.makeText(cambio_contrasenaActivity.this, "Error al cambiar la contraseña", Toast.LENGTH_LONG).show();
                        resetButton();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e(TAG, "Error de red: " + t.getMessage());
                    Toast.makeText(cambio_contrasenaActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    resetButton();
                }
            });
        } else {
            Toast.makeText(this, "Datos incompletos para cambiar la contraseña", Toast.LENGTH_LONG).show();
            resetButton();
        }
    }

    private void resetButton() {
        btnSavePassword.setEnabled(true);
        btnSavePassword.setText("Guardar Contraseña");
    }

    private void showSuccessAndNavigate() {
        Toast.makeText(this, "¡Contraseña actualizada correctamente!", Toast.LENGTH_SHORT).show();
        btnSavePassword.postDelayed(this::navigateToLoginScreen, 1500);
    }

    private void navigateToLoginScreen() {
        Intent intent = new Intent(this, InicioSesionActivity.class);
        if (email != null && !email.isEmpty()) {
            intent.putExtra("EMAIL", email);
            intent.putExtra("SHOW_SUCCESS_MESSAGE", true);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿Salir?")
                .setMessage("Si sales ahora, perderás los cambios. ¿Estás seguro?")
                .setPositiveButton("Sí, salir", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancelar", null)
                .show();
    }
}