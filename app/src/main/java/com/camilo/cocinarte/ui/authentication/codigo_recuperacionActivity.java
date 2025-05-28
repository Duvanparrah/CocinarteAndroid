package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class codigo_recuperacionActivity extends AppCompatActivity {

    private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    private Button btnVerify;
    private ImageButton btnBack;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_codigo_recuperacion);

        // Configurar insets para el sistema de navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recibir datos enviados desde la actividad anterior (opcional)
        if (getIntent().hasExtra("EMAIL")) {
            email = getIntent().getStringExtra("EMAIL");
            // Puedes usar el correo electrónico si lo necesitas, por ejemplo:
            // Toast.makeText(this, "Código enviado a: " + email, Toast.LENGTH_SHORT).show();
        }

        // Inicializar vistas
        etDigit1 = findViewById(R.id.etDigit1);
        etDigit2 = findViewById(R.id.etDigit2);
        etDigit3 = findViewById(R.id.etDigit3);
        etDigit4 = findViewById(R.id.etDigit4);
        etDigit5 = findViewById(R.id.etDigit5);
        etDigit6 = findViewById(R.id.etDigit6);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);

        // Configurar foco automático entre campos
        setupDigitFocusChangeListeners();

        // Configurar listeners para botones
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Volver a la pantalla anterior
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarCodigo();
            }
        });
    }

    private void setupDigitFocusChangeListeners() {
        // Configurar TextWatcher para el primer campo
        etDigit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etDigit2.requestFocus();
                }
            }
        });

        // Configurar TextWatcher para el segundo campo
        etDigit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etDigit3.requestFocus();
                } else if (s.length() == 0) {
                    etDigit1.requestFocus();
                }
            }
        });

        // Configurar TextWatcher para el tercer campo
        etDigit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etDigit4.requestFocus();
                } else if (s.length() == 0) {
                    etDigit2.requestFocus();
                }
            }
        });

        // Configurar TextWatcher para el cuarto campo
        etDigit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etDigit5.requestFocus();
                } else if (s.length() == 0) {
                    etDigit3.requestFocus();
                }
            }
        });

        // Configurar TextWatcher para el quinto campo
        etDigit5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    etDigit6.requestFocus();
                } else if (s.length() == 0) {
                    etDigit4.requestFocus();
                }
            }
        });

        // Configurar TextWatcher para el sexto campo
        etDigit6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    etDigit5.requestFocus();
                }
                // Verificar si todos los campos están completos
                if (camposCompletos()) {
                    // Ocultar teclado si se desea
                    // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // imm.hideSoftInputFromWindow(etDigit6.getWindowToken(), 0);
                }
            }
        });
    }

    private boolean camposCompletos() {
        return !etDigit1.getText().toString().isEmpty() &&
                !etDigit2.getText().toString().isEmpty() &&
                !etDigit3.getText().toString().isEmpty() &&
                !etDigit4.getText().toString().isEmpty() &&
                !etDigit5.getText().toString().isEmpty() &&
                !etDigit6.getText().toString().isEmpty();
    }

    private void verificarCodigo() {
        if (!camposCompletos()) {
            Toast.makeText(this, "Por favor, completa el código de verificación", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construir el código completo
        String codigo = etDigit1.getText().toString() +
                etDigit2.getText().toString() +
                etDigit3.getText().toString() +
                etDigit4.getText().toString() +
                etDigit5.getText().toString() +
                etDigit6.getText().toString();

        // Aquí iría la lógica para verificar el código
        // Por ejemplo, comparar con un código enviado desde el servidor o
        // enviar una solicitud a tu backend para validar el código

        // Para este ejemplo, mostraremos un mensaje de éxito y navegamos a la pantalla de cambio de contraseña
        Toast.makeText(this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();

        // Navegar a la pantalla de cambio de contraseña
        Intent intent = new Intent(this, cambio_contrasenaActivity.class);
        // Si necesitas pasar el email u otros datos a la siguiente pantalla:
        if (!email.isEmpty()) {
            intent.putExtra("EMAIL", email);
        }
        startActivity(intent);
        finish(); // Opcional: cierra esta actividad para que no pueda volver a ella con el botón atrás
    }
}