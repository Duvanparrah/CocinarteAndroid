package com.camilo.cocinarte.ui.banquetes;

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
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BanqueteAdapter extends RecyclerView.Adapter<BanqueteAdapter.BanqueteViewHolder> {

    private static final String TAG = "BanqueteAdapter";

    private Context context;
    private List<Banquete> banquetes;
    private OnBanqueteClickListener listener;
    private boolean hasAuthentication;
    private SessionManager sessionManager;

    public interface OnBanqueteClickListener {
        void onBanqueteClick(Banquete banquete, int position);
    }

    // ✅ CONSTRUCTOR CORREGIDO
    public BanqueteAdapter(Context context, List<Banquete> banquetes, OnBanqueteClickListener listener, boolean hasAuthentication) {
        this.context = context;
        this.banquetes = banquetes;
        this.listener = listener;
        this.hasAuthentication = hasAuthentication;

        try {
            this.sessionManager = SessionManager.getInstance(context);
            Log.d(TAG, "✅ SessionManager inicializado correctamente");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "❌ Error inicializando SessionManager: " + e.getMessage(), e);
            this.sessionManager = null;
            this.hasAuthentication = false;
        }

        Log.d(TAG, "🏗️ Adapter creado con autenticación: " + this.hasAuthentication);
    }

    @NonNull
    @Override
    public BanqueteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banquete, parent, false);
        return new BanqueteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BanqueteViewHolder holder, int position) {
        Banquete banquete = banquetes.get(position);

        Log.d(TAG, "📱 Binding banquete: " + banquete.getNombre());

        // ✅ INFORMACIÓN BÁSICA DEL BANQUETE
        if (banquete.getNombre() != null) {
            holder.nombreBanquete.setText(banquete.getNombre());
        } else {
            holder.nombreBanquete.setText("Sin nombre");
        }

        String cantidadTexto = "Para " + banquete.getCantidadPersonas() + " personas";
        holder.cantidadPersonas.setText(cantidadTexto);

        if (banquete.getTiempoPreparacion() != null && !banquete.getTiempoPreparacion().isEmpty()) {
            holder.tiempoPreparacion.setText("Tiempo: " + banquete.getTiempoPreparacion());
            holder.tiempoPreparacion.setVisibility(View.VISIBLE);
        } else {
            holder.tiempoPreparacion.setVisibility(View.GONE);
        }

        if (banquete.getDificultad() != null && !banquete.getDificultad().isEmpty()) {
            holder.dificultad.setText("Dificultad: " + banquete.getDificultad());
            holder.dificultad.setVisibility(View.VISIBLE);

            switch (banquete.getDificultad().toLowerCase()) {
                case "fácil":
                case "facil":
                    holder.dificultad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                    break;
                case "media":
                case "medio":
                    holder.dificultad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                    break;
                case "difícil":
                case "dificil":
                    holder.dificultad.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                    break;
                default:
                    holder.dificultad.setTextColor(ContextCompat.getColor(context, R.color.negro));
                    break;
            }
        } else {
            holder.dificultad.setVisibility(View.GONE);
        }

        // ✅ CARGAR IMAGEN
        cargarImagenBanquete(banquete, holder);

        // ✅ CONFIGURAR REACCIONES
        if (hasAuthentication && sessionManager != null) {
            Log.d(TAG, "🔐 Usuario autenticado: Cargando reacciones REALES");
            cargarReaccionesReales(banquete.getIdBanquete(), holder);
        } else {
            Log.d(TAG, "🌐 Usuario NO autenticado: Mostrando valores estáticos");
            mostrarValoresEstaticos(holder);
        }

        // Click listener para detalle
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBanqueteClick(banquete, position);
            }
        });

        // ✅ CONFIGURAR BOTONES
        setupButtonClickListeners(holder, banquete);
    }

    private void cargarImagenBanquete(Banquete banquete, BanqueteViewHolder holder) {
        if (banquete.tieneImagen()) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.banquete_nuevo)
                    .error(R.drawable.banquete_nuevo)
                    .centerCrop();

            Glide.with(context)
                    .load(banquete.getImagenUrl())
                    .apply(options)
                    .into(holder.imagenBanquete);
        } else {
            holder.imagenBanquete.setImageResource(R.drawable.banquete_nuevo);
        }
    }

    // ✅ CONFIGURAR BOTONES CON IDs CORRECTOS
    private void setupButtonClickListeners(BanqueteViewHolder holder, Banquete banquete) {
        // Botón de like
        if (holder.likeButton != null) {
            holder.likeButton.setOnClickListener(v -> {
                if (hasAuthentication && sessionManager != null) {
                    performLikeAction(banquete.getIdBanquete(), holder);
                } else {
                    Toast.makeText(context, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Botón de favoritos (save)
        if (holder.saveButton != null) {
            holder.saveButton.setOnClickListener(v -> {
                if (hasAuthentication && sessionManager != null) {
                    performFavoritoAction(banquete.getIdBanquete(), holder);
                } else {
                    Toast.makeText(context, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Botón de comentarios (solo mostrar mensaje por ahora)
        if (holder.commentButton != null) {
            holder.commentButton.setOnClickListener(v -> {
                Toast.makeText(context, "Función de comentarios próximamente", Toast.LENGTH_SHORT).show();
            });
        }

        // Botón de compartir
        if (holder.shareButton != null) {
            holder.shareButton.setOnClickListener(v -> {
                Toast.makeText(context, "Función de compartir próximamente", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ✅ VALORES ESTÁTICOS USANDO IDs CORRECTOS
    private void mostrarValoresEstaticos(BanqueteViewHolder holder) {
        int likesAleatorios = (int) (Math.random() * 200) + 20;
        int comentariosAleatorios = (int) (Math.random() * 30) + 2;

        if (holder.likesCount != null) {
            holder.likesCount.setText(String.valueOf(likesAleatorios));
        }

        if (holder.commentsCount != null) {
            holder.commentsCount.setText(String.valueOf(comentariosAleatorios));
        }

        Log.d(TAG, "✅ Valores estáticos: " + likesAleatorios + " likes, " + comentariosAleatorios + " comentarios");
    }

    // ✅ CARGAR REACCIONES REALES
    private void cargarReaccionesReales(int banqueteId, BanqueteViewHolder holder) {
        if (sessionManager == null) {
            mostrarValoresEstaticos(holder);
            return;
        }

        String token = sessionManager.getAuthToken();
        if (token == null) {
            mostrarValoresEstaticos(holder);
            return;
        }

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        banqueteApi.obtenerReaccionesBanquete(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        JSONObject likesObj = obj.optJSONObject("likes");
                        int totalLikes = 0;
                        boolean userLiked = false;

                        if (likesObj != null) {
                            totalLikes = likesObj.optInt("total", 0);
                            userLiked = likesObj.optBoolean("user_liked", false);
                        }

                        int totalComentarios = obj.optInt("total_comentarios", 0);

                        // ✅ ACTUALIZAR UI CON IDs CORRECTOS
                        if (holder.likesCount != null) {
                            holder.likesCount.setText(String.valueOf(totalLikes));
                        }

                        if (holder.commentsCount != null) {
                            holder.commentsCount.setText(String.valueOf(totalComentarios));
                        }

                        // Actualizar estado visual del like
                        if (holder.likeButton != null) {
                            if (userLiked) {
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error procesando reacciones", e);
                        mostrarValoresPorDefecto(holder);
                    }
                } else {
                    mostrarValoresPorDefecto(holder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo cargando reacciones", t);
                mostrarValoresPorDefecto(holder);
            }
        });
    }

    // ✅ ACCIÓN DE FAVORITO
    private void performFavoritoAction(int banqueteId, BanqueteViewHolder holder) {
        if (sessionManager == null) {
            Toast.makeText(context, "Error del sistema", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        banqueteApi.agregarBanqueteAFavoritos(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        String action = obj.optString("action", "");
                        String message = obj.optString("message", "Favorito actualizado");

                        // ✅ ACTUALIZAR UI CON ID CORRECTO
                        if (holder.saveButton != null) {
                            if ("added".equals(action)) {
                                holder.saveButton.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_orange_light),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                holder.saveButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error procesando favorito", e);
                        Toast.makeText(context, "Error al procesar favorito", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en favorito", t);
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ ACCIÓN DE LIKE
    private void performLikeAction(int banqueteId, BanqueteViewHolder holder) {
        if (sessionManager == null) {
            Toast.makeText(context, "Error del sistema", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        banqueteApi.toggleLikeBanquete(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        boolean isLiked = obj.optBoolean("isLiked", false);
                        int totalLikes = obj.optInt("totalLikes", 0);

                        // ✅ ACTUALIZAR UI CON IDs CORRECTOS
                        if (holder.likesCount != null) {
                            holder.likesCount.setText(String.valueOf(totalLikes));
                        }

                        if (holder.likeButton != null) {
                            if (isLiked) {
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                holder.likeButton.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                        String mensaje = isLiked ? "Te gusta este banquete" : "Like removido";
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error procesando like", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en like", t);
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ VALORES POR DEFECTO
    private void mostrarValoresPorDefecto(BanqueteViewHolder holder) {
        if (holder.likesCount != null) {
            holder.likesCount.setText("0");
        }

        if (holder.commentsCount != null) {
            holder.commentsCount.setText("0");
        }
    }

    // Métodos de gestión de datos
    public void updateBanquetes(List<Banquete> nuevosBanquetes) {
        if (this.banquetes != null) {
            this.banquetes.clear();
            if (nuevosBanquetes != null) {
                this.banquetes.addAll(nuevosBanquetes);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return banquetes != null ? banquetes.size() : 0;
    }

    // ✅ VIEWHOLDER CON IDs CORRECTOS
    static class BanqueteViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenBanquete;
        TextView nombreBanquete;
        TextView cantidadPersonas;
        TextView tiempoPreparacion;
        TextView dificultad;

        // Campos opcionales
        TextView totalPlatillos;
        TextView totalIngredientes;
        TextView fechaCreacion;
        TextView nombreCreador;

        // ✅ BOTONES DE INTERACCIÓN CON IDs CORRECTOS
        ImageView likeButton;        // like_button
        TextView likesCount;         // likes_count
        ImageView commentButton;     // comment_button
        TextView commentsCount;      // comments_count
        ImageView shareButton;       // share_button
        ImageView saveButton;        // save_button (favoritos)

        public BanqueteViewHolder(@NonNull View itemView) {
            super(itemView);

            // Campos principales
            imagenBanquete = itemView.findViewById(R.id.imageBanquete);
            nombreBanquete = itemView.findViewById(R.id.nombre_banquete);
            cantidadPersonas = itemView.findViewById(R.id.cantidad_personas);
            tiempoPreparacion = itemView.findViewById(R.id.tiempo_preparacion);
            dificultad = itemView.findViewById(R.id.dificultad);

            // Campos opcionales
            totalPlatillos = itemView.findViewById(R.id.total_platillos);
            totalIngredientes = itemView.findViewById(R.id.total_ingredientes);
            fechaCreacion = itemView.findViewById(R.id.fecha_creacion);
            nombreCreador = itemView.findViewById(R.id.nombre_creador);

            // ✅ BOTONES DE INTERACCIÓN CON IDs CORRECTOS
            likeButton = itemView.findViewById(R.id.like_button);
            likesCount = itemView.findViewById(R.id.likes_count);
            commentButton = itemView.findViewById(R.id.comment_button);
            commentsCount = itemView.findViewById(R.id.comments_count);
            shareButton = itemView.findViewById(R.id.share_button);
            saveButton = itemView.findViewById(R.id.save_button);
        }
    }
}