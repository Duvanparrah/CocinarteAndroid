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

public class formulario_plan_nutricional_6 extends AppCompatActivity {

    private ImageView backArrow;
    private LinearLayout opcionSi, opcionNo;
    private Button btnContinuar;
    // ❌ COMENTADO: Este botón no existe en el XML actual
    // private Button btnComenzarPlan;

    // Variables para recibir datos de formularios anteriores
    private String metodoPago;
    private String nombreUsuario;
    private String numeroNequi;
    private String tipoPlan;
    private String objetivo;
    private String sexo;
    private String edad;
    private String altura;
    private String peso;
    private String nivelActividad;

    // Variable para almacenar la respuesta sobre entrenamiento de fuerza
    private String entrenamientoFuerza = "";

    // Variables para controlar la selección visual
    private LinearLayout opcionSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional6);

        // Obtener datos del intent
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Deshabilitar botón continuar inicialmente
        deshabilitarBotonContinuar();

        // Mostrar información recibida
        mostrarInformacionRecibida();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            metodoPago = intent.getStringExtra("metodo_pago");
            nombreUsuario = intent.getStringExtra("nombre_usuario");
            numeroNequi = intent.getStringExtra("numero_nequi");
            tipoPlan = intent.getStringExtra("tipo_plan");
            objetivo = intent.getStringExtra("objetivo");
            sexo = intent.getStringExtra("sexo");
            edad = intent.getStringExtra("edad");
            altura = intent.getStringExtra("altura");
            peso = intent.getStringExtra("peso");
            nivelActividad = intent.getStringExtra("nivel_actividad");
        }
    }

    private void initViews() {
        backArrow = findViewById(R.id.backArrow);
        opcionSi = findViewById(R.id.opcionSi);
        opcionNo = findViewById(R.id.opcionNo);
        btnContinuar = findViewById(R.id.btnContinuar);

        // ❌ COMENTADO: Este botón no existe en el XML actual
        // btnComenzarPlan = findViewById(R.id.btnComenzarPlan);
    }

    private void setupListeners() {
        // Configurar botón de regreso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Configurar opción SÍ
        opcionSi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarEntrenamientoFuerza("si", opcionSi);
            }
        });

        // Configurar opción NO
        opcionNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarEntrenamientoFuerza("no", opcionNo);
            }
        });

        // ✅ FUNCIONANDO: Configurar botón continuar para ir al formulario 7
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navegarAlFormulario7();
            }
        });

        // ❌ COMENTADO: Este botón no existe en el XML actual - ESTA ERA LA LÍNEA 124 QUE CAUSABA EL ERROR
        /*
        btnComenzarPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(formulario_plan_nutricional_6.this, "¡Comenzando tu plan nutricional!", Toast.LENGTH_SHORT).show();
                navegarAlFormulario7();
            }
        });
        */
    }

    private void seleccionarEntrenamientoFuerza(String opcion, LinearLayout opcionView) {
        // Guardar la opción seleccionada
        entrenamientoFuerza = opcion;

        // Resetear el estado visual de todas las opciones
        resetearEstadoVisualOpciones();

        // Marcar la opción seleccionada visualmente
        opcionSeleccionada = opcionView;
        marcarOpcionSeleccionada(opcionView);

        // Habilitar botón continuar
        habilitarBotonContinuar();

        // Mostrar mensaje de confirmación
        String nombreOpcion = opcion.equals("si") ? "Sí" : "No";
        Toast.makeText(this, "Seleccionado: " + nombreOpcion, Toast.LENGTH_SHORT).show();
    }

    private void resetearEstadoVisualOpciones() {
        // Resetear ambas opciones al estado normal
        opcionSi.setBackground(getResources().getDrawable(R.drawable.circle_background));
        opcionNo.setBackground(getResources().getDrawable(R.drawable.circle_background));
    }

    private void marcarOpcionSeleccionada(LinearLayout opcionView) {
        // Marcar la opción seleccionada con un fondo diferente
        // Nota: Necesitarás crear un drawable para el estado seleccionado
        // Por ejemplo: circle_background_selected
        opcionView.setBackground(getResources().getDrawable(R.drawable.circle_background_selected));
    }

    private void habilitarBotonContinuar() {
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f);
    }

    private void deshabilitarBotonContinuar() {
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f);
    }

    private void mostrarInformacionRecibida() {
        String mensaje = "Datos recibidos - ";
        mensaje += "Nivel de actividad: " + (nivelActividad != null ? obtenerNombreNivel(nivelActividad) : "No especificado");

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private String obtenerNombreNivel(String nivel) {
        switch (nivel) {
            case "sedentario":
                return "Sedentario";
            case "ligera":
                return "Actividad Ligera";
            case "moderado":
                return "Moderadamente Activo";
            case "muy_activo":
                return "Muy Activo";
            case "extremadamente_activo":
                return "Extremadamente Activo";
            default:
                return nivel;
        }
    }

    private void continuarSiguienteFormulario() {
        if (entrenamientoFuerza.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona una opción", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar mensaje de confirmación
        String nombreOpcion = entrenamientoFuerza.equals("si") ? "Sí" : "No";
        Toast.makeText(this, "Entrenamiento de fuerza: " + nombreOpcion, Toast.LENGTH_SHORT).show();

        // Navegar al formulario 7
        navegarAlFormulario7();
    }

    private String obtenerNombreObjetivo(String obj) {
        switch (obj) {
            case "perder_grasa":
                return "Perder Grasa";
            case "ganar_musculo":
                return "Ganar Masa Muscular";
            case "mantener_peso":
                return "Mantener Peso";
            default:
                return obj;
        }
    }

    // ✅ MÉTODO PRINCIPAL - Navegar al formulario 7 (ingredientes)
    private void navegarAlFormulario7() {
        Toast.makeText(this, "🚀 Continuando al formulario 7 (Ingredientes)...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, formulario_plan_nutricional_7.class);

        // Pasar todos los datos recopilados al siguiente formulario
        intent.putExtra("metodo_pago", metodoPago);
        intent.putExtra("nombre_usuario", nombreUsuario);
        intent.putExtra("numero_nequi", numeroNequi);
        intent.putExtra("tipo_plan", tipoPlan);
        intent.putExtra("objetivo", objetivo);
        intent.putExtra("sexo", sexo);
        intent.putExtra("edad", edad);
        intent.putExtra("altura", altura);
        intent.putExtra("peso", peso);
        intent.putExtra("nivel_actividad", nivelActividad);
        intent.putExtra("entrenamiento_fuerza", entrenamientoFuerza);

        startActivity(intent);

        // Opcional: finalizar esta actividad
        // finish();
    }
}