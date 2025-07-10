package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;  // ← Este import faltaba
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class formulario_plan_nutricional extends AppCompatActivity {

    private ImageView backArrow;
    private Button btnContinuar;
    private String metodoPago;
    private String nombreUsuario;
    private String numeroNequi;
    private String tipoPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional);

        // Configuración de los márgenes del sistema
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Mostrar información del pago completado
        mostrarInformacionPago();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            metodoPago = intent.getStringExtra("metodo_pago");
            nombreUsuario = intent.getStringExtra("nombre_usuario");
            numeroNequi = intent.getStringExtra("numero_nequi");
            tipoPlan = intent.getStringExtra("tipo_plan");
        }
    }

    private void initViews() {
        backArrow = findViewById(R.id.backArrow);
        btnContinuar = findViewById(R.id.btnContinuar);
    }

    private void setupListeners() {
        // Configurar botón de regreso
        backArrow.setOnClickListener(v -> {
            // Opcional: mostrar confirmación antes de salir
            finish();
        });

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            continuarConFormulario();
        });
    }

    private void mostrarInformacionPago() {
        String mensaje;

        if ("gratuito".equals(tipoPlan)) {
            mensaje = "¡Perfecto! Has seleccionado el Plan Gratuito. Continúa para crear tu plan nutricional básico.";
        } else if ("pro".equals(tipoPlan)) {
            if ("tarjeta".equals(metodoPago)) {
                mensaje = "¡Pago con tarjeta exitoso! Plan Pro activado. Accede a todas las funciones premium.";
            } else if ("nequi".equals(metodoPago)) {
                mensaje = "¡Pago con Nequi exitoso! Plan Pro activado. Accede a todas las funciones premium.";
            } else {
                mensaje = "¡Plan Pro activado! Accede a todas las funciones premium.";
            }
        } else {
            mensaje = "¡Bienvenido! Continúa para crear tu plan nutricional personalizado.";
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void continuarConFormulario() {
        String tipoAcceso = "gratuito".equals(tipoPlan) ? "Plan Gratuito" : "Plan Pro";
        Toast.makeText(this, "Continuando con el formulario nutricional - " + tipoAcceso, Toast.LENGTH_SHORT).show();

        // Navegar al segundo formulario
        Intent intent = new Intent(this, formulario_plan_nutricional_2.class);
        intent.putExtra("metodo_pago", metodoPago);
        intent.putExtra("nombre_usuario", nombreUsuario);
        intent.putExtra("numero_nequi", numeroNequi);
        intent.putExtra("tipo_plan", tipoPlan);

        startActivity(intent);
    }
}