package com.camilo.cocinarte.ui.favoritos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.models.Comentario;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComentariosBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "ComentariosBottomSheet";
    private static final String ARG_RECETA_ID = "receta_id";
    private static final String ARG_COMENTARIOS_JSON = "comentarios_json";

    private ArrayList<Comentario> listaComentarios;
    private ComentariosAdapter adapter;
    private int idUsuarioActual;
    private int recetaId;
    private TextView sinComentarios;

    private ComentariosListener listener;

    public static ComentariosBottomSheetFragment newInstance(JSONArray comentariosArray, int recetaId) {
        ComentariosBottomSheetFragment fragment = new ComentariosBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMENTARIOS_JSON, comentariosArray.toString());
        args.putInt(ARG_RECETA_ID, recetaId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setComentariosListener(ComentariosListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comentarios_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar vistas
        RecyclerView recyclerView = view.findViewById(R.id.recycler_comentarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sinComentarios = view.findViewById(R.id.tv_sin_comentarios);

        // ✅ CORRECCIÓN: Inicializar SessionManager como final
        final SessionManager sessionManager;
        try {
            sessionManager = SessionManager.getInstance(requireContext());
            Log.d(TAG, "✅ SessionManager inicializado correctamente");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "❌ Error inicializando SessionManager: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error de seguridad en la aplicación", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        // Verificar que el usuario esté logueado
        if (sessionManager.getAuthToken() == null) {
            Log.e(TAG, "❌ Usuario no autenticado");
            Toast.makeText(requireContext(), "Debes iniciar sesión para comentar", Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        idUsuarioActual = sessionManager.getUserId();
        Log.d(TAG, "👤 ID usuario actual: " + idUsuarioActual);

        // Configurar lista de comentarios
        listaComentarios = new ArrayList<>();
        adapter = new ComentariosAdapter(listaComentarios, requireContext(), idUsuarioActual);
        recyclerView.setAdapter(adapter);

        // Obtener ID de la receta/banquete
        if (getArguments() != null) {
            recetaId = getArguments().getInt(ARG_RECETA_ID, -1);
            Log.d(TAG, "📋 ID receta/banquete: " + recetaId);
        }

        if (recetaId == -1) {
            Log.e(TAG, "❌ ID de receta/banquete inválido");
            Toast.makeText(requireContext(), "Error: ID inválido", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // Cargar comentarios iniciales
        recargarComentariosDesdeApi(recetaId);

        // Configurar envío de comentarios
        setupEnvioComentarios(view, sessionManager);
    }

    private void setupEnvioComentarios(View view, final SessionManager sessionManager) {
        EditText editTextComentario = view.findViewById(R.id.edit_text_comentario);
        ImageButton btnEnviar = view.findViewById(R.id.btn_enviar_comentario);

        if (editTextComentario == null || btnEnviar == null) {
            Log.e(TAG, "❌ No se encontraron las vistas de comentario");
            return;
        }

        btnEnviar.setOnClickListener(v -> {
            String texto = editTextComentario.getText().toString().trim();

            if (texto.isEmpty()) {
                Toast.makeText(getContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show();
                return;
            }

            if (texto.length() < 3) {
                Toast.makeText(getContext(), "El comentario debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            if (texto.length() > 500) {
                Toast.makeText(getContext(), "El comentario no puede exceder 500 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "📝 Enviando comentario: " + texto.substring(0, Math.min(texto.length(), 50)) + "...");

            // Deshabilitar botón mientras se envía
            btnEnviar.setEnabled(false);

            String token = "Bearer " + sessionManager.getAuthToken();
            ReaccionApi api = ApiConfig.getClient(requireContext()).create(ReaccionApi.class);

            api.enviarComentario(token, recetaId, texto).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    // Rehabilitar botón
                    btnEnviar.setEnabled(true);

                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Comentario enviado exitosamente");
                        Toast.makeText(getContext(), "Comentario enviado", Toast.LENGTH_SHORT).show();
                        editTextComentario.setText("");
                        recargarComentariosDesdeApi(recetaId);
                    } else {
                        Log.e(TAG, "❌ Error al enviar comentario: " + response.code());
                        String errorMsg = "Error al enviar comentario";

                        switch (response.code()) {
                            case 400:
                                errorMsg = "Comentario inválido";
                                break;
                            case 401:
                                errorMsg = "Sesión expirada, inicia sesión nuevamente";
                                break;
                            case 403:
                                errorMsg = "No tienes permisos para comentar";
                                break;
                            case 404:
                                errorMsg = "Receta no encontrada";
                                break;
                            case 500:
                                errorMsg = "Error del servidor";
                                break;
                        }

                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    // Rehabilitar botón
                    btnEnviar.setEnabled(true);

                    Log.e(TAG, "❌ Error de conexión al enviar comentario: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void recargarComentariosDesdeApi(int recetaId) {
        Log.d(TAG, "🔄 Recargando comentarios para receta/banquete: " + recetaId);

        ReaccionApi api = ApiConfig.getClient(requireContext()).create(ReaccionApi.class);

        api.getReaccionesPorReceta(recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        JSONArray comentariosArray = obj.optJSONArray("comentarios");

                        if (comentariosArray == null) {
                            comentariosArray = new JSONArray();
                        }

                        Log.d(TAG, "📝 Comentarios recibidos: " + comentariosArray.length());

                        listaComentarios.clear();

                        for (int i = 0; i < comentariosArray.length(); i++) {
                            try {
                                JSONObject c = comentariosArray.getJSONObject(i);
                                JSONObject u = c.optJSONObject("usuario");

                                if (u == null) {
                                    Log.w(TAG, "⚠️ Comentario " + i + " sin datos de usuario, saltando");
                                    continue;
                                }

                                Comentario comentario = new Comentario(
                                        c.optInt("id", -1),
                                        c.optString("contenido", ""),
                                        c.optString("fecha_creacion", ""),
                                        c.optString("fecha_edicion", null),
                                        c.optBoolean("editado", false),
                                        u.optInt("id", -1),
                                        u.optString("nombre", "Usuario anónimo"),
                                        u.optString("foto_perfil", null)
                                );

                                listaComentarios.add(comentario);

                            } catch (Exception e) {
                                Log.e(TAG, "❌ Error procesando comentario " + i + ": " + e.getMessage());
                            }
                        }

                        // Actualizar UI en el hilo principal
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }

                                if (sinComentarios != null) {
                                    if (listaComentarios.isEmpty()) {
                                        sinComentarios.setVisibility(View.VISIBLE);
                                        sinComentarios.setText("¡Sé el primero en comentar!");
                                    } else {
                                        sinComentarios.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }

                        Log.d(TAG, "✅ Comentarios procesados: " + listaComentarios.size());

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error procesando comentarios: " + e.getMessage(), e);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Error procesando los comentarios", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "❌ Error al cargar comentarios: " + response.code());

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al cargar comentarios: " + t.getMessage(), t);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de conexión al cargar comentarios", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public interface ComentariosListener {
        void onComentariosCerrados();
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "💬 Bottom sheet cerrado");

        if (listener != null) {
            listener.onComentariosCerrados();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ Vista destruida");

        // Limpiar referencias para evitar memory leaks
        if (listaComentarios != null) {
            listaComentarios.clear();
        }
        adapter = null;
        listener = null;
    }
}