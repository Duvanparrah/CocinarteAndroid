package com.camilo.cocinarte.ui.inicio;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.models.Receta;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecetasAdapter extends RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder> {

    private Context context;
    private List<Receta> recetas;
    private OnRecetaClickListener listener;

    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
    }

    public RecetasAdapter(Context context, List<Receta> recetas, OnRecetaClickListener listener) {
        this.context = context;
        this.recetas = recetas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // CAMBIO: Usar el nuevo layout simple
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta_simple, parent, false);
        return new RecetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = recetas.get(position);

        // Título de la receta
        if (receta.getTitulo() != null) {
            holder.titulo.setText(receta.getTitulo());
        } else {
            holder.titulo.setText("Sin título");
        }

        // Cargar imagen de la receta con Glide
        if (receta.getImagen() != null && !receta.getImagen().isEmpty() && !receta.getImagen().equals("null")) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.logo_cocinarte)
                    .error(R.drawable.logo_cocinarte)
                    .centerCrop();

            Glide.with(context)
                    .load(receta.getImagen())
                    .apply(options)
                    .into(holder.imagen);
        } else {
            holder.imagen.setImageResource(R.drawable.logo_cocinarte);
        }

        // Cargar datos reales de reacciones (likes y comentarios)
        cargarReaccionesReales(receta.getIdReceta(), holder);

        // Click listener para abrir detalle
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecetaClick(receta);
            }
        });

        // Click listeners para los botones (solo para mostrar info, sin funcionalidad)
        if (holder.likeButton != null) {
            holder.likeButton.setOnClickListener(v -> {
                Toast.makeText(context, "Ver detalle para dar like", Toast.LENGTH_SHORT).show();
            });
        }

        if (holder.commentButton != null) {
            holder.commentButton.setOnClickListener(v -> {
                Toast.makeText(context, "Ver detalle para comentar", Toast.LENGTH_SHORT).show();
            });
        }

        if (holder.shareButton != null) {
            holder.shareButton.setOnClickListener(v -> {
                // TODO: Implementar funcionalidad de compartir
                Toast.makeText(context, "Compartir " + receta.getTitulo(), Toast.LENGTH_SHORT).show();
            });
        }

        if (holder.saveButton != null) {
            holder.saveButton.setOnClickListener(v -> {
                // TODO: Implementar funcionalidad de guardar
                Toast.makeText(context, "Guardar " + receta.getTitulo(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * Cargar los datos reales de reacciones (likes y comentarios) desde la API
     */
    private void cargarReaccionesReales(int recetaId, RecetaViewHolder holder) {
        ReaccionApi reaccionApi = ApiClient.getClient(context).create(ReaccionApi.class);

        reaccionApi.getReaccionesPorReceta(recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        // Obtener datos de likes
                        int totalLikes = obj.getJSONObject("likes").getInt("total");
                        boolean userLiked = obj.getJSONObject("likes").getBoolean("user_liked");

                        // Obtener total de comentarios
                        int totalComentarios = obj.getInt("total_comentarios");

                        // Actualizar UI en el hilo principal
                        if (holder.likesCount != null) {
                            holder.likesCount.setText(formatearNumero(totalLikes));
                            // Cambiar color del texto según si el usuario dio like
                            if (userLiked) {
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.like_red));
                            } else {
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
                            }
                        }

                        if (holder.likeButton != null) {
                            // Cambiar ícono y color según si el usuario dio like
                            if (userLiked) {
                                // Usar el ícono de corazón relleno y ponerlo en rojo
                                holder.likeButton.setImageResource(R.drawable.ic_heart_like);
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.like_red),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                // Usar el ícono de corazón vacío y ponerlo en negro
                                holder.likeButton.setImageResource(R.drawable.ic_favoritos);
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                        if (holder.commentsCount != null) {
                            holder.commentsCount.setText(String.valueOf(totalComentarios));
                        }

                    } catch (IOException | JSONException e) {
                        Log.e("RecetasAdapter", "Error al procesar reacciones para receta " + recetaId, e);
                        // Mostrar valores por defecto en caso de error
                        mostrarValoresPorDefecto(holder);
                    }
                } else {
                    Log.w("RecetasAdapter", "Error al obtener reacciones para receta " + recetaId + ": " + response.code());
                    // Mostrar valores por defecto
                    mostrarValoresPorDefecto(holder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("RecetasAdapter", "Fallo al cargar reacciones para receta " + recetaId, t);
                // Mostrar valores por defecto en caso de fallo
                mostrarValoresPorDefecto(holder);
            }
        });
    }

    /**
     * Mostrar valores por defecto cuando hay error en la carga
     */
    private void mostrarValoresPorDefecto(RecetaViewHolder holder) {
        if (holder.likesCount != null) {
            holder.likesCount.setText("0");
            holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
        }
        if (holder.likeButton != null) {
            holder.likeButton.setImageResource(R.drawable.ic_favoritos);
            holder.likeButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.negro),
                    PorterDuff.Mode.SRC_IN
            );
        }
        if (holder.commentsCount != null) {
            holder.commentsCount.setText("0");
        }
    }

    /**
     * Método público para actualizar los datos cuando se regresa del detalle
     */
    public void actualizarDatos() {
        notifyDataSetChanged();
    }

    /**
     * Formatear números grandes (ej: 1500 -> "1.5k", 150000 -> "150k")
     */
    private String formatearNumero(int numero) {
        if (numero >= 1000000) {
            return String.format("%.1fm", numero / 1000000.0);
        } else if (numero >= 1000) {
            return String.format("%.1fk", numero / 1000.0);
        } else {
            return String.valueOf(numero);
        }
    }

    @Override
    public int getItemCount() {
        return recetas != null ? recetas.size() : 0;
    }

    // Método para actualizar las recetas
    public void updateRecetas(List<Receta> nuevasRecetas) {
        if (this.recetas != null) {
            this.recetas.clear();
            if (nuevasRecetas != null) {
                this.recetas.addAll(nuevasRecetas);
            }
            notifyDataSetChanged();
        }
    }

    // Método para agregar recetas
    public void addRecetas(List<Receta> nuevasRecetas) {
        if (this.recetas != null && nuevasRecetas != null) {
            int startPosition = this.recetas.size();
            this.recetas.addAll(nuevasRecetas);
            notifyItemRangeInserted(startPosition, nuevasRecetas.size());
        }
    }

    // Método para limpiar la lista
    public void clearRecetas() {
        if (this.recetas != null) {
            int size = this.recetas.size();
            this.recetas.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    static class RecetaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView titulo;
        ImageView likeButton;
        ImageView commentButton;
        ImageView shareButton;
        ImageView saveButton;
        TextView likesCount;
        TextView commentsCount;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            imagen = itemView.findViewById(R.id.recipe_image);
            titulo = itemView.findViewById(R.id.recipe_title);
            likeButton = itemView.findViewById(R.id.like_button);
            commentButton = itemView.findViewById(R.id.comment_button);
            shareButton = itemView.findViewById(R.id.share_button);
            saveButton = itemView.findViewById(R.id.save_button);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentsCount = itemView.findViewById(R.id.comments_count);
        }
    }
}