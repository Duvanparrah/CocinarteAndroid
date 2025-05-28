package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistroActivity extends AppCompatActivity {

    private CardView cardViewGoogle;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private Button buttonRegister;
    private TextView textViewLogin;
    private TextView textViewTerms;

    // Constante para la longitud mínima de la contraseña
    private static final int PASSWORD_MIN_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        cardViewGoogle = findViewById(R.id.cardViewGoogle);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        passwordLayout = findViewById(R.id.textInputLayoutPassword);
        confirmPasswordLayout = findViewById(R.id.textInputLayoutConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        textViewTerms = findViewById(R.id.textViewTerms);

        // Configurar listeners
        cardViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí iría la implementación para registro con Google
                Toast.makeText(RegistroActivity.this, "Registrando con Google...", Toast.LENGTH_SHORT).show();
            }
        });

        // Añadir validación en tiempo real para la contraseña
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
        });

        // Añadir validación en tiempo real para confirmar contraseña
        editTextConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de inicio de sesión
                Intent intent = new Intent(RegistroActivity.this, InicioSesionActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Configurar términos y condiciones clicables
        configurarTextoTerminos();
    }

    private void configurarTextoTerminos() {
        String textoCompleto = "Al registrarte estás aceptando nuestros Términos de Uso y Política de Privacidad.";
        SpannableString spannableString = new SpannableString(textoCompleto);

        ClickableSpan terminosSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navegar a términos de uso
                Toast.makeText(RegistroActivity.this, "Términos de Uso", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(RegistroActivity.this, TerminosActivity.class);
                // startActivity(intent);
            }
        };

        ClickableSpan privacidadSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Navegar a política de privacidad
                Toast.makeText(RegistroActivity.this, "Política de Privacidad", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(RegistroActivity.this, PrivacidadActivity.class);
                // startActivity(intent);
            }
        };

        // Encontrar la posición de "Términos de Uso" y "Política de Privacidad" en el texto
        int inicioTerminos = textoCompleto.indexOf("Términos de Uso");
        int finTerminos = inicioTerminos + "Términos de Uso".length();
        int inicioPrivacidad = textoCompleto.indexOf("Política de Privacidad");
        int finPrivacidad = inicioPrivacidad + "Política de Privacidad".length();

        // Aplicar los spans
        spannableString.setSpan(terminosSpan, inicioTerminos, finTerminos, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacidadSpan, inicioPrivacidad, finPrivacidad, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Asignar el texto con spans al TextView
        textViewTerms.setText(spannableString);
        textViewTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Valida la contraseña en tiempo real
     */
    private void validatePassword(String password) {
        if (password.length() < PASSWORD_MIN_LENGTH) {
            passwordLayout.setError("La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
        } else {
            passwordLayout.setError(null);
            passwordLayout.setErrorEnabled(false);
        }

        // Si ya existe texto en confirmar contraseña, validamos también
        if (editTextConfirmPassword.getText() != null && editTextConfirmPassword.getText().length() > 0) {
            validateConfirmPassword();
        }
    }

    /**
     * Valida que las contraseñas coincidan
     */
    private void validateConfirmPassword() {
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";
        String confirmPassword = editTextConfirmPassword.getText() != null ? editTextConfirmPassword.getText().toString() : "";

        if (!confirmPassword.isEmpty() && !confirmPassword.equals(password)) {
            confirmPasswordLayout.setError("Las contraseñas no coinciden");
        } else {
            confirmPasswordLayout.setError(null);
            confirmPasswordLayout.setErrorEnabled(false);
        }
    }

    private void registrarUsuario() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";
        String confirmPassword = editTextConfirmPassword.getText() != null ? editTextConfirmPassword.getText().toString() : "";

        // Validar campos
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar longitud mínima de contraseña
        if (password.length() < PASSWORD_MIN_LENGTH) {
            Toast.makeText(this, "La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres", Toast.LENGTH_SHORT).show();
            passwordLayout.setError("La contraseña debe tener al menos " + PASSWORD_MIN_LENGTH + " caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            confirmPasswordLayout.setError("Las contraseñas no coinciden");
            return;
        }

        // Aquí iría la lógica para registrar al usuario
        // Por ejemplo, crear una cuenta con Firebase Auth o tu propio servicio

        Toast.makeText(this, "Registrando usuario...", Toast.LENGTH_SHORT).show();

        // Si el registro es exitoso, navegar a la pantalla principal
        // Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
        // startActivity(intent);
        // finish(); // Evita que el usuario vuelva a la pantalla de registro al presionar "atrás"
    }
}