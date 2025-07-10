package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.camilo.cocinarte.R;

public class formulario_plan_nutricional_4 extends AppCompatActivity {

    private ImageView backArrow;
    private CardView cardSedentario, cardLigera, cardModerado, cardMuyActivo, cardExtremadamenteActivo;
    private Button btnContinuar;

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

    // Variable para almacenar el nivel de actividad seleccionado
    private String nivelActividad = "";

    // Variables para controlar la selección visual
    private CardView cardSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional4);

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
        }
    }

    private void initViews() {
        backArrow = findViewById(R.id.backArrow);

        // Inicializar CardViews
        cardSedentario = findViewById(R.id.cardSedentario);
        cardLigera = findViewById(R.id.cardLigera);
        cardModerado = findViewById(R.id.cardModerado);
        cardMuyActivo = findViewById(R.id.cardMuyActivo);
        cardExtremadamenteActivo = findViewById(R.id.cardExtremadamenteActivo);

        // Inicializar botón
        btnContinuar = findViewById(R.id.btnContinuar);
    }

    private void setupListeners() {
        // Configurar botón de regreso
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Configurar listeners para las opciones de actividad
        cardSedentario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarNivelActividad("sedentario", cardSedentario);
            }
        });

        cardLigera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarNivelActividad("ligera", cardLigera);
            }
        });

        cardModerado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarNivelActividad("moderado", cardModerado);
            }
        });

        cardMuyActivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarNivelActividad("muy_activo", cardMuyActivo);
            }
        });

        cardExtremadamenteActivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarNivelActividad("extremadamente_activo", cardExtremadamenteActivo);
            }
        });

        // ✅ CORREGIDO: Configurar botón continuar para ir directamente al formulario 6
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuarSiguienteFormulario();
            }
        });
    }

    private void seleccionarNivelActividad(String nivel, CardView cardView) {
        // Guardar el nivel de actividad seleccionado
        nivelActividad = nivel;

        // Resetear el estado visual de todas las cards
        resetearEstadoVisualCards();

        // Marcar la card seleccionada visualmente
        cardSeleccionado = cardView;
        marcarCardSeleccionada(cardView);

        // Habilitar botón continuar
        habilitarBotonContinuar();

        // Mostrar mensaje de confirmación
        String nombreNivel = obtenerNombreNivel(nivel);
        Toast.makeText(this, "Seleccionado: " + nombreNivel, Toast.LENGTH_SHORT).show();
    }

    private void resetearEstadoVisualCards() {
        // Resetear todas las cards al color por defecto
        // Si no tienes drawables personalizados, usa colores básicos
        cardSedentario.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardLigera.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardModerado.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardMuyActivo.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        cardExtremadamenteActivo.setCardBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void marcarCardSeleccionada(CardView cardView) {
        // Marcar la card seleccionada con color verde claro
        cardView.setCardBackgroundColor(getResources().getColor(R.color.verde_claro));
    }

    private void habilitarBotonContinuar() {
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f);
    }

    private void deshabilitarBotonContinuar() {
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f);
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

    private void mostrarInformacionRecibida() {
        String mensaje = "Datos recibidos - ";
        mensaje += "Sexo: " + (sexo != null ? sexo : "No especificado") + " | ";
        mensaje += "Edad: " + (edad != null ? edad : "No especificada") + " | ";
        mensaje += "Altura: " + (altura != null ? altura : "No especificada") + " cm | ";
        mensaje += "Peso: " + (peso != null ? peso : "No especificado") + " kg";

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void continuarSiguienteFormulario() {
        if (nivelActividad.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona tu nivel de actividad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar mensaje de confirmación
        String nombreNivel = obtenerNombreNivel(nivelActividad);
        Toast.makeText(this, "Nivel de actividad guardado: " + nombreNivel, Toast.LENGTH_SHORT).show();

        // ✅ CORREGIDO: Navegar al formulario 6
        navegarAlFormulario6();
    }

    // ✅ MÉTODO PRINCIPAL: Navegar al formulario 6 (entrenamiento de fuerza)
    private void navegarAlFormulario6() {
        Toast.makeText(this, "🚀 Continuando al formulario 6 (Entrenamiento de fuerza)...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, formulario_plan_nutricional_6.class);

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

        startActivity(intent);

        // Opcional: finalizar esta actividad
        // finish();
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
}