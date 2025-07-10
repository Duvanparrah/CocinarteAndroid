package com.camilo.cocinarte.ui.favoritos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.DetalleRecetaActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.FavoritosService;
import com.camilo.cocinarte.databinding.ActivityFavoritosBinding;
import com.camilo.cocinarte.models.FavoritosResponse;
import com.camilo.cocinarte.session.SessionManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosActivity extends AppCompatActivity {
    private static final String TAG = "FavoritosActivity";
    private ActivityFavoritosBinding binding;
    private FavoritosAdapter favoritosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityFavoritosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
        getRecetasFavoritas();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Configurar RecyclerView
        binding.recyclerViewFavoritos.setLayoutManager(new LinearLayoutManager(this));
    }

    protected void getRecetasFavoritas() {
        Log.d(TAG, "Obteniendo recetas favoritas...");

        SessionManager sessionManager = null;
        try {
            sessionManager = SessionManager.getInstance(this);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String token = sessionManager.getAuthToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        Log.d(TAG, "Token: " + authHeader.substring(0, Math.min(authHeader.length(), 20)) + "...");

        FavoritosService favoritosService = ApiConfig.getClient(getApplicationContext()).create(FavoritosService.class);

        favoritosService.getFavoritos(authHeader).enqueue(new Callback<FavoritosResponse>() {
            @Override
            public void onResponse(Call<FavoritosResponse> call, Response<FavoritosResponse> response) {
                Log.d(TAG, "Respuesta recibida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    FavoritosResponse favoritosResponse = response.body();
                    Log.d(TAG, "Favoritos obtenidos exitosamente. Total: " + favoritosResponse.getTotal());

                    if (favoritosResponse.getFavoritos() != null && !favoritosResponse.getFavoritos().isEmpty()) {
                        buildRecyclerviewFavoritos(favoritosResponse.getFavoritos());
                        showFavoritos();
                    } else {
                        showEmptyState();
                    }

                    Toast.makeText(getApplicationContext(), favoritosResponse.getMensaje(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code() + " - " + response.message());
                    if (response.code() == 401) {
                        Toast.makeText(getApplicationContext(), "Sesión expirada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error al obtener favoritos", Toast.LENGTH_SHORT).show();
                    }
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<FavoritosResponse> call, Throwable t) {
                Log.e(TAG, "Error de conexión: ", t);
                Toast.makeText(getApplicationContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    protected void buildRecyclerviewFavoritos(List<FavoritosResponse.Favorito> favoritos) {
        Log.d(TAG, "Construyendo RecyclerView con " + favoritos.size() + " favoritos");

        favoritosAdapter = new FavoritosAdapter(favoritos, this);
        binding.recyclerViewFavoritos.setAdapter(favoritosAdapter);
    }

    private void showFavoritos() {
        binding.recyclerViewFavoritos.setVisibility(View.VISIBLE);
        binding.containerMessageEmpty.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        binding.recyclerViewFavoritos.setVisibility(View.GONE);
        binding.containerMessageEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar favoritos cuando se regresa a la actividad
        getRecetasFavoritas();
    }
}

/* ================= ADAPTADOR RECYCLERVIEW CORREGIDO ================= */
class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {
    private static final String TAG = "FavoritosAdapter";

    private List<FavoritosResponse.Favorito> localDataSet;
    private Context context;

    // ViewHolder interno
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imgFavorito;
        private final TextView descripcion;
        private final TextView tiempo;
        private final TextView dificultad;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.nombreFavorito);
            imgFavorito = view.findViewById(R.id.imgFavorito);
            descripcion = view.findViewById(R.id.descripcionFavorito);
            tiempo = view.findViewById(R.id.tiempoFavorito);
            dificultad = view.findViewById(R.id.dificultadFavorito);
        }

        public TextView getTextView() { return textView; }
        public ImageView getImgFavorito() { return imgFavorito; }
        public TextView getDescripcion() { return descripcion; }
        public TextView getTiempo() { return tiempo; }
        public TextView getDificultad() { return dificultad; }
    }

    // Constructor del adapter
    public FavoritosAdapter(List<FavoritosResponse.Favorito> dataSet, Context context) {
        this.localDataSet = dataSet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_favoritos, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        FavoritosResponse.Favorito favorito = localDataSet.get(position);

        Log.d(TAG, "Binding favorito en posición " + position + ": " + favorito.getTitulo());

        // Configurar textos
        viewHolder.getTextView().setText(favorito.getTitulo() != null ? favorito.getTitulo() : "Sin título");

        if (viewHolder.getDescripcion() != null) {
            viewHolder.getDescripcion().setText(favorito.getDescripcion() != null ? favorito.getDescripcion() : "Sin descripción");
        }

        if (viewHolder.getTiempo() != null) {
            viewHolder.getTiempo().setText(favorito.getTiempo() != null ? favorito.getTiempo() : "No especificado");
        }

        if (viewHolder.getDificultad() != null) {
            viewHolder.getDificultad().setText(favorito.getDificultad() != null ? favorito.getDificultad() : "No especificada");
        }

        // Click listener para abrir detalle
        viewHolder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleRecetaActivity.class);

            // Enviar el ID correcto de la receta
            int recetaId = favorito.getIdReceta() != 0 ? favorito.getIdReceta() : favorito.getRecetaId();
            intent.putExtra("receta_id", recetaId);

            Log.d(TAG, "Abriendo detalle de receta con ID: " + recetaId);
            context.startActivity(intent);
        });

        // Cargar imagen
        String photoUrl = favorito.getImagenUrl();
        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.equals("null")) {
            Log.d(TAG, "Cargando imagen: " + photoUrl);

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.logo_cocinarte)
                    .error(R.drawable.logo_cocinarte)
                    .centerCrop();

            Glide.with(viewHolder.getImgFavorito().getContext())
                    .load(photoUrl)
                    .apply(options)
                    .into(viewHolder.getImgFavorito());
        } else {
            Log.d(TAG, "No hay imagen válida, usando placeholder");
            viewHolder.getImgFavorito().setImageResource(R.drawable.logo_cocinarte);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet != null ? localDataSet.size() : 0;
    }
}