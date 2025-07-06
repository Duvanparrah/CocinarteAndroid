package com.camilo.cocinarte.ui.banquetes;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BanqueteDetalleActivity extends AppCompatActivity {
    private static final String TAG = "BanqueteDetalleActivity";

    private Banquete banqueteActual;
    private SessionManager sessionManager;

    // ✅ VARIABLES PARA CONTROLAR MODO
    private boolean hasAuthentication = false;

    // Views principales
    private ProgressBar progressBar;
    private ImageView imagenBanquete;
    private TextView textNombreBanquete;
    private TextView textCantidadPersonas;
    private TextView textTiempoPreparacion;
    private TextView textDificultad;
    private TextView textDescripcionPreparacion;

    // Views de reacciones
    private ImageView btnLike;
    private ImageView btnFavorito;
    private ImageView btnCompartir;
    private TextView textTotalLikes;
    private TextView textTotalComentarios;

    // RecyclerViews para platillos e ingredientes
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

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Inicializar vistas
        initViews();

        // ✅ VERIFICAR AUTENTICACIÓN
        checkAuthentication();

        // Obtener ID del banquete del intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("banquete_id")) {
            int banqueteId = intent.getIntExtra("banquete_id", -1);
            Log.d(TAG, "Recibido banquete_id: " + banqueteId);
            Log.d(TAG, "Tiene autenticación: " + hasAuthentication);

            if (banqueteId != -1) {
                // ✅ CARGAR BANQUETE SEGÚN EL MODO
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

        // Configurar listeners
        setupListeners();
    }

    // ✅ VERIFICAR AUTENTICACIÓN
    private void checkAuthentication() {
        String token = sessionManager.getAuthToken();
        hasAuthentication = (token != null && !token.isEmpty());

        Log.d(TAG, "🔍 Verificación de autenticación:");
        Log.d(TAG, "   - Token presente: " + hasAuthentication);
        Log.d(TAG, "   - Modo: " + (hasAuthentication ? "CON AUTH" : "SIN AUTH"));
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);

        // Imagen y información básica
        imagenBanquete = findViewById(R.id.imagen_banquete);
        textNombreBanquete = findViewById(R.id.text_nombre_banquete);
        textCantidadPersonas = findViewById(R.id.text_cantidad_personas);
        textTiempoPreparacion = findViewById(R.id.text_tiempo_preparacion);
        textDificultad = findViewById(R.id.text_dificultad);
        textDescripcionPreparacion = findViewById(R.id.text_descripcion_preparacion);

        // Botones de reacciones
        btnLike = findViewById(R.id.btn_like);
        btnFavorito = findViewById(R.id.btn_favorito);
        btnCompartir = findViewById(R.id.btn_compartir);
        textTotalLikes = findViewById(R.id.text_total_likes);
        textTotalComentarios = findViewById(R.id.text_total_comentarios);

        // RecyclerViews
        recyclerPlatillos = findViewById(R.id.recycler_platillos);
        recyclerIngredientes = findViewById(R.id.recycler_ingredientes);
        textNoPlatillos = findViewById(R.id.text_no_platillos);
        textNoIngredientes = findViewById(R.id.text_no_ingredientes);

        // Configurar RecyclerViews
        setupRecyclerViews();
    }

    private void setupRecyclerViews() {
        // RecyclerView de platillos
        if (recyclerPlatillos != null) {
            recyclerPlatillos.setLayoutManager(new LinearLayoutManager(this));
            recyclerPlatillos.setNestedScrollingEnabled(false);
        }

        // RecyclerView de ingredientes
        if (recyclerIngredientes != null) {
            recyclerIngredientes.setLayoutManager(new LinearLayoutManager(this));
            recyclerIngredientes.setNestedScrollingEnabled(false);
        }
    }

    private void setupListeners() {
        // Listener para likes
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

        // Listener para favoritos
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

        // Listener para compartir
        if (btnCompartir != null) {
            btnCompartir.setOnClickListener(v -> {
                if (banqueteActual != null) {
                    compartirBanquete();
                }
            });
        }
    }

    // ✅ CARGAR BANQUETE CON AUTENTICACIÓN
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

                    // Mostrar detalles del banquete
                    mostrarDetallesBanquete(banqueteActual);

                    // Cargar reacciones del banquete
                    cargarReaccionesBanqueteConAuth(banqueteId, authHeader);
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

    // ✅ CARGAR BANQUETE SIN AUTENTICACIÓN
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

                    // ✅ MOSTRAR BANQUETE SIN CARGAR REACCIONES
                    mostrarDetallesBanquete(banqueteActual);

                    // ✅ CONFIGURAR UI PARA MODO SIN AUTH
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

    // ✅ CONFIGURAR UI PARA MODO SIN AUTENTICACIÓN
    private void configurarUISinAuth() {
        Log.d(TAG, "🎨 Configurando UI para modo SIN AUTH");

        // Mostrar valores por defecto para reacciones
        if (textTotalLikes != null) {
            textTotalLikes.setText("0");
        }
        if (textTotalComentarios != null) {
            textTotalComentarios.setText("0");
        }

        // Configurar iconos en estado inactivo
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

        // Mostrar mensaje informativo
        Toast.makeText(this, "💡 Inicia sesión para dar like, comentar y guardar favoritos", Toast.LENGTH_LONG).show();
    }

    // ✅ CARGAR REACCIONES CON AUTENTICACIÓN
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

                        // Obtener datos de likes
                        JSONObject likesObj = obj.optJSONObject("likes");
                        int totalLikes = 0;
                        boolean userLiked = false;

                        if (likesObj != null) {
                            totalLikes = likesObj.optInt("total", 0);
                            userLiked = likesObj.optBoolean("user_liked", false);
                        }

                        // Obtener total de comentarios
                        int totalComentarios = obj.optInt("total_comentarios", 0);

                        Log.d(TAG, "✅ Reacciones obtenidas - Likes: " + totalLikes +
                                " (user_liked=" + userLiked + "), Comentarios: " + totalComentarios);

                        // Actualizar UI
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

    // ✅ MOSTRAR DETALLES DEL BANQUETE
    private void mostrarDetallesBanquete(Banquete banquete) {
        Log.d(TAG, "📱 Mostrando detalles del banquete: " + banquete.getNombre());

        // Imagen principal
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

        // Información básica
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

            // Aplicar color según dificultad
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
                    case "difícil":
                    case "dificil":
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

        // ✅ MOSTRAR PLATILLOS
        mostrarPlatillos(banquete.getPlatillos());

        // ✅ MOSTRAR INGREDIENTES
        mostrarIngredientes(banquete.getIngredientes());
    }

    // ✅ MOSTRAR PLATILLOS
    private void mostrarPlatillos(List<BanquetePlatillo> platillos) {
        if (platillos != null && !platillos.isEmpty()) {
            Log.d(TAG, "🍽️ Mostrando " + platillos.size() + " platillos");

            if (recyclerPlatillos != null) {
                platillosAdapter = new PlatillosBanqueteAdapter(this, platillos);
                recyclerPlatillos.setAdapter(platillosAdapter);
                recyclerPlatillos.setVisibility(View.VISIBLE);
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

    // ✅ MOSTRAR INGREDIENTES
    private void mostrarIngredientes(List<BanqueteIngrediente> ingredientes) {
        if (ingredientes != null && !ingredientes.isEmpty()) {
            Log.d(TAG, "🥕 Mostrando " + ingredientes.size() + " ingredientes");

            if (recyclerIngredientes != null) {
                ingredientesAdapter = new IngredientesBanqueteAdapter(this, ingredientes);
                recyclerIngredientes.setAdapter(ingredientesAdapter);
                recyclerIngredientes.setVisibility(View.VISIBLE);
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

    // ✅ MOSTRAR REACCIONES
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

    // ✅ TOGGLE LIKE
    private void toggleLike(int banqueteId) {
        Log.d(TAG, "Toggle like para banquete: " + banqueteId);

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

                        // Actualizar UI
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

    // ✅ TOGGLE FAVORITO
    private void toggleFavorito(int banqueteId) {
        Log.d(TAG, "Toggle favorito para banquete: " + banqueteId);

        String token = "Bearer " + sessionManager.getAuthToken();
        BanqueteApi banqueteApi = ApiClient.getClient(getApplicationContext()).create(BanqueteApi.class);

        banqueteApi.agregarBanqueteAFavoritos(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        String action = obj.optString("action", "");
                        String message = obj.optString("message", "Favorito actualizado");

                        // Actualizar UI
                        if (btnFavorito != null) {
                            if ("added".equals(action)) {
                                btnFavorito.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                                ));
                            } else {
                                btnFavorito.setImageTintList(ColorStateList.valueOf(
                                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                                ));
                            }
                        }

                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar respuesta de favorito", e);
                        Toast.makeText(getApplicationContext(), "Error al procesar favorito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error en favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al gestionar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en favorito", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ COMPARTIR BANQUETE
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

            mensaje += "\n¡Descarga CocinarTe para ver más banquetes!";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mensaje);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Banquete: " + banqueteActual.getNombre());

            startActivity(Intent.createChooser(shareIntent, "Compartir banquete"));
        }
    }

    // ✅ LOADING STATE
    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ SOLO RECARGAR FAVORITO SI HAY AUTENTICACIÓN
        if (banqueteActual != null && hasAuthentication) {
            // TODO: Recargar estado de favorito si es necesario
        }
    }
}