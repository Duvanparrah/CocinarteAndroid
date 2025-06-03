package com.camilo.cocinarte.ui.nutricion;

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

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);
    }

    private void setupListeners() {
        // Configurar el botón de retroceso
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra la actividad actual y regresa a la anterior
            }
        });

        // Configurar el botón de finalizar compra
        btnFinalizarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes implementar lo que sucede cuando se finaliza la compra
                Toast.makeText(comprobante_pago_nequiActivity.this,
                        "Compra finalizada exitosamente", Toast.LENGTH_SHORT).show();
                finish(); // Opcional: cierra la actividad después de finalizar
            }
        });
    }
}