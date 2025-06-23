package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class InicioSesionActivity extends AppCompatActivity {

    private CardView cardViewGoogle;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_sesion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        cardViewGoogle = findViewById(R.id.cardViewGoogle);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Asegurar que los TextInputLayout estén configurados correctamente
        setupTextInputLayouts();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llamar al método iniciarSesion en lugar de la navegación directa
                iniciarSesion();
            }
        });

        cardViewGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementar inicio de sesión con Google
                Toast.makeText(InicioSesionActivity.this, "Iniciando sesión con Google", Toast.LENGTH_SHORT).show();
                // Después del inicio exitoso, navegar a MainActivity con el fragmento de inicio
                Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
                intent.putExtra("fragment_to_show", "inicio");
                startActivity(intent);
                finish();
            }
        });

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de recuperación de contraseña
                Toast.makeText(InicioSesionActivity.this, "Recuperar contraseña", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InicioSesionActivity.this, correo_Recuperar_Contrasena_Activity.class);
                startActivity(intent);
            }
        });

        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la pantalla de registro
                Toast.makeText(InicioSesionActivity.this, "Registro", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(InicioSesionActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupTextInputLayouts() {
        // Asegurar que los hints siempre sean visibles
        textInputLayoutEmail.setHintEnabled(true);
        textInputLayoutPassword.setHintEnabled(true);
    }

    /**
     * Valida que sea una dirección de email válida
     * @param email Email a validar
     * @return true si es un email válido, false en caso contrario
     */
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Valida que la contraseña tenga al menos 6 caracteres
     * @param password Contraseña a validar
     * @return true si cumple con los requisitos, false en caso contrario
     */
    private boolean isValidPassword(String password) {
        return password.length() >= 6;
    }

    private boolean validarCampos() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString() : "";
        String password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";

        boolean isValid = true;

        // Validar campos
        if (email.isEmpty()) {
            textInputLayoutEmail.setError("Por favor, ingrese su email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            textInputLayoutEmail.setError("Por favor, ingrese un email válido");
            isValid = false;
        } else {
            textInputLayoutEmail.setError(null);
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.setError("Por favor, ingrese su contraseña");
            isValid = false;
        } else if (!isValidPassword(password)) {
            textInputLayoutPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return isValid;
    }

    private void iniciarSesion() {
        // Validar los campos antes de proceder
        if (validarCampos()) {
            Toast.makeText(InicioSesionActivity.this, "Iniciando sesión...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(InicioSesionActivity.this, MainActivity.class);
            // Esto asegura que se muestre el fragmento de inicio
            intent.putExtra("fragment_to_show", "inicio");
            startActivity(intent);
            finish(); // Cierra la actividad actual para evitar volver atrás
        }
    }
}