package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiService;
import com.camilo.cocinarte.utils.SessionManager;

public class Tarjeta_creditoActivity extends AppCompatActivity {

    private static final String TAG = "TarjetaCreditoActivity";

    private EditText edtNombre, edtCC, edtNumeroTarjeta, edtFechaExpiracion, edtCVC;
    private TextView txtPais;
    private Button btnFinalizarCompra;
    private ImageButton btnBack;
    private String tipoPlan;

    // Servicios
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tarjeta_credito);

        // Configurar insets
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar servicios
        apiService = new ApiService(this);
        sessionManager = new SessionManager(this);

        // Obtener el tipo de plan del intent
        tipoPlan = getIntent().getStringExtra("tipo_plan");
        if (tipoPlan == null) {
            tipoPlan = "pro"; // Por defecto
        }

        Log.d(TAG, "💳 Iniciando pago para plan: " + tipoPlan);

        inicializarVistas();
        configurarEventos();
        configurarFormatoFecha();
    }

    private void inicializarVistas() {
        edtNombre = findViewById(R.id.edtNombre);
        edtCC = findViewById(R.id.edtCC);
        edtNumeroTarjeta = findViewById(R.id.edtNumeroTarjeta);
        edtFechaExpiracion = findViewById(R.id.edtFechaExpiracion);
        edtCVC = findViewById(R.id.edtCVC);
        txtPais = findViewById(R.id.txtPais);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);
        btnBack = findViewById(R.id.btnBack);
    }

    private void configurarEventos() {
        btnBack.setOnClickListener(v -> finish());
        btnFinalizarCompra.setOnClickListener(v -> {
            if (validarDatos()) {
                procesarPagoConBackend();
            }
        });
    }

    private void configurarFormatoFecha() {
        edtFechaExpiracion.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;
            String separador = "/";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;
                String texto = s.toString().replaceAll("[^\\d]", "");
                StringBuilder formateado = new StringBuilder();

                for (int i = 0; i < texto.length() && i < 4; i++) {
                    if (i == 2) {
                        formateado.append(separador);
                    }
                    formateado.append(texto.charAt(i));
                }

                s.replace(0, s.length(), formateado.toString());
                isFormatting = false;
            }
        });
    }

    private boolean validarDatos() {
        if (TextUtils.isEmpty(edtNombre.getText())) {
            mostrarError("Por favor, ingrese su nombre");
            return false;
        }

        if (TextUtils.isEmpty(edtCC.getText())) {
            mostrarError("Por favor, ingrese su número de CC");
            return false;
        }

        String numeroTarjeta = edtNumeroTarjeta.getText().toString();
        if (TextUtils.isEmpty(numeroTarjeta) || numeroTarjeta.length() < 13 || numeroTarjeta.length() > 16) {
            mostrarError("Por favor, ingrese un número de tarjeta válido");
            return false;
        }

        String fechaExp = edtFechaExpiracion.getText().toString();
        if (TextUtils.isEmpty(fechaExp) || fechaExp.length() != 5 || !fechaExp.contains("/")) {
            mostrarError("Por favor, ingrese una fecha de expiración válida (MM/AA)");
            return false;
        }

        try {
            int mes = Integer.parseInt(fechaExp.substring(0, 2));
            if (mes < 1 || mes > 12) {
                mostrarError("Mes de expiración inválido");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Formato de fecha inválido");
            return false;
        }

        String cvc = edtCVC.getText().toString();
        if (TextUtils.isEmpty(cvc) || cvc.length() < 3 || cvc.length() > 4) {
            mostrarError("Por favor, ingrese un código CVC válido");
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    /**
     * 💳 MÉTODO PRINCIPAL - Procesar pago con el backend
     */
    private void procesarPagoConBackend() {
        Log.d(TAG, "💳 Iniciando procesamiento de pago con backend...");

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Error: Debes iniciar sesión para procesar el pago", Toast.LENGTH_LONG).show();
            return;
        }

        // Deshabilitar botón para evitar doble click
        btnFinalizarCompra.setEnabled(false);
        btnFinalizarCompra.setText("Procesando...");

        // Crear referencia única para el pago
        String referenciaPago = "CARD_" + sessionManager.getUserId() + "_" + System.currentTimeMillis();

        // Obtener datos de la tarjeta
        String nombreTitular = edtNombre.getText().toString().trim();
        String numeroTarjeta = edtNumeroTarjeta.getText().toString().trim();

        Log.d(TAG, "📋 Datos del pago:");
        Log.d(TAG, "  - Tipo plan: " + tipoPlan);
        Log.d(TAG, "  - Referencia: " + referenciaPago);
        Log.d(TAG, "  - Titular: " + nombreTitular);

        // Procesar pago
        apiService.procesarPagoPlanPro(
                token,
                "tarjeta", // método de pago
                referenciaPago,
                49900.0, // monto en pesos colombianos
                new ApiService.PagoCallback() {
                    @Override
                    public void onSuccess(String message, String referencia) {
                        Log.d(TAG, "✅ Pago procesado exitosamente: " + referencia);

                        runOnUiThread(() -> {
                            Toast.makeText(Tarjeta_creditoActivity.this,
                                    "✅ " + message, Toast.LENGTH_LONG).show();

                            // Ir al formulario de plan nutricional
                            Intent intent = new Intent(Tarjeta_creditoActivity.this, formulario_plan_nutricional.class);
                            intent.putExtra("nombre_usuario", nombreTitular);
                            intent.putExtra("metodo_pago", "tarjeta");
                            intent.putExtra("tipo_plan", tipoPlan);
                            intent.putExtra("referencia_pago", referencia);
                            intent.putExtra("pago_procesado", true);

                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "❌ Error procesando pago: " + error);

                        runOnUiThread(() -> {
                            // Rehabilitar botón
                            btnFinalizarCompra.setEnabled(true);
                            btnFinalizarCompra.setText("Finalizar Compra");

                            Toast.makeText(Tarjeta_creditoActivity.this,
                                    "Error procesando pago: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }
}