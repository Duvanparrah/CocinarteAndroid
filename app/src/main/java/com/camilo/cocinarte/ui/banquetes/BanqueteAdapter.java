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
import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.session.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// ✅ CAMBIO: Nombre de clase corregido para coincidir con el archivo
public class BanqueteAdapter extends RecyclerView.Adapter<BanqueteAdapter.BanqueteViewHolder> {

    private static final String TAG = "BanqueteAdapter";

    private Context context;
    private List<Banquete> banquetes;
    private OnBanqueteClickListener listener;
    private boolean hasAuthentication; // Flag para saber si hay autenticación
    private SessionManager sessionManager;

    public interface OnBanqueteClickListener {
        void onBanqueteClick(Banquete banquete, int position);
    }

    // Constructor principal
    public BanqueteAdapter(Context context, List<Banquete> banquetes, OnBanqueteClickListener listener, boolean hasAuthentication) {
        this.context = context;
        this.banquetes = banquetes;
        this.listener = listener;
        this.hasAuthentication = hasAuthentication;
        this.sessionManager = SessionManager.getInstance(context);
        Log.d(TAG, "🏗️ Adapter creado con autenticación: " + hasAuthentication);
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
        Log.d(TAG, "   - hasAuthentication: " + hasAuthentication);

        // ✅ INFORMACIÓN BÁSICA DEL BANQUETE

        // Nombre del banquete
        if (banquete.getNombre() != null) {
            holder.nombreBanquete.setText(banquete.getNombre());
        } else {
            holder.nombreBanquete.setText("Sin nombre");
        }

        // Cantidad de personas
        String cantidadTexto = "Para " + banquete.getCantidadPersonas() + " personas";
        holder.cantidadPersonas.setText(cantidadTexto);

        // Tiempo de preparación
        if (banquete.getTiempoPreparacion() != null && !banquete.getTiempoPreparacion().isEmpty()) {
            holder.tiempoPreparacion.setText("Tiempo: " + banquete.getTiempoPreparacion());
            holder.tiempoPreparacion.setVisibility(View.VISIBLE);
        } else {
            holder.tiempoPreparacion.setVisibility(View.GONE);
        }

        // Dificultad con colores
        if (banquete.getDificultad() != null && !banquete.getDificultad().isEmpty()) {
            holder.dificultad.setText("Dificultad: " + banquete.getDificultad());
            holder.dificultad.setVisibility(View.VISIBLE);

            // Aplicar color según dificultad
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

        // ✅ CARGAR IMAGEN DEL BANQUETE
        cargarImagenBanquete(banquete, holder);

        // ✅ CONFIGURAR REACCIONES
        if (hasAuthentication) {
            Log.d(TAG, "🔐 Usuario autenticado: Cargando reacciones REALES para " + banquete.getNombre());
            cargarReaccionesReales(banquete.getIdBanquete(), holder);
        } else {
            Log.d(TAG, "🌐 Usuario NO autenticado: Mostrando valores estáticos para " + banquete.getNombre());
            mostrarValoresEstaticos(holder);
        }

        // Click listener para abrir detalle
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBanqueteClick(banquete, position);
            }
        });

        // ✅ CONFIGURAR CLICK LISTENERS DE BOTONES
        setupButtonClickListeners(holder, banquete);
    }

    // ✅ CARGAR IMAGEN DEL BANQUETE
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

    // ✅ CONFIGURAR CLICK LISTENERS DE BOTONES
    private void setupButtonClickListeners(BanqueteViewHolder holder, Banquete banquete) {
        // Estos campos están marcados como opcionales (visibility="gone") en el XML
        // Solo configurar si existen en el layout actual

        if (holder.iconoFavorito != null) {
            holder.iconoFavorito.setOnClickListener(v -> {
                if (hasAuthentication) {
                    performFavoritoAction(banquete.getIdBanquete(), holder);
                } else {
                    Toast.makeText(context, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Click en likes (si existe en el layout)
        ImageView btnLike = holder.itemView.findViewById(R.id.btn_like);
        if (btnLike != null) {
            btnLike.setOnClickListener(v -> {
                if (hasAuthentication) {
                    performLikeAction(banquete.getIdBanquete(), holder);
                } else {
                    Toast.makeText(context, "Inicia sesión para dar like", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ✅ MOSTRAR VALORES ESTÁTICOS PARA USUARIOS NO AUTENTICADOS
    private void mostrarValoresEstaticos(BanqueteViewHolder holder) {
        // Generar números aleatorios convincentes para banquetes
        int likesAleatorios = (int) (Math.random() * 200) + 20; // Entre 20 y 220
        int comentariosAleatorios = (int) (Math.random() * 30) + 2; // Entre 2 y 32

        // Configurar contadores (si existen en el layout)
        if (holder.totalLikes != null) {
            holder.totalLikes.setText(String.valueOf(likesAleatorios));
        }

        if (holder.totalComentarios != null) {
            holder.totalComentarios.setText(String.valueOf(comentariosAleatorios));
        }

        Log.d(TAG, "✅ Valores estáticos configurados: " + likesAleatorios + " likes, " + comentariosAleatorios + " comentarios");
    }

    // ✅ CARGAR REACCIONES REALES PARA USUARIOS AUTENTICADOS
    private void cargarReaccionesReales(int banqueteId, BanqueteViewHolder holder) {
        Log.d(TAG, "🌐 Cargando reacciones reales para banquete " + banqueteId);

        String token = sessionManager.getAuthToken();
        if (token == null) {
            Log.w(TAG, "⚠️ No hay token, mostrando valores estáticos");
            mostrarValoresEstaticos(holder);
            return;
        }

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        // ✅ CAMBIO: Usar ResponseBody directamente
        banqueteApi.obtenerReaccionesBanquete(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
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

                        Log.d(TAG, "✅ Reacciones reales obtenidas para banquete " + banqueteId +
                                ": " + totalLikes + " likes (user_liked=" + userLiked + "), " +
                                totalComentarios + " comentarios");

                        // Actualizar UI en el hilo principal
                        if (holder.totalLikes != null) {
                            holder.totalLikes.setText(String.valueOf(totalLikes));
                        }

                        if (holder.totalComentarios != null) {
                            holder.totalComentarios.setText(String.valueOf(totalComentarios));
                        }

                        // Actualizar estado visual de like si existe el botón
                        ImageView btnLike = holder.itemView.findViewById(R.id.btn_like);
                        if (btnLike != null) {
                            if (userLiked) {
                                btnLike.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                btnLike.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar reacciones para banquete " + banqueteId, e);
                        mostrarValoresPorDefecto(holder);
                    }
                } else {
                    Log.w(TAG, "⚠️ Error al obtener reacciones para banquete " + banqueteId + ": " + response.code());
                    mostrarValoresPorDefecto(holder);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo al cargar reacciones para banquete " + banqueteId, t);
                mostrarValoresPorDefecto(holder);
            }
        });
    }

    // ✅ ACCIÓN DE FAVORITO - CORREGIDA
    private void performFavoritoAction(int banqueteId, BanqueteViewHolder holder) {
        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "⭐ Toggle favorito para banquete: " + banqueteId);

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        // ✅ CAMBIO: Usar ResponseBody para favori  tos
        banqueteApi.agregarBanqueteAFavoritos(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        String action = obj.optString("action", "");
                        String message = obj.optString("message", "Favorito actualizado");

                        // Actualizar UI según la acción
                        if (holder.iconoFavorito != null) {
                            if ("added".equals(action)) {
                                holder.iconoFavorito.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_orange_light),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                holder.iconoFavorito.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error al procesar respuesta de favorito", e);
                        Toast.makeText(context, "Error al procesar favorito", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error en favorito: " + response.code());
                    Toast.makeText(context, "Error al gestionar favorito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo en favorito", t);
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ ACCIÓN DE LIKE - CORREGIDA
    private void performLikeAction(int banqueteId, BanqueteViewHolder holder) {
        String token = sessionManager.getAuthToken();
        if (token == null) {
            Toast.makeText(context, "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "❤️ Toggle like para banquete: " + banqueteId);

        BanqueteApi banqueteApi = ApiClient.getClient(context).create(BanqueteApi.class);

        // ✅ CAMBIO: Usar ResponseBody para likes
        banqueteApi.toggleLikeBanquete(banqueteId, "Bearer " + token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        boolean isLiked = obj.optBoolean("isLiked", false);
                        int totalLikes = obj.optInt("totalLikes", 0);

                        Log.d(TAG, "✅ Like actualizado: liked=" + isLiked + ", total=" + totalLikes);

                        // Actualizar UI
                        if (holder.totalLikes != null) {
                            holder.totalLikes.setText(String.valueOf(totalLikes));
                        }

                        ImageView btnLike = holder.itemView.findViewById(R.id.btn_like);
                        if (btnLike != null) {
                            if (isLiked) {
                                btnLike.setColorFilter(
                                        ContextCompat.getColor(context, android.R.color.holo_red_dark),
                                        PorterDuff.Mode.SRC_IN
                                );
                            } else {
                                btnLike.setColorFilter(
                                        ContextCompat.getColor(context, R.color.negro),
                                        PorterDuff.Mode.SRC_IN
                                );
                            }
                        }

                        String mensaje = isLiked ? "Te gusta este banquete" : "Like removido";
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

    // ✅ VALORES POR DEFECTO PARA ERRORES
    private void mostrarValoresPorDefecto(BanqueteViewHolder holder) {
        if (holder.totalLikes != null) {
            holder.totalLikes.setText("0");
        }

        if (holder.totalComentarios != null) {
            holder.totalComentarios.setText("0");
        }
    }

    // ✅ MÉTODO PÚBLICO PARA ACTUALIZAR DATOS
    public void actualizarDatos() {
        Log.d(TAG, "🔄 Actualizando datos del adapter de banquetes");
        notifyDataSetChanged();
    }

    // ✅ MÉTODOS ADICIONALES PARA GESTIÓN DE DATOS
    public void updateBanquetes(List<Banquete> nuevosBanquetes) {
        if (this.banquetes != null) {
            this.banquetes.clear();
            if (nuevosBanquetes != null) {
                this.banquetes.addAll(nuevosBanquetes);
            }
            notifyDataSetChanged();
        }
    }

    public void addBanquetes(List<Banquete> nuevosBanquetes) {
        if (this.banquetes != null && nuevosBanquetes != null) {
            int startPosition = this.banquetes.size();
            this.banquetes.addAll(nuevosBanquetes);
            notifyItemRangeInserted(startPosition, nuevosBanquetes.size());
        }
    }

    public void clearBanquetes() {
        if (this.banquetes != null) {
            int size = this.banquetes.size();
            this.banquetes.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    @Override
    public int getItemCount() {
        return banquetes != null ? banquetes.size() : 0;
    }

    // ✅ VIEWHOLDER CLASS
    static class BanqueteViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenBanquete;
        TextView nombreBanquete;
        TextView cantidadPersonas;
        TextView tiempoPreparacion;
        TextView dificultad;

        // Campos opcionales (pueden no existir en algunos layouts)
        TextView totalPlatillos;
        TextView totalIngredientes;
        TextView fechaCreacion;
        TextView nombreCreador;
        ImageView iconoFavorito;
        TextView totalLikes;
        TextView totalComentarios;

        public BanqueteViewHolder(@NonNull View itemView) {
            super(itemView);

            // Campos principales
            imagenBanquete = itemView.findViewById(R.id.imageBanquete);
            nombreBanquete = itemView.findViewById(R.id.nombre_banquete);
            cantidadPersonas = itemView.findViewById(R.id.cantidad_personas);
            tiempoPreparacion = itemView.findViewById(R.id.tiempo_preparacion);
            dificultad = itemView.findViewById(R.id.dificultad);

            // Campos opcionales (pueden ser null)
            totalPlatillos = itemView.findViewById(R.id.total_platillos);
            totalIngredientes = itemView.findViewById(R.id.total_ingredientes);
            fechaCreacion = itemView.findViewById(R.id.fecha_creacion);
            nombreCreador = itemView.findViewById(R.id.nombre_creador);
            iconoFavorito = itemView.findViewById(R.id.icono_favorito);
            totalLikes = itemView.findViewById(R.id.total_likes);
            totalComentarios = itemView.findViewById(R.id.total_comentarios);
        }
    }
}