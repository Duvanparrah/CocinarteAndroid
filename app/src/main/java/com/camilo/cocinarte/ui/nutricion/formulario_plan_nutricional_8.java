package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

import java.util.ArrayList;

public class formulario_plan_nutricional_8 extends AppCompatActivity {

    // Variables para recibir datos del formulario anterior
    private String metodoPago, nombreUsuario, numeroNequi, tipoPlan;
    private String objetivo, sexo, edad, altura, peso, nivelActividad, entrenamientoFuerza;
    private ArrayList<String> ingredientesSeleccionados;

    // Views del layout
    private ImageView btnBack;
    private ProgressBar caloriesProgress;
    private TextView caloriesConsumed, caloriesTotal;
    private Button btnContinuar; // ✅ AGREGADO: Botón continuar

    // Datos nutricionales calculados
    private double imcCalculado = 0;
    private double tmbCalculado = 0;
    private double caloriasDiarias = 0;
    private double caloriasObjetivo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional8);

        // Obtener datos del formulario anterior
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Mostrar datos recibidos
        mostrarDatosRecibidos();

        // Calcular y mostrar datos nutricionales
        calcularYMostrarDatosNutricionales();

        // Inicializar la lógica específica del formulario 8
        inicializarFormulario8();
    }

    /**
     * 🎨 Inicializar todas las vistas del layout
     */
    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        caloriesProgress = findViewById(R.id.calories_progress);
        btnContinuar = findViewById(R.id.btnContinuar); // ✅ CORREGIDO: Conectar botón continuar

        // Buscar TextViews de calorías en el círculo central (pueden tener IDs diferentes)
        // Si no tienen ID específico, los actualizaremos programáticamente

        Log.d("Formulario8", "🎨 Vistas inicializadas");

        // Verificar que el botón continuar existe
        if (btnContinuar != null) {
            Log.d("Formulario8", "✅ Botón continuar encontrado correctamente");
        } else {
            Log.e("Formulario8", "❌ ERROR: Botón continuar no encontrado en el layout");
        }
    }

    /**
     * 🔗 Configurar listeners de los elementos interactivos - COMPLETAMENTE CORREGIDO
     */
    private void setupListeners() {
        // Botón atrás - con verificación null
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d("Formulario8", "🔙 Botón atrás presionado");
                onBackPressed();
            });
        } else {
            Log.w("Formulario8", "⚠️ Botón atrás no encontrado");
        }

        // ✅ CORREGIDO: Botón continuar - PRINCIPAL CAMBIO
        if (btnContinuar != null) {
            btnContinuar.setOnClickListener(v -> {
                Log.d("Formulario8", "🚀 Botón continuar presionado - Navegando al formulario 9");
                Toast.makeText(this, "Continuando al paso final...", Toast.LENGTH_SHORT).show();
                continuarAlFormulario9();
            });
            Log.d("Formulario8", "✅ Listener del botón continuar configurado correctamente");
        } else {
            Log.e("Formulario8", "❌ ERROR: No se pudo configurar el listener del botón continuar");
            // Fallback: Mostrar mensaje de error
            Toast.makeText(this, "Error: Botón continuar no disponible", Toast.LENGTH_SHORT).show();
        }

        // Opcional: Click en el círculo de calorías como alternativa
        if (caloriesProgress != null) {
            caloriesProgress.setOnClickListener(v -> {
                Log.d("Formulario8", "📊 Círculo de calorías presionado");
                Toast.makeText(this, "Toca 'Continuar' para proceder", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 📨 Obtener todos los datos del Intent
     */
    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Datos básicos del plan
            metodoPago = intent.getStringExtra("metodo_pago");
            nombreUsuario = intent.getStringExtra("nombre_usuario");
            numeroNequi = intent.getStringExtra("numero_nequi");
            tipoPlan = intent.getStringExtra("tipo_plan");

            // Datos nutricionales
            objetivo = intent.getStringExtra("objetivo");
            sexo = intent.getStringExtra("sexo");
            edad = intent.getStringExtra("edad");
            altura = intent.getStringExtra("altura");
            peso = intent.getStringExtra("peso");
            nivelActividad = intent.getStringExtra("nivel_actividad");
            entrenamientoFuerza = intent.getStringExtra("entrenamiento_fuerza");

            // Ingredientes seleccionados
            ingredientesSeleccionados = intent.getStringArrayListExtra("ingredientes_seleccionados");
            if (ingredientesSeleccionados == null) {
                ingredientesSeleccionados = new ArrayList<>();
            }

            Log.d("Formulario8", "📋 Datos recibidos correctamente");
            Log.d("Formulario8", "👤 Usuario: " + nombreUsuario);
            Log.d("Formulario8", "🥗 Ingredientes: " + ingredientesSeleccionados.size());
        } else {
            Log.w("Formulario8", "⚠️ No se recibieron datos del formulario anterior");
        }
    }

    /**
     * 📊 Mostrar resumen de datos recibidos
     */
    private void mostrarDatosRecibidos() {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Plan Nutricional Generado 🎉\n");
        mensaje.append("Usuario: ").append(nombreUsuario != null ? nombreUsuario : "No especificado").append("\n");
        mensaje.append("Objetivo: ").append(obtenerNombreObjetivo(objetivo)).append("\n");
        mensaje.append("Ingredientes: ").append(ingredientesSeleccionados.size()).append(" seleccionados");

        Toast.makeText(this, mensaje.toString(), Toast.LENGTH_LONG).show();

        // Log detallado
        Log.d("Formulario8", "=== PLAN NUTRICIONAL GENERADO ===");
        Log.d("Formulario8", "Usuario: " + nombreUsuario);
        Log.d("Formulario8", "Objetivo: " + objetivo);
        Log.d("Formulario8", "Peso: " + peso + " kg");
        Log.d("Formulario8", "Altura: " + altura + " cm");
        Log.d("Formulario8", "Nivel Actividad: " + nivelActividad);
        Log.d("Formulario8", "Ingredientes: " + ingredientesSeleccionados.toString());
    }

    /**
     * 🧮 Calcular y mostrar datos nutricionales
     */
    private void calcularYMostrarDatosNutricionales() {
        try {
            if (peso != null && altura != null && !peso.isEmpty() && !altura.isEmpty()) {
                double pesoNum = Double.parseDouble(peso);
                double alturaNum = Double.parseDouble(altura) / 100.0; // Convertir cm a metros

                // Calcular IMC
                imcCalculado = pesoNum / (alturaNum * alturaNum);

                // Calcular TMB (Tasa Metabólica Basal)
                if (edad != null && !edad.isEmpty()) {
                    int edadNum = Integer.parseInt(edad);

                    if ("Masculino".equals(sexo) || "masculino".equals(sexo)) {
                        tmbCalculado = 88.362 + (13.397 * pesoNum) + (4.799 * (alturaNum * 100)) - (5.677 * edadNum);
                    } else if ("Femenino".equals(sexo) || "femenino".equals(sexo)) {
                        tmbCalculado = 447.593 + (9.247 * pesoNum) + (3.098 * (alturaNum * 100)) - (4.330 * edadNum);
                    }
                }

                // Aplicar factor de actividad
                double factorActividad = obtenerFactorActividad(nivelActividad);
                caloriasDiarias = tmbCalculado * factorActividad;

                // Ajustar según objetivo
                caloriasObjetivo = ajustarCaloriasPorObjetivo(caloriasDiarias, objetivo);

                // Actualizar la UI con los datos calculados
                actualizarUIConDatosNutricionales();

                Log.d("Formulario8", "📊 Datos calculados - IMC: " + String.format("%.1f", imcCalculado) +
                        ", Calorías objetivo: " + String.format("%.0f", caloriasObjetivo));

            } else {
                Log.w("Formulario8", "⚠️ Datos de peso o altura faltantes para cálculos");
            }
        } catch (NumberFormatException e) {
            Log.e("Formulario8", "❌ Error calculando datos nutricionales: " + e.getMessage());
            // Usar valores por defecto
            caloriasObjetivo = 2000; // Valor por defecto
        }
    }

    /**
     * 🎨 Actualizar la UI con los datos nutricionales calculados
     */
    private void actualizarUIConDatosNutricionales() {
        // Actualizar el progreso del círculo de calorías (ejemplo: 75% del objetivo)
        if (caloriesProgress != null) {
            int progreso = 75; // Ejemplo: 75% del objetivo diario
            caloriesProgress.setProgress(progreso);
            Log.d("Formulario8", "📊 Progreso de calorías actualizado: " + progreso + "%");
        }

        // Aquí puedes actualizar otros TextViews si tienen IDs específicos
        // Por ejemplo, si hay TextViews para mostrar las calorías:
        /*
        TextView tvCaloriasConsumed = findViewById(R.id.tv_calories_consumed);
        TextView tvCaloriasTotal = findViewById(R.id.tv_calories_total);

        if (tvCaloriasConsumed != null) {
            tvCaloriasConsumed.setText(String.format("%.0f kcal", caloriasObjetivo * 0.75));
        }
        if (tvCaloriasTotal != null) {
            tvCaloriasTotal.setText(String.format("%.0f Kcal", caloriasObjetivo));
        }
        */
    }

    /**
     * 🚀 Inicializar la lógica específica del formulario 8
     */
    private void inicializarFormulario8() {
        Toast.makeText(this, "¡Plan nutricional personalizado generado! 🎉", Toast.LENGTH_SHORT).show();

        // Mostrar ingredientes disponibles
        if (!ingredientesSeleccionados.isEmpty()) {
            StringBuilder ingredientesTexto = new StringBuilder("Basado en tus ingredientes: ");
            for (int i = 0; i < Math.min(3, ingredientesSeleccionados.size()); i++) {
                ingredientesTexto.append(ingredientesSeleccionados.get(i));
                if (i < Math.min(2, ingredientesSeleccionados.size() - 1)) {
                    ingredientesTexto.append(", ");
                }
            }
            if (ingredientesSeleccionados.size() > 3) {
                ingredientesTexto.append(" y ").append(ingredientesSeleccionados.size() - 3).append(" más");
            }

            Log.d("Formulario8", ingredientesTexto.toString());
        }

        Log.d("Formulario8", "✅ Formulario 8 inicializado correctamente");
    }

    /**
     * ➡️ MÉTODO PRINCIPAL - Continuar al formulario 9 - MEJORADO
     */
    private void continuarAlFormulario9() {
        Log.d("Formulario8", "🚀 Iniciando navegación al formulario 9...");

        try {
            // Validar que tenemos datos mínimos necesarios
            if (nombreUsuario == null || nombreUsuario.isEmpty()) {
                Log.w("Formulario8", "⚠️ Advertencia: Nombre de usuario no disponible");
                nombreUsuario = "Usuario"; // Valor por defecto
            }

            // Crear Intent para ir al formulario 9
            Intent intentFormulario9 = new Intent(this, formulario_plan_nutricional_9.class);

            // Pasar todos los datos anteriores
            intentFormulario9.putExtra("metodo_pago", metodoPago);
            intentFormulario9.putExtra("nombre_usuario", nombreUsuario);
            intentFormulario9.putExtra("numero_nequi", numeroNequi);
            intentFormulario9.putExtra("tipo_plan", tipoPlan);
            intentFormulario9.putExtra("objetivo", objetivo);
            intentFormulario9.putExtra("sexo", sexo);
            intentFormulario9.putExtra("edad", edad);
            intentFormulario9.putExtra("altura", altura);
            intentFormulario9.putExtra("peso", peso);
            intentFormulario9.putExtra("nivel_actividad", nivelActividad);
            intentFormulario9.putExtra("entrenamiento_fuerza", entrenamientoFuerza);

            // Pasar ingredientes seleccionados
            intentFormulario9.putStringArrayListExtra("ingredientes_seleccionados",
                    new ArrayList<>(ingredientesSeleccionados));

            // Pasar datos nutricionales calculados
            intentFormulario9.putExtra("imc_calculado", imcCalculado);
            intentFormulario9.putExtra("tmb_calculado", tmbCalculado);
            intentFormulario9.putExtra("calorias_diarias", caloriasDiarias);
            intentFormulario9.putExtra("calorias_objetivo", caloriasObjetivo);

            Log.d("Formulario8", "📦 Datos preparados para enviar al formulario 9");
            Log.d("Formulario8", "👤 Usuario: " + nombreUsuario);
            Log.d("Formulario8", "📊 Calorías objetivo: " + caloriasObjetivo);
            Log.d("Formulario8", "🥗 Ingredientes: " + ingredientesSeleccionados.size());

            // Iniciar el formulario 9
            startActivity(intentFormulario9);

            Toast.makeText(this, "✅ Continuando al paso final...", Toast.LENGTH_SHORT).show();
            Log.d("Formulario8", "✅ Navegación al formulario 9 exitosa");

        } catch (Exception e) {
            Log.e("Formulario8", "❌ Error navegando al formulario 9: " + e.getMessage());
            Toast.makeText(this, "Error al continuar. Intenta nuevamente.", Toast.LENGTH_SHORT).show();
        }
    }

    // ============================================
    // MÉTODOS DE CÁLCULO NUTRICIONAL
    // ============================================

    private double obtenerFactorActividad(String nivel) {
        if (nivel == null) return 1.2;
        switch (nivel) {
            case "sedentario": return 1.2;
            case "ligera": return 1.375;
            case "moderado": return 1.55;
            case "muy_activo": return 1.725;
            case "extremadamente_activo": return 1.9;
            default: return 1.2;
        }
    }

    private double ajustarCaloriasPorObjetivo(double calorias, String obj) {
        if (obj == null) return calorias;
        switch (obj) {
            case "perder_grasa": return calorias * 0.8; // Reducir 20%
            case "ganar_musculo": return calorias * 1.15; // Aumentar 15%
            case "mantener_peso": return calorias; // Mantener
            default: return calorias;
        }
    }

    private String obtenerNombreObjetivo(String obj) {
        if (obj == null) return "No especificado";
        switch (obj) {
            case "perder_grasa": return "Perder Grasa";
            case "ganar_musculo": return "Ganar Masa Muscular";
            case "mantener_peso": return "Mantener Peso";
            default: return obj;
        }
    }

    private String obtenerNombreNivel(String nivel) {
        if (nivel == null) return "No especificado";
        switch (nivel) {
            case "sedentario": return "Sedentario";
            case "ligera": return "Actividad Ligera";
            case "moderado": return "Moderadamente Activo";
            case "muy_activo": return "Muy Activo";
            case "extremadamente_activo": return "Extremadamente Activo";
            default: return nivel;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("Formulario8", "🔙 Regresando al formulario anterior");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Formulario8", "🧹 Formulario 8 destruido");
    }
}