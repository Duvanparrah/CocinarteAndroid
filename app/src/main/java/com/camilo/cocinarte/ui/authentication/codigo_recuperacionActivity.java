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

                // Log para debugging
                android.util.Log.d("API_VERIFY", "Response code: " + response.code());

                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        android.util.Log.d("API_VERIFY", "Response message: " + apiResponse.getMessage());

                        // Mostrar mensaje de éxito
                        String successMessage = apiResponse.getMessage() != null ?
                                apiResponse.getMessage() : "Código verificado correctamente";
                        Toast.makeText(codigo_recuperacionActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                        // Navegar a cambio de contraseña
                        Intent intent = new Intent(codigo_recuperacionActivity.this, cambio_contrasenaActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("VERIFIED_CODE", code); // Opcional: pasar el código verificado
                        startActivity(intent);
                        finish();
                    } else {
                        // Respuesta exitosa pero sin body - asumir éxito
                        Toast.makeText(codigo_recuperacionActivity.this, "Código verificado correctamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(codigo_recuperacionActivity.this, cambio_contrasenaActivity.class);
                        intent.putExtra("EMAIL", email);
                        intent.putExtra("VERIFIED_CODE", code);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // Manejar errores HTTP
                    String errorMessage = "Error al verificar el código";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("API_VERIFY_ERROR", "Código: " + response.code() + " - Error: " + errorBody);

                            // Mensajes de error más específicos
                            switch (response.code()) {
                                case 400:
                                    errorMessage = "Código incorrecto o expirado";
                                    break;
                                case 404:
                                    errorMessage = "Email no encontrado";
                                    break;
                                case 422:
                                    errorMessage = "Código inválido";
                                    break;
                                default:
                                    if (response.code() >= 500) {
                                        errorMessage = "Error del servidor. Intenta más tarde";
                                    }
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("API_VERIFY_ERROR", "Error al leer respuesta: " + e.getMessage());
                    }

                    Toast.makeText(codigo_recuperacionActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnVerify.setEnabled(true);
                btnVerify.setText("Verificar");
                android.util.Log.e("API_VERIFY_FAILURE", "Error de conexión: " + t.getMessage());
                Toast.makeText(codigo_recuperacionActivity.this, "Error de conexión. Verifica tu internet", Toast.LENGTH_LONG).show();
            }
        });
    }
}