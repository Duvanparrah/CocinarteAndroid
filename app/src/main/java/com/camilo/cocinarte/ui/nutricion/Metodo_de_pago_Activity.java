package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
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
    private String selectedPaymentMethod = "credit_card"; // Por defecto
    private LinearLayout nequiInputLayout;
    private EditText nequiNumberInput;
    private int nequiInputOriginalHeight = 0;

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

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Inicializar vistas
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

        // Ocultar inicialmente el layout de Nequi
        nequiInputLayout.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Configurar el botón de regreso
        btnBack.setOnClickListener(view -> finish());

        // Configurar los RadioButtons
        rbCreditCard.setOnClickListener(view -> {
            updatePaymentMethod("credit_card");
        });

        rbNequi.setOnClickListener(view -> {
            updatePaymentMethod("nequi");
        });

        // Hacer que las tarjetas completas sean cliqueables
        cardOptionCard.setOnClickListener(view -> {
            updatePaymentMethod("credit_card");
        });

        nequiOptionCard.setOnClickListener(view -> {
            updatePaymentMethod("nequi");
        });

        // Configurar el botón de pago
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
        // Simplificar la animación para garantizar que el EditText sea funcional
        if (show) {
            // Hacer visible con una animación fade in
            nequiInputLayout.setVisibility(View.VISIBLE);
            nequiInputLayout.setAlpha(0f);
            nequiInputLayout.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            // Animar fade out y ocultar al finalizar
            nequiInputLayout.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(() -> nequiInputLayout.setVisibility(View.GONE))
                    .start();
        }
    }

    private void proceedToPayment() {
        // Aquí iría la lógica para proceder al pago según el método seleccionado
        if (selectedPaymentMethod.equals("credit_card")) {
            // Navegar a la pantalla de ingreso de datos de tarjeta
            Intent intent = new Intent(this, Tarjeta_creditoActivity.class);
            startActivity(intent);
        } else if (selectedPaymentMethod.equals("nequi")) {
            // Verificar que se haya ingresado un número de Nequi
            String nequiNumber = nequiNumberInput.getText().toString().trim();
            if (nequiNumber.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el número de Nequi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Aquí puedes agregar validación adicional del número si es necesario
            if (nequiNumber.length() < 10) {
                Toast.makeText(this, "El número de Nequi debe tener al menos 10 dígitos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Navegar a la pantalla de comprobante de pago de Nequi
            Intent intent = new Intent(this, comprobante_pago_nequiActivity.class);
            // Opcionalmente, puedes pasar el número de Nequi como extra si lo necesitas en la siguiente pantalla
            intent.putExtra("nequi_number", nequiNumber);
            startActivity(intent);
        }
    }
}