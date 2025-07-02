package com.camilo.cocinarte.ui.comunidad;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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

        cargarDatosUsuario(view);


        ImageView iconComentario = view.findViewById(R.id.icon_comentario);
        iconComentario.setOnClickListener(v -> abrirSeccionComentarios());


        ImageView iconCompartir = view.findViewById(R.id.icon_compartir);
        iconCompartir.setOnClickListener(v -> {
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
        });


        iconGuardar = view.findViewById(R.id.icon_guardar);

        iconGuardar.setOnClickListener(v -> {
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
        });

        iconLike = view.findViewById(R.id.icon_like);
        textLikeCount = view.findViewById(R.id.text_like_count);

        iconLike.setOnClickListener(v -> {
            if (recetaActual != null && likeInicializado) {
                toggleLike(recetaActual.getIdReceta());
            }
        });

        if (getArguments() != null) {
            int idReceta = getArguments().getInt("id_receta", -1);
            if (idReceta != -1) {
                obtenerRecetaDesdeApi(idReceta);
                obtenerReacciones(idReceta);
            }
        }

        String origen = getArguments().getString("origen", "mis_recetas");
        ImageView btnEliminar = view.findViewById(R.id.btn_delete_recipe);
        if ("comunidad".equals(origen)) {
            btnEliminar.setVisibility(View.GONE);
        } else {
            btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        }


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




    private void iniciarPollingComentarios() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (recetaActual != null && estaBottomSheetAbierto) {
                    obtenerReacciones(recetaActual.getIdReceta());
                    pollingHandler.postDelayed(this, 5000);
                }
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    private void detenerPollingComentarios() {
        pollingHandler.removeCallbacks(pollingRunnable);
    }

    public void refrescarComentarios() {
        if (recetaActual != null) {
            obtenerReacciones(recetaActual.getIdReceta());
        }
    }

    private void animarIcono(View icono) {
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
            View loading = requireView().findViewById(R.id.loading_container);
            View contenido = requireView().findViewById(R.id.contenido_receta);

            loading.setVisibility(View.GONE);
            contenido.setVisibility(View.VISIBLE);
        }
    }

    private void obtenerRecetaDesdeApi(int idReceta) {
        RecetaApi recetaApi = ApiClient.getClient(requireContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String token = loginManager.getToken();

        recetaApi.getRecetaById(idReceta, "Bearer " + token).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recetaActual = response.body();

                    recetaGuardada = prefsGuardado.getBoolean(String.valueOf(recetaActual.getIdReceta()), false);
                    actualizarIconoGuardar();

                    mostrarDetallesReceta(recetaActual);
                    recetaCargada = true;
                    verificarCargaCompleta();
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("DETALLE_ERROR", "Error al obtener receta: " + t.getMessage());
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
                        TextView textCommentCount = requireView().findViewById(R.id.text_coments_count);
                        textCommentCount.setText(String.valueOf(totalComentarios));

                        reaccionesCargadas = true;
                        verificarCargaCompleta();

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("REACCIONES", "Error en respuesta");
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
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(iconLike, "scaleX", 1f, 1.4f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(iconLike, "scaleY", 1f, 1.4f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new DecelerateInterpolator());
        scaleY.setInterpolator(new DecelerateInterpolator());
        scaleX.start();
        scaleY.start();
    }

    private void actualizarLikeUI() {
        if (iconLike != null) {
            int iconRes = userLiked ? R.drawable.ic_heart_like : R.drawable.ic_heart_outline;
            iconLike.setImageResource(iconRes);
            iconLike.setVisibility(View.VISIBLE);
        }
        textLikeCount.setText(String.valueOf(totalLikes));
    }

    private void cargarDatosUsuario(View view) {
        ImageView userProfileImage = view.findViewById(R.id.user_profile_image);
        TextView userEmail = view.findViewById(R.id.user_email);
        TextView userName = view.findViewById(R.id.user_name);

        if ("comunidad".equals(origen)) {
            userProfileImage.setVisibility(View.GONE);
            userEmail.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            return;
        }

        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String photoUriString = preferences.getString("profile_image_uri", null);

        LoginManager loginManager = new LoginManager(requireContext());
        String correo = loginManager.getUsuario().getCorreo();
        String nombreUsuario = loginManager.getUsuario().getNombreUsuario();

        userEmail.setText(correo);
        userName.setText(nombreUsuario);

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


    private void mostrarDetallesReceta(Receta receta) {
        TextView nombreReceta = getView().findViewById(R.id.recipe_name);
        nombreReceta.setText(receta.getTitulo());

        TextView kcl = getView().findViewById(R.id.nutrition_kcl);
        TextView p = getView().findViewById(R.id.nutrition_p);
        TextView c = getView().findViewById(R.id.nutrition_c);
        TextView gt = getView().findViewById(R.id.nutrition_gt);

        kcl.setText(String.valueOf(receta.getCalorias()));
        p.setText(String.valueOf(receta.getProteinas()));
        c.setText(String.valueOf(receta.getCarbohidratos()));
        gt.setText(String.valueOf(receta.getGrasas()));

        ImageView imagenReceta = getView().findViewById(R.id.photoImageDetails);
        Glide.with(requireContext()).load(receta.getImagen()).into(imagenReceta);

        TextView tiempo = getView().findViewById(R.id.tv_tiempo);
        TextView dificultad = getView().findViewById(R.id.tv_dificultad);
        tiempo.setText(receta.getTiempoPreparacion());
        dificultad.setText(receta.getDificultad());

        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = getView().findViewById(R.id.lista_ingredientes);
        contenedorIngredientes.removeAllViews();
        for (Ingrediente ingrediente : receta.getIngredientes()) {
            agregarChipIngrediente(contenedorIngredientes, ingrediente.getNombreIngrediente());
        }

        LinearLayout contenedorPasos = getView().findViewById(R.id.lista_pasos);
        contenedorPasos.removeAllViews();

        List<String> pasos = Arrays.asList(receta.getPreparacion().split("\n"));
        int pasoNum = 1;
        for (String paso : pasos) {
            TextView tvPaso = new TextView(getContext());
            tvPaso.setText(String.format("%d. %s", pasoNum++, paso));
            tvPaso.setTextSize(16);
            tvPaso.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            tvPaso.setPadding(0, 0, 0, 16);
            contenedorPasos.addView(tvPaso);
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
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        recetaApi.deleteReceta(recetaActual.getIdReceta(), "Bearer " + tokenGuardado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ELIMINAR_ERROR", "Error en conexión: " + t.getMessage());
            }
        });
    }
}
