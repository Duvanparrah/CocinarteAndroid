package com.camilo.cocinarte.ui.favoritos;

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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.FavoritosService;
import com.camilo.cocinarte.databinding.ActivityFavoritosBinding;
import com.camilo.cocinarte.models.FavoritosResponse;
import com.camilo.cocinarte.session.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://cocinarte-backend-production.up.railway.app/")
                .addConverterFactory(ScalarsConverterFactory.create()) // Para strings simples
                .addConverterFactory(GsonConverterFactory.create())    // Para objetos como ResetPasswordRequest
                .build();

        FavoritosService favoritosService = retrofit.create(FavoritosService.class);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imgFavorito;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.nombreFavorito);
            imgFavorito = (ImageView) view.findViewById(R.id.imgFavorito);
        }

        public TextView getTextView() {
            return textView;
        }
        public ImageView getImgFavorito() {
            return imgFavorito;
        }
    }

    public FavoritosAdapter(List<FavoritosResponse.Favorito> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_favoritos, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(localDataSet.get(position).getTitulo() );
        viewHolder.getImgFavorito().setOnClickListener(v -> {
            //Navegar a receta seleccionada
        });

        String photoUrl = localDataSet.get(position).getImagenUrl();

        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.equals("null")) {
            // Cargar imagen desde URL
            Glide.with(viewHolder.getImgFavorito().getContext())
                    .load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.perfil_chef)
                    .error(R.drawable.perfil_chef)
                    .into(viewHolder.imgFavorito);
            }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}