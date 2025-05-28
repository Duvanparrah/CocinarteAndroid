package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class cambio_contrasenaActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private Button btnSavePassword;
    private ImageButton btnBack;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cambio_contrasena);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recibir datos enviados desde la actividad anterior (opcional)
        if (getIntent().hasExtra("EMAIL")) {
            email = getIntent().getStringExtra("EMAIL");
        }

        // Inicializar vistas
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        btnBack = findViewById(R.id.btnBack);

        // Configurar botón atrás
        btnBack.setOnClickListener(v -> onBackPressed());

        // Validación de contraseñas en tiempo real
        setUpPasswordValidation();

        // Configurar botón guardar
        btnSavePassword.setOnClickListener(v -> {
            if (validatePasswords()) {
                saveNewPassword();
            }
        });
    }

    private void setUpPasswordValidation() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePasswordsInput();
            }
        };

        etNewPassword.addTextChangedListener(textWatcher);
        etConfirmPassword.addTextChangedListener(textWatcher);
    }

    private boolean validatePasswordsInput() {
        String password = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validar contraseña
        if (password.isEmpty()) {
            passwordLayout.setError("La contraseña no puede estar vacía");
            return false;
        } else if (password.length() < 6) {
            passwordLayout.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        } else {
            passwordLayout.setError(null);
        }

        // Validar confirmación de contraseña
        if (!confirmPassword.isEmpty() && !password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Las contraseñas no coinciden");
            return false;
        } else {
            confirmPasswordLayout.setError(null);
        }

        return true;
    }

    private boolean validatePasswords() {
        if (!validatePasswordsInput()) {
            return false;
        }

        String confirmPassword = etConfirmPassword.getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Por favor confirma tu contraseña");
            return false;
        }

        return true;
    }

    private void saveNewPassword() {
        String newPassword = etNewPassword.getText().toString().trim();

        // Aquí implementarías la lógica para guardar la contraseña en tu sistema
        // Por ejemplo, usando Firebase, Room Database o una API REST

        // Por ejemplo, si estás usando Firebase Auth:
        /*
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (!email.isEmpty()) {
            auth.confirmPasswordReset(code, newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        navigateToLoginScreen();
                    } else {
                        Toast.makeText(this, "Error al cambiar la contraseña: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
        }
        */

        // Simulación de guardado exitoso
        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();

        // Navegar a la pantalla de inicio de sesión
        navigateToLoginScreen();
    }

    private void navigateToLoginScreen() {
        // Crear un Intent para navegar a la pantalla de inicio de sesión
        Intent intent = new Intent(this, InicioSesionActivity.class);

        // Si quieres pasar el email u otros datos a la pantalla de inicio de sesión
        if (!email.isEmpty()) {
            intent.putExtra("EMAIL", email);
        }

        // Limpiar la pila de actividades anteriores para que el usuario no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}