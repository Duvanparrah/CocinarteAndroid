package com.camilo.cocinarte.ui.favoritos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.camilo.cocinarte.DetalleRecetaActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.FavoritosService;
import com.camilo.cocinarte.databinding.ActivityFavoritosBinding;
import com.camilo.cocinarte.models.FavoritosResponse;
import com.camilo.cocinarte.session.SessionManager;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritosActivity extends AppCompatActivity {
    private ActivityFavoritosBinding binding;


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

        getRecetasFavoritas();

        binding.btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    protected void getRecetasFavoritas() {
        SessionManager sessionManager = SessionManager.getInstance(this);
        String token = "Bearer " + sessionManager.getAuthToken();

        FavoritosService favoritosService = ApiConfig.getClient(getApplicationContext()).create(FavoritosService.class);
        favoritosService.getFavoritos(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<FavoritosResponse> call, Response<FavoritosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FavoritosResponse favoritos = response.body();
                    Toast.makeText(getApplicationContext(), "Favoritos obtenidos", Toast.LENGTH_SHORT).show();
                    biildRecyclerviewFavoritos(favoritos.getFavoritos());
                } else {
                    Toast.makeText(getApplicationContext(), "Error al obtener favoritos", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<FavoritosResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    protected void biildRecyclerviewFavoritos(List<FavoritosResponse.Favorito> favoritos){
        FavoritosAdapter favoritosAdapter = new FavoritosAdapter(favoritos);
        RecyclerView recyclerView = binding.recyclerViewFavoritos;
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(favoritosAdapter);
    }
}


/* ================= ADAPTADOR RECYCLERVIEW ================= */
class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.ViewHolder> {

    private List<FavoritosResponse.Favorito> localDataSet;

    // ViewHolder interno
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imgFavorito;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.nombreFavorito);
            imgFavorito = view.findViewById(R.id.imgFavorito);
        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getImgFavorito() {
            return imgFavorito;
        }
    }

    // Constructor del adapter
    public FavoritosAdapter(List<FavoritosResponse.Favorito> dataSet) {
        localDataSet = dataSet;
    }

    // onCreateViewHolder va AQUÍ
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_favoritos, viewGroup, false);
        return new ViewHolder(view);
    }

    // ✅ onBindViewHolder también va AQUÍ (fuera del ViewHolder)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet.get(position).getTitulo());

        viewHolder.getImgFavorito().setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DetalleRecetaActivity.class);
            // Enviar por intent
            intent.putExtra("receta_id", localDataSet.get(position).getRecetaId() );
            context.startActivity(intent);
        });

        String photoUrl = localDataSet.get(position).getImagenUrl();

        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.equals("null")) {
            Glide.with(viewHolder.getImgFavorito().getContext())
                    .load(photoUrl)                     .into(viewHolder.imgFavorito);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
