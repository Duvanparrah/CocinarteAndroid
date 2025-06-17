package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.auth.AuthApiClient;
import com.camilo.cocinarte.api.auth.AuthService;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

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

        initializeViews();
        getIntentData();
        setupListeners();
    }

    private void initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("EMAIL");
            resetCode = intent.getStringExtra("VERIFIED_CODE");

            if (resetCode == null || resetCode.isEmpty()) {
                resetCode = intent.getStringExtra("RESET_CODE");
            }
            if (resetCode == null || resetCode.isEmpty()) {
                resetCode = intent.getStringExtra("CODE");
            }

            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "Error: No se recibió el email correctamente", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (resetCode == null || resetCode.isEmpty()) {
                Toast.makeText(this, "Error: No se recibió el código de verificación", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnSavePassword.setOnClickListener(v -> saveNewPassword());
    }

    private boolean validatePassword(String password) {
        if (password.length() < 6) {
            passwordLayout.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordLayout.setError("Debe contener al menos una letra mayúscula");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            passwordLayout.setError("Debe contener al menos una letra minúscula");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            passwordLayout.setError("Debe contener al menos un número");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~`].*")) {
            passwordLayout.setError("Debe contener al menos un carácter especial");
            return false;
        }

        if (password.contains(" ")) {
            passwordLayout.setError("No debe contener espacios");
            return false;
        }

        passwordLayout.setError(null); // Limpia errores si todo está bien
        return true;
    }

    private void saveNewPassword() {
        String newPassword = "";
        String confirmPassword = "";

        if (etNewPassword.getText() != null) {
            newPassword = etNewPassword.getText().toString().trim();
        }
        if (etConfirmPassword.getText() != null) {
            confirmPassword = etConfirmPassword.getText().toString().trim();
        }

        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        if (newPassword.isEmpty()) {
            passwordLayout.setError("Por favor ingresa una contraseña");
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Por favor confirma la contraseña");
            etConfirmPassword.requestFocus();
            return;
        }

        if (confirmPassword.contains(" ")) {
            confirmPasswordLayout.setError("La confirmación no debe contener espacios");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!validatePassword(newPassword)) {
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Error: Email no válido. Regresa e intenta nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        if (resetCode == null || resetCode.trim().isEmpty()) {
            Toast.makeText(this, "Error: Código de verificación no válido. Regresa e intenta nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        String cleanEmail = email.trim();
        String cleanResetCode = resetCode.trim();

        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Guardando...");

        ResetPasswordRequest request = new ResetPasswordRequest(cleanEmail, newPassword);

        AuthService authService = AuthApiClient.getClient(getApplicationContext()).create(AuthService.class);

        Call<ApiResponse> call = authService.resetPassword(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                resetButton();

                if (response.isSuccessful()) {
                    Toast.makeText(cambio_contrasenaActivity.this, "¡Contraseña actualizada correctamente!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(cambio_contrasenaActivity.this, InicioSesionActivity.class);
                    intent.putExtra("EMAIL", cleanEmail);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "Error al cambiar la contraseña (Código: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JSONObject errorJson = new JSONObject(errorBody);
                            String serverError = errorJson.optString("error", errorBody);

                            if (serverError.contains("Email y contraseña son requeridos")) {
                                errorMsg = "Faltan datos requeridos.";
                            } else if (serverError.contains("invalid") || serverError.contains("inválido")) {
                                errorMsg = "El código de verificación es inválido.";
                            } else if (serverError.contains("expired") || serverError.contains("expirado")) {
                                errorMsg = "El código ha expirado.";
                            }

                            if (response.code() == 401) {
                                errorMsg = "No autorizado. El código puede haber expirado.";
                            } else if (response.code() == 404) {
                                errorMsg = "Usuario no encontrado.";
                            } else if (response.code() == 500) {
                                errorMsg = "Error del servidor. Intenta nuevamente.";
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error leyendo error body", e);
                        errorMsg += " (Error desconocido)";
                    }

                    Toast.makeText(cambio_contrasenaActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                resetButton();
                String errorMessage = "Error de conexión";

                if (t.getMessage() != null) {
                    if (t.getMessage().contains("timeout")) {
                        errorMessage = "Tiempo de espera agotado.";
                    } else if (t.getMessage().contains("network")) {
                        errorMessage = "Problemas de red.";
                    } else {
                        errorMessage += ": " + t.getMessage();
                    }
                }

                Toast.makeText(cambio_contrasenaActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resetButton() {
        runOnUiThread(() -> {
            btnSavePassword.setEnabled(true);
            btnSavePassword.setText("Guardar nueva contraseña");
        });
    }
}
