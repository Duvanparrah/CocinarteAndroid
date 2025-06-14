package com.camilo.cocinarte.ui.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.ForgotPasswordRequest;
import com.camilo.cocinarte.models.VerifyCodeRequest;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CodigoRecuperacionActivity extends AppCompatActivity {

    private static final int CODE_LENGTH = 6;

    private EditText etDigit1, etDigit2, etDigit3, etDigit4, etDigit5, etDigit6;
    private Button btnVerify;
    private ImageButton btnBack;
    private String email = "";
    private AuthService authService;

    private String BASE_URL = "";

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

        this.BASE_URL = "https://"+ this.getString(R.string.myhost) +"/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(AuthService.class);


        if (getIntent().hasExtra("EMAIL")) {
            email = getIntent().getStringExtra("EMAIL");
        }

        initializeViews();
        setupDigitFocusChangeListeners();

        btnBack.setOnClickListener(v -> finish());

        // Saltamos la verificación y vamos directamente a la pantalla de cambio de contraseña
        btnVerify.setOnClickListener(v -> {
            Intent intent = new Intent(CodigoRecuperacionActivity.this, CambioContrasenaActivity.class);
            intent.putExtra("EMAIL", email);
            String code = etDigit1.getText().toString()+""+etDigit2.getText().toString()+""+etDigit3.getText().toString()+""+etDigit4.getText().toString()+""+etDigit5.getText().toString()+""+etDigit6.getText().toString();
            intent.putExtra("RESET_CODE", code);


            VerifyCodeRequest request = new VerifyCodeRequest(email, code);

            authService.verifyCode(request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful()) {
                        // Usar el mensaje del servidor si está disponible
                        ApiResponse apiResponse = response.body();
                        String message = apiResponse.getMessage();

                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "No se pudo validar el codigo. Intenta más tarde.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
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
