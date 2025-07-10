package com.camilo.cocinarte.ui.banquetes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.models.BanqueteIngrediente;
import com.camilo.cocinarte.models.BanquetePlatillo;
import com.camilo.cocinarte.models.EscaladoResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment;
import com.camilo.cocinarte.utils.EscaladoBanqueteManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BanqueteDetalleActivity extends AppCompatActivity {
    private static final String TAG = "BanqueteDetalleActivity";

    private Banquete banqueteActual;
    private SessionManager sessionManager;
    private EscaladoBanqueteManager escaladoManager;

    // Authentication control
    private boolean hasAuthentication = false;

    // Scaling variables
    private int personasOriginales = 8;
    private int personasActuales = 8;
    private boolean ingredientesEscalados = false;
    private List<BanqueteIngrediente> ingredientesOriginales = new ArrayList<>();
    private List<BanqueteIngrediente> ingredientesEscaladosActuales = new ArrayList<>();

    // Main views
    private ProgressBar progressBar;
    private ImageView imagenBanquete;
    private TextView textNombreBanquete;
    private TextView textCantidadPersonas;
    private TextView textTiempoPreparacion;
    private TextView textDificultad;
    private TextView textDescripcionPreparacion;

    // Scaling views
    private LinearLayout sectionEscalado;
    private EditText editCantidadPersonas;
    private ImageButton btnMenosPersonas;
    private ImageButton btnMasPersonas;
    private Button btnCalcularEscalado;
    private LinearLayout containerInfoEscalado;
    private TextView textFactorEscalado;
    private TextView textMetodoCalculo;
    private ProgressBar progressEscalado;
    private TextView textEstadoEscalado;

    // Reaction views
    private ImageView btnLike;
    private ImageView btnFavorito;
    private ImageView btnCompartir;
    private ImageView btnComentarios;
    private TextView textTotalLikes;
    private TextView textTotalComentarios;

    // RecyclerViews for dishes and ingredients
    private RecyclerView recyclerPlatillos;
    private RecyclerView recyclerIngredientes;
    private TextView textNoPlatillos;
    private TextView textNoIngredientes;

    // Adapters
    private PlatillosBanqueteAdapter platillosAdapter;
    private IngredientesBanqueteAdapter ingredientesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_banquete_detalle);

        // Initialize managers
        try {
            sessionManager = SessionManager.getInstance(this);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        escaladoManager = new EscaladoBanqueteManager(this);

        // Initialize views
        initViews();

        // Check authentication
        checkAuthentication();

        // Get banquet ID from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("banquete_id")) {
            int banqueteId = intent.getIntExtra("banquete_id", -1);
            Log.d(TAG, "Recibido banquete_id: " + banqueteId);
            Log.d(TAG, "Tiene autenticación: " + hasAuthentication);

            if (banqueteId != -1) {
                if (hasAuthentication) {
                    getBanqueteByIdConAuth(banqueteId);
                } else {
                    getBanqueteByIdSinAuth(banqueteId);
                }
            } else {
                Log.e(TAG, "ID de banquete inválido");
                Toast.makeText(this, "Error: ID de banquete inválido", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "No se recibió ID de banquete");
            Toast.makeText(this, "Error: No se encontró el banquete", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Setup listeners
        setupListeners();
    }

    private void checkAuthentication() {
        String token = sessionManager.getAuthToken();
        hasAuthentication = (token != null && !token.isEmpty());
        Log.d(TAG, "🔍 Verificación de autenticación:");
        Log.d(TAG, "   - Token presente: " + hasAuthentication);
        Log.d(TAG, "   - Modo: " + (hasAuthentication ? "CON AUTH" : "SIN AUTH"));
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        imagenBanquete = findViewById(R.id.imagen_banquete);
        textNombreBanquete = findViewById(R.id.text_nombre_banquete);
        textCantidadPersonas = findViewById(R.id.text_cantidad_personas);
        textTiempoPreparacion = findViewById(R.id.text_tiempo_preparacion);
        textDificultad = findViewById(R.id.text_dificultad);
        textDescripcionPreparacion = findViewById(R.id.text_descripcion_preparacion);

        sectionEscalado = findViewById(R.id.section_escalado);
        editCantidadPersonas = findViewById(R.id.edit_cantidad_personas);
        btnMenosPersonas = findViewById(R.id.btn_menos_personas);
        btnMasPersonas = findViewById(R.id.btn_mas_personas);
        btnCalcularEscalado = findViewById(R.id.btn_calcular_escalado);
        containerInfoEscalado = findViewById(R.id.container_info_escalado);
        textFactorEscalado = findViewById(R.id.text_factor_escalado);
        textMetodoCalculo = findViewById(R.id.text_metodo_calculo);
        progressEscalado = findViewById(R.id.progress_escalado);
        textEstadoEscalado = findViewById(R.id.text_estado_escalado);

        btnLike = findViewById(R.id.btn_like);
        btnFavorito = findViewById(R.id.btn_favorito);
        btnCompartir = findViewById(R.id.btn_compartir);
        btnComentarios = findViewById(R.id.btn_comentarios_favoritos);
        textTotalLikes = findViewById(R.id.text_total_likes);
        textTotalComentarios = findViewById(R.id.text_total_comentarios);

        recyclerPlatillos = findViewById(R.id.recycler_platillos);
        recyclerIngredientes = findViewById(R.id.recycler_ingredientes);
        textNoPlatillos = findViewById(R.id.text_no_platillos);
        textNoIngredientes = findViewById(R.id.text_no_ingredientes);

        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        if (recyclerPlatillos != null) {
            recyclerPlatillos.setLayoutManager(new LinearLayoutManager(this));
            recyclerPlatillos.setNestedScrollingEnabled(false);
        }

        if (recyclerIngredientes != null) {
            recyclerIngredientes.setLayoutManager(new LinearLayoutManager(this));
            recyclerIngredientes.setNestedScrollingEnabled(false);
        }
    }

    private void setupListeners() {
        setupEscaladoListeners();

        if (btnLike != null) {
            btnLike.setOnClickListener(v -> {
                if (banqueteActual != null) {
                    if (hasAuthentication) {
                        toggleLike(banqueteActual.getIdBanquete());
                    } else {
                        Toast.makeText(this, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Banquete no cargado", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnComentarios != null) {
            btnComentarios.setOnClickListener(v -> {
                if (banqueteActual != null) {
                    if (hasAuthentication) {
                        abrirSeccionComentarios();
                    } else {
                        Toast.makeText(this, "Inicia sesión para ver comentarios", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Banquete no cargado", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnFavorito != null) {
            btnFavorito.setOnClickListener(v -> {
                if (banqueteActual != null) {
                    if (hasAuthentication) {
                        toggleFavorito(banqueteActual.getIdBanquete());
                    } else {
                        Toast.makeText(this, "Inicia sesión para guardar en favoritos", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Banquete no cargado", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCompartir != null) {
            btnCompartir.setOnClickListener(v -> {
                if (banqueteActual != null) {
                    compartirBanquete();
                }
            });
        }
    }

    private void setupEscaladoListeners() {
        if (btnMenosPersonas != null) {
            btnMenosPersonas.setOnClickListener(v -> {
                int cantidadActual = getCurrentPersonasFromEditText();
                if (cantidadActual > 1) {
                    editCantidadPersonas.setText(String.valueOf(cantidadActual - 1));
                }
            });
        }

        if (btnMasPersonas != null) {
            btnMasPersonas.setOnClickListener(v -> {
                int cantidadActual = getCurrentPersonasFromEditText();
                if (cantidadActual < 999) {
                    editCantidadPersonas.setText(String.valueOf(cantidadActual + 1));
                }
            });
        }

        if (btnCalcularEscalado != null) {
            btnCalcularEscalado.setOnClickListener(v -> ejecutarEscalado());
        }

        if (editCantidadPersonas != null) {
            editCantidadPersonas.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateCalcularButtonState();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private int getCurrentPersonasFromEditText() {
        try {
            String text = editCantidadPersonas.getText().toString().trim();
            if (text.isEmpty()) return personasOriginales;
            int personas = Integer.parseInt(text);
            return Math.max(1, Math.min(999, personas));
        } catch (NumberFormatException e) {
            return personasOriginales;
        }
    }

    private void updateCalcularButtonState() {
        if (btnCalcularEscalado == null || editCantidadPersonas == null) return;

        int personasNuevas = getCurrentPersonasFromEditText();
        boolean enabled = personasNuevas != personasActuales && personasNuevas > 0;

        btnCalcularEscalado.setEnabled(enabled);
        btnCalcularEscalado.setAlpha(enabled ? 1.0f : 0.6f);

        if (enabled) {
            double factor = EscaladoBanqueteManager.calcularFactorEscala(personasActuales, personasNuevas);
            String mensaje = EscaladoBanqueteManager.obtenerMensajeEscalado(personasActuales, personasNuevas);
            btnCalcularEscalado.setText("Calcular (" + EscaladoBanqueteManager.formatearFactor(factor) + ")");
        } else {
            btnCalcularEscalado.setText("Calcular");
        }
    }

    private void ejecutarEscalado() {
        if (banqueteActual == null) {
            Toast.makeText(this, "Banquete no cargado", Toast.LENGTH_SHORT).show();
            return;
        }

        int personasNuevas = getCurrentPersonasFromEditText();

        Log.d(TAG, "🍽️ Ejecutando escalado:");
        Log.d(TAG, "   - Banquete ID: " + banqueteActual.getIdBanquete());
        Log.d(TAG, "   - Personas originales: " + personasOriginales);
        Log.d(TAG, "   - Personas actuales: " + personasActuales);
        Log.d(TAG, "   - Personas nuevas: " + personasNuevas);

        if (personasNuevas == personasActuales) {
            Toast.makeText(this, "La cantidad ya está configurada para " + personasNuevas + " personas", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!EscaladoBanqueteManager.validarPersonas(personasNuevas)) {
            Toast.makeText(this, "La cantidad de personas debe estar entre 1 y 1000", Toast.LENGTH_SHORT).show();
            return;
        }

        setEscaladoLoading(true);
        showInfoEscalado(true);

        escaladoManager.escalarBanquete(
                banqueteActual.getIdBanquete(),
                personasOriginales,
                personasNuevas,
                new EscaladoBanqueteManager.EscaladoCallback() {
                    @Override
                    public void onSuccess(EscaladoResponse.EscaladoData escaladoData) {
                        runOnUiThread(() -> {
                            Log.d(TAG, "✅ Escalado completado exitosamente");
                            procesarEscaladoExitoso(escaladoData, personasNuevas);
                            setEscaladoLoading(false);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "❌ Error en escalado: " + error);
                            Toast.makeText(BanqueteDetalleActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                            setEscaladoLoading(false);
                            showInfoEscalado(false);
                        });
                    }

                    @Override
                    public void onProgress(String mensaje) {
                        runOnUiThread(() -> {
                            if (textMetodoCalculo != null) {
                                textMetodoCalculo.setText(mensaje);
                            }
                        });
                    }
                }
        );
    }

    private void procesarEscaladoExitoso(EscaladoResponse.EscaladoData escaladoData, int personasNuevas) {
        Log.d(TAG, "📊 Procesando escalado exitoso:");

        if (escaladoData == null) {
            Log.e(TAG, "❌ EscaladoData es null");
            Toast.makeText(this, "Error: No se recibieron datos de escalado del servidor", Toast.LENGTH_LONG).show();
            setEscaladoLoading(false);
            showInfoEscalado(false);
            return;
        }

        Log.d(TAG, "   - Factor: " + escaladoData.getScaleFactor());

        List<EscaladoResponse.IngredienteEscalado> ingredientesEscaladosList = escaladoData.getScaledIngredients();
        if (ingredientesEscaladosList == null || ingredientesEscaladosList.isEmpty()) {
            Log.w(TAG, "⚠️ Lista de ingredientes escalados es NULL o vacía");
            Toast.makeText(this, "Error: No se recibieron ingredientes escalados del servidor", Toast.LENGTH_LONG).show();
            setEscaladoLoading(false);
            showInfoEscalado(false);
            return;
        }

        Log.d(TAG, "   - Ingredientes escalados: " + ingredientesEscaladosList.size());
        Log.d(TAG, "   - IA procesado: " + escaladoData.isAiProcessed());

        personasActuales = personasNuevas;
        ingredientesEscalados = true;

        List<BanqueteIngrediente> ingredientesConvertidos = convertirIngredientesEscalados(ingredientesEscaladosList);

        if (!ingredientesConvertidos.isEmpty()) {
            ingredientesEscaladosActuales = ingredientesConvertidos;
            mostrarIngredientes(ingredientesConvertidos);

            if (textFactorEscalado != null) {
                textFactorEscalado.setText("Factor de escalado: " + String.format("%.1fx", escaladoData.getScaleFactor()));
            }

            if (textMetodoCalculo != null) {
                String metodo = escaladoData.isAiProcessed() ? "Calculado con IA inteligente" : "Cálculo básico";
                if (escaladoData.getAiVersion() != null && !escaladoData.getAiVersion().isEmpty()) {
                    metodo += " v" + escaladoData.getAiVersion();
                }
                textMetodoCalculo.setText(metodo);
            }

            if (textEstadoEscalado != null) {
                textEstadoEscalado.setText("✅ Escalado para " + personasNuevas + " personas");
                textEstadoEscalado.setVisibility(View.VISIBLE);
            }

            updateCalcularButtonState();

            String mensaje = "Ingredientes escalados para " + personasNuevas + " personas";
            if (escaladoData.getRecommendations() != null && !escaladoData.getRecommendations().isEmpty()) {
                mensaje += "\n💡 " + escaladoData.getRecommendations().get(0);
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        } else {
            Log.e(TAG, "❌ No se pudieron convertir los ingredientes escalados");
            Toast.makeText(this, "Error procesando ingredientes escalados", Toast.LENGTH_SHORT).show();
            setEscaladoLoading(false);
            showInfoEscalado(false);
        }
    }

    private List<BanqueteIngrediente> convertirIngredientesEscalados(List<EscaladoResponse.IngredienteEscalado> ingredientesEscalados) {
        List<BanqueteIngrediente> convertidos = new ArrayList<>();

        if (ingredientesEscalados == null) {
            Log.w(TAG, "⚠️ Lista de ingredientes escalados es null");
            return convertidos;
        }

        if (ingredientesEscalados.isEmpty()) {
            Log.w(TAG, "⚠️ Lista de ingredientes escalados está vacía");
            return convertidos;
        }

        Log.d(TAG, "🔄 Convirtiendo " + ingredientesEscalados.size() + " ingredientes escalados");

        for (int i = 0; i < ingredientesEscalados.size(); i++) {
            EscaladoResponse.IngredienteEscalado escalado = ingredientesEscalados.get(i);

            if (escalado == null) {
                Log.w(TAG, "⚠️ Ingrediente escalado " + i + " es null, saltando...");
                continue;
            }

            try {
                BanqueteIngrediente ingrediente = new BanqueteIngrediente();

                // CAMBIO PRINCIPAL: usar int en lugar de Integer
                int id = escalado.getId(); // Tu modelo usa int, no Integer
                ingrediente.setId_ingrediente(id);

                String name = escalado.getName();
                ingrediente.setNombreIngrediente(name != null ? name : "Ingrediente sin nombre");

                String scaledQuantity = escalado.getScaledQuantity();
                ingrediente.setCantidad(scaledQuantity != null ? scaledQuantity : "1 unidad");

                String category = escalado.getCategory();
                ingrediente.setCategoria(category != null ? category : "Sin categoría");

                if (escalado.getNutrition() != null) {
                    EscaladoResponse.NutricionEscalada nutricion = escalado.getNutrition();

                    // Usar getters directos ya que tu modelo usa double, no Double
                    ingrediente.setCalorias_por_100g(nutricion.getCalories());
                    ingrediente.setProteinas_por_100g(nutricion.getProtein());
                    ingrediente.setCarbohidratos_por_100g(nutricion.getCarbs());
                    ingrediente.setGrasas_totales_por_100g(nutricion.getFats());
                    ingrediente.setAzucar_por_100g(nutricion.getSugar());
                }

                // Buscar imagen e información nutricional original si no viene en el escalado
                if (ingredientesOriginales != null && id > 0) {
                    for (BanqueteIngrediente original : ingredientesOriginales) {
                        if (original.getId_ingrediente() == id) {
                            ingrediente.setImagen(original.getImagen());
                            if (escalado.getNutrition() == null) {
                                ingrediente.setCalorias_por_100g(original.getCalorias_por_100g());
                                ingrediente.setProteinas_por_100g(original.getProteinas_por_100g());
                                ingrediente.setCarbohidratos_por_100g(original.getCarbohidratos_por_100g());
                                ingrediente.setGrasas_totales_por_100g(original.getGrasas_totales_por_100g());
                                ingrediente.setAzucar_por_100g(original.getAzucar_por_100g());
                            }
                            break;
                        }
                    }
                }

                convertidos.add(ingrediente);

                String originalQuantity = escalado.getOriginalQuantity();
                String scalingNotes = escalado.getScalingNotes();

                Log.d(TAG, "✅ Ingrediente " + (i + 1) + " convertido: " + name +
                        " - " + (originalQuantity != null ? originalQuantity : "N/A") +
                        " → " + (scaledQuantity != null ? scaledQuantity : "N/A"));

                if (scalingNotes != null && !scalingNotes.isEmpty()) {
                    Log.d(TAG, "   📝 Notas de escalado: " + scalingNotes);
                }

            } catch (Exception e) {
                String nombreIngrediente = escalado.getName() != null ? escalado.getName() : "null";
                Log.e(TAG, "❌ Error convirtiendo ingrediente " + i + ": " + nombreIngrediente, e);
            }
        }

        Log.d(TAG, "✅ Conversión completada: " + convertidos.size() + "/" + ingredientesEscalados.size() + " ingredientes");
        return convertidos;
    }

    private void setEscaladoLoading(boolean loading) {
        if (progressEscalado != null) {
            progressEscalado.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        if (btnCalcularEscalado != null) {
            btnCalcularEscalado.setEnabled(!loading);
            btnCalcularEscalado.setText(loading ? "Calculando..." : "Calcular");
        }

        if (btnMenosPersonas != null) {
            btnMenosPersonas.setEnabled(!loading);
        }

        if (btnMasPersonas != null) {
            btnMasPersonas.setEnabled(!loading);
        }

        if (editCantidadPersonas != null) {
            editCantidadPersonas.setEnabled(!loading);
        }
    }

    private void showInfoEscalado(boolean show) {
        if (containerInfoEscalado != null) {
            containerInfoEscalado.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void getBanqueteByIdConAuth(int banqueteId) {
        Log.d(TAG, "🔐 Cargando banquete CON autenticación - ID: " + banqueteId);

        String token = sessionManager.getAuthToken();
        String authHeader = "Bearer " + token;
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        setLoading(true);

        banqueteApi.obtenerBanquetePorId(banqueteId, authHeader).enqueue(new Callback<Banquete>() {
            @Override
            public void onResponse(Call<Banquete> call, Response<Banquete> response) {
                Log.d(TAG, "Respuesta de getBanqueteById CON AUTH: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    banqueteActual = response.body();
                    Log.d(TAG, "✅ Banquete obtenido CON AUTH: " + banqueteActual.getNombre());
                    configurarEscaladoInicial();
                    mostrarDetallesBanquete(banqueteActual);
                    cargarReaccionesBanqueteConAuth(banqueteId, authHeader);
                    cargarEstadoInicialFavorito(banqueteId);
                } else {
                    Log.e(TAG, "❌ Error al obtener banquete CON AUTH: " + response.code() + " - " + response.message());
                    Toast.makeText(getApplicationContext(), "Error al cargar el banquete", Toast.LENGTH_SHORT).show();
                    finish();
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<Banquete> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener banquete CON AUTH: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                setLoading(false);
                finish();
            }
        });
    }

    private void getBanqueteByIdSinAuth(int banqueteId) {
        Log.d(TAG, "🌐 Cargando banquete SIN autenticación - ID: " + banqueteId);

        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        setLoading(true);

        banqueteApi.obtenerBanquetePorId(banqueteId).enqueue(new Callback<Banquete>() {
            @Override
            public void onResponse(Call<Banquete> call, Response<Banquete> response) {
                Log.d(TAG, "Respuesta de getBanqueteById SIN AUTH: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    banqueteActual = response.body();
                    Log.d(TAG, "✅ Banquete obtenido SIN AUTH: " + banqueteActual.getNombre());
                    configurarEscaladoInicial();
                    mostrarDetallesBanquete(banqueteActual);
                    configurarUISinAuth();
                } else {
                    Log.e(TAG, "❌ Error al obtener banquete SIN AUTH: " + response.code() + " - " + response.message());
                    Toast.makeText(getApplicationContext(), "Error al cargar el banquete", Toast.LENGTH_SHORT).show();
                    finish();
                }
                setLoading(false);
            }

            @Override
            public void onFailure(Call<Banquete> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener banquete SIN AUTH: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                setLoading(false);
                finish();
            }
        });
    }

    private void configurarEscaladoInicial() {
        if (banqueteActual == null) return;

        personasOriginales = banqueteActual.getCantidadPersonas();
        personasActuales = personasOriginales;

        Log.d(TAG, "⚙️ Configurando escalado inicial:");
        Log.d(TAG, "   - Personas originales: " + personasOriginales);

        if (banqueteActual.getIngredientes() != null) {
            ingredientesOriginales = new ArrayList<>(banqueteActual.getIngredientes());
            Log.d(TAG, "   - Ingredientes originales guardados: " + ingredientesOriginales.size());
        }

        if (editCantidadPersonas != null) {
            editCantidadPersonas.setText(String.valueOf(personasOriginales));
        }

        ingredientesEscalados = false;
        showInfoEscalado(false);
        updateCalcularButtonState();

        if (textEstadoEscalado != null) {
            textEstadoEscalado.setVisibility(View.GONE);
        }
    }

    private void configurarUISinAuth() {
        Log.d(TAG, "🎨 Configurando UI para modo SIN AUTH");

        if (textTotalLikes != null) {
            int likesAleatorios = (int) (Math.random() * 150) + 10;
            textTotalLikes.setText(String.valueOf(likesAleatorios));
        }
        if (textTotalComentarios != null) {
            int comentariosAleatorios = (int) (Math.random() * 25) + 2;
            textTotalComentarios.setText(String.valueOf(comentariosAleatorios));
        }

        if (btnLike != null) {
            btnLike.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
            ));
        }

        if (btnFavorito != null) {
            btnFavorito.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
            ));
        }

        Toast.makeText(this, "💡 Inicia sesión para dar like, comentar y guardar favoritos", Toast.LENGTH_LONG).show();
    }

    private void mostrarDetallesBanquete(Banquete banquete) {
        Log.d(TAG, "📱 Mostrando detalles del banquete: " + banquete.getNombre());

        if (imagenBanquete != null) {
            if (banquete.tieneImagen()) {
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.banquete_nuevo)
                        .error(R.drawable.banquete_nuevo)
                        .centerCrop();

                Glide.with(getApplicationContext())
                        .load(banquete.getImagenUrl())
                        .apply(options)
                        .into(imagenBanquete);
            } else {
                imagenBanquete.setImageResource(R.drawable.banquete_nuevo);
            }
        }

        if (textNombreBanquete != null) {
            textNombreBanquete.setText(banquete.getNombre());
        }

        if (textCantidadPersonas != null) {
            textCantidadPersonas.setText("Para " + banquete.getCantidadPersonas() + " personas");
        }

        if (textTiempoPreparacion != null) {
            String tiempo = banquete.getTiempoPreparacion();
            textTiempoPreparacion.setText(tiempo != null ? tiempo : "No especificado");
        }

        if (textDificultad != null) {
            String dificultad = banquete.getDificultad();
            textDificultad.setText(dificultad != null ? dificultad : "No especificada");

            if (dificultad != null) {
                switch (dificultad.toLowerCase()) {
                    case "fácil":
                    case "facil":
                        textDificultad.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                        break;
                    case "media":
                    case "medio":
                        textDificultad.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                        break;
                    case "dificil":
                    case "difícil":
                        textDificultad.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                        break;
                    default:
                        textDificultad.setTextColor(ContextCompat.getColor(this, R.color.negro));
                        break;
                }
            }
        }

        if (textDescripcionPreparacion != null) {
            String descripcion = banquete.getDescripcionPreparacion();
            textDescripcionPreparacion.setText(descripcion != null ? descripcion : "Sin descripción");
        }

        mostrarPlatillos(banquete.getPlatillos());
        mostrarIngredientes(banquete.getIngredientes());
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (banqueteActual != null && hasAuthentication) {
            cargarEstadoInicialFavorito(banqueteActual.getIdBanquete());
        }
    }

    private void mostrarPlatillos(List<BanquetePlatillo> platillos) {
        Log.d(TAG, "🍽️ ==================== MOSTRAR PLATILLOS ====================");
        Log.d(TAG, "🍽️ Lista de platillos recibida: " + (platillos != null ? platillos.size() : "null"));

        if (platillos != null && !platillos.isEmpty()) {
            Log.d(TAG, "🍽️ Configurando adapter con " + platillos.size() + " platillos");

            if (recyclerPlatillos != null) {
                platillosAdapter = new PlatillosBanqueteAdapter(this, platillos);
                recyclerPlatillos.setAdapter(platillosAdapter);
                recyclerPlatillos.setVisibility(View.VISIBLE);

                recyclerPlatillos.post(() -> {
                    if (platillosAdapter != null) {
                        platillosAdapter.notifyDataSetChanged();
                        Log.d(TAG, "🍽️ Adapter notificado y actualizado");
                    }
                });
            }

            if (textNoPlatillos != null) {
                textNoPlatillos.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "⚠️ No hay platillos para mostrar");

            if (recyclerPlatillos != null) {
                recyclerPlatillos.setVisibility(View.GONE);
            }

            if (textNoPlatillos != null) {
                textNoPlatillos.setVisibility(View.VISIBLE);
            }
        }
    }

    private void mostrarIngredientes(List<BanqueteIngrediente> ingredientes) {
        Log.d(TAG, "🥕 ==================== MOSTRAR INGREDIENTES ====================");
        Log.d(TAG, "🥕 Lista de ingredientes recibida: " + (ingredientes != null ? ingredientes.size() : "null"));
        Log.d(TAG, "🥕 Ingredientes escalados: " + ingredientesEscalados);

        if (ingredientes != null && !ingredientes.isEmpty()) {
            Log.d(TAG, "🥕 Configurando adapter con " + ingredientes.size() + " ingredientes");

            if (recyclerIngredientes != null) {
                ingredientesAdapter = new IngredientesBanqueteAdapter(this, ingredientes);
                recyclerIngredientes.setAdapter(ingredientesAdapter);
                recyclerIngredientes.setVisibility(View.VISIBLE);

                recyclerIngredientes.post(() -> {
                    if (ingredientesAdapter != null) {
                        ingredientesAdapter.notifyDataSetChanged();
                        Log.d(TAG, "🥕 Adapter notificado y actualizado");
                    }
                });
            }

            if (textNoIngredientes != null) {
                textNoIngredientes.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "⚠️ No hay ingredientes para mostrar");

            if (recyclerIngredientes != null) {
                recyclerIngredientes.setVisibility(View.GONE);
            }

            if (textNoIngredientes != null) {
                textNoIngredientes.setVisibility(View.VISIBLE);
            }
        }
    }

    private void cargarReaccionesBanqueteConAuth(int banqueteId, String authHeader) {
        Log.d(TAG, "🔄 Obteniendo reacciones CON AUTH para banquete: " + banqueteId);

        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.obtenerReaccionesBanquete(banqueteId, authHeader).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        JSONObject likesObj = obj.optJSONObject("likes");
                        int totalLikes = 0;
                        boolean userLiked = false;

                        if (likesObj != null) {
                            totalLikes = likesObj.optInt("total", 0);
                            userLiked = likesObj.optBoolean("user_liked", false);
                        }

                        int totalComentarios = obj.optInt("total_comentarios", 0);

                        Log.d(TAG, "✅ Reacciones obtenidas - Likes: " + totalLikes +
                                " (user_liked=" + userLiked + "), Comentarios: " + totalComentarios);

                        mostrarReacciones(totalLikes, totalComentarios, userLiked);

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar reacciones", e);
                        mostrarReaccionesPorDefecto();
                    }
                } else {
                    Log.e(TAG, "⚠️ Error al obtener reacciones: " + response.code());
                    mostrarReaccionesPorDefecto();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener reacciones: ", t);
                mostrarReaccionesPorDefecto();
            }
        });
    }

    private void mostrarReacciones(int totalLikes, int totalComentarios, boolean userLiked) {
        if (textTotalLikes != null) {
            textTotalLikes.setText(String.valueOf(totalLikes));
        }

        if (textTotalComentarios != null) {
            textTotalComentarios.setText(String.valueOf(totalComentarios));
        }

        if (btnLike != null) {
            if (userLiked) {
                btnLike.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
                ));
            } else {
                btnLike.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                ));
            }
        }
    }

    private void mostrarReaccionesPorDefecto() {
        if (textTotalLikes != null) {
            textTotalLikes.setText("0");
        }

        if (textTotalComentarios != null) {
            textTotalComentarios.setText("0");
        }

        if (btnLike != null) {
            btnLike.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
            ));
        }
    }

    private void toggleLike(int banqueteId) {
        Log.d(TAG, "❤️ Toggle like para banquete: " + banqueteId);

        String token = "Bearer " + sessionManager.getAuthToken();
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.toggleLikeBanquete(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        boolean isLiked = obj.optBoolean("isLiked", false);
                        int totalLikes = obj.optInt("totalLikes", 0);

                        Log.d(TAG, "✅ Like actualizado: liked=" + isLiked + ", total=" + totalLikes);

                        if (textTotalLikes != null) {
                            textTotalLikes.setText(String.valueOf(totalLikes));
                        }

                        if (btnLike != null) {
                            if (isLiked) {
                                btnLike.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
                                ));
                            } else {
                                btnLike.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                                ));
                            }
                        }

                        String mensaje = isLiked ? "Te gusta este banquete" : "Like removido";
                        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar respuesta de like", e);
                        Toast.makeText(getApplicationContext(), "Error al procesar like", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error en like: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al dar like", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en like", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorito(int banqueteId) {
        Log.d(TAG, "⭐ Toggle favorito para banquete: " + banqueteId);

        String token = "Bearer " + sessionManager.getAuthToken();
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.verificarBanqueteFavorito(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        boolean esFavorito = obj.optBoolean("esFavorito", false);

                        Log.d(TAG, "🔍 Estado actual favorito: " + esFavorito);

                        if (esFavorito) {
                            quitarDeFavoritos(banqueteId, token);
                        } else {
                            agregarAFavoritos(banqueteId, token);
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar verificación de favorito", e);
                        Toast.makeText(getApplicationContext(), "Error al verificar favorito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error al verificar favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al verificar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al verificar favorito", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarAFavoritos(int banqueteId, String token) {
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.agregarBanqueteAFavoritos(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Banquete agregado a favoritos");
                    Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();

                    if (btnFavorito != null) {
                        btnFavorito.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                        ));
                    }
                } else {
                    Log.e(TAG, "❌ Error al agregar favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al agregar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al agregar favorito", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void quitarDeFavoritos(int banqueteId, String token) {
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.quitarBanqueteDeFavoritos(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Banquete quitado de favoritos");
                    Toast.makeText(getApplicationContext(), "Quitado de favoritos", Toast.LENGTH_SHORT).show();

                    if (btnFavorito != null) {
                        btnFavorito.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                        ));
                    }
                } else {
                    Log.e(TAG, "❌ Error al quitar favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al quitar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al quitar favorito", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEstadoInicialFavorito(int banqueteId) {
        Log.d(TAG, "🔍 Cargando estado inicial del favorito para banquete: " + banqueteId);

        String token = "Bearer " + sessionManager.getAuthToken();
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.verificarBanqueteFavorito(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        boolean esFavorito = obj.optBoolean("esFavorito", false);

                        Log.d(TAG, "✅ Estado inicial favorito: " + esFavorito);

                        if (btnFavorito != null) {
                            if (esFavorito) {
                                btnFavorito.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                                ));
                            } else {
                                btnFavorito.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                                ));
                            }
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar estado favorito", e);
                        if (btnFavorito != null) {
                            btnFavorito.setImageTintList(ColorStateList.valueOf(
                                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                            ));
                        }
                    }
                } else {
                    Log.e(TAG, "❌ Error al cargar estado favorito: " + response.code());
                    if (btnFavorito != null) {
                        btnFavorito.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                        ));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al cargar estado favorito", t);
                if (btnFavorito != null) {
                    btnFavorito.setImageTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                    ));
                }
            }
        });
    }

    private void abrirSeccionComentarios() {
        if (banqueteActual != null) {
            Log.d(TAG, "💬 Abriendo sección de comentarios para banquete: " + banqueteActual.getIdBanquete());

            BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);
            String token = "Bearer " + sessionManager.getAuthToken();

            banqueteApi.obtenerReaccionesBanquete(banqueteActual.getIdBanquete(), token).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JSONObject obj = new JSONObject(json);
                            JSONArray comentariosArray = obj.optJSONArray("comentarios");

                            if (comentariosArray == null) {
                                comentariosArray = new JSONArray();
                            }

                            Log.d(TAG, "💬 Comentarios obtenidos: " + comentariosArray.length());

                            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(
                                    comentariosArray,
                                    banqueteActual.getIdBanquete()
                            );
                            modal.show(getSupportFragmentManager(), "ComentariosBanqueteBottomSheet");

                        } catch (IOException | JSONException e) {
                            Log.e(TAG, "❌ Error al procesar comentarios: ", e);
                            Toast.makeText(getApplicationContext(), "Error al procesar comentarios", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error al obtener Comentarios: " + response.code());
                        Toast.makeText(getApplicationContext(), "No se pudieron obtener los comentarios", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Error de red al cargar comentarios: ", t);
                    Toast.makeText(getApplicationContext(), "Error de red al cargar comentarios", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void compartirBanquete() {
        if (banqueteActual != null) {
            String mensaje = "¡Mira este delicioso banquete!\n\n" +
                    "🍽️ " + banqueteActual.getNombre() + "\n" +
                    "👥 Para " + banqueteActual.getCantidadPersonas() + " personas\n";

            if (banqueteActual.getTiempoPreparacion() != null) {
                mensaje += "⏰ " + banqueteActual.getTiempoPreparacion() + "\n";
            }

            if (banqueteActual.getDificultad() != null) {
                mensaje += "📊 Dificultad: " + banqueteActual.getDificultad() + "\n";
            }

            if (ingredientesEscalados && personasActuales != personasOriginales) {
                mensaje += "\n🤖 ¡Escalado inteligentemente para " + personasActuales + " personas con IA!\n";
            }

            mensaje += "\n¡Descarga CocinarTe para ver más banquetes!";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mensaje);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Banquete: " + banqueteActual.getNombre());

            startActivity(Intent.createChooser(shareIntent, "Compartir banquete"));
        }
    }
}
