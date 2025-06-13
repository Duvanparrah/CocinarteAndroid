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
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class cambio_contrasenaActivity extends AppCompatActivity {

    private static final String TAG = "CambioContrasena";

    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnSavePassword;
    private ImageButton btnBack;

    private String email = "";
    private String resetCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cambio_contrasena);

        // Configurar padding para system bars
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
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("EMAIL");
            resetCode = intent.getStringExtra("VERIFIED_CODE");

            // Verificar si no viene como VERIFIED_CODE, intentar con otros nombres
            if (resetCode == null || resetCode.isEmpty()) {
                resetCode = intent.getStringExtra("RESET_CODE");
            }
            if (resetCode == null || resetCode.isEmpty()) {
                resetCode = intent.getStringExtra("CODE");
            }

            Log.d(TAG, "Email recibido: " + email);
            Log.d(TAG, "Código recibido: " + resetCode);

            // Validar que los datos no sean null
            if (email == null || email.isEmpty()) {
                Log.e(TAG, "ERROR: Email es null o vacío");
                Toast.makeText(this, "Error: No se recibió el email correctamente", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            if (resetCode == null || resetCode.isEmpty()) {
                Log.e(TAG, "ERROR: Reset code es null o vacío");
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
        // Validar que la contraseña tenga al menos 6 caracteres
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveNewPassword() {
        // Obtener valores de los campos, asegurándonos de que no sean null
        String newPassword = "";
        String confirmPassword = "";

        if (etNewPassword.getText() != null) {
            newPassword = etNewPassword.getText().toString().trim();
        }
        if (etConfirmPassword.getText() != null) {
            confirmPassword = etConfirmPassword.getText().toString().trim();
        }

        // Validaciones más estrictas
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una contraseña", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor confirma la contraseña", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }

        if (!validatePassword(newPassword)) {
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }

        // Validar que tenemos email y código (doble verificación)
        if (email == null || email.trim().isEmpty()) {
            Log.e(TAG, "Email es null o vacío al momento de guardar");
            Toast.makeText(this, "Error: Email no válido. Regresa e intenta nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        if (resetCode == null || resetCode.trim().isEmpty()) {
            Log.e(TAG, "Reset code es null o vacío al momento de guardar");
            Toast.makeText(this, "Error: Código de verificación no válido. Regresa e intenta nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        // Limpiar espacios en blanco de email y código
        String cleanEmail = email.trim();
        String cleanResetCode = resetCode.trim();

        Log.d(TAG, "=== INICIANDO CAMBIO DE CONTRASEÑA ===");
        Log.d(TAG, "Email (limpio): '" + cleanEmail + "'");
        Log.d(TAG, "Código (limpio): '" + cleanResetCode + "'");
        Log.d(TAG, "Longitud contraseña: " + newPassword.length());
        Log.d(TAG, "Email vacío: " + cleanEmail.isEmpty());
        Log.d(TAG, "Código vacío: " + cleanResetCode.isEmpty());

        // Deshabilitar botón
        btnSavePassword.setEnabled(false);
        btnSavePassword.setText("Guardando...");

        // CORRECCIÓN PRINCIPAL: Crear request solo con email y password
        // El servidor espera {"email": "...", "password": "..."}
        ResetPasswordRequest request;
        try {
            // Usar constructor de 2 parámetros (email, password)
            request = new ResetPasswordRequest(cleanEmail, newPassword);

            Log.d(TAG, "Request creado exitosamente");
            Log.d(TAG, "Datos del request - Email: " + cleanEmail + ", Password: [OCULTA]");

            // Verificar que el request se creó correctamente
            if (request == null) {
                throw new Exception("Request es null después de crearlo");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creando request", e);
            resetButton();
            Toast.makeText(this, "Error al crear la solicitud: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Obtener servicio de autenticación
        AuthService authService;
        try {
            authService = ApiClient.getClient(getApplicationContext()).create(AuthService.class);
            if (authService == null) {
                throw new Exception("AuthService es null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error obteniendo AuthService", e);
            resetButton();
            Toast.makeText(this, "Error de configuración del servicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realizar llamada al API
        try {
            Call<ApiResponse> call = authService.resetPassword(request);
            Log.d(TAG, "Realizando llamada al servidor...");

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    Log.d(TAG, "=== RESPUESTA RECIBIDA ===");
                    Log.d(TAG, "Código de respuesta: " + response.code());
                    Log.d(TAG, "Es exitosa: " + response.isSuccessful());
                    Log.d(TAG, "Headers: " + response.headers().toString());

                    resetButton();

                    if (response.isSuccessful()) {
                        ApiResponse apiResponse = response.body();
                        Log.d(TAG, "Contraseña cambiada exitosamente");
                        if (apiResponse != null) {
                            Log.d(TAG, "Mensaje del servidor: " + apiResponse.getMessage());
                        }

                        Toast.makeText(cambio_contrasenaActivity.this, "¡Contraseña actualizada correctamente!", Toast.LENGTH_SHORT).show();

                        // Navegar al login
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
                                Log.e(TAG, "Error body completo: " + errorBody);

                                // Intentar parsear el JSON de error
                                try {
                                    JSONObject errorJson = new JSONObject(errorBody);
                                    String serverError = errorJson.optString("error", errorBody);

                                    // Mensajes de error más específicos basados en el servidor
                                    if (serverError.contains("Email y contraseña son requeridos")) {
                                        errorMsg = "Error: Faltan datos requeridos. Verifica que todos los campos estén completos.";
                                    } else if (serverError.contains("invalid") || serverError.contains("inválido")) {
                                        errorMsg = "Error: El código de verificación es inválido o ha expirado.";
                                    } else if (serverError.contains("expired") || serverError.contains("expirado")) {
                                        errorMsg = "Error: El código de verificación ha expirado. Solicita uno nuevo.";
                                    } else {
                                        errorMsg = "Error del servidor: " + serverError;
                                    }
                                } catch (Exception parseEx) {
                                    Log.e(TAG, "Error parseando JSON de error", parseEx);
                                    errorMsg = "Error: " + errorBody;
                                }

                                // Mensajes adicionales por código HTTP
                                if (response.code() == 400) {
                                    // Ya manejado arriba
                                } else if (response.code() == 401) {
                                    errorMsg = "Error: No autorizado. El código puede haber expirado.";
                                } else if (response.code() == 404) {
                                    errorMsg = "Error: Usuario no encontrado.";
                                } else if (response.code() == 500) {
                                    errorMsg = "Error del servidor. Intenta nuevamente en unos minutos.";
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error leyendo error body", e);
                            errorMsg += " (No se pudo leer el detalle del error)";
                        }

                        Log.e(TAG, "Error final mostrado: " + errorMsg);
                        Toast.makeText(cambio_contrasenaActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e(TAG, "=== ERROR DE RED ===");
                    Log.e(TAG, "Error de red al cambiar contraseña", t);
                    Log.e(TAG, "Tipo de error: " + t.getClass().getSimpleName());
                    Log.e(TAG, "Mensaje: " + t.getMessage());

                    resetButton();

                    String errorMessage = "Error de conexión";
                    if (t.getMessage() != null) {
                        if (t.getMessage().contains("timeout")) {
                            errorMessage = "Error: Tiempo de espera agotado. Intenta nuevamente.";
                        } else if (t.getMessage().contains("network")) {
                            errorMessage = "Error: Problemas de red. Verifica tu conexión.";
                        } else {
                            errorMessage += ": " + t.getMessage();
                        }
                    }

                    Toast.makeText(cambio_contrasenaActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al hacer la llamada al API", e);
            resetButton();
            Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetButton() {
        runOnUiThread(() -> {
            btnSavePassword.setEnabled(true);
            btnSavePassword.setText("Guardar Contraseña");
        });
    }
}