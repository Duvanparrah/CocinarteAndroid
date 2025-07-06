package com.camilo.cocinarte.ui.inicio;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecetasAdapter extends RecyclerView.Adapter<RecetasAdapter.RecetaViewHolder> {

    private static final String TAG = "RecetasAdapter";

    private Context context;
    private List<Receta> recetas;
    private OnRecetaClickListener listener;
    private boolean isInicioScreen; // Flag para saber si estamos en inicio
    private SessionManager sessionManager; // Para verificar autenticación

    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
    }

    // Constructor original (para comunidad con autenticación)
    public RecetasAdapter(Context context, List<Receta> recetas, OnRecetaClickListener listener) {
        this.context = context;
        this.recetas = recetas;
        this.listener = listener;
        this.isInicioScreen = false; // Por defecto no es inicio
        this.sessionManager = SessionManager.getInstance(context);
        Log.d(TAG, "🏗️ Adapter creado para COMUNIDAD (isInicioScreen = false)");
    }

    // Constructor para indicar si es pantalla de inicio
    public RecetasAdapter(Context context, List<Receta> recetas, OnRecetaClickListener listener, boolean isInicioScreen) {
        this.context = context;
        this.recetas = recetas;
        this.listener = listener;
        this.isInicioScreen = isInicioScreen;
        this.sessionManager = SessionManager.getInstance(context);
        Log.d(TAG, "🏗️ Adapter creado para " + (isInicioScreen ? "INICIO" : "COMUNIDAD") + " (isInicioScreen = " + isInicioScreen + ")");
    }

    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta_simple, parent, false);
        return new RecetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = recetas.get(position);

        // ✅ VERIFICAR SI HAY USUARIO AUTENTICADO
        boolean isUserAuthenticated = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        Log.d(TAG, "📱 Binding receta: " + receta.getTitulo());
        Log.d(TAG, "   - isInicioScreen: " + isInicioScreen);
        Log.d(TAG, "   - isUserAuthenticated: " + isUserAuthenticated);

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

        // ✅ LÓGICA ACTUALIZADA: Cargar reacciones reales si hay autenticación
        if (isUserAuthenticated) {
            Log.d(TAG, "🔐 Usuario autenticado: Cargando reacciones REALES para " + receta.getTitulo());
            cargarReaccionesReales(receta.getIdReceta(), holder);
        } else {
            Log.d(TAG, "🌐 Usuario NO autenticado: Mostrando valores estáticos para " + receta.getTitulo());
            mostrarValoresEstaticosInicio(holder);
        }

        // Click listener para abrir detalle
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecetaClick(receta);
            }
        });

        // Click listeners para los botones
        setupButtonClickListeners(holder, receta, isUserAuthenticated);
    }

    /**
     * ✅ NUEVO MÉTODO: Configurar click listeners de botones
     */
    private void setupButtonClickListeners(RecetaViewHolder holder, Receta receta, boolean isUserAuthenticated) {
        if (holder.likeButton != null) {
            holder.likeButton.setOnClickListener(v -> {
                if (isUserAuthenticated) {
                    // ✅ USUARIO AUTENTICADO: Permitir like
                    performLikeAction(receta.getIdReceta(), holder);
                } else {
                    // Usuario no autenticado: Mostrar mensaje
                    Toast.makeText(context, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (holder.commentButton != null) {
            holder.commentButton.setOnClickListener(v -> {
                if (isUserAuthenticated) {
                    Toast.makeText(context, "Ver detalle para comentar", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Inicia sesión para comentar", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (holder.shareButton != null) {
            holder.shareButton.setOnClickListener(v -> {
                Toast.makeText(context, "Compartir " + receta.getTitulo(), Toast.LENGTH_SHORT).show();
            });
        }

        if (holder.saveButton != null) {
            holder.saveButton.setOnClickListener(v -> {
                if (isUserAuthenticated) {
                    Toast.makeText(context, "Guardar " + receta.getTitulo(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * ✅ NUEVO MÉTODO: Realizar acción de like
     */
    private void performLikeAction(int recetaId, RecetaViewHolder holder) {
        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "❤️ Enviando like para receta: " + recetaId);

        ReaccionApi reaccionApi = ApiClient.getClient(context).create(ReaccionApi.class);

        reaccionApi.toggleLike("Bearer " + token, recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        // Parsear respuesta del like
                        boolean liked = obj.optBoolean("liked", false);
                        int totalLikes = obj.optInt("total_likes", 0);

                        Log.d(TAG, "✅ Like actualizado: liked=" + liked + ", total=" + totalLikes);

                        // Actualizar UI
                        if (holder.likesCount != null) {
                            holder.likesCount.setText(formatearNumero(totalLikes));
                        }

                        if (holder.likeButton != null) {
                            if (liked) {
                                holder.likeButton.setImageResource(R.drawable.love);
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                            } else {
                                holder.likeButton.setImageResource(R.drawable.love);
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
                            }
                        }

                        // Mostrar feedback al usuario
                        String mensaje = liked ? "Te gusta esta receta" : "Like removido";
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar respuesta de like", e);
                        Toast.makeText(context, "Error al procesar like", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error en like: " + response.code());
                    Toast.makeText(context, "Error al dar like", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en like", t);
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * ✅ MÉTODO ACTUALIZADO: Solo para usuarios NO autenticados
     */
    private void mostrarValoresEstaticosInicio(RecetaViewHolder holder) {
        Log.d(TAG, "🎭 Generando valores estáticos para usuario NO autenticado");

        // Generar números aleatorios convincentes
        int likesAleatorios = (int) (Math.random() * 500) + 50; // Entre 50 y 550
        int comentariosAleatorios = (int) (Math.random() * 50) + 5; // Entre 5 y 55

        // Configurar likes count
        if (holder.likesCount != null) {
            holder.likesCount.setText(formatearNumero(likesAleatorios));
            holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
        }

        // Configurar like button (siempre sin like para no autenticados)
        if (holder.likeButton != null) {
            holder.likeButton.setImageResource(R.drawable.love);
            holder.likeButton.setColorFilter(
                    ContextCompat.getColor(context, R.color.negro),
                    PorterDuff.Mode.SRC_IN
            );
        }

        // Configurar comments count
        if (holder.commentsCount != null) {
            holder.commentsCount.setText(String.valueOf(comentariosAleatorios));
        }

        Log.d(TAG, "✅ Valores estáticos configurados: " + likesAleatorios + " likes, " + comentariosAleatorios + " comentarios");
    }

    /**
     * Cargar los datos reales de reacciones (likes y comentarios) desde la API
     * SOLO para usuarios autenticados
     */
    private void cargarReaccionesReales(int recetaId, RecetaViewHolder holder) {
        Log.d(TAG, "🌐 Cargando reacciones reales para receta " + recetaId);

        String token = sessionManager.getAuthToken();
        if (token == null) {
            Log.w(TAG, "⚠️ No hay token, mostrando valores estáticos");
            mostrarValoresEstaticosInicio(holder);
            return;
        }

        ReaccionApi reaccionApi = ApiClient.getClient(context).create(ReaccionApi.class);

        // ✅ USAR EL ENDPOINT CORRECTO CON TOKEN
        reaccionApi.getReaccionesPorReceta(recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
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

                        Log.d(TAG, "✅ Reacciones reales obtenidas para receta " + recetaId +
                                ": " + totalLikes + " likes (user_liked=" + userLiked + "), " +
                                totalComentarios + " comentarios");

                        // Actualizar UI en el hilo principal
                        if (holder.likesCount != null) {
                            holder.likesCount.setText(formatearNumero(totalLikes));
                            if (userLiked) {
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                            } else {
                                holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
                            }
                        }

                        if (holder.likeButton != null) {
                            if (userLiked) {
                                holder.likeButton.setImageResource(R.drawable.love);
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                holder.likeButton.setImageResource(R.drawable.love);
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
                        Log.e(TAG, "❌ Error al procesar reacciones para receta " + recetaId, e);
                        mostrarValoresPorDefectoComunidad(holder);
                    }
                } else {
                    Log.w(TAG, "⚠️ Error al obtener reacciones para receta " + recetaId + ": " + response.code());
                    mostrarValoresPorDefectoComunidad(holder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo al cargar reacciones para receta " + recetaId, t);
                mostrarValoresPorDefectoComunidad(holder);
            }
        });
    }

    /**
     * ✅ MÉTODO PARA ERRORES: Mostrar valores por defecto para usuarios autenticados (cuando hay error)
     */
    private void mostrarValoresPorDefectoComunidad(RecetaViewHolder holder) {
        Log.d(TAG, "🔧 Mostrando valores por defecto para usuario autenticado (error en API)");

        if (holder.likesCount != null) {
            holder.likesCount.setText("0");
            holder.likesCount.setTextColor(ContextCompat.getColor(context, R.color.negro));
        }

        if (holder.likeButton != null) {
            holder.likeButton.setImageResource(R.drawable.love);
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
        Log.d(TAG, "🔄 Actualizando datos del adapter (isInicioScreen = " + isInicioScreen + ")");
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