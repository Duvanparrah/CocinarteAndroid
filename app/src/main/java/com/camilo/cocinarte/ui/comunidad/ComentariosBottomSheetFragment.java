package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
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
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.models.Comentario;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComentariosBottomSheetFragment extends BottomSheetDialogFragment {

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
        RecyclerView recyclerView = view.findViewById(R.id.recycler_comentarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sinComentarios = view.findViewById(R.id.tv_sin_comentarios);

        listaComentarios = new ArrayList<>();
        LoginManager loginManager = new LoginManager(requireContext());
        idUsuarioActual = loginManager.getUsuario().getIdUsuario();

        adapter = new ComentariosAdapter(listaComentarios, requireContext(), idUsuarioActual);
        recyclerView.setAdapter(adapter);

        recetaId = getArguments().getInt(ARG_RECETA_ID, -1);
        recargarComentariosDesdeApi(recetaId);

        EditText editTextComentario = view.findViewById(R.id.edit_text_comentario);
        ImageButton btnEnviar = view.findViewById(R.id.btn_enviar_comentario);

        btnEnviar.setOnClickListener(v -> {
            String texto = editTextComentario.getText().toString().trim();
            if (!texto.isEmpty()) {
                String token = "Bearer " + loginManager.getToken();

                ReaccionApi api = ApiClient.getClient(requireContext()).create(ReaccionApi.class);
                api.enviarComentario(token, recetaId, texto).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Comentario enviado", Toast.LENGTH_SHORT).show();
                            editTextComentario.setText("");
                            recargarComentariosDesdeApi(recetaId);
                        } else {
                            Toast.makeText(getContext(), "Error al enviar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void recargarComentariosDesdeApi(int recetaId) {
        ReaccionApi api = ApiClient.getClient(requireContext()).create(ReaccionApi.class);
        api.getReaccionesPorReceta(recetaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);
                        JSONArray comentariosArray = obj.getJSONArray("comentarios");

                        listaComentarios.clear();
                        for (int i = 0; i < comentariosArray.length(); i++) {
                            JSONObject c = comentariosArray.getJSONObject(i);
                            JSONObject u = c.getJSONObject("usuario");

                            Comentario comentario = new Comentario(
                                    c.getInt("id"),
                                    c.getString("contenido"),
                                    c.getString("fecha_creacion"),
                                    c.optString("fecha_edicion", null),
                                    c.optBoolean("editado", false),
                                    u.getInt("id"),
                                    u.getString("nombre"),
                                    u.optString("foto_perfil", null)
                            );

                            listaComentarios.add(comentario);
                        }

                        adapter.notifyDataSetChanged();


                        if (listaComentarios.isEmpty()) {
                            sinComentarios.setVisibility(View.VISIBLE);
                        } else {
                            sinComentarios.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error procesando los comentarios", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "Error de red al cargar comentarios", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public interface ComentariosListener {
        void onComentariosCerrados();
    }

    @Override
    public void onDismiss(@NonNull android.content.DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null) {
            listener.onComentariosCerrados();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (getParentFragment() instanceof DetalleRecetaFragment) {
            ((DetalleRecetaFragment) getParentFragment()).refrescarComentarios();
        }
    }

}
