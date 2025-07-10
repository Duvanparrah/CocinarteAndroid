package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class formulario_plan_nutricional_7 extends AppCompatActivity {

    private ImageView backArrow;
    private Button btnContinuar;
    private ProgressBar progressBar;
    private LinearLayout containerIngredientes;
    private TextView textTitulo, textContador;

    // Variables para datos del formulario anterior
    private String metodoPago, nombreUsuario, numeroNequi, tipoPlan;
    private String objetivo, sexo, edad, altura, peso, nivelActividad, entrenamientoFuerza;

    // ✅ ESTRUCTURA MEJORADA: Ingredientes organizados por categoría
    private Map<String, List<Ingrediente>> ingredientesPorCategoria = new HashMap<>();
    private Map<String, List<Integer>> ingredientesSeleccionadosPorCategoria = new HashMap<>();
    private Map<String, Integer> minimosRequeridos = new HashMap<>();

    // RequestQueue para Volley
    private RequestQueue requestQueue;

    // URL del backend
    private static final String BASE_URL = "https://cocinarte-backend-production.up.railway.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional7);

        Log.d("FormularioNutricional", "🚀 === INICIANDO FORMULARIO NUTRICIONAL 7 ===");
        Log.d("FormularioNutricional", "🔗 Backend URL: " + BASE_URL);

        // Inicializar Volley
        requestQueue = Volley.newRequestQueue(this);

        // Obtener datos del intent
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Inicializar mínimos requeridos
        inicializarMinimosRequeridos();

        // Deshabilitar botón continuar inicialmente
        deshabilitarBotonContinuar();

        // ✅ AGREGAR TEST DE CONEXIÓN (quitar después de confirmar que funciona)
        testConexionBackend();

        // CARGAR INGREDIENTES DINÁMICAMENTE DESDE EL BACKEND
        cargarIngredientesDesdeBackend();

        // Mostrar información recibida
        mostrarInformacionRecibida();
    }

    /**
     * 📊 Inicializar mínimos requeridos por categoría
     */
    private void inicializarMinimosRequeridos() {
        minimosRequeridos.put("Verduras", 3);
        minimosRequeridos.put("Frutas", 2);
        minimosRequeridos.put("Carnes", 2);
        minimosRequeridos.put("Pescados y Mariscos", 1);
        minimosRequeridos.put("Lácteos", 2);
        minimosRequeridos.put("Granos y Legumbres", 2);
        minimosRequeridos.put("Cereales", 2);
        minimosRequeridos.put("Huevos", 1);
        minimosRequeridos.put("Frutos secos y Semillas", 1);
        minimosRequeridos.put("Aceites y Grasas", 1);
        minimosRequeridos.put("Bebidas", 1);
        minimosRequeridos.put("Productos Procesados", 0);
    }

    private void initViews() {
        backArrow = findViewById(R.id.backArrow);
        btnContinuar = findViewById(R.id.btnContinuar);
        progressBar = findViewById(R.id.progressBar);
        textTitulo = findViewById(R.id.textTitulo);
        textContador = findViewById(R.id.textContador);

        // Crear container dinámico
        android.widget.ScrollView scrollView = findViewById(R.id.scrollView);
        containerIngredientes = new LinearLayout(this);
        containerIngredientes.setOrientation(LinearLayout.VERTICAL);
        containerIngredientes.setPadding(16, 16, 16, 32);

        scrollView.removeAllViews();
        scrollView.addView(containerIngredientes);
    }

    private void setupListeners() {
        backArrow.setOnClickListener(v -> finish());

        btnContinuar.setOnClickListener(v -> {
            if (validarSeleccionMinima()) {
                continuarSiguienteFormulario();
            } else {
                mostrarErrorValidacion();
            }
        });
    }

    /**
     * 🧪 MÉTODO DE PRUEBA: Test de conexión con el backend
     */
    private void testConexionBackend() {
        Log.d("FormularioNutricional", "🧪 === INICIANDO TEST DE CONEXIÓN ===");

        // Test 1: Verificar conectividad básica
        String urlTest = BASE_URL + "/api/ingredientes/test/status";

        JsonObjectRequest testRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlTest,
                null,
                response -> {
                    Log.d("FormularioNutricional", "✅ TEST 1 EXITOSO - Servidor responde");
                    Log.d("FormularioNutricional", "📄 Respuesta test: " + response.toString());

                    // Test 2: Probar endpoint de categorías
                    testEndpointCategorias();
                },
                error -> {
                    Log.e("FormularioNutricional", "❌ TEST 1 FALLIDO - Error de conectividad");
                    if (error.networkResponse != null) {
                        Log.e("FormularioNutricional", "❌ Código: " + error.networkResponse.statusCode);
                    }

                    // Test 3: Probar endpoint alternativo
                    testEndpointAlternativo();
                }
        );

        testRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1.0f));
        requestQueue.add(testRequest);
    }

    private void testEndpointCategorias() {
        Log.d("FormularioNutricional", "🧪 === TEST 2: ENDPOINT CATEGORÍAS ===");

        String urlCategorias = BASE_URL + "/api/ingredientes/categorias";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                urlCategorias,
                null,
                response -> {
                    Log.d("FormularioNutricional", "✅ TEST 2 EXITOSO - Endpoint categorías funciona");
                    Log.d("FormularioNutricional", "📊 Claves en respuesta: " + response.keys().toString());

                    // Verificar estructura de respuesta
                    if (response.has("data")) {
                        try {
                            JSONObject data = response.getJSONObject("data");
                            if (data.has("ingredientes_por_categoria")) {
                                JSONObject categorias = data.getJSONObject("ingredientes_por_categoria");
                                Log.d("FormularioNutricional", "✅ Estructura correcta - Categorías encontradas: " + categorias.keys().toString());
                            } else {
                                Log.w("FormularioNutricional", "⚠️ Falta 'ingredientes_por_categoria' en data");
                            }
                        } catch (JSONException e) {
                            Log.e("FormularioNutricional", "❌ Error parseando data: " + e.getMessage());
                        }
                    } else {
                        Log.w("FormularioNutricional", "⚠️ Respuesta no tiene campo 'data'");
                    }
                },
                error -> {
                    Log.e("FormularioNutricional", "❌ TEST 2 FALLIDO - Endpoint categorías no funciona");
                    if (error.networkResponse != null) {
                        Log.e("FormularioNutricional", "❌ Código: " + error.networkResponse.statusCode);
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("FormularioNutricional", "❌ Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("FormularioNutricional", "❌ Error leyendo error body: " + e.getMessage());
                        }
                    }

                    testEndpointAlternativo();
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1.0f));
        requestQueue.add(request);
    }

    private void testEndpointAlternativo() {
        Log.d("FormularioNutricional", "🧪 === TEST 3: ENDPOINT ALTERNATIVO ===");

        String urlAlternativo = BASE_URL + "/api/ingredientes";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                urlAlternativo,
                null,
                response -> {
                    Log.d("FormularioNutricional", "✅ TEST 3 EXITOSO - Endpoint alternativo funciona");
                    Log.d("FormularioNutricional", "📊 Claves en respuesta alternativa: " + response.keys().toString());

                    if (response.has("ingredientes")) {
                        try {
                            JSONArray ingredientes = response.getJSONArray("ingredientes");
                            Log.d("FormularioNutricional", "✅ Array de ingredientes encontrado con " + ingredientes.length() + " elementos");
                        } catch (JSONException e) {
                            Log.e("FormularioNutricional", "❌ Error parseando ingredientes: " + e.getMessage());
                        }
                    } else {
                        Log.w("FormularioNutricional", "⚠️ Respuesta alternativa no tiene 'ingredientes'");
                    }
                },
                error -> {
                    Log.e("FormularioNutricional", "❌ TEST 3 FALLIDO - Endpoint alternativo no funciona");
                    if (error.networkResponse != null) {
                        Log.e("FormularioNutricional", "❌ Código: " + error.networkResponse.statusCode);
                    }

                    Log.e("FormularioNutricional", "❌ === TODOS LOS TESTS FALLARON ===");
                    Log.e("FormularioNutricional", "❌ Posibles causas:");
                    Log.e("FormularioNutricional", "❌ 1. Sin conexión a internet");
                    Log.e("FormularioNutricional", "❌ 2. Servidor backend inaccesible");
                    Log.e("FormularioNutricional", "❌ 3. URL incorrecta: " + BASE_URL);
                    Log.e("FormularioNutricional", "❌ 4. Problema con certificados SSL");
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 1.0f));
        requestQueue.add(request);
    }

    /**
     * 🌟 MÉTODO PRINCIPAL CORREGIDO - Cargar ingredientes dinámicamente desde el backend
     */
    private void cargarIngredientesDesdeBackend() {
        Log.d("FormularioNutricional", "🔄 Iniciando carga de ingredientes...");

        mostrarLoading(true);

        // ✅ CAMBIO 1: Usar la URL correcta del endpoint
        String url = BASE_URL + "/api/ingredientes/categorias";

        Log.d("FormularioNutricional", "🔗 URL completa: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    Log.d("FormularioNutricional", "✅ Respuesta recibida exitosamente");
                    Log.d("FormularioNutricional", "📄 Respuesta JSON: " + response.toString());
                    procesarIngredientesResponse(response);
                    mostrarLoading(false);
                },
                error -> {
                    Log.e("FormularioNutricional", "❌ Error cargando ingredientes: " + error.getMessage());

                    // ✅ CAMBIO 2: Mejor manejo de errores con más información
                    if (error.networkResponse != null) {
                        Log.e("FormularioNutricional", "❌ Código de error HTTP: " + error.networkResponse.statusCode);
                        try {
                            String errorBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("FormularioNutricional", "❌ Cuerpo del error: " + errorBody);
                        } catch (Exception e) {
                            Log.e("FormularioNutricional", "❌ Error al leer cuerpo de respuesta: " + e.getMessage());
                        }
                    } else {
                        Log.e("FormularioNutricional", "❌ Error de red sin respuesta del servidor");
                    }

                    // ✅ CAMBIO 3: Intentar con endpoint alternativo
                    cargarIngredientesEndpointAlternativo();
                }
        );

        // ✅ CAMBIO 4: Configurar timeout más largo y reintentos
        request.setRetryPolicy(new DefaultRetryPolicy(
                20000, // 20 segundos de timeout
                2, // 2 reintentos
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    /**
     * 🔄 NUEVO MÉTODO: Endpoint alternativo en caso de fallo
     */
    private void cargarIngredientesEndpointAlternativo() {
        Log.d("FormularioNutricional", "🔄 Intentando con endpoint alternativo...");

        String urlAlternativa = BASE_URL + "/api/ingredientes";

        Log.d("FormularioNutricional", "🔗 URL alternativa: " + urlAlternativa);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                urlAlternativa,
                null,
                response -> {
                    Log.d("FormularioNutricional", "✅ Respuesta alternativa recibida");
                    Log.d("FormularioNutricional", "📄 Respuesta JSON alternativa: " + response.toString());
                    procesarRespuestaAlternativa(response);
                    mostrarLoading(false);
                },
                error -> {
                    Log.e("FormularioNutricional", "❌ Error con endpoint alternativo: " + error.getMessage());

                    if (error.networkResponse != null) {
                        Log.e("FormularioNutricional", "❌ Código de error alternativo: " + error.networkResponse.statusCode);
                    }

                    // Si ambos endpoints fallan, usar ingredientes por defecto
                    mostrarErrorYFallback();
                    mostrarLoading(false);
                }
        );

        // Configurar timeout para el endpoint alternativo
        request.setRetryPolicy(new DefaultRetryPolicy(
                15000, // 15 segundos
                1, // 1 reintento
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    /**
     * 📊 MÉTODO CORREGIDO: Procesar respuesta del endpoint principal
     */
    private void procesarIngredientesResponse(JSONObject response) {
        try {
            Log.d("FormularioNutricional", "📊 Iniciando procesamiento de respuesta...");

            // ✅ CAMBIO 5: Manejar diferentes estructuras de respuesta
            JSONObject data;
            JSONObject ingredientesPorCategoriaJson;

            // Verificar si la respuesta tiene el campo "data"
            if (response.has("data")) {
                Log.d("FormularioNutricional", "📋 Respuesta tiene campo 'data'");
                data = response.getJSONObject("data");

                if (data.has("ingredientes_por_categoria")) {
                    ingredientesPorCategoriaJson = data.getJSONObject("ingredientes_por_categoria");
                    Log.d("FormularioNutricional", "✅ Encontrado 'ingredientes_por_categoria' en data");
                } else {
                    Log.e("FormularioNutricional", "❌ No se encontró 'ingredientes_por_categoria' en data");
                    throw new JSONException("No se encontró 'ingredientes_por_categoria' en data");
                }
            } else if (response.has("ingredientes_por_categoria")) {
                // Respuesta directa sin campo "data"
                Log.d("FormularioNutricional", "📋 Respuesta directa con 'ingredientes_por_categoria'");
                ingredientesPorCategoriaJson = response.getJSONObject("ingredientes_por_categoria");
            } else {
                Log.e("FormularioNutricional", "❌ Estructura de respuesta no reconocida");
                Log.e("FormularioNutricional", "❌ Claves disponibles: " + response.keys().toString());
                throw new JSONException("Estructura de respuesta no reconocida");
            }

            Log.d("FormularioNutricional", "📋 Categorías encontradas: " + ingredientesPorCategoriaJson.keys().toString());

            // Limpiar estructura anterior
            ingredientesPorCategoria.clear();
            ingredientesSeleccionadosPorCategoria.clear();

            // ✅ CAMBIO 6: Procesar todas las categorías dinámicamente
            Iterator<String> categorias = ingredientesPorCategoriaJson.keys();
            int totalIngredientes = 0;

            while (categorias.hasNext()) {
                String categoria = categorias.next();

                try {
                    JSONArray ingredientesArray = ingredientesPorCategoriaJson.getJSONArray(categoria);
                    List<Ingrediente> ingredientesCategoria = new ArrayList<>();

                    Log.d("FormularioNutricional", "🔍 Procesando categoría: " + categoria + " con " + ingredientesArray.length() + " ingredientes");

                    for (int i = 0; i < ingredientesArray.length(); i++) {
                        JSONObject ingredienteJson = ingredientesArray.getJSONObject(i);

                        // ✅ VALIDAR que el ingrediente tenga los campos necesarios
                        if (!ingredienteJson.has("id_ingrediente") || !ingredienteJson.has("nombre_ingrediente")) {
                            Log.w("FormularioNutricional", "⚠️ Ingrediente incompleto en posición " + i + ": " + ingredienteJson.toString());
                            continue;
                        }

                        Ingrediente ingrediente = new Ingrediente(
                                ingredienteJson.getInt("id_ingrediente"),
                                ingredienteJson.getString("nombre_ingrediente"),
                                categoria,
                                ingredienteJson.optString("imagen", null)
                        );

                        ingredientesCategoria.add(ingrediente);
                    }

                    if (!ingredientesCategoria.isEmpty()) {
                        ingredientesPorCategoria.put(categoria, ingredientesCategoria);
                        ingredientesSeleccionadosPorCategoria.put(categoria, new ArrayList<>());
                        totalIngredientes += ingredientesCategoria.size();

                        Log.d("FormularioNutricional", "✅ Categoría " + categoria + ": " + ingredientesCategoria.size() + " ingredientes añadidos");
                    } else {
                        Log.w("FormularioNutricional", "⚠️ Categoría " + categoria + " está vacía");
                    }

                } catch (JSONException e) {
                    Log.e("FormularioNutricional", "❌ Error procesando categoría " + categoria + ": " + e.getMessage());
                }
            }

            Log.d("FormularioNutricional", "📊 RESUMEN FINAL:");
            Log.d("FormularioNutricional", "  📊 Total ingredientes cargados: " + totalIngredientes);
            Log.d("FormularioNutricional", "  📊 Total categorías: " + ingredientesPorCategoria.size());

            // ✅ CAMBIO 7: Verificar que se cargaron ingredientes
            if (totalIngredientes == 0) {
                Log.w("FormularioNutricional", "⚠️ No se cargaron ingredientes válidos, usando fallback");
                crearIngredientesPorDefecto();
            } else {
                // Crear la UI dinámicamente
                Log.d("FormularioNutricional", "🎨 Creando interfaz de usuario...");
                crearUIIngredientes();
            }

        } catch (JSONException e) {
            Log.e("FormularioNutricional", "❌ Error JSON procesando ingredientes: " + e.getMessage());
            e.printStackTrace();
            crearIngredientesPorDefecto();
        } catch (Exception e) {
            Log.e("FormularioNutricional", "❌ Error general procesando ingredientes: " + e.getMessage());
            e.printStackTrace();
            crearIngredientesPorDefecto();
        }
    }

    /**
     * 🔄 NUEVO MÉTODO: Procesar respuesta del endpoint alternativo
     */
    private void procesarRespuestaAlternativa(JSONObject response) {
        try {
            Log.d("FormularioNutricional", "📊 Procesando respuesta alternativa...");

            JSONArray ingredientesArray = null;

            // Manejar diferentes estructuras de respuesta
            if (response.has("ingredientes")) {
                ingredientesArray = response.getJSONArray("ingredientes");
                Log.d("FormularioNutricional", "✅ Encontrado array 'ingredientes'");
            } else if (response.has("data")) {
                JSONObject data = response.getJSONObject("data");
                if (data.has("ingredientes")) {
                    ingredientesArray = data.getJSONArray("ingredientes");
                    Log.d("FormularioNutricional", "✅ Encontrado array 'ingredientes' en data");
                }
            }

            if (ingredientesArray == null) {
                Log.e("FormularioNutricional", "❌ No se encontraron ingredientes en respuesta alternativa");
                throw new JSONException("No se encontraron ingredientes en la respuesta");
            }

            Log.d("FormularioNutricional", "📊 Total ingredientes en respuesta alternativa: " + ingredientesArray.length());

            // Organizar ingredientes por categoría
            ingredientesPorCategoria.clear();
            ingredientesSeleccionadosPorCategoria.clear();

            for (int i = 0; i < ingredientesArray.length(); i++) {
                JSONObject ingredienteJson = ingredientesArray.getJSONObject(i);

                String categoria = ingredienteJson.optString("categoria", "Otros");

                if (!ingredientesPorCategoria.containsKey(categoria)) {
                    ingredientesPorCategoria.put(categoria, new ArrayList<>());
                    ingredientesSeleccionadosPorCategoria.put(categoria, new ArrayList<>());
                }

                // Validar campos esenciales
                if (!ingredienteJson.has("id_ingrediente") || !ingredienteJson.has("nombre_ingrediente")) {
                    Log.w("FormularioNutricional", "⚠️ Ingrediente incompleto en respuesta alternativa: " + ingredienteJson.toString());
                    continue;
                }

                Ingrediente ingrediente = new Ingrediente(
                        ingredienteJson.getInt("id_ingrediente"),
                        ingredienteJson.getString("nombre_ingrediente"),
                        categoria,
                        ingredienteJson.optString("imagen", null)
                );

                ingredientesPorCategoria.get(categoria).add(ingrediente);
            }

            Log.d("FormularioNutricional", "✅ Ingredientes organizados desde endpoint alternativo en " + ingredientesPorCategoria.size() + " categorías");
            crearUIIngredientes();

        } catch (JSONException e) {
            Log.e("FormularioNutricional", "❌ Error JSON en respuesta alternativa: " + e.getMessage());
            crearIngredientesPorDefecto();
        } catch (Exception e) {
            Log.e("FormularioNutricional", "❌ Error general en respuesta alternativa: " + e.getMessage());
            crearIngredientesPorDefecto();
        }
    }

    /**
     * 🎨 Crear la interfaz de usuario dinámicamente
     */
    private void crearUIIngredientes() {
        runOnUiThread(() -> {
            // Limpiar container
            containerIngredientes.removeAllViews();

            // Crear secciones por categoría
            for (Map.Entry<String, List<Ingrediente>> entrada : ingredientesPorCategoria.entrySet()) {
                String categoria = entrada.getKey();
                List<Ingrediente> ingredientes = entrada.getValue();

                // Solo mostrar categorías que tienen ingredientes
                if (!ingredientes.isEmpty()) {
                    crearSeccionCategoria(categoria, ingredientes);
                }
            }

            Log.d("FormularioNutricional", "✅ UI creada con " + ingredientesPorCategoria.size() + " categorías");
            actualizarContador();
        });
    }

    /**
     * 📦 Crear una sección para una categoría específica
     */
    private void crearSeccionCategoria(String categoria, List<Ingrediente> ingredientes) {
        // Título de la categoría con contador
        LinearLayout tituloLayout = new LinearLayout(this);
        tituloLayout.setOrientation(LinearLayout.HORIZONTAL);
        tituloLayout.setPadding(0, 32, 0, 16);

        TextView tvCategoria = new TextView(this);
        tvCategoria.setText(categoria);
        tvCategoria.setTextSize(16);
        tvCategoria.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvMinimo = new TextView(this);
        int minimo = minimosRequeridos.getOrDefault(categoria, 0);
        int seleccionados = ingredientesSeleccionadosPorCategoria.get(categoria).size();
        tvMinimo.setText(" (" + seleccionados + "/" + minimo + " mín)");
        tvMinimo.setTextSize(12);
        tvMinimo.setTextColor(seleccionados >= minimo ?
                getColor(R.color.verde) : getColor(android.R.color.holo_red_dark));

        tituloLayout.addView(tvCategoria);
        tituloLayout.addView(tvMinimo);
        containerIngredientes.addView(tituloLayout);

        // Grid container para los ingredientes
        LinearLayout gridContainer = new LinearLayout(this);
        gridContainer.setOrientation(LinearLayout.VERTICAL);

        // Crear filas de 3 elementos
        LinearLayout currentRow = null;
        for (int i = 0; i < ingredientes.size(); i++) {
            if (i % 3 == 0) {
                // Nueva fila
                currentRow = new LinearLayout(this);
                currentRow.setOrientation(LinearLayout.HORIZONTAL);
                currentRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                gridContainer.addView(currentRow);
            }

            // Crear botón de ingrediente
            Ingrediente ingrediente = ingredientes.get(i);
            View botonIngrediente = crearBotonIngrediente(ingrediente, categoria);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
            );
            params.setMargins(8, 8, 8, 8);
            botonIngrediente.setLayoutParams(params);

            currentRow.addView(botonIngrediente);
        }

        containerIngredientes.addView(gridContainer);
    }

    /**
     * 🔘 Crear un botón individual para un ingrediente
     */
    private View crearBotonIngrediente(Ingrediente ingrediente, String categoria) {
        // Crear CardView contenedor
        CardView cardView = new CardView(this);
        cardView.setRadius(12);
        cardView.setCardElevation(4);
        cardView.setClickable(true);
        cardView.setFocusable(true);

        // LinearLayout interno
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(12, 12, 12, 12);
        layout.setGravity(android.view.Gravity.CENTER);

        // ImageView para la imagen
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(64, 64);
        imageView.setLayoutParams(imgParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Cargar imagen con Glide o usar placeholder
        if (ingrediente.getImagen() != null && !ingrediente.getImagen().isEmpty()) {
            Glide.with(this)
                    .load(ingrediente.getImagen())
                    .placeholder(R.drawable.ic_placeholder_food)
                    .error(R.drawable.ic_placeholder_food)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_placeholder_food);
        }

        // TextView para el nombre
        TextView textView = new TextView(this);
        textView.setText(ingrediente.getNombre());
        textView.setTextSize(12);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, 8, 0, 0);
        textView.setMaxLines(2);

        // Agregar views al layout
        layout.addView(imageView);
        layout.addView(textView);
        cardView.addView(layout);

        // ✅ IMPORTANTE: Configurar click listener con ID del ingrediente
        cardView.setOnClickListener(v -> toggleIngrediente(ingrediente, cardView, categoria));

        // Verificar si ya está seleccionado
        List<Integer> seleccionados = ingredientesSeleccionadosPorCategoria.get(categoria);
        if (seleccionados != null && seleccionados.contains(ingrediente.getId())) {
            cardView.setCardBackgroundColor(getColor(R.color.verde_claro));
        }

        return cardView;
    }

    /**
     * 🔄 Toggle selección de ingrediente (CON ID)
     */
    private void toggleIngrediente(Ingrediente ingrediente, CardView cardView, String categoria) {
        List<Integer> seleccionadosCategoria = ingredientesSeleccionadosPorCategoria.get(categoria);

        if (seleccionadosCategoria == null) {
            seleccionadosCategoria = new ArrayList<>();
            ingredientesSeleccionadosPorCategoria.put(categoria, seleccionadosCategoria);
        }

        if (seleccionadosCategoria.contains(ingrediente.getId())) {
            // Deseleccionar
            seleccionadosCategoria.remove(Integer.valueOf(ingrediente.getId()));
            cardView.setCardBackgroundColor(getColor(android.R.color.white));
            Toast.makeText(this, "Deseleccionado: " + ingrediente.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            // Seleccionar
            seleccionadosCategoria.add(ingrediente.getId());
            cardView.setCardBackgroundColor(getColor(R.color.verde_claro));
            Toast.makeText(this, "Seleccionado: " + ingrediente.getNombre(), Toast.LENGTH_SHORT).show();
        }

        // Actualizar contador y verificar botón continuar
        actualizarContador();
        verificarBotonContinuar();

        // Actualizar contador de la categoría
        actualizarContadorCategoria(categoria);
    }

    /**
     * 🔢 Actualizar el contador general de ingredientes seleccionados
     */
    private void actualizarContador() {
        int totalSeleccionados = 0;
        for (List<Integer> lista : ingredientesSeleccionadosPorCategoria.values()) {
            totalSeleccionados += lista.size();
        }

        if (textContador != null) {
            String texto = totalSeleccionados + " ingredientes seleccionados";
            textContador.setText(texto);
        }
    }

    /**
     * 📊 Actualizar contador de una categoría específica
     */
    private void actualizarContadorCategoria(String categoria) {
        // Recorrer las vistas para encontrar el contador de esta categoría
        for (int i = 0; i < containerIngredientes.getChildCount(); i++) {
            View child = containerIngredientes.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                if (layout.getChildCount() >= 2 &&
                        layout.getChildAt(0) instanceof TextView &&
                        layout.getChildAt(1) instanceof TextView) {

                    TextView tvCategoria = (TextView) layout.getChildAt(0);
                    TextView tvMinimo = (TextView) layout.getChildAt(1);

                    if (tvCategoria.getText().toString().equals(categoria)) {
                        int minimo = minimosRequeridos.getOrDefault(categoria, 0);
                        int seleccionados = ingredientesSeleccionadosPorCategoria.get(categoria).size();

                        tvMinimo.setText(" (" + seleccionados + "/" + minimo + " mín)");
                        tvMinimo.setTextColor(seleccionados >= minimo ?
                                getColor(R.color.verde) : getColor(android.R.color.holo_red_dark));
                        break;
                    }
                }
            }
        }
    }

    /**
     * ✅ Validar que se cumplan los mínimos por categoría
     */
    private boolean validarSeleccionMinima() {
        List<String> categoriasFaltantes = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : minimosRequeridos.entrySet()) {
            String categoria = entry.getKey();
            int minimo = entry.getValue();

            List<Integer> seleccionados = ingredientesSeleccionadosPorCategoria.get(categoria);
            int cantidadSeleccionada = seleccionados != null ? seleccionados.size() : 0;

            if (cantidadSeleccionada < minimo) {
                categoriasFaltantes.add(categoria + " (" + cantidadSeleccionada + "/" + minimo + ")");
            }
        }

        return categoriasFaltantes.isEmpty();
    }

    /**
     * ⚠️ Mostrar error de validación
     */
    private void mostrarErrorValidacion() {
        StringBuilder mensaje = new StringBuilder("Faltan ingredientes en:\n");

        for (Map.Entry<String, Integer> entry : minimosRequeridos.entrySet()) {
            String categoria = entry.getKey();
            int minimo = entry.getValue();

            List<Integer> seleccionados = ingredientesSeleccionadosPorCategoria.get(categoria);
            int cantidadSeleccionada = seleccionados != null ? seleccionados.size() : 0;

            if (cantidadSeleccionada < minimo) {
                mensaje.append("• ").append(categoria).append(": ")
                        .append(cantidadSeleccionada).append("/").append(minimo).append(" mín\n");
            }
        }

        Toast.makeText(this, mensaje.toString(), Toast.LENGTH_LONG).show();
    }

    /**
     * ✅ Verificar si se puede habilitar el botón continuar
     */
    private void verificarBotonContinuar() {
        if (validarSeleccionMinima()) {
            habilitarBotonContinuar();
        } else {
            deshabilitarBotonContinuar();
        }
    }

    private void habilitarBotonContinuar() {
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f);
    }

    private void deshabilitarBotonContinuar() {
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f);
    }

    /**
     * 🎨 Mostrar/ocultar loading
     */
    private void mostrarLoading(boolean mostrar) {
        ProgressBar loadingProgressBar = findViewById(R.id.loadingProgressBar);

        if (mostrar) {
            loadingProgressBar.setVisibility(View.VISIBLE);
            containerIngredientes.setVisibility(View.GONE);
            textTitulo.setText("Cargando ingredientes...");
        } else {
            loadingProgressBar.setVisibility(View.GONE);
            containerIngredientes.setVisibility(View.VISIBLE);
            textTitulo.setText("Elige los ingredientes que tienes a mano.");
        }
    }

    /**
     * ⚠️ Mostrar error y cargar ingredientes por defecto
     */
    private void mostrarErrorYFallback() {
        runOnUiThread(() -> {
            Toast.makeText(this,
                    "No se pudieron cargar los ingredientes desde el servidor.\n" +
                            "Verifica tu conexión a internet.\n" +
                            "Usando lista básica de ingredientes.",
                    Toast.LENGTH_LONG).show();
        });

        crearIngredientesPorDefecto();
    }

    /**
     * 📋 MÉTODO MEJORADO: Crear ingredientes por defecto con más opciones
     */
    private void crearIngredientesPorDefecto() {
        Log.w("FormularioNutricional", "⚠️ Creando ingredientes por defecto...");

        ingredientesPorCategoria.clear();
        ingredientesSeleccionadosPorCategoria.clear();

        // ✅ CAMBIO 8: Más ingredientes por defecto en cada categoría

        // Carnes
        List<Ingrediente> carnes = new ArrayList<>();
        carnes.add(new Ingrediente(1, "Pollo", "Carnes", null));
        carnes.add(new Ingrediente(2, "Carne de res", "Carnes", null));
        carnes.add(new Ingrediente(3, "Cerdo", "Carnes", null));
        carnes.add(new Ingrediente(4, "Pavo", "Carnes", null));
        ingredientesPorCategoria.put("Carnes", carnes);
        ingredientesSeleccionadosPorCategoria.put("Carnes", new ArrayList<>());

        // Verduras
        List<Ingrediente> verduras = new ArrayList<>();
        verduras.add(new Ingrediente(5, "Brócoli", "Verduras", null));
        verduras.add(new Ingrediente(6, "Zanahoria", "Verduras", null));
        verduras.add(new Ingrediente(7, "Espinaca", "Verduras", null));
        verduras.add(new Ingrediente(8, "Tomate", "Verduras", null));
        verduras.add(new Ingrediente(9, "Cebolla", "Verduras", null));
        verduras.add(new Ingrediente(10, "Apio", "Verduras", null));
        ingredientesPorCategoria.put("Verduras", verduras);
        ingredientesSeleccionadosPorCategoria.put("Verduras", new ArrayList<>());

        // Frutas
        List<Ingrediente> frutas = new ArrayList<>();
        frutas.add(new Ingrediente(11, "Manzana", "Frutas", null));
        frutas.add(new Ingrediente(12, "Plátano", "Frutas", null));
        frutas.add(new Ingrediente(13, "Naranja", "Frutas", null));
        frutas.add(new Ingrediente(14, "Fresas", "Frutas", null));
        ingredientesPorCategoria.put("Frutas", frutas);
        ingredientesSeleccionadosPorCategoria.put("Frutas", new ArrayList<>());

        // Lácteos
        List<Ingrediente> lacteos = new ArrayList<>();
        lacteos.add(new Ingrediente(15, "Leche", "Lácteos", null));
        lacteos.add(new Ingrediente(16, "Queso", "Lácteos", null));
        lacteos.add(new Ingrediente(17, "Yogur", "Lácteos", null));
        lacteos.add(new Ingrediente(18, "Mantequilla", "Lácteos", null));
        ingredientesPorCategoria.put("Lácteos", lacteos);
        ingredientesSeleccionadosPorCategoria.put("Lácteos", new ArrayList<>());

        // Granos y Legumbres
        List<Ingrediente> granos = new ArrayList<>();
        granos.add(new Ingrediente(19, "Arroz", "Granos y Legumbres", null));
        granos.add(new Ingrediente(20, "Frijoles", "Granos y Legumbres", null));
        granos.add(new Ingrediente(21, "Lentejas", "Granos y Legumbres", null));
        granos.add(new Ingrediente(22, "Garbanzos", "Granos y Legumbres", null));
        ingredientesPorCategoria.put("Granos y Legumbres", granos);
        ingredientesSeleccionadosPorCategoria.put("Granos y Legumbres", new ArrayList<>());

        // Cereales
        List<Ingrediente> cereales = new ArrayList<>();
        cereales.add(new Ingrediente(23, "Avena", "Cereales", null));
        cereales.add(new Ingrediente(24, "Quinoa", "Cereales", null));
        cereales.add(new Ingrediente(25, "Trigo", "Cereales", null));
        ingredientesPorCategoria.put("Cereales", cereales);
        ingredientesSeleccionadosPorCategoria.put("Cereales", new ArrayList<>());

        // Huevos
        List<Ingrediente> huevos = new ArrayList<>();
        huevos.add(new Ingrediente(26, "Huevo de gallina", "Huevos", null));
        ingredientesPorCategoria.put("Huevos", huevos);
        ingredientesSeleccionadosPorCategoria.put("Huevos", new ArrayList<>());

        // Frutos secos y Semillas
        List<Ingrediente> frutosSecos = new ArrayList<>();
        frutosSecos.add(new Ingrediente(27, "Almendras", "Frutos secos y Semillas", null));
        frutosSecos.add(new Ingrediente(28, "Nueces", "Frutos secos y Semillas", null));
        ingredientesPorCategoria.put("Frutos secos y Semillas", frutosSecos);
        ingredientesSeleccionadosPorCategoria.put("Frutos secos y Semillas", new ArrayList<>());

        // Aceites y Grasas
        List<Ingrediente> aceites = new ArrayList<>();
        aceites.add(new Ingrediente(29, "Aceite de oliva", "Aceites y Grasas", null));
        aceites.add(new Ingrediente(30, "Aceite de coco", "Aceites y Grasas", null));
        ingredientesPorCategoria.put("Aceites y Grasas", aceites);
        ingredientesSeleccionadosPorCategoria.put("Aceites y Grasas", new ArrayList<>());

        // Bebidas
        List<Ingrediente> bebidas = new ArrayList<>();
        bebidas.add(new Ingrediente(31, "Agua", "Bebidas", null));
        bebidas.add(new Ingrediente(32, "Té verde", "Bebidas", null));
        ingredientesPorCategoria.put("Bebidas", bebidas);
        ingredientesSeleccionadosPorCategoria.put("Bebidas", new ArrayList<>());

        Log.d("FormularioNutricional", "✅ Ingredientes por defecto creados: " + ingredientesPorCategoria.size() + " categorías");

        // Mostrar mensaje al usuario
        Toast.makeText(this, "Usando ingredientes por defecto. Verifica tu conexión a internet.", Toast.LENGTH_LONG).show();

        // Crear UI con ingredientes por defecto
        crearUIIngredientes();
    }

    // ============================================
    // MÉTODOS EXISTENTES (mantener igual)
    // ============================================

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
            entrenamientoFuerza = intent.getStringExtra("entrenamiento_fuerza");
        }
    }

    private void mostrarInformacionRecibida() {
        String mensaje = "Datos recibidos - ";
        mensaje += "Entrenamiento de fuerza: " + (entrenamientoFuerza != null ?
                (entrenamientoFuerza.equals("si") ? "Sí" : "No") : "No especificado");
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    /**
     * 🚀 MÉTODO MODIFICADO - Continuar al siguiente formulario (formulario 8) CON IDs
     */
    private void continuarSiguienteFormulario() {
        if (!validarSeleccionMinima()) {
            mostrarErrorValidacion();
            return;
        }

        // Contar total de ingredientes seleccionados
        int totalSeleccionados = 0;
        for (List<Integer> lista : ingredientesSeleccionadosPorCategoria.values()) {
            totalSeleccionados += lista.size();
        }

        Toast.makeText(this, "¡Ingredientes validados correctamente! (" +
                totalSeleccionados + " ingredientes)", Toast.LENGTH_SHORT).show();

        // Crear Intent para ir al formulario 8
        Intent intentFormulario8 = new Intent(this, formulario_plan_nutricional_8.class);

        // Pasar todos los datos anteriores
        intentFormulario8.putExtra("metodo_pago", metodoPago);
        intentFormulario8.putExtra("nombre_usuario", nombreUsuario);
        intentFormulario8.putExtra("numero_nequi", numeroNequi);
        intentFormulario8.putExtra("tipo_plan", tipoPlan);
        intentFormulario8.putExtra("objetivo", objetivo);
        intentFormulario8.putExtra("sexo", sexo);
        intentFormulario8.putExtra("edad", edad);
        intentFormulario8.putExtra("altura", altura);
        intentFormulario8.putExtra("peso", peso);
        intentFormulario8.putExtra("nivel_actividad", nivelActividad);
        intentFormulario8.putExtra("entrenamiento_fuerza", entrenamientoFuerza);

        // ✅ PASAR INGREDIENTES CON ESTRUCTURA COMPLETA PARA EL BACKEND
        intentFormulario8.putExtra("ingredientes_por_categoria",
                serializarIngredientesSeleccionados());

        // Iniciar el formulario 8
        startActivity(intentFormulario8);
    }

    /**
     * 📦 Serializar ingredientes seleccionados para el backend
     */
    private String serializarIngredientesSeleccionados() {
        try {
            JSONObject ingredientesJson = new JSONObject();

            for (Map.Entry<String, List<Integer>> entry : ingredientesSeleccionadosPorCategoria.entrySet()) {
                String categoria = entry.getKey();
                List<Integer> ids = entry.getValue();

                if (!ids.isEmpty()) {
                    JSONArray idsArray = new JSONArray();
                    for (Integer id : ids) {
                        idsArray.put(id);
                    }
                    ingredientesJson.put(categoria, idsArray);
                }
            }

            return ingredientesJson.toString();

        } catch (JSONException e) {
            Log.e("FormularioNutricional", "Error serializando ingredientes: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 📦 Clase interna para representar un ingrediente
     */
    private static class Ingrediente {
        private int id;
        private String nombre;
        private String categoria;
        private String imagen;

        public Ingrediente(int id, String nombre, String categoria, String imagen) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.imagen = imagen;
        }

        // Getters
        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getImagen() { return imagen; }
    }

    /**
     * A simple {@link Fragment} subclass.
     * Use the {@link formulario_plan_nutricional9Fragment#newInstance} factory method to
     * create an instance of this fragment.
     */
    public static class formulario_plan_nutricional9Fragment extends Fragment {

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        // TODO: Rename and change types of parameters
        private String mParam1;
        private String mParam2;

        public formulario_plan_nutricional9Fragment() {
            // Required empty public constructor
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment formulario_plan_nutricional9Fragment.
         */
        // TODO: Rename and change types and number of parameters
        public static formulario_plan_nutricional9Fragment newInstance(String param1, String param2) {
            formulario_plan_nutricional9Fragment fragment = new formulario_plan_nutricional9Fragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mParam1 = getArguments().getString(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_formulario_plan_nutricional9, container, false);
        }
    }
}