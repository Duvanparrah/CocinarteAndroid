package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class correo_Recuperar_Contrasena_Activity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSend;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_correo_recuperar_contrasena);

        // Configurar insets para el sistema de navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        etEmail = findViewById(R.id.etEmail);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);

        // Configurar listeners
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Volver a la pantalla anterior
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCorreoRecuperacion();
            }
        });
    }

    private void enviarCorreoRecuperacion() {
        String email = etEmail.getText().toString().trim();

        // Validar que el campo de correo no esté vacío
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Por favor ingresa tu correo electrónico");
            return;
        }

        // Validar formato de correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Por favor ingresa un correo electrónico válido");
            return;
        }

        // Aquí iría la lógica para enviar el correo de recuperación
        // Por ejemplo, usando Firebase Auth u otro servicio

        // Mostrar mensaje de éxito
        Toast.makeText(this, "Se ha enviado un correo a " + email + " para restablecer tu contraseña",
                Toast.LENGTH_SHORT).show();

        // Navegar a la pantalla de verificación de código
        Intent intent = new Intent(correo_Recuperar_Contrasena_Activity.this, codigo_recuperacionActivity.class);
        // Si deseas pasar el correo electrónico a la siguiente actividad, puedes hacerlo así:
        intent.putExtra("EMAIL", email);
        startActivity(intent);

        // Si no quieres volver a esta pantalla cuando presiones "Atrás" en la pantalla de verificación,
        // descomenta la siguiente línea:
        // finish();
    }
}