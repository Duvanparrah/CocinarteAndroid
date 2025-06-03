package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class codigo_recuperacionActivity extends AppCompatActivity {

    private static final int CODE_LENGTH = 6;

    private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    private Button btnVerify;
    private ImageButton btnBack;
    private String email = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_codigo_recuperacion);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("EMAIL")) {
            email = getIntent().getStringExtra("EMAIL");
        }

        initializeViews();
        setupDigitFocusChangeListeners();

        btnBack.setOnClickListener(v -> finish());

        // Saltamos la verificación y vamos directamente a la pantalla de cambio de contraseña
        btnVerify.setOnClickListener(v -> {
            Intent intent = new Intent(codigo_recuperacionActivity.this, cambio_contrasenaActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            finish();
        });
    }

    private void initializeViews() {
        etDigit1 = findViewById(R.id.etDigit1);
        etDigit2 = findViewById(R.id.etDigit2);
        etDigit3 = findViewById(R.id.etDigit3);
        etDigit4 = findViewById(R.id.etDigit4);
        etDigit5 = findViewById(R.id.etDigit5);
        etDigit6 = findViewById(R.id.etDigit6);

        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupDigitFocusChangeListeners() {
        setupWatcher(etDigit1, etDigit2, null);
        setupWatcher(etDigit2, etDigit3, etDigit1);
        setupWatcher(etDigit3, etDigit4, etDigit2);
        setupWatcher(etDigit4, etDigit5, etDigit3);
        setupWatcher(etDigit5, etDigit6, etDigit4);
        setupWatcher(etDigit6, null, etDigit5);
    }

    private void setupWatcher(EditText current, EditText next, EditText prev) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();

                if (text.length() == 1 && next != null) {
                    next.requestFocus();
                } else if (text.length() == 0 && prev != null) {
                    prev.requestFocus();
                }

                if (text.length() > 1) {
                    current.setText(text.substring(0, 1));
                    current.setSelection(1);
                }
            }
        });
    }
}
