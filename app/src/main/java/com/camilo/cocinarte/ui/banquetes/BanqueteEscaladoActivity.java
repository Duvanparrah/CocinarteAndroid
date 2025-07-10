package com.camilo.cocinarte.ui.banquetes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.models.BanqueteEscalado;
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BanqueteEscaladoActivity extends AppCompatActivity {
    private static final String TAG = "BanqueteEscaladoActivity";

    // ✅ VIEWS PRINCIPALES
    private ImageButton btnVolver;
    private ImageView imagenBanquete;
    private TextView textNombreBanquete;
    private TextView textDescripcionOriginal;
    private CardView cardPersonasOriginales;
    private TextView textPersonasOriginales;
    private CardView cardPersonasNuevas;
    private EditText editPersonasNuevas;
    private Button btnEscalar;
    private Button btnReiniciar;
    private ProgressBar progressBar;

    // ✅ VIEWS DE RESULTADO
    private CardView cardResultado;
    private TextView textFactorEscala;
    private TextView textRecomendaciones;
    private RecyclerView recyclerIngredientesEscalados;
    private TextView textPreparacionAjustada;

    // ✅ DATOS
    private Banquete banqueteOriginal;
    private BanqueteEscalado banqueteEscalado;
    private SessionManager sessionManager;
    private BanqueteApi banqueteApi;

    // ✅ ADAPTERS
    private IngredientesEscaladosAdapter ingredientesAdapter;

    // ✅ VARIABLES DE ESTADO
    private int personasOriginales = 0;
    private int personasNuevas = 0;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_banquete_escalado);

        // Inicializar servicios
        try {
            sessionManager = SessionManager.getInstance(this);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        banqueteApi = ApiClient.getClient(this).create(BanqueteApi.class);

        // Inicializar vistas
        initViews();

        // Obtener datos del intent
        obtenerDatosIntent();

        // Configurar listeners
        setupListeners();

        // Cargar banquete original
        if (banqueteOriginal != null) {
            mostrarBanqueteOriginal();
        }
    }

    // ✅ INICIALIZAR VISTAS
    private void initViews() {
        btnVolver = findViewById(R.id.btn_volver);
        imagenBanquete = findViewById(R.id.imagen_banquete);
        textNombreBanquete = findViewById(R.id.text_nombre_banquete);
        textDescripcionOriginal = findViewById(R.id.text_descripcion_original);

        cardPersonasOriginales = findViewById(R.id.card_personas_originales);
        textPersonasOriginales = findViewById(R.id.text_personas_originales);

        cardPersonasNuevas = findViewById(R.id.card_personas_nuevas);
        editPersonasNuevas = findViewById(R.id.edit_personas_nuevas);

        btnEscalar = findViewById(R.id.btn_escalar);
        btnReiniciar = findViewById(R.id.btn_reiniciar);
        progressBar = findViewById(R.id.progress_bar);

        // Views de resultado
        cardResultado = findViewById(R.id.card_resultado);
        textFactorEscala = findViewById(R.id.text_factor_escala);
        textRecomendaciones = findViewById(R.id.text_recomendaciones);
        recyclerIngredientesEscalados = findViewById(R.id.recycler_ingredientes_escalados);
        textPreparacionAjustada = findViewById(R.id.text_preparacion_ajustada);

        // Configurar RecyclerView
        setupRecyclerView();

        // Ocultar resultado inicialmente
        cardResultado.setVisibility(View.GONE);
    }

    // ✅ CONFIGURAR RECYCLERVIEW
    private void setupRecyclerView() {
        if (recyclerIngredientesEscalados != null) {
            recyclerIngredientesEscalados.setLayoutManager(new LinearLayoutManager(this));
            recyclerIngredientesEscalados.setNestedScrollingEnabled(false);

            ingredientesAdapter = new IngredientesEscaladosAdapter(this);
            recyclerIngredientesEscalados.setAdapter(ingredientesAdapter);
        }
    }

    // ✅ OBTENER DATOS DEL INTENT
    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            // Obtener banquete desde intent
            if (intent.hasExtra("banquete")) {
                banqueteOriginal = (Banquete) intent.getSerializableExtra("banquete");
                Log.d(TAG, "✅ Banquete recibido: " + banqueteOriginal.getNombre());
            } else if (intent.hasExtra("banquete_id")) {
                int banqueteId = intent.getIntExtra("banquete_id", -1);
                Log.d(TAG, "📋 ID de banquete recibido: " + banqueteId);
                // TODO: Cargar banquete por ID si es necesario
            }

            // Verificar si se recibió el banquete
            if (banqueteOriginal == null) {
                Log.e(TAG, "❌ No se recibió banquete válido");
                Toast.makeText(this, "Error: No se pudo cargar el banquete", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            personasOriginales = banqueteOriginal.getCantidadPersonas();
            Log.d(TAG, "👥 Personas originales: " + personasOriginales);
        }
    }

    // ✅ CONFIGURAR LISTENERS
    private void setupListeners() {
        // Botón volver
        btnVolver.setOnClickListener(v -> finish());

        // TextWatcher para personas nuevas
        editPersonasNuevas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if (!s.toString().trim().isEmpty()) {
                        personasNuevas = Integer.parseInt(s.toString().trim());
                        validarEntrada();
                    } else {
                        personasNuevas = 0;
                        btnEscalar.setEnabled(false);
                    }
                } catch (NumberFormatException e) {
                    personasNuevas = 0;
                    btnEscalar.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Botón escalar
        btnEscalar.setOnClickListener(v -> {
            if (validarEntrada()) {
                realizarEscalado();
            }
        });

        // Botón reiniciar
        btnReiniciar.setOnClickListener(v -> reiniciarFormulario());
    }

    // ✅ MOSTRAR BANQUETE ORIGINAL
    private void mostrarBanqueteOriginal() {
        Log.d(TAG, "📱 Mostrando banquete original: " + banqueteOriginal.getNombre());

        // Nombre del banquete
        textNombreBanquete.setText(banqueteOriginal.getNombre());

        // Descripción
        String descripcion = banqueteOriginal.getDescripcionPreparacion();
        if (descripcion != null && !descripcion.isEmpty()) {
            textDescripcionOriginal.setText(descripcion);
        } else {
            textDescripcionOriginal.setText("Sin descripción disponible");
        }

        // Personas originales
        textPersonasOriginales.setText(String.valueOf(personasOriginales));

        // Imagen del banquete
        if (banqueteOriginal.tieneImagen()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.banquete_nuevo)
                    .error(R.drawable.banquete_nuevo)
                    .centerCrop();

            Glide.with(this)
                    .load(banqueteOriginal.getImagenUrl())
                    .apply(options)
                    .into(imagenBanquete);
        } else {
            imagenBanquete.setImageResource(R.drawable.banquete_nuevo);
        }

        // Configurar placeholder en personas nuevas
        editPersonasNuevas.setHint("Ej: " + (personasOriginales * 2));
    }

    // ✅ VALIDAR ENTRADA
    private boolean validarEntrada() {
        if (personasNuevas <= 0) {
            btnEscalar.setEnabled(false);
            return false;
        }

        if (personasNuevas == personasOriginales) {
            btnEscalar.setEnabled(false);
            editPersonasNuevas.setError("Debe ser diferente a " + personasOriginales);
            return false;
        }

        if (personasNuevas > 1000) {
            btnEscalar.setEnabled(false);
            editPersonasNuevas.setError("Máximo 1000 personas");
            return false;
        }

        // Validación exitosa
        editPersonasNuevas.setError(null);
        btnEscalar.setEnabled(true);
        return true;
    }

    // ✅ REALIZAR ESCALADO CON IA
    private void realizarEscalado() {
        if (isLoading) {
            Log.w(TAG, "⚠️ Ya hay un escalado en proceso");
            return;
        }

        Log.d(TAG, "🤖 Iniciando escalado inteligente:");
        Log.d(TAG, "   - Banquete: " + banqueteOriginal.getNombre());
        Log.d(TAG, "   - Personas originales: " + personasOriginales);
        Log.d(TAG, "   - Personas nuevas: " + personasNuevas);

        setLoading(true);

        // Crear JSON para la petición
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.put("banquetId", banqueteOriginal.getIdBanquete());
            requestJson.put("originalPeople", personasOriginales);
            requestJson.put("newPeople", personasNuevas);

            Log.d(TAG, "📋 JSON de petición: " + requestJson.toString());
        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando JSON: ", e);
            setLoading(false);
            mostrarError("Error preparando la solicitud");
            return;
        }

        // Crear RequestBody
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                requestJson.toString()
        );

        // Realizar petición
        banqueteApi.escalarBanqueteConIA(requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d(TAG, "📡 Respuesta recibida: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseJson = response.body().string();
                        Log.d(TAG, "✅ Respuesta exitosa recibida");
                        Log.d(TAG, "📄 JSON respuesta: " + responseJson.substring(0, Math.min(200, responseJson.length())) + "...");

                        procesarRespuestaEscalado(responseJson);

                    } catch (IOException e) {
                        Log.e(TAG, "❌ Error leyendo respuesta: ", e);
                        mostrarError("Error procesando la respuesta del servidor");
                    }
                } else {
                    Log.e(TAG, "❌ Error en la respuesta: " + response.code());
                    String errorMsg = "Error del servidor (" + response.code() + ")";

                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Error body: " + errorBody);

                            JSONObject errorObj = new JSONObject(errorBody);
                            if (errorObj.has("error")) {
                                errorMsg = errorObj.getString("error");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parseando error: ", e);
                    }

                    mostrarError(errorMsg);
                }

                setLoading(false);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión: ", t);
                setLoading(false);
                mostrarError("Error de conexión. Verifica tu internet.");
            }
        });
    }

    // ✅ PROCESAR RESPUESTA DE ESCALADO
    private void procesarRespuestaEscalado(String responseJson) {
        try {
            JSONObject response = new JSONObject(responseJson);

            // Verificar si fue exitoso
            if (!response.optBoolean("success", false)) {
                String error = response.optString("error", "Error desconocido");
                mostrarError(error);
                return;
            }

            // Obtener datos del escalado
            JSONObject data = response.optJSONObject("data");
            if (data == null) {
                mostrarError("Respuesta inválida del servidor");
                return;
            }

            // Crear objeto BanqueteEscalado
            banqueteEscalado = BanqueteEscalado.fromJson(data);

            if (banqueteEscalado != null) {
                Log.d(TAG, "✅ Escalado procesado exitosamente");
                Log.d(TAG, "   - Factor: " + banqueteEscalado.getScaleFactor());
                Log.d(TAG, "   - Ingredientes escalados: " + banqueteEscalado.getScaledIngredients().size());
                Log.d(TAG, "   - Recomendaciones: " + banqueteEscalado.getRecommendations().size());

                mostrarResultadoEscalado();
            } else {
                mostrarError("Error procesando el escalado");
            }

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error parseando respuesta JSON: ", e);
            mostrarError("Error procesando la respuesta");
        }
    }

    // ✅ MOSTRAR RESULTADO DEL ESCALADO
    private void mostrarResultadoEscalado() {
        Log.d(TAG, "📱 Mostrando resultado del escalado");

        // Mostrar factor de escala
        String factorText = String.format("Factor de escala: %.2fx\n(%d → %d personas)",
                banqueteEscalado.getScaleFactor(),
                personasOriginales,
                personasNuevas);
        textFactorEscala.setText(factorText);

        // Mostrar recomendaciones
        if (banqueteEscalado.getRecommendations() != null && !banqueteEscalado.getRecommendations().isEmpty()) {
            StringBuilder recomendaciones = new StringBuilder("🤖 Recomendaciones de IA:\n\n");
            for (String rec : banqueteEscalado.getRecommendations()) {
                recomendaciones.append("• ").append(rec).append("\n");
            }
            textRecomendaciones.setText(recomendaciones.toString());
            textRecomendaciones.setVisibility(View.VISIBLE);
        } else {
            textRecomendaciones.setVisibility(View.GONE);
        }

        // Mostrar ingredientes escalados
        if (banqueteEscalado.getScaledIngredients() != null && !banqueteEscalado.getScaledIngredients().isEmpty()) {
            ingredientesAdapter.updateIngredientes(banqueteEscalado.getScaledIngredients());
        }

        // Mostrar preparación ajustada
        if (banqueteEscalado.getAdjustedPreparation() != null && !banqueteEscalado.getAdjustedPreparation().isEmpty()) {
            textPreparacionAjustada.setText(banqueteEscalado.getAdjustedPreparation());
            textPreparacionAjustada.setVisibility(View.VISIBLE);
        } else {
            textPreparacionAjustada.setVisibility(View.GONE);
        }

        // Mostrar card de resultado
        cardResultado.setVisibility(View.VISIBLE);

        // Scroll para mostrar resultado
        cardResultado.post(() -> {
            cardResultado.requestFocus();
        });

        Toast.makeText(this, "✅ Escalado completado con IA", Toast.LENGTH_SHORT).show();
    }

    // ✅ REINICIAR FORMULARIO
    private void reiniciarFormulario() {
        Log.d(TAG, "🔄 Reiniciando formulario");

        editPersonasNuevas.setText("");
        editPersonasNuevas.setError(null);
        personasNuevas = 0;
        btnEscalar.setEnabled(false);

        cardResultado.setVisibility(View.GONE);
        banqueteEscalado = null;

        if (ingredientesAdapter != null) {
            ingredientesAdapter.clearIngredientes();
        }

        Toast.makeText(this, "Formulario reiniciado", Toast.LENGTH_SHORT).show();
    }

    // ✅ MOSTRAR ERROR
    private void mostrarError(String mensaje) {
        Log.e(TAG, "❌ Error mostrado: " + mensaje);

        runOnUiThread(() -> {
            Toast.makeText(this, "❌ " + mensaje, Toast.LENGTH_LONG).show();
        });
    }

    // ✅ GESTIONAR ESTADO DE LOADING
    private void setLoading(boolean loading) {
        isLoading = loading;

        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            }

            btnEscalar.setEnabled(!loading && validarEntrada());
            editPersonasNuevas.setEnabled(!loading);
        });
    }

    // ✅ MÉTODO ESTÁTICO PARA LANZAR ACTIVIDAD
    public static void launch(android.content.Context context, Banquete banquete) {
        Intent intent = new Intent(context, BanqueteEscaladoActivity.class);
        intent.putExtra("banquete", banquete);
        context.startActivity(intent);
    }
}