package com.camilo.cocinarte.ui.comunidad;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRecetaFragment extends Fragment {
    private static final String TAG = "DetalleRecetaFragment";

    private Receta recetaActual;
    private ImageView iconLike;
    private TextView textLikeCount;
    private boolean userLiked = false;
    private int totalLikes = 0;
    private boolean likeInicializado = false;
    private boolean recetaCargada = false;
    private boolean reaccionesCargadas = false;
    private ImageView iconGuardar;
    private boolean recetaGuardada = false;
    private SharedPreferences prefsGuardado;

    private String origen = "mis_recetas";

    private JSONArray comentariosArray = new JSONArray();
    private Handler pollingHandler = new Handler();
    private Runnable pollingRunnable;
    private boolean estaBottomSheetAbierto = false;

    // ✅ VARIABLES PARA TABLA NUTRICIONAL - CORREGIDAS
    private TextView nutritionKcl;
    private TextView nutritionP;
    private TextView nutritionC;
    private TextView nutritionGt;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detalle_receta_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            origen = getArguments().getString("origen", "mis_recetas");
        }

        prefsGuardado = requireContext().getSharedPreferences("recetas_guardadas", Context.MODE_PRIVATE);

        // ✅ INICIALIZAR CAMPOS NUTRICIONALES
        inicializarCamposNutricionales(view);

        cargarDatosUsuario(view);

        // ✅ CONFIGURAR ICONOS DE INTERACCIÓN
        configurarIconosInteraccion(view);

        if (getArguments() != null) {
            int idReceta = getArguments().getInt("id_receta", -1);
            if (idReceta != -1) {
                obtenerRecetaDesdeApi(idReceta);
                obtenerReacciones(idReceta);
            }
        }

        // ✅ CONFIGURAR BOTÓN ELIMINAR SEGÚN EL ORIGEN
        configurarBotonEliminar(view);
    }

    // ✅ MÉTODO CORREGIDO: Inicializar campos nutricionales con IDs correctos
    private void inicializarCamposNutricionales(View view) {
        nutritionKcl = view.findViewById(R.id.nutrition_kcl);
        nutritionP = view.findViewById(R.id.nutrition_p);
        nutritionC = view.findViewById(R.id.nutrition_c);
        nutritionGt = view.findViewById(R.id.nutrition_gt);

        // ✅ VERIFICAR QUE LOS ELEMENTOS EXISTAN
        if (nutritionKcl == null || nutritionP == null || nutritionC == null || nutritionGt == null) {
            Log.e(TAG, "❌ ERROR: No se pudieron encontrar los elementos de nutrición en el layout");
            Log.e(TAG, "nutrition_kcl: " + (nutritionKcl != null ? "✅ ENCONTRADO" : "❌ NULL"));
            Log.e(TAG, "nutrition_p: " + (nutritionP != null ? "✅ ENCONTRADO" : "❌ NULL"));
            Log.e(TAG, "nutrition_c: " + (nutritionC != null ? "✅ ENCONTRADO" : "❌ NULL"));
            Log.e(TAG, "nutrition_gt: " + (nutritionGt != null ? "✅ ENCONTRADO" : "❌ NULL"));
        } else {
            // Valores iniciales
            nutritionKcl.setText("0 kcal");
            nutritionP.setText("0 P");
            nutritionC.setText("0 C");
            nutritionGt.setText("0 GT");
            Log.d(TAG, "✅ Campos nutricionales inicializados correctamente");
        }
    }

    // ✅ MÉTODO SEPARADO: Configurar iconos de interacción
    private void configurarIconosInteraccion(View view) {
        // ✅ CONFIGURAR ICONOS CON IDs CORRECTOS DEL LAYOUT
        ImageView iconComentario = view.findViewById(R.id.btn_comentarios_favoritos);
        if (iconComentario != null) {
            iconComentario.setOnClickListener(v -> abrirSeccionComentarios());
        } else {
            Log.w(TAG, "⚠️ btn_comentarios_favoritos no encontrado en el layout");
        }

        ImageView iconCompartir = view.findViewById(R.id.comentarios_favoritos); // Buscar el ícono de compartir correcto
        if (iconCompartir == null) {
            // Buscar por otros posibles IDs de compartir
            iconCompartir = view.findViewById(R.id.share_button);
        }
        if (iconCompartir != null) {
            iconCompartir.setOnClickListener(v -> compartirReceta());
        } else {
            Log.w(TAG, "⚠️ Ícono de compartir no encontrado");
        }

        iconGuardar = view.findViewById(R.id.imgFavorito);
        if (iconGuardar != null) {
            iconGuardar.setOnClickListener(v -> toggleGuardarReceta());
        } else {
            Log.w(TAG, "⚠️ imgFavorito no encontrado en el layout");
        }

        iconLike = view.findViewById(R.id.btn_likes_favoritos);
        textLikeCount = view.findViewById(R.id.likes_favoritos);

        if (iconLike != null) {
            iconLike.setOnClickListener(v -> {
                if (recetaActual != null && likeInicializado) {
                    toggleLike(recetaActual.getIdReceta());
                }
            });
        } else {
            Log.w(TAG, "⚠️ btn_likes_favoritos no encontrado en el layout");
        }
    }

    // ✅ MÉTODO SEPARADO: Configurar botón eliminar
    private void configurarBotonEliminar(View view) {
        ImageView btnEliminar = view.findViewById(R.id.btn_delete_recipe);
        if (btnEliminar != null) {
            if ("comunidad".equals(origen)) {
                btnEliminar.setVisibility(View.GONE);
            } else {
                btnEliminar.setOnClickListener(v -> confirmarEliminacion());
            }
        }
    }

    // ✅ MÉTODO CORREGIDO: Configurar tabla nutricional con datos de la receta
    private void configurarTablaNutricional(Receta receta) {
        if (receta == null) {
            Log.w(TAG, "⚠️ Receta es null, no se puede configurar tabla nutricional");
            return;
        }

        Log.d(TAG, "=== DEBUGGING TABLA NUTRICIONAL ===");
        Log.d(TAG, "🍎 Configurando tabla nutricional para: " + receta.getTitulo());

        // ✅ OBTENER VALORES NUTRICIONALES CON PRIORIDADES
        int calorias = 0;
        double proteinas = 0.0;
        double carbohidratos = 0.0;
        double grasas = 0.0;

        // ✅ PRIORIDAD 1: Usar campos directos de la receta (calculados por IA)
        if (receta.getCalorias() > 0) {
            calorias = receta.getCalorias();
            proteinas = receta.getProteinas();
            carbohidratos = receta.getCarbohidratos();
            grasas = receta.getGrasas();

            Log.d(TAG, "✅ Usando valores directos calculados por IA:");
            Log.d(TAG, "   - Calorías: " + calorias + " kcal");
            Log.d(TAG, "   - Proteínas: " + proteinas + " g");
            Log.d(TAG, "   - Carbohidratos: " + carbohidratos + " g");
            Log.d(TAG, "   - Grasas: " + grasas + " g");
        }
        // ✅ PRIORIDAD 2: Usar objeto nutricion si existe
        else if (receta.getNutricion() != null) {
            Receta.NutricionInfo nutricion = receta.getNutricion();
            calorias = nutricion.getCalorias();
            proteinas = nutricion.getProteinas();
            carbohidratos = nutricion.getCarbohidratos();
            grasas = nutricion.getGrasas();

            Log.d(TAG, "✅ Usando objeto nutrición: cal=" + calorias + ", prot=" + proteinas +
                    ", carb=" + carbohidratos + ", gras=" + grasas);
        }
        // ✅ FALLBACK: Valores por defecto si no hay datos nutricionales
        else {
            Log.w(TAG, "⚠️ No hay datos nutricionales para " + receta.getTitulo() + ", usando valores por defecto");
        }

        // ✅ ACTUALIZAR LA UI CON LOS VALORES CALCULADOS - VERIFICANDO QUE LOS ELEMENTOS EXISTAN
        if (nutritionKcl != null) {
            String caloriasTxt = calorias > 0 ? calorias + " kcal" : "000 kcal";
            nutritionKcl.setText(caloriasTxt);
            Log.d(TAG, "✅ Calorías actualizadas: " + caloriasTxt);
        } else {
            Log.e(TAG, "❌ nutrition_kcl es NULL, no se puede actualizar");
        }

        if (nutritionP != null) {
            String proteinasTxt = proteinas > 0 ? String.format("%.0f P", proteinas) : "00 P";
            nutritionP.setText(proteinasTxt);
            Log.d(TAG, "✅ Proteínas actualizadas: " + proteinasTxt);
        } else {
            Log.e(TAG, "❌ nutrition_p es NULL, no se puede actualizar");
        }

        if (nutritionC != null) {
            String carbohidratosTxt = carbohidratos > 0 ? String.format("%.0f C", carbohidratos) : "00 C";
            nutritionC.setText(carbohidratosTxt);
            Log.d(TAG, "✅ Carbohidratos actualizados: " + carbohidratosTxt);
        } else {
            Log.e(TAG, "❌ nutrition_c es NULL, no se puede actualizar");
        }

        if (nutritionGt != null) {
            String grasasTxt = grasas > 0 ? String.format("%.0f GT", grasas) : "00 GT";
            nutritionGt.setText(grasasTxt);
            Log.d(TAG, "✅ Grasas actualizadas: " + grasasTxt);
        } else {
            Log.e(TAG, "❌ nutrition_gt es NULL, no se puede actualizar");
        }

        // ✅ MOSTRAR MENSAJE INFORMATIVO SI LOS VALORES FUERON CALCULADOS
        if (calorias > 0 && getContext() != null) {
            Toast.makeText(getContext(),
                    "📊 Valores nutricionales calculados con IA",
                    Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "🎯 Tabla nutricional configurada exitosamente");
    }

    // ✅ MÉTODO SEPARADO: Compartir receta
    private void compartirReceta() {
        if (recetaActual != null) {
            int id = recetaActual.getIdReceta();
            String url = "https://cocinarte-frontend.vercel.app/receta/" + id;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta receta en Cocinarte! 🍽️\n" + url);

            startActivity(Intent.createChooser(intent, "Compartir receta con..."));
        } else {
            Toast.makeText(getContext(), "Espera a que se cargue la receta", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ MÉTODO SEPARADO: Toggle guardar receta
    private void toggleGuardarReceta() {
        recetaGuardada = !recetaGuardada;

        if (recetaActual != null) {
            prefsGuardado.edit()
                    .putBoolean(String.valueOf(recetaActual.getIdReceta()), recetaGuardada)
                    .apply();
        }

        actualizarIconoGuardar();
        Toast.makeText(getContext(),
                recetaGuardada ? "Receta guardada" : "Guardado eliminado",
                Toast.LENGTH_SHORT).show();

        animarIcono(iconGuardar);
    }

    private void actualizarIconoGuardar() {
        if (iconGuardar != null) {
            int iconRes = recetaGuardada ?
                    R.drawable.ic_bookmark_filled_orange :
                    R.drawable.ic_bookmark_outline;
            iconGuardar.setImageResource(iconRes);
        }
    }

    private void abrirSeccionComentarios() {
        if (recetaActual != null) {
            ReaccionApi api = ApiClient.getClient(requireContext()).create(ReaccionApi.class);
            api.getReaccionesPorReceta(recetaActual.getIdReceta()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JSONObject obj = new JSONObject(json);
                            JSONArray comentariosArray = obj.getJSONArray("comentarios");

                            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(comentariosArray, recetaActual.getIdReceta());

                            modal.setComentariosListener(new ComentariosBottomSheetFragment.ComentariosListener() {
                                @Override
                                public void onComentariosCerrados() {
                                    actualizarComentariosAlCerrarModal();
                                }
                            });

                            modal.show(getChildFragmentManager(), modal.getTag());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error al procesar comentarios", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "No se pudieron obtener los comentarios", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "Error de red al cargar comentarios", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void actualizarComentariosAlCerrarModal() {
        if (recetaActual != null) {
            Log.d("DETALLE", "Modal cerrado. Actualizando comentarios...");
            obtenerReacciones(recetaActual.getIdReceta());
        }
    }

    private void animarIcono(View icono) {
        if (icono == null) return;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(icono, "scaleX", 1f, 1.4f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(icono, "scaleY", 1f, 1.4f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void verificarCargaCompleta() {
        if (recetaCargada && reaccionesCargadas) {
            Log.d("DETALLE", "✅ Receta y reacciones cargadas completamente");

            if (getContext() != null) {
                Toast.makeText(getContext(), "Receta cargada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ MÉTODO CORREGIDO: Obtener receta desde la API
    private void obtenerRecetaDesdeApi(int idReceta) {
        RecetaApi recetaApi = ApiClient.getClient(requireContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String token = loginManager.getToken();

        // ✅ USAR EL MÉTODO CON AUTENTICACIÓN PARA OBTENER NUTRICIÓN COMPLETA
        recetaApi.obtenerRecetaPorIdConAuth(idReceta, "Bearer " + token).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recetaActual = response.body();

                    Log.d(TAG, "✅ Receta obtenida: " + recetaActual.getTitulo());
                    Log.d(TAG, "📊 Datos nutricionales directos:");
                    Log.d(TAG, "   - Calorías: " + recetaActual.getCalorias());
                    Log.d(TAG, "   - Proteínas: " + recetaActual.getProteinas());
                    Log.d(TAG, "   - Carbohidratos: " + recetaActual.getCarbohidratos());
                    Log.d(TAG, "   - Grasas: " + recetaActual.getGrasas());

                    if (recetaActual.getNutricion() != null) {
                        Log.d(TAG, "📊 Objeto nutrición también disponible: " + recetaActual.getNutricion().toString());
                    } else {
                        Log.d(TAG, "📊 Objeto nutrición: NULL");
                    }

                    recetaGuardada = prefsGuardado.getBoolean(String.valueOf(recetaActual.getIdReceta()), false);
                    actualizarIconoGuardar();

                    // ✅ PRIMERO CONFIGURAR NUTRICIÓN, LUEGO MOSTRAR DETALLES
                    configurarTablaNutricional(recetaActual);

                    // ✅ MOSTRAR DETALLES SIN SOBRESCRIBIR NUTRICIÓN
                    mostrarDetallesReceta(recetaActual);

                    recetaCargada = true;
                    verificarCargaCompleta();
                } else {
                    Log.e("DETALLE_ERROR", "Error HTTP: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar la receta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("DETALLE_ERROR", "Error al obtener receta: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void obtenerReacciones(int recetaId) {
        ReaccionApi reaccionApi = ApiClient.getClient(requireContext()).create(ReaccionApi.class);
        reaccionApi.getReaccionesPorReceta(recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        totalLikes = obj.getJSONObject("likes").getInt("total");
                        userLiked = obj.getJSONObject("likes").getBoolean("user_liked");
                        likeInicializado = true;
                        actualizarLikeUI();

                        comentariosArray = obj.getJSONArray("comentarios");
                        int totalComentarios = obj.getInt("total_comentarios");

                        // ✅ USAR EL ID CORRECTO DEL LAYOUT
                        TextView textCommentCount = requireView().findViewById(R.id.comentarios_favoritos);
                        if (textCommentCount != null) {
                            textCommentCount.setText(String.valueOf(totalComentarios));
                        } else {
                            Log.d("DETALLE", "Total comentarios: " + totalComentarios);
                        }

                        reaccionesCargadas = true;
                        verificarCargaCompleta();

                    } catch (IOException | JSONException e) {
                        Log.e("REACCIONES", "Error al procesar respuesta: " + e.getMessage());
                        e.printStackTrace();
                    }

                } else {
                    Log.e("REACCIONES", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("REACCIONES", "Fallo conexión: " + t.getMessage());
            }
        });
    }

    private void toggleLike(int recetaId) {
        ReaccionApi reaccionApi = ApiClient.getClient(requireContext()).create(ReaccionApi.class);

        LoginManager loginManager = new LoginManager(requireContext());
        String token = loginManager.getToken();

        reaccionApi.toggleLike("Bearer " + token, recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        userLiked = obj.getBoolean("isLiked");
                        totalLikes = obj.getInt("totalLikes");

                        animarCorazon();
                        actualizarLikeUI();

                    } catch (IOException | JSONException e) {
                        Log.e("LIKE", "Error al parsear respuesta: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Log.e("LIKE", "Error HTTP: " + response.code());
                        if (response.errorBody() != null) {
                            Log.e("LIKE", "ErrorBody: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("LIKE", "Error en conexión: " + t.getMessage());
            }
        });
    }

    private void animarCorazon() {
        if (iconLike != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconLike, "scaleX", 1f, 1.4f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconLike, "scaleY", 1f, 1.4f, 1f);
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            scaleX.setInterpolator(new DecelerateInterpolator());
            scaleY.setInterpolator(new DecelerateInterpolator());
            scaleX.start();
            scaleY.start();
        }
    }

    private void actualizarLikeUI() {
        if (iconLike != null) {
            int iconRes = userLiked ? R.drawable.ic_heart_like : R.drawable.ic_heart_outline;
            iconLike.setImageResource(iconRes);
            iconLike.setVisibility(View.VISIBLE);
        }
        if (textLikeCount != null) {
            textLikeCount.setText(String.valueOf(totalLikes));
        } else {
            Log.d("DETALLE", "Likes: " + totalLikes + (userLiked ? " (me gusta)" : ""));
        }
    }

    private void cargarDatosUsuario(View view) {
        ImageView userProfileImage = view.findViewById(R.id.user_profile_image);
        TextView userEmail = view.findViewById(R.id.user_email);
        TextView userName = view.findViewById(R.id.user_name);

        if ("comunidad".equals(origen)) {
            if (userProfileImage != null) userProfileImage.setVisibility(View.GONE);
            if (userEmail != null) userEmail.setVisibility(View.GONE);
            if (userName != null) userName.setVisibility(View.GONE);
            return;
        }

        try {
            SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String photoUriString = preferences.getString("profile_image_uri", null);

            LoginManager loginManager = new LoginManager(requireContext());
            if (loginManager.getUsuario() != null) {
                String correo = loginManager.getUsuario().getCorreo();
                String nombreUsuario = loginManager.getUsuario().getNombreUsuario();

                if (userEmail != null) userEmail.setText(correo != null ? correo : "");
                if (userName != null) userName.setText(nombreUsuario != null ? nombreUsuario : "");

                if (userProfileImage != null) {
                    if (photoUriString != null && !photoUriString.isEmpty()) {
                        Glide.with(this)
                                .load(Uri.parse(photoUriString))
                                .circleCrop()
                                .placeholder(R.drawable.perfil)
                                .into(userProfileImage);
                    } else {
                        userProfileImage.setImageResource(R.drawable.perfil);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("DETALLE", "Error al cargar datos de usuario: " + e.getMessage());
        }
    }

    // ✅ MÉTODO CORREGIDO: Mostrar detalles de la receta SIN tocar nutrición
    private void mostrarDetallesReceta(Receta receta) {
        if (getView() == null) return;

        TextView nombreReceta = getView().findViewById(R.id.recipe_name);
        if (nombreReceta != null) nombreReceta.setText(receta.getTitulo());

        // ✅ CRÍTICO: NO TOCAR LOS CAMPOS NUTRICIONALES AQUÍ
        // Los valores nutricionales se manejan EXCLUSIVAMENTE en configurarTablaNutricional()

        ImageView imagenReceta = getView().findViewById(R.id.photoImageDetails);
        if (imagenReceta != null) {
            Glide.with(requireContext()).load(receta.getImagen()).into(imagenReceta);
        }

        TextView tiempo = getView().findViewById(R.id.tv_tiempo);
        TextView dificultad = getView().findViewById(R.id.tv_dificultad);
        if (tiempo != null) tiempo.setText(receta.getTiempoPreparacion());
        if (dificultad != null) dificultad.setText(receta.getDificultad());

        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = getView().findViewById(R.id.lista_ingredientes);
        if (contenedorIngredientes != null) {
            contenedorIngredientes.removeAllViews();
            if (receta.getIngredientes() != null) {
                for (Ingrediente ingrediente : receta.getIngredientes()) {
                    agregarChipIngrediente(contenedorIngredientes, ingrediente.getNombreIngrediente());
                }
            }
        }

        LinearLayout contenedorPasos = getView().findViewById(R.id.lista_pasos);
        if (contenedorPasos != null) {
            contenedorPasos.removeAllViews();

            // ✅ CORRECCIÓN: Usar getDescripcion() en lugar de getPreparacion()
            List<String> pasos;
            if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
                pasos = Arrays.asList(receta.getDescripcion().split("\n"));
            } else {
                pasos = Arrays.asList("No hay pasos de preparación disponibles");
            }

            int pasoNum = 1;
            for (String paso : pasos) {
                // Evitar pasos vacíos
                if (paso.trim().isEmpty()) continue;

                TextView tvPaso = new TextView(getContext());
                tvPaso.setText(String.format("%d. %s", pasoNum++, paso.trim()));
                tvPaso.setTextSize(16);
                tvPaso.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                tvPaso.setPadding(0, 0, 0, 16);
                contenedorPasos.addView(tvPaso);
            }
        }
    }

    private void agregarChipIngrediente(com.google.android.flexbox.FlexboxLayout contenedor, String ingrediente) {
        TextView tvIngrediente = new TextView(getContext());
        tvIngrediente.setText(ingrediente);
        tvIngrediente.setPadding(30, 20, 30, 20);
        tvIngrediente.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        tvIngrediente.setBackgroundResource(R.drawable.bg_chip);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        tvIngrediente.setLayoutParams(params);
        contenedor.addView(tvIngrediente);
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarReceta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarReceta() {
        if (recetaActual == null) return;

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        // ✅ USAR EL MÉTODO CORRECTO SEGÚN EL TIPO DE RECETA
        if (recetaActual.getCreador() != null) {
            String tipoCreador = recetaActual.getCreador().getTipo_usuario();

            if ("usuario".equals(tipoCreador)) {
                // ✅ ELIMINAR RECETA DE USUARIO REGULAR
                recetaApi.eliminarRecetaUsuario(recetaActual.getIdReceta(), "Bearer " + tokenGuardado)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                } else {
                                    Toast.makeText(getContext(), "Error al eliminar la receta", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("ELIMINAR_ERROR", "Error en conexión: " + t.getMessage());
                                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // ✅ RECETA DE ADMINISTRADOR - No se puede eliminar desde aquí
                Toast.makeText(getContext(), "Las recetas de administradores no se pueden eliminar desde la app", Toast.LENGTH_LONG).show();
            }
        } else {
            // ✅ FALLBACK: Usar endpoint general si no hay información del creador
            Log.w(TAG, "No se pudo determinar el tipo de creador, usando endpoint general");

            // Usar el método deprecated como fallback
            recetaApi.deleteReceta(recetaActual.getIdReceta(), "Bearer " + tokenGuardado)
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Receta eliminada correctamente", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(getContext(), "Error al eliminar la receta", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("ELIMINAR_ERROR", "Error en conexión: " + t.getMessage());
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar handlers para evitar memory leaks
        if (pollingHandler != null && pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }
}