package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiService;
import com.camilo.cocinarte.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class formulario_plan_nutricional_9 extends AppCompatActivity {

    private static final String TAG = "FormularioNutricional9";

    // Variables para recibir datos de formularios anteriores
    private String metodoPago, nombreUsuario, numeroNequi, tipoPlan;
    private String objetivo, sexo, edad, altura, peso, nivelActividad, entrenamientoFuerza;
    private ArrayList<String> ingredientesSeleccionados;

    // Datos nutricionales calculados
    private double imcCalculado = 0;
    private double tmbCalculado = 0;
    private double caloriasDiarias = 0;
    private double caloriasObjetivo = 0;

    // Variables para el plan nutricional
    private int selectedDayIndex = 3; // Jueves por defecto
    private List<TextView> dayViews;

    // Views del layout
    private ImageView btnBack;
    private TextView tvCaloriesConsumed, tvCaloriesTotal;
    private TextView tvCarbohidratos, tvProteinas, tvGrasas;
    private LinearLayout mealsContainer;

    // Servicios
    private ApiService apiService;
    private SessionManager sessionManager;

    // Variables para las comidas generadas
    private List<MealData> comidasDelDia;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional9);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "✅ onCreate iniciado correctamente");

        // Inicializar servicios
        apiService = new ApiService(this);
        sessionManager = new SessionManager(this);
        random = new Random();

        // Inicializar variables
        dayViews = new ArrayList<>();
        comidasDelDia = new ArrayList<>();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Obtener datos del intent
        obtenerDatosIntent();

        // Configurar calendario semanal
        setupWeeklyCalendar();

        // ✅ GENERAR PLAN NUTRICIONAL AUTOMÁTICAMENTE
        generarPlanNutricional();

        Log.d(TAG, "✅ onCreate completado exitosamente");
    }

    /**
     * 🎨 Inicializar todas las vistas del layout
     */
    private void initViews() {
        try {
            btnBack = findViewById(R.id.btn_back);

            // TextViews de calorías
            tvCaloriesConsumed = findViewById(R.id.tv_calories_consumed);
            tvCaloriesTotal = findViewById(R.id.tv_calories_total);

            // TextViews de macronutrientes
            tvCarbohidratos = findViewById(R.id.tv_carbohidratos);
            tvProteinas = findViewById(R.id.tv_proteinas);
            tvGrasas = findViewById(R.id.tv_grasas);

            // Container de comidas
            mealsContainer = findViewById(R.id.meals_container);

            // Views de los días de la semana
            try {
                dayViews.add(findViewById(R.id.day_monday));
                dayViews.add(findViewById(R.id.day_tuesday));
                dayViews.add(findViewById(R.id.day_wednesday));
                dayViews.add(findViewById(R.id.day_thursday));
                dayViews.add(findViewById(R.id.day_friday));
                dayViews.add(findViewById(R.id.day_saturday));
                dayViews.add(findViewById(R.id.day_sunday));
            } catch (Exception e) {
                Log.w(TAG, "⚠️ Algunos días de la semana no están disponibles en el layout");
            }

            Log.d(TAG, "🎨 Vistas inicializadas correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando vistas: " + e.getMessage());
        }
    }

    /**
     * 🔗 Configurar listeners de los elementos interactivos
     */
    private void setupListeners() {
        try {
            // Botón atrás
            if (btnBack != null) {
                btnBack.setOnClickListener(v -> {
                    Log.d(TAG, "🔙 Botón atrás presionado");
                    onBackPressed();
                });
            }

            // Configurar clicks en días de la semana (solo para plan Pro)
            if ("pro".equals(tipoPlan)) {
                configurarClicksDiasSemana();
            }

            Log.d(TAG, "✅ Listeners configurados correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando listeners: " + e.getMessage());
        }
    }

    /**
     * 📅 Configurar clicks en días de la semana (solo plan Pro)
     */
    private void configurarClicksDiasSemana() {
        for (int i = 0; i < dayViews.size(); i++) {
            TextView dayView = dayViews.get(i);
            if (dayView != null) {
                final int dayIndex = i;
                dayView.setOnClickListener(v -> seleccionarDia(dayIndex));
            }
        }
    }

    /**
     * 📨 Obtener todos los datos del Intent
     */
    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            try {
                // Datos básicos del plan
                metodoPago = intent.getStringExtra("metodo_pago");
                nombreUsuario = intent.getStringExtra("nombre_usuario");
                numeroNequi = intent.getStringExtra("numero_nequi");
                tipoPlan = intent.getStringExtra("tipo_plan");

                // Datos nutricionales del usuario
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

                // Datos calculados
                imcCalculado = intent.getDoubleExtra("imc_calculado", 0);
                tmbCalculado = intent.getDoubleExtra("tmb_calculado", 0);
                caloriasDiarias = intent.getDoubleExtra("calorias_diarias", 0);
                caloriasObjetivo = intent.getDoubleExtra("calorias_objetivo", 0);

                Log.d(TAG, "📋 Datos recibidos correctamente");
                Log.d(TAG, "📋 Tipo de plan: " + tipoPlan);
                Log.d(TAG, "📋 Calorías objetivo: " + caloriasObjetivo);

            } catch (Exception e) {
                Log.e(TAG, "❌ Error obteniendo datos: " + e.getMessage());
                inicializarValoresPorDefecto();
            }
        } else {
            inicializarValoresPorDefecto();
        }
    }

    /**
     * ⚙️ Inicializar valores por defecto
     */
    private void inicializarValoresPorDefecto() {
        if (ingredientesSeleccionados == null) {
            ingredientesSeleccionados = new ArrayList<>();
        }
        if (nombreUsuario == null || nombreUsuario.isEmpty()) {
            nombreUsuario = "Usuario";
        }
        if (caloriasObjetivo == 0) {
            caloriasObjetivo = 2000;
        }
        if (tipoPlan == null) {
            tipoPlan = "gratuito";
        }
        if (objetivo == null) {
            objetivo = "mantener_peso";
        }
    }

    /**
     * 🍽️ MÉTODO PRINCIPAL - Generar plan nutricional completo
     */
    private void generarPlanNutricional() {
        Log.d(TAG, "🍽️ Generando plan nutricional personalizado...");

        // Calcular distribución de calorías por comida
        double caloriasTotales = caloriasObjetivo > 0 ? caloriasObjetivo : 2000;

        // Distribución típica: Desayuno 25%, Almuerzo 35%, Cena 30%, Snack 10%
        double caloriasDesayuno = caloriasTotales * 0.25;
        double caloriasAlmuerzo = caloriasTotales * 0.35;
        double caloriasCena = caloriasTotales * 0.30;
        double caloriasSnack = caloriasTotales * 0.10;

        // Generar comidas basadas en el objetivo
        comidasDelDia.clear();
        comidasDelDia.add(generarDesayuno(caloriasDesayuno));
        comidasDelDia.add(generarAlmuerzo(caloriasAlmuerzo));
        comidasDelDia.add(generarCena(caloriasCena));
        comidasDelDia.add(generarSnack(caloriasSnack));

        // Calcular totales nutricionales
        calcularYMostrarTotalesNutricionales();

        // Mostrar comidas en la UI
        mostrarComidasEnUI();

        // Configurar según tipo de plan
        configurarSegunTipoPlan();

        Toast.makeText(this, "✅ Plan nutricional personalizado generado", Toast.LENGTH_LONG).show();
        Log.d(TAG, "✅ Plan nutricional generado exitosamente");
    }

    /**
     * 🥐 Generar desayuno personalizado
     */
    private MealData generarDesayuno(double calorias) {
        MealData desayuno = new MealData();
        desayuno.nombre = "Desayuno";
        desayuno.calorias = calorias;
        desayuno.ingredientes = new ArrayList<>();

        // Seleccionar ingredientes según objetivo
        if ("perder_grasa".equals(objetivo)) {
            desayuno.ingredientes.add("Avena con frutas (1 taza)");
            desayuno.ingredientes.add("Huevo cocido (1 unidad)");
            desayuno.ingredientes.add("Té verde (1 taza)");
            desayuno.proteinas = calorias * 0.25 / 4; // 25% proteínas
            desayuno.carbohidratos = calorias * 0.45 / 4; // 45% carbohidratos
            desayuno.grasas = calorias * 0.30 / 9; // 30% grasas
        } else if ("ganar_musculo".equals(objetivo)) {
            desayuno.ingredientes.add("Batido de proteína con avena");
            desayuno.ingredientes.add("Plátano (1 unidad)");
            desayuno.ingredientes.add("Almendras (15 unidades)");
            desayuno.proteinas = calorias * 0.30 / 4; // 30% proteínas
            desayuno.carbohidratos = calorias * 0.40 / 4; // 40% carbohidratos
            desayuno.grasas = calorias * 0.30 / 9; // 30% grasas
        } else {
            desayuno.ingredientes.add("Pan integral (2 rebanadas)");
            desayuno.ingredientes.add("Aguacate (1/4 pieza)");
            desayuno.ingredientes.add("Huevo (1 grande)");
            desayuno.ingredientes.add("Fresas (1 taza)");
            desayuno.proteinas = calorias * 0.20 / 4; // 20% proteínas
            desayuno.carbohidratos = calorias * 0.50 / 4; // 50% carbohidratos
            desayuno.grasas = calorias * 0.30 / 9; // 30% grasas
        }

        return desayuno;
    }

    /**
     * 🍽️ Generar almuerzo personalizado
     */
    private MealData generarAlmuerzo(double calorias) {
        MealData almuerzo = new MealData();
        almuerzo.nombre = "Almuerzo";
        almuerzo.calorias = calorias;
        almuerzo.ingredientes = new ArrayList<>();

        if ("perder_grasa".equals(objetivo)) {
            almuerzo.ingredientes.add("Pechuga de pollo a la plancha (150g)");
            almuerzo.ingredientes.add("Ensalada mixta abundante");
            almuerzo.ingredientes.add("Quinoa (1/2 taza cocida)");
            almuerzo.ingredientes.add("Aceite de oliva (1 cucharada)");
            almuerzo.proteinas = calorias * 0.35 / 4;
            almuerzo.carbohidratos = calorias * 0.35 / 4;
            almuerzo.grasas = calorias * 0.30 / 9;
        } else if ("ganar_musculo".equals(objetivo)) {
            almuerzo.ingredientes.add("Salmón a la plancha (180g)");
            almuerzo.ingredientes.add("Arroz integral (1 taza cocida)");
            almuerzo.ingredientes.add("Brócoli al vapor");
            almuerzo.ingredientes.add("Aguacate (1/2 pieza)");
            almuerzo.proteinas = calorias * 0.30 / 4;
            almuerzo.carbohidratos = calorias * 0.40 / 4;
            almuerzo.grasas = calorias * 0.30 / 9;
        } else {
            almuerzo.ingredientes.add("Pollo a la plancha (120g)");
            almuerzo.ingredientes.add("Arroz integral (1/2 taza cocida)");
            almuerzo.ingredientes.add("Ensalada mixta con aceite de oliva");
            almuerzo.ingredientes.add("Verduras al vapor");
            almuerzo.proteinas = calorias * 0.25 / 4;
            almuerzo.carbohidratos = calorias * 0.45 / 4;
            almuerzo.grasas = calorias * 0.30 / 9;
        }

        return almuerzo;
    }

    /**
     * 🌙 Generar cena personalizada
     */
    private MealData generarCena(double calorias) {
        MealData cena = new MealData();
        cena.nombre = "Cena";
        cena.calorias = calorias;
        cena.ingredientes = new ArrayList<>();

        if ("perder_grasa".equals(objetivo)) {
            cena.ingredientes.add("Pescado blanco al horno (150g)");
            cena.ingredientes.add("Verduras asadas variadas");
            cena.ingredientes.add("Ensalada verde");
            cena.proteinas = calorias * 0.40 / 4;
            cena.carbohidratos = calorias * 0.30 / 4;
            cena.grasas = calorias * 0.30 / 9;
        } else if ("ganar_musculo".equals(objetivo)) {
            cena.ingredientes.add("Carne magra (150g)");
            cena.ingredientes.add("Batata al horno (1 mediana)");
            cena.ingredientes.add("Espinacas salteadas");
            cena.ingredientes.add("Aceite de oliva");
            cena.proteinas = calorias * 0.30 / 4;
            cena.carbohidratos = calorias * 0.40 / 4;
            cena.grasas = calorias * 0.30 / 9;
        } else {
            cena.ingredientes.add("Salmón a la plancha (100g)");
            cena.ingredientes.add("Quinoa (1/2 taza cocida)");
            cena.ingredientes.add("Espárragos al vapor");
            cena.ingredientes.add("Ensalada mixta");
            cena.proteinas = calorias * 0.30 / 4;
            cena.carbohidratos = calorias * 0.40 / 4;
            cena.grasas = calorias * 0.30 / 9;
        }

        return cena;
    }

    /**
     * 🥜 Generar snack personalizado
     */
    private MealData generarSnack(double calorias) {
        MealData snack = new MealData();
        snack.nombre = "Snack";
        snack.calorias = calorias;
        snack.ingredientes = new ArrayList<>();

        if ("perder_grasa".equals(objetivo)) {
            snack.ingredientes.add("Yogur griego natural (1 taza)");
            snack.ingredientes.add("Arándanos (1/2 taza)");
            snack.proteinas = calorias * 0.40 / 4;
            snack.carbohidratos = calorias * 0.40 / 4;
            snack.grasas = calorias * 0.20 / 9;
        } else if ("ganar_musculo".equals(objetivo)) {
            snack.ingredientes.add("Batido de proteína");
            snack.ingredientes.add("Plátano (1 unidad)");
            snack.ingredientes.add("Mantequilla de almendras (1 cucharada)");
            snack.proteinas = calorias * 0.35 / 4;
            snack.carbohidratos = calorias * 0.35 / 4;
            snack.grasas = calorias * 0.30 / 9;
        } else {
            snack.ingredientes.add("Manzana (1 unidad)");
            snack.ingredientes.add("Almendras (15 unidades)");
            snack.proteinas = calorias * 0.15 / 4;
            snack.carbohidratos = calorias * 0.55 / 4;
            snack.grasas = calorias * 0.30 / 9;
        }

        return snack;
    }

    /**
     * 🧮 Calcular y mostrar totales nutricionales
     */
    private void calcularYMostrarTotalesNutricionales() {
        double totalCalorias = 0;
        double totalProteinas = 0;
        double totalCarbohidratos = 0;
        double totalGrasas = 0;

        for (MealData comida : comidasDelDia) {
            totalCalorias += comida.calorias;
            totalProteinas += comida.proteinas;
            totalCarbohidratos += comida.carbohidratos;
            totalGrasas += comida.grasas;
        }

        // Simular progreso del 25% para el día actual
        double progreso = 0.25;

        if (tvCaloriesConsumed != null) {
            tvCaloriesConsumed.setText(String.format("%.0f kcal", totalCalorias * progreso));
        }
        if (tvCaloriesTotal != null) {
            tvCaloriesTotal.setText(String.format("%.0f Kcal", totalCalorias));
        }

        if (tvCarbohidratos != null) {
            tvCarbohidratos.setText(String.format("%.0f g", totalCarbohidratos * progreso));
        }
        if (tvProteinas != null) {
            tvProteinas.setText(String.format("%.0f g", totalProteinas * progreso));
        }
        if (tvGrasas != null) {
            tvGrasas.setText(String.format("%.0f g", totalGrasas * progreso));
        }

        Log.d(TAG, "📊 Totales nutricionales calculados: " + totalCalorias + " kcal");
    }

    /**
     * 🎨 Mostrar comidas en la UI
     */
    private void mostrarComidasEnUI() {
        if (mealsContainer == null) {
            Log.w(TAG, "⚠️ mealsContainer es null, no se pueden mostrar las comidas");
            return;
        }

        // Limpiar contenedor
        mealsContainer.removeAllViews();

        // Crear vista para cada comida
        for (MealData comida : comidasDelDia) {
            CardView comidaView = crearVistaComida(comida);
            mealsContainer.addView(comidaView);
        }

        Log.d(TAG, "🎨 Comidas mostradas en la UI");
    }

    /**
     * 🏗️ Crear vista para una comida (MÉTODO CORREGIDO)
     */
    private CardView crearVistaComida(MealData comida) {
        // Crear CardView contenedor
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation(dpToPx(3));

        // LinearLayout principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        mainLayout.setBackgroundColor(getResources().getColor(android.R.color.white, null));

        // Título de la comida
        TextView tvNombreComida = new TextView(this);
        tvNombreComida.setText(comida.nombre);
        tvNombreComida.setTextSize(16);
        tvNombreComida.setTypeface(null, android.graphics.Typeface.BOLD);
        tvNombreComida.setTextColor(getResources().getColor(android.R.color.black, null));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, dpToPx(8));
        tvNombreComida.setLayoutParams(titleParams);

        // Container para información nutricional
        LinearLayout nutritionLayout = new LinearLayout(this);
        nutritionLayout.setOrientation(LinearLayout.HORIZONTAL);
        nutritionLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light, null));
        nutritionLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        nutritionLayout.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nutritionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nutritionParams.setMargins(0, 0, 0, dpToPx(12));
        nutritionLayout.setLayoutParams(nutritionParams);

        // Calorías
        TextView tvCalorias = createNutritionTextView(String.format("%.0f kcal", comida.calorias));
        nutritionLayout.addView(tvCalorias);

        // Proteínas
        TextView tvProteinas = createNutritionTextView(String.format("%.0f P", comida.proteinas));
        nutritionLayout.addView(tvProteinas);

        // Carbohidratos
        TextView tvCarbohidratos = createNutritionTextView(String.format("%.0f C", comida.carbohidratos));
        nutritionLayout.addView(tvCarbohidratos);

        // Grasas
        TextView tvGrasas = createNutritionTextView(String.format("%.0f G", comida.grasas));
        nutritionLayout.addView(tvGrasas);

        // Container para ingredientes
        LinearLayout ingredientesLayout = new LinearLayout(this);
        ingredientesLayout.setOrientation(LinearLayout.VERTICAL);
        ingredientesLayout.setBackgroundColor(getResources().getColor(android.R.color.background_light, null));
        ingredientesLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        // Agregar ingredientes
        for (String ingrediente : comida.ingredientes) {
            TextView tvIngrediente = new TextView(this);
            tvIngrediente.setText(ingrediente);
            tvIngrediente.setTextSize(13);
            tvIngrediente.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
            LinearLayout.LayoutParams ingredientParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            ingredientParams.setMargins(0, dpToPx(4), 0, dpToPx(4));
            tvIngrediente.setLayoutParams(ingredientParams);
            ingredientesLayout.addView(tvIngrediente);
        }

        // Ensamblar la vista
        mainLayout.addView(tvNombreComida);
        mainLayout.addView(nutritionLayout);
        mainLayout.addView(ingredientesLayout);
        cardView.addView(mainLayout);

        return cardView;
    }

    /**
     * 🏗️ Crear TextView para información nutricional
     */
    private TextView createNutritionTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);
        textView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        textView.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        textView.setLayoutParams(params);

        return textView;
    }

    /**
     * 🔧 Convertir dp a px
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * ⚙️ Configurar según tipo de plan
     */
    private void configurarSegunTipoPlan() {
        if ("gratuito".equals(tipoPlan)) {
            // Deshabilitar navegación entre días
            deshabilitarNavegacionDias();
            Toast.makeText(this, "Plan Gratuito - Solo día actual disponible", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Plan Pro - Navegación completa disponible", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🔒 Deshabilitar navegación entre días (plan gratuito)
     */
    private void deshabilitarNavegacionDias() {
        for (TextView dayView : dayViews) {
            if (dayView != null) {
                dayView.setClickable(false);
                dayView.setAlpha(0.5f);
            }
        }

        // Mostrar solo el día actual activo
        if (dayViews.size() > selectedDayIndex && dayViews.get(selectedDayIndex) != null) {
            TextView diaActual = dayViews.get(selectedDayIndex);
            diaActual.setAlpha(1.0f);
            diaActual.setBackgroundResource(R.drawable.day_selected_background);
            diaActual.setTextColor(getResources().getColor(android.R.color.white, null));
        }
    }

    /**
     * 📅 Configurar el calendario semanal dinámico
     */
    private void setupWeeklyCalendar() {
        try {
            String[] dayNames = {"L", "M", "M", "J", "V", "S", "D"};

            for (int i = 0; i < dayViews.size(); i++) {
                TextView dayView = dayViews.get(i);
                if (dayView != null) {
                    int dayNumber = 13 + i; // Días simulados
                    String dayText = dayNames[i] + "\n" + dayNumber;
                    dayView.setText(dayText);

                    // Marcar el día actual como seleccionado
                    if (i == selectedDayIndex) {
                        seleccionarDia(i);
                    }
                }
            }

            Log.d(TAG, "📅 Calendario semanal configurado");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error configurando calendario: " + e.getMessage());
        }
    }

    /**
     * 🗓️ Seleccionar un día específico
     */
    private void seleccionarDia(int dayIndex) {
        try {
            // Solo permitir en plan Pro
            if ("gratuito".equals(tipoPlan)) {
                Toast.makeText(this, "Upgrade a Plan Pro para ver otros días", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedDayIndex = dayIndex;

            // Actualizar apariencia visual
            for (int i = 0; i < dayViews.size(); i++) {
                TextView dayView = dayViews.get(i);
                if (dayView != null) {
                    if (i == dayIndex) {
                        dayView.setBackgroundResource(R.drawable.day_selected_background);
                        dayView.setTextColor(getResources().getColor(android.R.color.white, null));
                    } else {
                        dayView.setBackgroundResource(R.drawable.day_selector_background);
                        dayView.setTextColor(getResources().getColor(R.color.text_gray, null));
                    }
                }
            }

            // Regenerar comidas para el día seleccionado
            generarPlanNutricional();

            Log.d(TAG, "🗓️ Día seleccionado: " + dayIndex);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error seleccionando día: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Plan nutricional guardado", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🧹 Formulario 9 destruido");
    }

    /**
     * 📊 Clase para datos de comida
     */
    private static class MealData {
        public String nombre;
        public double calorias;
        public double proteinas;
        public double carbohidratos;
        public double grasas;
        public List<String> ingredientes;

        public MealData() {
            ingredientes = new ArrayList<>();
        }
    }
}