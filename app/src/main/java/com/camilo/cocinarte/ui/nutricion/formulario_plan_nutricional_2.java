package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class formulario_plan_nutricional_2 extends AppCompatActivity {

    private ImageView backArrow;
    private Button btnContinuar;
    private LinearLayout opcionesLayout;
    private String objetivoSeleccionado = "";

    // Referencia a la opción seleccionada para el feedback visual
    private LinearLayout opcionActualmenteSeleccionada = null;

    // Variables para recibir datos del formulario anterior
    private String metodoPago;
    private String nombreUsuario;
    private String numeroNequi;
    private String tipoPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional2);

        // Configuración de los márgenes del sistema
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener datos del intent anterior
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Deshabilitar el botón continuar inicialmente
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f); // Hacerlo más transparente para indicar que está deshabilitado
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
        opcionesLayout = findViewById(R.id.opcionesLayout);
    }

    private void setupListeners() {
        // Configurar botón de regreso
        backArrow.setOnClickListener(v -> finish());

        // Configurar las opciones de objetivo
        configurarOpcionesObjetivo();

        // Configurar botón continuar
        btnContinuar.setOnClickListener(v -> {
            if (validarSeleccion()) {
                continuarSiguienteFormulario();
            }
        });
    }

    private void configurarOpcionesObjetivo() {
        // Obtener cada LinearLayout de opción y configurar click listeners
        LinearLayout opcion1 = (LinearLayout) opcionesLayout.getChildAt(0); // Perder Grasa
        LinearLayout opcion2 = (LinearLayout) opcionesLayout.getChildAt(1); // Ganar Masa Muscular
        LinearLayout opcion3 = (LinearLayout) opcionesLayout.getChildAt(2); // Mantener Peso

        opcion1.setOnClickListener(v -> seleccionarObjetivo("perder_grasa", opcion1, "Perder Grasa"));
        opcion2.setOnClickListener(v -> seleccionarObjetivo("ganar_musculo", opcion2, "Ganar Masa Muscular"));
        opcion3.setOnClickListener(v -> seleccionarObjetivo("mantener_peso", opcion3, "Mantener Peso"));
    }

    private void seleccionarObjetivo(String objetivo, LinearLayout opcionSeleccionada, String nombreObjetivo) {
        // Resetear el fondo de todas las opciones
        resetearTodasLasOpciones();

        // Marcar la nueva opción como seleccionada con color verde
        opcionSeleccionada.setBackgroundColor(getResources().getColor(R.color.verde));
        opcionSeleccionada.setAlpha(1.0f); // Asegurar que esté completamente opaco

        // Actualizar variables
        objetivoSeleccionado = objetivo;
        opcionActualmenteSeleccionada = opcionSeleccionada;

        // Habilitar el botón continuar
        habilitarBotonContinuar();

        // Mostrar feedback al usuario
        Toast.makeText(this, nombreObjetivo + " seleccionado", Toast.LENGTH_SHORT).show();
    }

    private void resetearTodasLasOpciones() {
        for (int i = 0; i < opcionesLayout.getChildCount(); i++) {
            View child = opcionesLayout.getChildAt(i);
            child.setBackgroundResource(R.drawable.boton_fondo); // Restaurar fondo original
            child.setAlpha(1.0f); // Mantener todas las opciones visibles por igual
        }
    }

    private void habilitarBotonContinuar() {
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f); // Hacer el botón completamente opaco cuando esté habilitado
    }

    private boolean validarSeleccion() {
        if (objetivoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona un objetivo antes de continuar", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void continuarSiguienteFormulario() {
        // Mostrar mensaje de confirmación
        Toast.makeText(this, "Continuando con " + obtenerNombreObjetivo(), Toast.LENGTH_SHORT).show();

        // Navegar al siguiente formulario (formulario_plan_nutricional_3)
        Intent intent = new Intent(this, formulario_plan_nutricional_3.class);

        // Pasar todos los datos acumulados
        intent.putExtra("metodo_pago", metodoPago);
        intent.putExtra("nombre_usuario", nombreUsuario);
        intent.putExtra("numero_nequi", numeroNequi);
        intent.putExtra("tipo_plan", tipoPlan);
        intent.putExtra("objetivo", objetivoSeleccionado);

        startActivity(intent);
    }

    private String obtenerNombreObjetivo() {
        switch (objetivoSeleccionado) {
            case "perder_grasa":
                return "Perder Grasa";
            case "ganar_musculo":
                return "Ganar Masa Muscular";
            case "mantener_peso":
                return "Mantener Peso";
            default:
                return "objetivo seleccionado";
        }
    }
}