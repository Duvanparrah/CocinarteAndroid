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
import com.camilo.cocinarte.utils.ReaccionCache;
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
    private Receta recetaActual;
    private ReaccionesResponse reaccionesResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_receta);
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("receta_id")) {
            int recetaId = intent.getIntExtra("receta_id", -1);
            getRecetaById(recetaId);
        }


        actions();
    }

    public void actions() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        ImageView like = findViewById(R.id.btn_likes_favoritos);
        ImageView comentarios = findViewById(R.id.btn_comentarios_favoritos);
        ImageView imgFavorito = findViewById(R.id.imgFavorito);


        like.setOnClickListener(v -> {
            String token = "Bearer " + sessionManager.getAuthToken();

            RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);
            recetasService.sendLike(recetaActual.getIdReceta(), token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<LikeResponse> call, Response<LikeResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().isLiked()) {
                            like.setImageTintList(ColorStateList.valueOf(
                                    ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
                            ));
                        } else {
                            like.setImageTintList(ColorStateList.valueOf(
                                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                            ));
                        }

                        TextView likes_favoritos = findViewById(R.id.likes_favoritos);
                        likes_favoritos.setText(""+response.body().getTotalLikes());

                    }
                }
                @Override
                public void onFailure(Call<LikeResponse> call, Throwable t) {
                    t.printStackTrace();
                }

            });
        });

        comentarios.setOnClickListener(v -> {
            abrirSeccionComentarios();
            /*JSONObject cache = ReaccionCache.getReacciones(Integer.parseInt(sessionManager.getUserId()));
            JSONArray comentarios = new JSONArray();
            if (cache != null && cache.has("comentarios")) {
                comentarios = cache.optJSONArray("comentarios");
            }

            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(comentarios, recetaActual.getId_receta());
            modal.setComentariosListener(() -> {
                // Al cerrar el modal, recargamos reacciones
                //adapter.actualizarReacciones(null);
            });
            modal.show(getSupportFragmentManager(), "ComentariosBottomSheet");*/
        });


        imgFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorito(recetaActual.getIdReceta());
            }
        });

    }

    private void setFavorito(int recetaId){
        SessionManager sessionManager = SessionManager.getInstance(this);
        String token = "Bearer " + sessionManager.getAuthToken();
        FavoritosService recetasService = ApiConfig.getClient(getApplicationContext()).create(FavoritosService.class);



        recetasService.verificarFavorito(recetaId, token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<VerificarFavoritoResponse> call, Response<VerificarFavoritoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageView imgFavorito = findViewById(R.id.imgFavorito);

                    if (response.body().isEsFavorito()) {
                        recetasService.deleteFavorito(recetaId, token).enqueue(new Callback<>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse favorito = response.body();
                                    Toast.makeText(getApplicationContext(), "Favoritos actualizado", Toast.LENGTH_SHORT).show();

                                    imgFavorito.setImageTintList(ColorStateList.valueOf(
                                            ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
                                    ));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error enviar favorito", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }else {
                        recetasService.setFavorito(recetaId, token).enqueue(new Callback<>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse favorito = response.body();
                                    Toast.makeText(getApplicationContext(), "Favoritos actualizado", Toast.LENGTH_SHORT).show();

                                    imgFavorito.setImageTintList(ColorStateList.valueOf(
                                            ContextCompat.getColor(getApplicationContext(), android.R.color.holo_orange_light)
                                    ));
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error enviar favorito", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error validar favorito", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<VerificarFavoritoResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void getRecetaById(int recetaId){
        SessionManager sessionManager = SessionManager.getInstance(this);
        String token = "Bearer " + sessionManager.getAuthToken();

        RecetasService recetasService = ApiConfig.getClient(getApplicationContext()).create(RecetasService.class);
        recetasService.getRecetaById(recetaId, token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Receta favorito = response.body();
                    Toast.makeText(getApplicationContext(), "Favoritos obtenidos", Toast.LENGTH_SHORT).show();
                    //biildRecyclerviewFavoritos(favoritos.getFavoritos());
                    recetaActual = favorito;

                    recetasService.getReaccionesRecetaById(recetaId, token).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<ReaccionesResponse> call, Response<ReaccionesResponse> response) {
                            ReaccionesResponse favorito = response.body();
                            Toast.makeText(getApplicationContext(), "Favoritos obtenidos", Toast.LENGTH_SHORT).show();
                            if (response.isSuccessful() && response.body() != null) {
                                ReaccionesResponse _reaccionesResponse = response.body();
                                Toast.makeText(getApplicationContext(), "Reacciones", Toast.LENGTH_SHORT).show();
                                reaccionesResponse = _reaccionesResponse;
                                mostrarDetallesReceta(recetaActual);
                            } else {
                                Toast.makeText(getApplicationContext(), "Error al obtener favoritos", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ReaccionesResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Error al obtener favoritos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void mostrarDetallesReceta(Receta receta) {
        ImageView logo = findViewById(R.id.logo_image);
        Glide.with(this).load(R.drawable.logo_cocinarte).into(logo);

        TextView nombreReceta = findViewById(R.id.recipe_name);
        nombreReceta.setText(receta.getTitulo());

        TextView kcl = findViewById(R.id.nutrition_kcl);
        TextView p = findViewById(R.id.nutrition_p);
        TextView c = findViewById(R.id.nutrition_c);
        TextView gt = findViewById(R.id.nutrition_gt);

        kcl.setText(String.valueOf(receta.getCalorias()));
        p.setText(String.valueOf(receta.getProteinas()));
        c.setText(String.valueOf(receta.getCarbohidratos()));
        gt.setText(String.valueOf(receta.getGrasas()));

        ImageView imagenReceta = findViewById(R.id.photoImageDetails);
        if (receta.getImagen() != null && !receta.getImagen().isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(receta.getImagen())
                    .into(imagenReceta);
        }

        TextView tiempo = findViewById(R.id.tv_tiempo);
        TextView dificultad = findViewById(R.id.tv_dificultad);

        tiempo.setText(receta.getTiempoPreparacion());
        dificultad.setText(receta.getDificultad());


        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = findViewById(R.id.lista_ingredientes);
        contenedorIngredientes.removeAllViews();

        List<String> ingredientes = new ArrayList<>();
        if (receta.getIngredientes() != null) {
            for (Ingrediente ingrediente : receta.getIngredientes())
            {
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

        LinearLayout contenedorPasos = findViewById(R.id.lista_pasos);
        contenedorPasos.removeAllViews();

        int pasoNum = 1;
        ArrayList<String> pasos = new ArrayList<>(Arrays.asList(receta.getDescripcion().split("\n")));

        for (String paso : pasos ) {
            TextView tvPaso = new TextView(getApplicationContext());
            tvPaso.setText(String.format("%d. %s", pasoNum++, paso));
            tvPaso.setTextSize(16);
            tvPaso.setTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.black));
            tvPaso.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvPaso.setPadding(0, 0, 0, 16);
            contenedorPasos.addView(tvPaso);
        }


        //Reacciones

        TextView likes_favoritos = findViewById(R.id.likes_favoritos);
        TextView comentarios_favoritos = findViewById(R.id.comentarios_favoritos);

        likes_favoritos.setText(""+reaccionesResponse.getLikes().getTotal() );
        comentarios_favoritos.setText(""+reaccionesResponse.getTotal_comentarios());


        ImageView like = findViewById(R.id.btn_likes_favoritos);
        if (reaccionesResponse.getLikes().isUser_liked()) {
            like.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark)
            ));
        } else {
            like.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray)
            ));
        }
    }


    private void abrirSeccionComentarios() {
        if (recetaActual != null) {
            ReaccionApi api =  ApiConfig.getClient(getApplicationContext()).create(ReaccionApi.class);
            api.getReaccionesPorReceta(recetaActual.getIdReceta()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JSONObject obj = new JSONObject(json);
                            JSONArray comentariosArray = obj.getJSONArray("comentarios");

                            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(comentariosArray, recetaActual.getIdReceta());
                            modal.show(getSupportFragmentManager(), modal.getTag());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Error al procesar comentarios", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No se pudieron obtener los comentarios", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        tvIngrediente.setLayoutParams(params);

        contenedor.addView(tvIngrediente);
    }
}