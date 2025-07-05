package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.ReaccionCache;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterComunidad extends BaseAdapter {

    private static final String TAG = "AdapterComunidad";

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

        // ✅ VALIDACIÓN SEGURA DEL USUARIO - SOLUCIONANDO EL CRASH
        this.idUsuarioActual = obtenerIdUsuarioSeguro(context);

        Log.d(TAG, "AdapterComunidad inicializado con usuario ID: " + idUsuarioActual);
        Log.d(TAG, "📊 Total recetas a mostrar: " + items.size());

        // ✅ LOG DE VERIFICACIÓN: Confirmar que solo hay recetas de usuarios
        int recetasUsuarios = 0;
        int recetasAdmin = 0;
        for (Receta receta : items) {
            if (receta.getCreador() != null) {
                if ("usuario".equals(receta.getCreador().getTipo_usuario())) {
                    recetasUsuarios++;
                } else {
                    recetasAdmin++;
                    Log.w(TAG, "⚠️ Receta de admin detectada en adapter: " + receta.getTitulo());
                }
            }
        }
        Log.d(TAG, "📈 Estadísticas en adapter - Usuarios: " + recetasUsuarios + ", Admins: " + recetasAdmin);
    }

    // ✅ MÉTODO SEGURO PARA OBTENER ID DE USUARIO
    private int obtenerIdUsuarioSeguro(Context context) {
        // Intentar primero con LoginManager
        try {
            LoginManager loginManager = new LoginManager(context);
            Usuario usuario = loginManager.getUsuario();

            if (usuario != null) {
                int userId = usuario.getIdUsuario();
                Log.d(TAG, "Usuario obtenido desde LoginManager. ID: " + userId);
                return userId;
            } else {
                Log.w(TAG, "Usuario es null en LoginManager, intentando SessionManager");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario desde LoginManager: " + e.getMessage());
        }

        // Si falla LoginManager, intentar con SessionManager
        try {
            SessionManager sessionManager = SessionManager.getInstance(context);
            SessionManager.SessionData sessionData = sessionManager.getSessionData();

            if (sessionData != null && sessionData.userId != null && !sessionData.userId.isEmpty()) {
                int userId = Integer.parseInt(sessionData.userId);
                Log.d(TAG, "Usuario obtenido desde SessionManager. ID: " + userId);
                return userId;
            } else {
                Log.w(TAG, "SessionData es null o userId vacío");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener usuario desde SessionManager: " + e.getMessage());
        }

        // Último recurso: ID por defecto
        Log.w(TAG, "No se pudo obtener ID de usuario, usando ID por defecto: -1");
        return -1; // ID que indica "usuario no identificado"
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

        // Cargar imagen de la receta
        Glide.with(context)
                .load(receta.getImagen())
                .centerCrop()
                .placeholder(R.drawable.temp_plato)
                .into(iVReceta);

        tVTitle.setText(receta.getTitulo());

        // ✅ VALIDACIÓN SEGURA DEL CREADOR SIN EMAIL
        if (receta.getCreador() != null) {
            String nombreCreador = receta.getCreador().getNombre_usuario();
            String correoCreador = receta.getCreador().getCorreo();
            String tipoCreador = receta.getCreador().getTipo_usuario();

            // Mostrar nombre del usuario
            tVNameUser.setText(nombreCreador != null ? nombreCreador : "Usuario");

            // ✅ LOG PARA VERIFICAR QUE SOLO SE MUESTRAN USUARIOS
            Log.d(TAG, "📋 Mostrando receta: " + receta.getTitulo());
            Log.d(TAG, "👤 Creador: " + nombreCreador + " (" + correoCreador + ") - Tipo: " + tipoCreador);

            // ✅ VERIFICACIÓN ADICIONAL: Confirmar que es usuario regular
            if (!"usuario".equals(tipoCreador)) {
                Log.w(TAG, "⚠️ ADVERTENCIA: Se está mostrando una receta que NO es de usuario regular!");
                Log.w(TAG, "   Receta: " + receta.getTitulo() + " - Tipo creador: " + tipoCreador);
            }

            // Cargar foto del creador
            String fotoCreador = receta.getCreador().getFoto_perfil();
            if (fotoCreador != null && !fotoCreador.isEmpty()) {
                Glide.with(context)
                        .load(fotoCreador)
                        .placeholder(R.drawable.perfil_chef)
                        .circleCrop()
                        .into(iVPhoto);
            } else {
                iVPhoto.setImageResource(R.drawable.perfil_chef);
            }
        } else {
            // Si no hay información del creador
            tVNameUser.setText("Usuario");
            iVPhoto.setImageResource(R.drawable.perfil_chef);

            Log.w(TAG, "⚠️ Receta sin información de creador: " + receta.getTitulo());
        }

        // Manejar guardado de receta
        boolean guardado = prefsGuardado.getBoolean(String.valueOf(receta.getIdReceta()), false);
        iconGuardar.setImageResource(guardado ? R.drawable.ic_bookmark_filled_orange : R.drawable.ic_bookmark_outline);

        iconGuardar.setOnClickListener(v -> {
            boolean nuevoEstado = !prefsGuardado.getBoolean(String.valueOf(receta.getIdReceta()), false);
            prefsGuardado.edit().putBoolean(String.valueOf(receta.getIdReceta()), nuevoEstado).apply();
            iconGuardar.setImageResource(nuevoEstado ? R.drawable.ic_bookmark_filled_orange : R.drawable.ic_bookmark_outline);
        });

        // Manejar compartir receta
        iconCompartir.setOnClickListener(v -> {
            String url = "https://cocinarte-frontend.vercel.app/receta/" + receta.getIdReceta();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "¡Mira esta receta de " + receta.getNombreCreador() + "! " + url);
            context.startActivity(Intent.createChooser(intent, "Compartir receta"));
        });

        // ✅ CARGAR REACCIONES DE FORMA SEGURA
        cargarReacciones(receta, iconLike, textLikeCount, textComentCount);

        // Click en imagen para ver detalle
        iVReceta.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecetaClick(receta);
            }
        });

        // ✅ MANEJAR LIKE CON VALIDACIÓN DE TOKEN
        iconLike.setOnClickListener(v -> manejarLike(receta));

        // Click en comentarios
        iconComentario.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComentariosClick(receta);
            }
        });

        return view;
    }

    // ✅ MÉTODO PARA CARGAR REACCIONES DE FORMA SEGURA
    private void cargarReacciones(Receta receta, ImageView iconLike, TextView textLikeCount, TextView textComentCount) {
        JSONObject cache = ReaccionCache.getReacciones(receta.getIdReceta());
        int likes = 0;
        int comentarios = 0;
        boolean userLiked = false;

        if (cache != null) {
            JSONObject likesObj = cache.optJSONObject("likes");
            if (likesObj != null) {
                likes = likesObj.optInt("total", 0);
                userLiked = likesObj.optBoolean("user_liked", false);
            }
            comentarios = cache.optInt("total_comentarios", 0);
        }

        textLikeCount.setText(String.valueOf(likes));
        textComentCount.setText(String.valueOf(comentarios));
        iconLike.setImageResource(userLiked ? R.drawable.ic_heart_like : R.drawable.ic_heart_outline);
    }

    // ✅ MÉTODO PARA MANEJAR LIKE CON VALIDACIÓN DE TOKEN
    private void manejarLike(Receta receta) {
        try {
            String token = obtenerTokenSeguro();
            if (token == null) {
                Log.e(TAG, "No se pudo obtener token para hacer like");
                return;
            }

            ReaccionApi api = ApiClient.getClient(context).create(ReaccionApi.class);
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
                            Log.e(TAG, "Error al procesar respuesta de like: " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error al hacer like: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al manejar like: " + e.getMessage());
        }
    }

    // ✅ MÉTODO PARA OBTENER TOKEN DE FORMA SEGURA
    private String obtenerTokenSeguro() {
        // Intentar primero con LoginManager
        try {
            LoginManager loginManager = new LoginManager(context);
            String token = loginManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener token desde LoginManager: " + e.getMessage());
        }

        // Si falla, intentar con SessionManager
        try {
            SessionManager sessionManager = SessionManager.getInstance(context);
            String token = sessionManager.getAuthToken();
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener token desde SessionManager: " + e.getMessage());
        }

        Log.e(TAG, "No se pudo obtener token desde ningún manager");
        return null;
    }

    public void actualizarReacciones(Runnable onFinish) {
        final int total = items.size();
        final int[] completados = {0};

        if (total == 0) {
            if (onFinish != null) onFinish.run();
            return;
        }

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
                                    Log.e(TAG, "Error al procesar reacciones: " + e.getMessage());
                                }
                            }
                            verificarFinalizado();
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e(TAG, "Error al cargar reacciones: " + t.getMessage());
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
    }
}