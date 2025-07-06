package com.camilo.cocinarte;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.FavoritosService;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.api.RecetasService;
import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.LikeResponse;
import com.camilo.cocinarte.models.ReaccionesResponse;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.VerificarFavoritoResponse;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment;
import com.camilo.cocinarte.models.Ingrediente;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRecetaActivity extends AppCompatActivity {
    private static final String TAG = "DetalleRecetaActivity";

    private Receta recetaActual;
    private ReaccionesResponse reaccionesResponse;
    private SessionManager sessionManager;

    // ✅ NUEVAS VARIABLES PARA CONTROLAR MODO
    private boolean isFromInicio = false;
    private boolean hasAuthentication = false;

    // Views principales
    private ImageView imgFavorito;
    private ImageView btnLikes;
    private ImageView btnComentarios;
    private TextView likesCount;
    private TextView comentariosCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_receta);

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // Inicializar vistas
        initViews();

        // ✅ VERIFICAR ORIGEN Y AUTENTICACIÓN
        checkAuthenticationAndOrigin();

        // Obtener ID de la receta del intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("receta_id")) {
            int recetaId = intent.getIntExtra("receta_id", -1);
            Log.d(TAG, "Recibido receta_id: " + recetaId);
            Log.d(TAG, "Viene de inicio: " + isFromInicio);
            Log.d(TAG, "Tiene autenticación: " + hasAuthentication);

            if (recetaId != -1) {
                // ✅ CARGAR RECETA SEGÚN EL MODO
                if (hasAuthentication) {
                    getRecetaByIdConAuth(recetaId);
                } else {
                    getRecetaByIdSinAuth(recetaId);
                }
            } else {
                Log.e(TAG, "ID de receta inválido");
                Toast.makeText(this, "Error: ID de receta inválido", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.e(TAG, "No se recibió ID de receta");
            Toast.makeText(this, "Error: No se encontró la receta", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar listeners
        setupListeners();
    }

    // ✅ NUEVO MÉTODO: Verificar autenticación y origen
    private void checkAuthenticationAndOrigin() {
        Intent intent = getIntent();
        isFromInicio = intent.getBooleanExtra("from_inicio", false);

        String token = sessionManager.getAuthToken();
        hasAuthentication = (token != null && !token.isEmpty());

        Log.d(TAG, "🔍 Verificación de autenticación:");
        Log.d(TAG, "   - Viene de inicio: " + isFromInicio);
        Log.d(TAG, "   - Token presente: " + hasAuthentication);
        Log.d(TAG, "   - Modo: " + (hasAuthentication ? "CON AUTH" : "SIN AUTH"));
    }

    private void initViews() {
        btnLikes = findViewById(R.id.btn_likes_favoritos);
        btnComentarios = findViewById(R.id.btn_comentarios_favoritos);
        imgFavorito = findViewById(R.id.imgFavorito);
        likesCount = findViewById(R.id.likes_favoritos);
        comentariosCount = findViewById(R.id.comentarios_favoritos);
    }

    private void setupListeners() {
        // Listener para likes
        btnLikes.setOnClickListener(v -> {
            if (recetaActual != null) {
                if (hasAuthentication) {
                    toggleLike(recetaActual.getIdReceta());
                } else {
                    // ✅ MOSTRAR MENSAJE PARA USUARIOS SIN AUTH
                    Toast.makeText(this, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Receta no cargada", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para comentarios
        btnComentarios.setOnClickListener(v -> {
            if (recetaActual != null) {
                if (hasAuthentication) {
                    abrirSeccionComentarios();
                } else {
                    // ✅ MOSTRAR MENSAJE PARA USUARIOS SIN AUTH
                    Toast.makeText(this, "Inicia sesión para ver comentarios", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Receta no cargada", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener para favoritos
        imgFavorito.setOnClickListener(v -> {
            if (recetaActual != null) {
                if (hasAuthentication) {
                    toggleFavorito(recetaActual.getIdReceta());
                } else {
                    // ✅ MOSTRAR MENSAJE PARA USUARIOS SIN AUTH
                    Toast.makeText(this, "Inicia sesión para guardar en favoritos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Receta no cargada", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ NUEVO MÉTODO: Cargar receta CON autenticación (original)
    private void getRecetaByIdConAuth(int recetaId) {
        Log.d(TAG, "🔐 Cargando receta CON autenticación - ID: " + recetaId);

        String token = sessionManager.getAuthToken();
        String authHeader = "Bearer " + token;
        RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);

        recetasService.getRecetaById(recetaId, authHeader).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                Log.d(TAG, "Respuesta de getRecetaById CON AUTH: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    recetaActual = response.body();
                    Log.d(TAG, "✅ Receta obtenida CON AUTH: " + recetaActual.getTitulo());

                    // Cargar reacciones de la receta
                    getReaccionesRecetaConAuth(recetaId, authHeader);
                } else {
                    Log.e(TAG, "❌ Error al obtener receta CON AUTH: " + response.code() + " - " + response.message());
                    Toast.makeText(getApplicationContext(), "Error al cargar la receta", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener receta CON AUTH: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ✅ NUEVO MÉTODO: Cargar receta SIN autenticación
    private void getRecetaByIdSinAuth(int recetaId) {
        Log.d(TAG, "🌐 Cargando receta SIN autenticación - ID: " + recetaId);

        RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);

        // ✅ LLAMAR SIN TOKEN (usando el método original pero sin auth header)
        recetasService.getRecetaById(recetaId, "").enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                Log.d(TAG, "Respuesta de getRecetaById SIN AUTH: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    recetaActual = response.body();
                    Log.d(TAG, "✅ Receta obtenida SIN AUTH: " + recetaActual.getTitulo());

                    // ✅ MOSTRAR RECETA SIN CARGAR REACCIONES
                    mostrarDetallesReceta(recetaActual);

                    // ✅ CONFIGURAR UI PARA MODO SIN AUTH
                    configurarUISinAuth();
                } else {
                    Log.e(TAG, "❌ Error al obtener receta SIN AUTH: " + response.code() + " - " + response.message());
                    Toast.makeText(getApplicationContext(), "Error al cargar la receta", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener receta SIN AUTH: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // ✅ NUEVO MÉTODO: Configurar UI para modo sin autenticación
    private void configurarUISinAuth() {
        Log.d(TAG, "🎨 Configurando UI para modo SIN AUTH");

        // Mostrar valores por defecto para reacciones
        likesCount.setText("0");
        comentariosCount.setText("0");

        // Configurar iconos en estado inactivo
        btnLikes.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
        ));

        imgFavorito.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
        ));

        // Mostrar mensaje informativo
        Toast.makeText(this, "💡 Inicia sesión para dar like, comentar y guardar favoritos", Toast.LENGTH_LONG).show();
    }

    // ✅ MÉTODO ORIGINAL: Cargar reacciones con autenticación
    private void getReaccionesRecetaConAuth(int recetaId, String authHeader) {
        Log.d(TAG, "🔄 Obteniendo reacciones CON AUTH para receta: " + recetaId);

        RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);

        recetasService.getReaccionesRecetaById(recetaId, authHeader).enqueue(new Callback<ReaccionesResponse>() {
            @Override
            public void onResponse(Call<ReaccionesResponse> call, Response<ReaccionesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reaccionesResponse = response.body();
                    Log.d(TAG, "✅ Reacciones obtenidas - Likes: " + reaccionesResponse.getLikes().getTotal() +
                            ", Comentarios: " + reaccionesResponse.getTotal_comentarios());

                    // Mostrar todos los detalles de la receta
                    mostrarDetallesReceta(recetaActual);

                    // Cargar estado inicial del favorito
                    cargarEstadoInicialFavorito(recetaId);
                } else {
                    Log.e(TAG, "⚠️ Error al obtener reacciones: " + response.code());
                    // Mostrar la receta sin reacciones
                    mostrarDetallesReceta(recetaActual);
                    cargarEstadoInicialFavorito(recetaId);
                }
            }

            @Override
            public void onFailure(Call<ReaccionesResponse> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al obtener reacciones: ", t);
                // Mostrar la receta sin reacciones
                mostrarDetallesReceta(recetaActual);
                cargarEstadoInicialFavorito(recetaId);
            }
        });
    }

    // ✅ MÉTODO ORIGINAL SIN CAMBIOS (el resto del código permanece igual)
    private void mostrarDetallesReceta(Receta receta) {
        Log.d(TAG, "📱 Mostrando detalles de la receta: " + receta.getTitulo());

        // Logo
        ImageView logo = findViewById(R.id.logo_image);
        Glide.with(this).load(R.drawable.logo_cocinarte).into(logo);

        // Nombre de la receta
        TextView nombreReceta = findViewById(R.id.recipe_name);
        nombreReceta.setText(receta.getTitulo());

        // Información nutricional
        TextView kcl = findViewById(R.id.nutrition_kcl);
        TextView p = findViewById(R.id.nutrition_p);
        TextView c = findViewById(R.id.nutrition_c);
        TextView gt = findViewById(R.id.nutrition_gt);

        kcl.setText(String.valueOf((int) receta.getCalorias()));
        p.setText(String.valueOf((int) receta.getProteinas()));
        c.setText(String.valueOf((int) receta.getCarbohidratos()));
        gt.setText(String.valueOf((int) receta.getGrasas()));

        // Imagen de la receta
        ImageView imagenReceta = findViewById(R.id.photoImageDetails);
        if (receta.getImagen() != null && !receta.getImagen().isEmpty() && !receta.getImagen().equals("null")) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.logo_cocinarte)
                    .error(R.drawable.logo_cocinarte)
                    .centerCrop();

            Glide.with(getApplicationContext())
                    .load(receta.getImagen())
                    .apply(options)
                    .into(imagenReceta);
        } else {
            imagenReceta.setImageResource(R.drawable.logo_cocinarte);
        }

        // Tiempo y dificultad
        TextView tiempo = findViewById(R.id.tv_tiempo);
        TextView dificultad = findViewById(R.id.tv_dificultad);

        tiempo.setText(receta.getTiempoPreparacion() != null ? receta.getTiempoPreparacion() : "No especificado");
        dificultad.setText(receta.getDificultad() != null ? receta.getDificultad() : "No especificada");

        // Ingredientes
        mostrarIngredientes(receta);

        // Pasos de preparación
        mostrarPasosPreparacion(receta);

        // ✅ MOSTRAR REACCIONES SOLO SI HAY AUTENTICACIÓN
        if (hasAuthentication) {
            mostrarReacciones();
        }
    }

    private void mostrarIngredientes(Receta receta) {
        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = findViewById(R.id.lista_ingredientes);
        contenedorIngredientes.removeAllViews();

        List<String> ingredientes = new ArrayList<>();
        if (receta.getIngredientes() != null) {
            for (Ingrediente ingrediente : receta.getIngredientes()) {
                ingredientes.add(ingrediente.getNombreIngrediente());
            }
        }

        if (ingredientes.size() == 1 && ingredientes.get(0).contains(",")) {
            String[] separados = ingredientes.get(0).split(",");
            for (String ingrediente : separados) {
                agregarChipIngrediente(contenedorIngredientes, ingrediente.trim());
            }
        } else {
            for (String ingrediente : ingredientes) {
                agregarChipIngrediente(contenedorIngredientes, ingrediente.trim());
            }
        }
    }

    private void mostrarPasosPreparacion(Receta receta) {
        LinearLayout contenedorPasos = findViewById(R.id.lista_pasos);
        contenedorPasos.removeAllViews();

        if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
            int pasoNum = 1;
            ArrayList<String> pasos = new ArrayList<>(Arrays.asList(receta.getDescripcion().split("\n")));

            for (String paso : pasos) {
                if (!paso.trim().isEmpty()) {
                    TextView tvPaso = new TextView(getApplicationContext());
                    tvPaso.setText(String.format("%d. %s", pasoNum++, paso.trim()));
                    tvPaso.setTextSize(16);
                    tvPaso.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
                    tvPaso.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    ));
                    tvPaso.setPadding(0, 0, 0, 16);
                    contenedorPasos.addView(tvPaso);
                }
            }
        }
    }

    private void mostrarReacciones() {
        if (reaccionesResponse != null) {
            // Mostrar contadores
            likesCount.setText(String.valueOf(reaccionesResponse.getLikes().getTotal()));
            comentariosCount.setText(String.valueOf(reaccionesResponse.getTotal_comentarios()));

            // Actualizar estado visual del botón de like
            if (reaccionesResponse.getLikes().isUser_liked()) {
                btnLikes.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
                ));
            } else {
                btnLikes.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                ));
            }
        } else {
            // Valores por defecto si no hay reacciones
            likesCount.setText("0");
            comentariosCount.setText("0");
            btnLikes.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
            ));
        }
    }

    private void toggleLike(int recetaId) {
        Log.d(TAG, "Toggle like para receta: " + recetaId);

        String token = "Bearer " + sessionManager.getAuthToken();
        RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);

        recetasService.sendLike(recetaId, token).enqueue(new Callback<LikeResponse>() {
            @Override
            public void onResponse(Call<LikeResponse> call, Response<LikeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LikeResponse likeResponse = response.body();
                    Log.d(TAG, "Like actualizado - Liked: " + likeResponse.isLiked() +
                            ", Total: " + likeResponse.getTotalLikes());

                    // Actualizar UI
                    if (likeResponse.isLiked()) {
                        btnLikes.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
                        ));
                    } else {
                        btnLikes.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                        ));
                    }

                    likesCount.setText(String.valueOf(likeResponse.getTotalLikes()));
                } else {
                    Log.e(TAG, "Error al dar like: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al dar like", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LikeResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión al dar like: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFavorito(int recetaId) {
        Log.d(TAG, "Toggle favorito para receta: " + recetaId);

        String token = "Bearer " + sessionManager.getAuthToken();
        FavoritosService favoritosService = ApiConfig.getClient(getApplicationContext()).create(FavoritosService.class);

        // Primero verificar el estado actual
        favoritosService.verificarFavorito(recetaId, token).enqueue(new Callback<VerificarFavoritoResponse>() {
            @Override
            public void onResponse(Call<VerificarFavoritoResponse> call, Response<VerificarFavoritoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean esFavorito = response.body().isEsFavorito();
                    Log.d(TAG, "Estado actual favorito: " + esFavorito);

                    if (esFavorito) {
                        // Quitar de favoritos
                        quitarDeFavoritos(recetaId, favoritosService, token);
                    } else {
                        // Agregar a favoritos
                        agregarAFavoritos(recetaId, favoritosService, token);
                    }
                } else {
                    Log.e(TAG, "Error al verificar favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al verificar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VerificarFavoritoResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión al verificar favorito: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void agregarAFavoritos(int recetaId, FavoritosService service, String token) {
        service.setFavorito(recetaId, token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Agregado a favoritos exitosamente");
                    Toast.makeText(getApplicationContext(), "Agregado a favoritos", Toast.LENGTH_SHORT).show();

                    // Actualizar UI - favorito activo
                    imgFavorito.setImageTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                    ));
                } else {
                    Log.e(TAG, "Error al agregar favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al agregar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión al agregar favorito: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void quitarDeFavoritos(int recetaId, FavoritosService service, String token) {
        service.deleteFavorito(recetaId, token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Removido de favoritos exitosamente");
                    Toast.makeText(getApplicationContext(), "Removido de favoritos", Toast.LENGTH_SHORT).show();

                    // Actualizar UI - favorito inactivo
                    imgFavorito.setImageTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                    ));
                } else {
                    Log.e(TAG, "Error al remover favorito: " + response.code());
                    Toast.makeText(getApplicationContext(), "Error al remover favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión al remover favorito: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarEstadoInicialFavorito(int recetaId) {
        // ✅ SOLO CARGAR FAVORITO SI HAY AUTENTICACIÓN
        if (!hasAuthentication) {
            Log.d(TAG, "⚠️ Sin autenticación, no cargando estado de favorito");
            return;
        }

        Log.d(TAG, "Cargando estado inicial del favorito para receta: " + recetaId);

        String token = "Bearer " + sessionManager.getAuthToken();
        FavoritosService favoritosService = ApiConfig.getClient(getApplicationContext()).create(FavoritosService.class);

        favoritosService.verificarFavorito(recetaId, token).enqueue(new Callback<VerificarFavoritoResponse>() {
            @Override
            public void onResponse(Call<VerificarFavoritoResponse> call, Response<VerificarFavoritoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean esFavorito = response.body().isEsFavorito();
                    Log.d(TAG, "Estado inicial favorito: " + esFavorito);

                    // Actualizar UI según el estado
                    if (esFavorito) {
                        imgFavorito.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                        ));
                    } else {
                        imgFavorito.setImageTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                        ));
                    }
                } else {
                    Log.e(TAG, "Error al cargar estado favorito: " + response.code());
                    // Estado por defecto
                    imgFavorito.setImageTintList(ColorStateList.valueOf(
                            ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                    ));
                }
            }

            @Override
            public void onFailure(Call<VerificarFavoritoResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión al cargar estado favorito: ", t);
                // Estado por defecto
                imgFavorito.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                ));
            }
        });
    }

    private void abrirSeccionComentarios() {
        if (recetaActual != null) {
            Log.d(TAG, "Abriendo sección de comentarios para receta: " + recetaActual.getIdReceta());

            ReaccionApi api = ApiConfig.getClient(getApplicationContext()).create(ReaccionApi.class);
            api.getReaccionesPorReceta(recetaActual.getIdReceta()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JSONObject obj = new JSONObject(json);
                            JSONArray comentariosArray = obj.getJSONArray("comentarios");

                            Log.d(TAG, "Comentarios obtenidos: " + comentariosArray.length());

                            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(
                                    comentariosArray,
                                    recetaActual.getIdReceta()
                            );
                            modal.show(getSupportFragmentManager(), "ComentariosBottomSheet");
                        } catch (Exception e) {
                            Log.e(TAG, "Error al procesar comentarios: ", e);
                            Toast.makeText(getApplicationContext(), "Error al procesar comentarios", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error al obtener comentarios: " + response.code());
                        Toast.makeText(getApplicationContext(), "No se pudieron obtener los comentarios", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error de red al cargar comentarios: ", t);
                    Toast.makeText(getApplicationContext(), "Error de red al cargar comentarios", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void agregarChipIngrediente(com.google.android.flexbox.FlexboxLayout contenedor, String ingrediente) {
        TextView tvIngrediente = new TextView(getApplicationContext());
        tvIngrediente.setText(ingrediente);
        tvIngrediente.setTextSize(14);
        tvIngrediente.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
        tvIngrediente.setPadding(30, 20, 30, 20);
        tvIngrediente.setBackgroundResource(R.drawable.bg_chip);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(10, 10, 10, 10);
        tvIngrediente.setLayoutParams(params);

        contenedor.addView(tvIngrediente);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ SOLO RECARGAR FAVORITO SI HAY AUTENTICACIÓN
        if (recetaActual != null && hasAuthentication) {
            cargarEstadoInicialFavorito(recetaActual.getIdReceta());
        }
    }
}