package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class Metodo_de_pago_Activity extends AppCompatActivity {

    private RadioButton rbCreditCard, rbNequi;
    private CardView cardOptionCard, nequiOptionCard;
    private Button pago;
    private ImageButton btnBack;
    private String selectedPaymentMethod = "credit_card";
    private LinearLayout nequiInputLayout;
    private EditText nequiNumberInput;
    private String tipoPlan; // Nuevo campo para almacenar el tipo de plan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_metodo_de_pago);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener el tipo de plan del intent
        tipoPlan = getIntent().getStringExtra("tipo_plan");
        if (tipoPlan == null) {
            tipoPlan = "pro"; // Por defecto
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        rbCreditCard = findViewById(R.id.rbCreditCard);
        rbNequi = findViewById(R.id.rbNequi);
        cardOptionCard = findViewById(R.id.cardOptionCard);
        nequiOptionCard = findViewById(R.id.nequiOptionCard);
        pago = findViewById(R.id.pago);
        btnBack = findViewById(R.id.btnBack);
        nequiInputLayout = findViewById(R.id.nequiInputLayout);
        nequiNumberInput = findViewById(R.id.nequiNumberInput);

        // Estado inicial
        rbCreditCard.setChecked(true);
        rbNequi.setChecked(false);
        nequiInputLayout.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(view -> finish());

        rbCreditCard.setOnClickListener(view -> updatePaymentMethod("credit_card"));
        rbNequi.setOnClickListener(view -> updatePaymentMethod("nequi"));

        cardOptionCard.setOnClickListener(view -> updatePaymentMethod("credit_card"));
        nequiOptionCard.setOnClickListener(view -> updatePaymentMethod("nequi"));

        // Formatear número de Nequi
        nequiNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().replaceAll("[^\\d]", "");
                if (texto.length() > 10) {
                    texto = texto.substring(0, 10);
                }
                if (!texto.equals(s.toString())) {
                    nequiNumberInput.removeTextChangedListener(this);
                    nequiNumberInput.setText(texto);
                    nequiNumberInput.setSelection(texto.length());
                    nequiNumberInput.addTextChangedListener(this);
                }
            }
        });

        pago.setOnClickListener(view -> proceedToPayment());
    }

    private void updatePaymentMethod(String method) {
        selectedPaymentMethod = method;

        if (method.equals("credit_card")) {
            rbCreditCard.setChecked(true);
            rbNequi.setChecked(false);
            if (nequiInputLayout.getVisibility() == View.VISIBLE) {
                animateNequiInputLayout(false);
            }
        } else if (method.equals("nequi")) {
            rbCreditCard.setChecked(false);
            rbNequi.setChecked(true);
            if (nequiInputLayout.getVisibility() != View.VISIBLE) {
                animateNequiInputLayout(true);
            }
        }
    }

    private void animateNequiInputLayout(final boolean show) {
        if (show) {
            nequiInputLayout.setVisibility(View.VISIBLE);
            nequiInputLayout.setAlpha(0f);
            nequiInputLayout.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            nequiInputLayout.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> nequiInputLayout.setVisibility(View.GONE))
                    .start();
        }
    }

    private void proceedToPayment() {
        if (selectedPaymentMethod.equals("credit_card")) {
            Intent intent = new Intent(this, Tarjeta_creditoActivity.class);
            intent.putExtra("tipo_plan", tipoPlan); // Pasar el tipo de plan
            startActivity(intent);
        } else if (selectedPaymentMethod.equals("nequi")) {
            String nequiNumber = nequiNumberInput.getText().toString().trim();
            if (nequiNumber.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el número de Nequi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nequiNumber.length() < 10) {
                Toast.makeText(this, "El número de Nequi debe tener al menos 10 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, comprobante_pago_nequiActivity.class);
            intent.putExtra("numero_nequi", nequiNumber);
            intent.putExtra("tipo_plan", tipoPlan); // Pasar el tipo de plan
            startActivity(intent);
        }
    }
}