package com.camilo.cocinarte.ui.nutricion;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiService;
import com.camilo.cocinarte.utils.SessionManager; // ✅ IMPORT CORREGIDO

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlanCompletoActivity extends AppCompatActivity {

    private static final String TAG = "PlanCompletoActivity";

    private ImageView btnBack;
    private TextView tvTituloPlan, tvObjetivo, tvDuracion;
    private TextView tvCaloriasDiarias, tvProteinasDiarias, tvCarbohidratosDiarios, tvGrasasDiarias;
    private RecyclerView recyclerDias;

    private ApiService apiService;
    private SessionManager sessionManager;
    private String tipoPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // ✅ USAR LAYOUT TEMPORAL HASTA CREAR EL CORRECTO
        setContentView(R.layout.activity_formulario_plan_nutricional9);

        // Obtener tipo de plan del intent
        tipoPlan = getIntent().getStringExtra("tipo_plan");
        if (tipoPlan == null) {
            tipoPlan = "Pro";
        }

        // Inicializar servicios
        apiService = new ApiService(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
        cargarPlanCompleto();

        Log.d(TAG, "✅ PlanCompletoActivity iniciado para plan: " + tipoPlan);
    }

    private void initViews() {
        try {
            // ✅ USAR IDs QUE EXISTEN EN EL LAYOUT ACTUAL
            btnBack = findViewById(R.id.btn_back);

            // Para TextViews que no existen, usar findViewById defensivo
            tvTituloPlan = findViewById(R.id.tv_titulo_plan);
            if (tvTituloPlan == null) {
                // Buscar un TextView alternativo o crear uno dinámicamente
                tvTituloPlan = new TextView(this);
                tvTituloPlan.setText("Plan Nutricional");
            }

            tvObjetivo = findViewById(R.id.tv_objetivo);
            if (tvObjetivo == null) {
                tvObjetivo = new TextView(this);
            }

            tvDuracion = findViewById(R.id.tv_duracion);
            if (tvDuracion == null) {
                tvDuracion = new TextView(this);
            }

            tvCaloriasDiarias = findViewById(R.id.tv_calories_total);
            tvProteinasDiarias = findViewById(R.id.tv_proteinas);
            tvCarbohidratosDiarios = findViewById(R.id.tv_carbohidratos);
            tvGrasasDiarias = findViewById(R.id.tv_grasas);

            // Para RecyclerView, usar el container existente o crear uno simple
            recyclerDias = findViewById(R.id.recycler_dias);
            if (recyclerDias == null) {
                // Si no existe, mostrar solo información básica
                Toast.makeText(this, "Vista simplificada del plan", Toast.LENGTH_SHORT).show();
            } else {
                recyclerDias.setLayoutManager(new LinearLayoutManager(this));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inicializando vistas: " + e.getMessage());
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    /**
     * 📊 Cargar plan completo desde el backend
     */
    private void cargarPlanCompleto() {
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toast.makeText(this, "Cargando tu plan completo...", Toast.LENGTH_SHORT).show();

        apiService.obtenerPlanActivo(token, new ApiService.PlanNutricionalCallback() {
            @Override
            public void onSuccess(JSONObject planGenerado) {
                Log.d(TAG, "✅ Plan completo obtenido del backend");

                runOnUiThread(() -> {
                    try {
                        procesarPlanCompleto(planGenerado);
                        Toast.makeText(PlanCompletoActivity.this,
                                "✅ Plan cargado exitosamente", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Error procesando plan completo: " + e.getMessage());
                        mostrarPlanSimulado();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error obteniendo plan completo: " + error);

                runOnUiThread(() -> {
                    Toast.makeText(PlanCompletoActivity.this,
                            "Error cargando plan: " + error, Toast.LENGTH_LONG).show();

                    // Mostrar plan simulado como fallback
                    mostrarPlanSimulado();
                });
            }
        });
    }

    /**
     * 📊 Procesar el plan completo del backend
     */
    private void procesarPlanCompleto(JSONObject planGenerado) {
        try {
            JSONObject data = planGenerado.getJSONObject("data");

            // Información básica del plan
            String objetivo = data.optString("objetivo", "Mantener Peso");
            String tipoPlan = data.optString("tipo_plan", "Pro");
            int diasRestantes = data.optJSONObject("informacion_plan") != null ?
                    data.getJSONObject("informacion_plan").optInt("dias_restantes", 30) : 30;

            // Requerimientos nutricionales
            JSONObject requerimientos = data.optJSONObject("requerimientos_diarios");
            if (requerimientos == null) {
                requerimientos = data.optJSONObject("requerimientos_nutricionales");
            }

            double calorias = 2000;
            double proteinas = 150;
            double carbohidratos = 250;
            double grasas = 65;

            if (requerimientos != null) {
                calorias = requerimientos.optDouble("calorias", 2000);
                proteinas = requerimientos.optDouble("proteinas", 150);
                carbohidratos = requerimientos.optDouble("carbohidratos", 250);
                grasas = requerimientos.optDouble("grasas", 65);
            }

            // Actualizar UI
            actualizarUIConDatosReales(objetivo, tipoPlan, diasRestantes,
                    calorias, proteinas, carbohidratos, grasas);

            // Procesar plan semanal/mensual
            if (data.has("plan_semanal")) {
                JSONObject planSemanal = data.getJSONObject("plan_semanal");
                procesarDiasDePlan(planSemanal);
            } else {
                // Si no hay plan semanal, mostrar datos básicos
                mostrarInformacionBasica(objetivo, tipoPlan, calorias, proteinas, carbohidratos, grasas);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parseando plan completo: " + e.getMessage());
            mostrarPlanSimulado();
        }
    }

    /**
     * 🎨 Actualizar UI con datos reales
     */
    private void actualizarUIConDatosReales(String objetivo, String tipoPlan, int diasRestantes,
                                            double calorias, double proteinas, double carbohidratos, double grasas) {

        if (tvTituloPlan != null) {
            tvTituloPlan.setText("Plan Nutricional " + tipoPlan);
        }
        if (tvObjetivo != null) {
            tvObjetivo.setText("Objetivo: " + objetivo);
        }
        if (tvDuracion != null) {
            tvDuracion.setText(diasRestantes + " días restantes");
        }

        if (tvCaloriasDiarias != null) {
            tvCaloriasDiarias.setText(String.format("%.0f kcal", calorias));
        }
        if (tvProteinasDiarias != null) {
            tvProteinasDiarias.setText(String.format("%.0f g", proteinas));
        }
        if (tvCarbohidratosDiarios != null) {
            tvCarbohidratosDiarios.setText(String.format("%.0f g", carbohidratos));
        }
        if (tvGrasasDiarias != null) {
            tvGrasasDiarias.setText(String.format("%.0f g", grasas));
        }
    }

    /**
     * 📅 Procesar días del plan
     */
    private void procesarDiasDePlan(JSONObject planSemanal) {
        try {
            List<DayPlanData> diasPlan = new ArrayList<>();

            // Procesar según tipo de plan
            if ("Gratis".equals(tipoPlan)) {
                // Plan gratis: solo 1 día
                if (planSemanal.has("dia")) {
                    JSONObject dia = planSemanal.getJSONObject("dia");
                    DayPlanData dayData = parsearDiaPlan(dia);
                    diasPlan.add(dayData);
                }
            } else {
                // Plan Pro: múltiples días
                if (planSemanal.has("dias")) {
                    JSONArray dias = planSemanal.getJSONArray("dias");
                    for (int i = 0; i < Math.min(dias.length(), 7); i++) {
                        JSONObject dia = dias.getJSONObject(i);
                        DayPlanData dayData = parsearDiaPlan(dia);
                        diasPlan.add(dayData);
                    }
                }
            }

            // Solo configurar adapter si el RecyclerView existe
            if (recyclerDias != null && !diasPlan.isEmpty()) {
                DiasPlanAdapter adapter = new DiasPlanAdapter(diasPlan);
                recyclerDias.setAdapter(adapter);
            } else {
                // Mostrar información en TextViews si no hay RecyclerView
                mostrarDiasEnTexto(diasPlan);
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error procesando días del plan: " + e.getMessage());
        }
    }

    /**
     * 📋 Mostrar información básica cuando no se puede mostrar lista completa
     */
    private void mostrarInformacionBasica(String objetivo, String tipoPlan,
                                          double calorias, double proteinas,
                                          double carbohidratos, double grasas) {
        String info = String.format(
                "Plan %s\nObjetivo: %s\nCalorías diarias: %.0f kcal\nProteínas: %.0fg\nCarbohidratos: %.0fg\nGrasas: %.0fg",
                tipoPlan, objetivo, calorias, proteinas, carbohidratos, grasas
        );

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        Log.d(TAG, "📋 Plan básico: " + info);
    }

    /**
     * 📝 Mostrar días en formato texto si no hay RecyclerView
     */
    private void mostrarDiasEnTexto(List<DayPlanData> diasPlan) {
        if (diasPlan.isEmpty()) return;

        StringBuilder info = new StringBuilder("Plan de comidas:\n");
        for (DayPlanData dia : diasPlan) {
            info.append(String.format("\n%s: %.0f kcal", dia.nombreDia, dia.totalCalorias));
        }

        Log.d(TAG, "📝 Días del plan: " + info.toString());
    }

    /**
     * 📋 Parsear un día individual del plan
     */
    private DayPlanData parsearDiaPlan(JSONObject dia) throws JSONException {
        DayPlanData dayData = new DayPlanData();

        dayData.dia = dia.optInt("day", 1);
        dayData.fecha = dia.optString("date", "");
        dayData.nombreDia = dia.optString("dayName", "Lunes");

        // Totales del día
        if (dia.has("dailyTotals")) {
            JSONObject totales = dia.getJSONObject("dailyTotals");
            dayData.totalCalorias = totales.optDouble("calories", 0);
            dayData.totalProteinas = totales.optDouble("protein", 0);
            dayData.totalCarbohidratos = totales.optDouble("carbs", 0);
            dayData.totalGrasas = totales.optDouble("fats", 0);
        }

        // Comidas del día
        if (dia.has("meals")) {
            JSONObject comidas = dia.getJSONObject("meals");
            dayData.comidas = new ArrayList<>();

            // Procesar cada comida
            for (String tipoComida : new String[]{"desayuno", "almuerzo", "cena", "snack"}) {
                if (comidas.has(tipoComida)) {
                    JSONObject comida = comidas.getJSONObject(tipoComida);
                    MealData mealData = parsearComida(comida, tipoComida);
                    dayData.comidas.add(mealData);
                }
            }
        }

        return dayData;
    }

    /**
     * 🍽️ Parsear una comida individual
     */
    private MealData parsearComida(JSONObject comida, String tipo) throws JSONException {
        MealData mealData = new MealData();
        mealData.tipo = tipo;

        // Nutrición total de la comida
        if (comida.has("totalNutrition")) {
            JSONObject nutricion = comida.getJSONObject("totalNutrition");
            mealData.calorias = nutricion.optDouble("calories", 0);
            mealData.proteinas = nutricion.optDouble("protein", 0);
            mealData.carbohidratos = nutricion.optDouble("carbs", 0);
            mealData.grasas = nutricion.optDouble("fats", 0);
        }

        // Ingredientes de la comida
        if (comida.has("ingredients")) {
            JSONArray ingredientes = comida.getJSONArray("ingredients");
            mealData.ingredientes = new ArrayList<>();

            for (int i = 0; i < ingredientes.length(); i++) {
                JSONObject ingrediente = ingredientes.getJSONObject(i);
                String nombreIngrediente = ingrediente.optString("name", "Ingrediente");
                String cantidad = ingrediente.optString("quantity", "1") + " " +
                        ingrediente.optString("unit", "unidad");

                mealData.ingredientes.add(nombreIngrediente + " (" + cantidad + ")");
            }
        }

        return mealData;
    }

    /**
     * 📊 Mostrar plan simulado como fallback
     */
    private void mostrarPlanSimulado() {
        // Datos simulados
        actualizarUIConDatosReales("Mantener Peso", tipoPlan,
                "Gratis".equals(tipoPlan) ? 1 : 30, 2000, 150, 250, 65);

        Toast.makeText(this, "Mostrando plan simulado - " + tipoPlan, Toast.LENGTH_SHORT).show();
    }

    private String obtenerNombreDia(int index) {
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        return dias[index % 7];
    }

    // ============================================
    // CLASES DE DATOS
    // ============================================

    /**
     * 📊 Datos de un día del plan
     */
    public static class DayPlanData {
        public int dia;
        public String fecha;
        public String nombreDia;
        public double totalCalorias;
        public double totalProteinas;
        public double totalCarbohidratos;
        public double totalGrasas;
        public List<MealData> comidas;
    }

    /**
     * 🍽️ Datos de una comida
     */
    public static class MealData {
        public String tipo;
        public double calorias;
        public double proteinas;
        public double carbohidratos;
        public double grasas;
        public List<String> ingredientes;
    }

    /**
     * 📋 Adapter simplificado para RecyclerView (solo si existe)
     */
    private static class DiasPlanAdapter extends RecyclerView.Adapter<DiasPlanAdapter.DayViewHolder> {
        private List<DayPlanData> dias;

        public DiasPlanAdapter(List<DayPlanData> dias) {
            this.dias = dias;
        }

        @Override
        public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // ✅ USAR LAYOUT SIMPLE EXISTENTE
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new DayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DayViewHolder holder, int position) {
            DayPlanData dia = dias.get(position);
            holder.bind(dia);
        }

        @Override
        public int getItemCount() {
            return dias.size();
        }

        static class DayViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;

            public DayViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }

            public void bind(DayPlanData dia) {
                if (text1 != null) {
                    text1.setText(dia.nombreDia + " - " + dia.fecha);
                }
                if (text2 != null) {
                    text2.setText(String.format("%.0f kcal", dia.totalCalorias));
                }
            }
        }
    }
}