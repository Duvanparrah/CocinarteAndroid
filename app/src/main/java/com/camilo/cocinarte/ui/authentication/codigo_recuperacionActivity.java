package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.VerifyCodeRequest;
import com.camilo.cocinarte.models.ApiResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class codigo_recuperacionActivity extends AppCompatActivity {

    private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    private Button btnVerify;
    private ImageButton btnBack;

    private AuthService authService;
    private static final String BASE_URL = "https://cocinarte-production.up.railway.app/api/";

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);

        btnBack.setOnClickListener(v -> finish());
        btnVerify.setOnClickListener(v -> verificarCodigo());
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
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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

    private void verificarCodigo() {
        String code = etDigit1.getText().toString().trim()
                + etDigit2.getText().toString().trim()
                + etDigit3.getText().toString().trim()
                + etDigit4.getText().toString().trim()
                + etDigit5.getText().toString().trim()
                + etDigit6.getText().toString().trim();

        if (code.length() < 6 || !TextUtils.isDigitsOnly(code)) {
            Toast.makeText(this, "Ingresa el código completo de 6 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);
        btnVerify.setText("Verificando...");

        VerifyCodeRequest request = new VerifyCodeRequest(email, code);

        authService.verifyRecoveryCode(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verificar");

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(codigo_recuperacionActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(codigo_recuperacionActivity.this, cambio_contrasenaActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        } else {
                            errorBody = "Respuesta sin cuerpo de error";
                        }
                    } catch (Exception e) {
                        errorBody = "Excepción al leer el cuerpo de error: " + e.getMessage();
                    }

                    android.util.Log.e("API_VERIFY_ERROR", "Código: " + response.code() + " - Error: " + errorBody);
                    Toast.makeText(codigo_recuperacionActivity.this, "Error al verificar: " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verificar");
                Toast.makeText(codigo_recuperacionActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
