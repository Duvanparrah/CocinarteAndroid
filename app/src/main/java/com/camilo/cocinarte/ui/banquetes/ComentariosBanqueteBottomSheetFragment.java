package com.camilo.cocinarte.ui.banquetes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.ComentarioBanquete;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComentariosBanqueteBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "ComentariosBanqueteBottomSheet";
    private static final String ARG_BANQUETE_ID = "banquete_id";
    private static final String ARG_COMENTARIOS_JSON = "comentarios_json";

    private RecyclerView recyclerViewComentarios;
    private EditText editTextComentario;
    private ImageButton buttonEnviarComentario;

    private ComentariosBanqueteAdapter comentariosAdapter;
    private List<ComentarioBanquete> comentarios = new ArrayList<>();

    private int banqueteId;
    private SessionManager sessionManager;
    private BanqueteApi banqueteApi;

    public static ComentariosBanqueteBottomSheetFragment newInstance(JSONArray comentariosArray, int banqueteId) {
        ComentariosBanqueteBottomSheetFragment fragment = new ComentariosBanqueteBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMENTARIOS_JSON, comentariosArray.toString());
        args.putInt(ARG_BANQUETE_ID, banqueteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_comentarios_banquete, container, false);

        // Inicializar vistas
        initViews(view);

        // Configurar servicios
        setupServices();

        // Obtener argumentos
        if (getArguments() != null) {
            banqueteId = getArguments().getInt(ARG_BANQUETE_ID);
            String comentariosJson = getArguments().getString(ARG_COMENTARIOS_JSON);
            processComentarios(comentariosJson);
        }

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        recyclerViewComentarios = view.findViewById(R.id.recyclerViewComentarios);
        editTextComentario = view.findViewById(R.id.editTextComentario);
        buttonEnviarComentario = view.findViewById(R.id.buttonEnviarComentario);
    }

    private void setupServices() {
        try {
            sessionManager = SessionManager.getInstance(requireContext());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        banqueteApi = ApiClient.getClient(requireContext()).create(BanqueteApi.class);
    }

    private void setupRecyclerView() {
        comentariosAdapter = new ComentariosBanqueteAdapter(comentarios);
        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewComentarios.setAdapter(comentariosAdapter);
    }

    private void setupListeners() {
        // Listener para el botón de enviar
        buttonEnviarComentario.setOnClickListener(v -> enviarComentario());

        // Listener para el texto del comentario
        editTextComentario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonEnviarComentario.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void processComentarios(String comentariosJson) {
        try {
            JSONArray jsonArray = new JSONArray(comentariosJson);
            comentarios.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject comentarioObj = jsonArray.getJSONObject(i);
                ComentarioBanquete comentario = parseComentario(comentarioObj);
                if (comentario != null) {
                    comentarios.add(comentario);
                }
            }

            Log.d(TAG, "✅ Procesados " + comentarios.size() + " comentarios");

        } catch (JSONException e) {
            Log.e(TAG, "❌ Error parseando comentarios", e);
        }
    }

    private ComentarioBanquete parseComentario(JSONObject comentarioObj) {
        try {
            int id = comentarioObj.optInt("id", 0);
            String contenido = comentarioObj.optString("contenido", "");
            String fechaCreacion = comentarioObj.optString("fecha_creacion", "");
            String fechaEdicion = comentarioObj.optString("fecha_edicion", "");
            boolean editado = comentarioObj.optBoolean("editado", false);

            // Datos del usuario
            JSONObject usuarioObj = comentarioObj.optJSONObject("usuario");
            int usuarioId = 0;
            String nombreUsuario = "Usuario";
            String fotoPerfil = "";

            if (usuarioObj != null) {
                usuarioId = usuarioObj.optInt("id", 0);
                nombreUsuario = usuarioObj.optString("nombre", "Usuario");
                fotoPerfil = usuarioObj.optString("foto_perfil", "");
            }

            return new ComentarioBanquete(id, contenido, fechaCreacion, fechaEdicion,
                    editado, usuarioId, nombreUsuario, fotoPerfil);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parseando comentario individual", e);
            return null;
        }
    }

    private void enviarComentario() {
        String contenido = editTextComentario.getText().toString().trim();

        if (contenido.isEmpty()) {
            Toast.makeText(getContext(), "Escribe un comentario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(getContext(), "Debes iniciar sesión para comentar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Deshabilitar botón mientras se envía
        buttonEnviarComentario.setEnabled(false);

        String token = "Bearer " + sessionManager.getAuthToken();

        // Crear JSON para el comentario
        JSONObject comentarioJson = new JSONObject();
        try {
            comentarioJson.put("contenido", contenido);
        } catch (JSONException e) {
            Log.e(TAG, "❌ Error creando JSON de comentario", e);
            buttonEnviarComentario.setEnabled(true);
            return;
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                comentarioJson.toString()
        );

        banqueteApi.agregarComentarioBanquete(banqueteId, token).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseJson = response.body().string();
                        JSONObject responseObj = new JSONObject(responseJson);

                        if (responseObj.has("comentario")) {
                            JSONObject nuevoComentarioObj = responseObj.getJSONObject("comentario");
                            ComentarioBanquete nuevoComentario = parseComentario(nuevoComentarioObj);

                            if (nuevoComentario != null) {
                                // Agregar al inicio de la lista
                                comentarios.add(0, nuevoComentario);
                                comentariosAdapter.notifyItemInserted(0);
                                recyclerViewComentarios.scrollToPosition(0);

                                // Limpiar campo de texto
                                editTextComentario.setText("");

                                Toast.makeText(getContext(), "Comentario agregado", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "❌ Error procesando respuesta", e);
                        Toast.makeText(getContext(), "Error al procesar comentario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error enviando comentario: " + response.code());
                    Toast.makeText(getContext(), "Error al enviar comentario", Toast.LENGTH_SHORT).show();
                }

                buttonEnviarComentario.setEnabled(true);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Fallo enviando comentario", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                buttonEnviarComentario.setEnabled(true);
            }
        });
    }
}