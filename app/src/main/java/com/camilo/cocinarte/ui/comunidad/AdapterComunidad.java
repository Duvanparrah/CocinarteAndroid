package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.utils.ReaccionCache;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterComunidad extends BaseAdapter {

    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
        void onComentariosClick(Receta receta);
    }

    private final Context context;
    private final List<Receta> items;
    private final LayoutInflater inflater;
    private final OnRecetaClickListener listener;
    private final SharedPreferences prefsGuardado;
    private final int idUsuarioActual;

    public AdapterComunidad(Context context, List<Receta> items, OnRecetaClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.inflater = LayoutInflater.from(context);
        this.prefsGuardado = context.getSharedPreferences("recetas_guardadas", Context.MODE_PRIVATE);
        this.idUsuarioActual = new LoginManager(context).getUsuario().getIdUsuario();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ? convertView : inflater.inflate(R.layout.card_comunidad, parent, false);

        Receta receta = items.get(position);

        ImageView iVReceta = view.findViewById(R.id.iVReceta);
        TextView tVTitle = view.findViewById(R.id.tVTitle);
        TextView tVNameUser = view.findViewById(R.id.tVNameUser);
        ImageView iVPhoto = view.findViewById(R.id.iVPhoto);

        ImageView iconLike = view.findViewById(R.id.icon_like);
        TextView textLikeCount = view.findViewById(R.id.text_like_count);

        ImageView iconComentario = view.findViewById(R.id.icon_comentario);
        TextView textComentCount = view.findViewById(R.id.text_coments_count);

        ImageView iconGuardar = view.findViewById(R.id.icon_guardar);
        ImageView iconCompartir = view.findViewById(R.id.icon_compartir);

        Glide.with(context)
                .load(receta.getImagen())
                .centerCrop()
                .placeholder(R.drawable.temp_plato)
                .into(iVReceta);

        tVTitle.setText(receta.getTitulo());

        if (receta.getCreador() != null) {
            tVNameUser.setText(receta.getCreador().getNombreUsuario());
            Glide.with(context)
                    .load(receta.getCreador().getFotoPerfil())
                    .placeholder(R.drawable.perfil_chef)
                    .circleCrop()
                    .into(iVPhoto);
        } else {
            tVNameUser.setText("Usuario");
            iVPhoto.setImageResource(R.drawable.perfil_chef);
        }

        boolean guardado = prefsGuardado.getBoolean(String.valueOf(receta.getIdReceta()), false);
        iconGuardar.setImageResource(guardado ? R.drawable.ic_bookmark_filled_orange : R.drawable.ic_bookmark_outline);

        iconGuardar.setOnClickListener(v -> {
            boolean nuevoEstado = !prefsGuardado.getBoolean(String.valueOf(receta.getIdReceta()), false);
            prefsGuardado.edit().putBoolean(String.valueOf(receta.getIdReceta()), nuevoEstado).apply();
            iconGuardar.setImageResource(nuevoEstado ? R.drawable.ic_bookmark_filled_orange : R.drawable.ic_bookmark_outline);
        });

        iconCompartir.setOnClickListener(v -> {
            String url = "https://cocinarte-frontend.vercel.app/receta/" + receta.getIdReceta();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, url);
            context.startActivity(Intent.createChooser(intent, "Compartir receta"));
        });

        JSONObject cache = ReaccionCache.getReacciones(receta.getIdReceta());
        int likes = 0;
        int comentarios = 0;
        boolean userLiked = false;

        if (cache != null) {
            likes = cache.optJSONObject("likes").optInt("total", 0);
            userLiked = cache.optJSONObject("likes").optBoolean("user_liked", false);
            comentarios = cache.optInt("total_comentarios", 0);
        }

        textLikeCount.setText(String.valueOf(likes));
        textComentCount.setText(String.valueOf(comentarios));
        iconLike.setImageResource(userLiked ? R.drawable.ic_heart_like : R.drawable.ic_heart_outline);

        iVReceta.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecetaClick(receta);
            }
        });

        iconLike.setOnClickListener(v -> {
            ReaccionApi api = ApiClient.getClient(context).create(ReaccionApi.class);
            String token = new LoginManager(context).getToken();
            api.toggleLike("Bearer " + token, receta.getIdReceta()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject obj = new JSONObject(response.body().string());
                            boolean nuevoEstado = obj.getBoolean("isLiked");
                            int totalLikes = obj.getInt("totalLikes");
                            ReaccionCache.actualizarLike(receta.getIdReceta(), nuevoEstado, totalLikes);
                            notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                }
            });
        });

        iconComentario.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComentariosClick(receta);
            }
        });

        return view;
    }

    public void actualizarReacciones(Runnable onFinish) {
        final int total = items.size();
        final int[] completados = {0};

        for (Receta receta : items) {
            ApiClient.getClient(context).create(ReaccionApi.class)
                    .getReaccionesPorReceta(receta.getIdReceta())
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    JSONObject obj = new JSONObject(response.body().string());
                                    ReaccionCache.guardarReacciones(receta.getIdReceta(), obj);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            verificarFinalizado();
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            verificarFinalizado();
                        }

                        private void verificarFinalizado() {
                            completados[0]++;
                            if (completados[0] == total) {
                                notifyDataSetChanged();
                                if (onFinish != null) onFinish.run();
                            }
                        }
                    });
        }

        if (total == 0 && onFinish != null) {
            onFinish.run();
        }
    }
}
