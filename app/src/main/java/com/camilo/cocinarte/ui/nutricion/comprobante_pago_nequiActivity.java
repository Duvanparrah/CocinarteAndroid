package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class comprobante_pago_nequiActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnFinalizarCompra;
    private String numeroNequi;
    private String tipoPlan; // Nuevo campo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comprobante_pago_nequi);

        // Configuración de los márgenes del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent
        numeroNequi = getIntent().getStringExtra("numero_nequi");
        tipoPlan = getIntent().getStringExtra("tipo_plan");
        if (tipoPlan == null) {
            tipoPlan = "pro"; // Por defecto
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnFinalizarCompra.setOnClickListener(v -> finalizarCompraNequi());
    }

    private void finalizarCompraNequi() {
        Toast.makeText(this, "Pago con Nequi procesado exitosamente", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, formulario_plan_nutricional.class);
        intent.putExtra("numero_nequi", numeroNequi);
        intent.putExtra("metodo_pago", "nequi");
        intent.putExtra("tipo_plan", tipoPlan); // Pasar el tipo de plan

        startActivity(intent);
        finish();
    }
}