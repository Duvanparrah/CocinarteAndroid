package com.camilo.cocinarte.ui.comunidad;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.Receta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleRecetaFragment extends Fragment {

    private Receta recetaActual;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_detalle_receta_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cargarDatosUsuario(view);

        if (getArguments() != null) {
            int idReceta = getArguments().getInt("id_receta", -1);
            if (idReceta != -1) {
                obtenerRecetaDesdeApi(idReceta);
            }
        }

        String origen = getArguments().getString("origen", "mis_recetas");
        ImageView btnEliminar = view.findViewById(R.id.btn_delete_recipe);
        if ("comunidad".equals(origen)) {
            btnEliminar.setVisibility(View.GONE);
        } else {
            btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        }
    }

    private void obtenerRecetaDesdeApi(int idReceta) {
        RecetaApi recetaApi = ApiClient.getClient(requireContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String token = loginManager.getToken();

        recetaApi.getRecetaById(idReceta, "Bearer " + token).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recetaActual = response.body();
                    mostrarDetallesReceta(recetaActual);
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("DETALLE_ERROR", "Error al obtener receta: " + t.getMessage());
            }
        });
    }

    private void cargarDatosUsuario(View view) {
        ImageView userProfileImage = view.findViewById(R.id.user_profile_image);
        TextView userEmail = view.findViewById(R.id.user_email);
        TextView userName = view.findViewById(R.id.user_name);

        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String photoUriString = preferences.getString("profile_image_uri", null);

        LoginManager loginManager = new LoginManager(requireContext());
        String correo = loginManager.getUsuario().getCorreo();
        String nombreUsuario = loginManager.getUsuario().getNombreUsuario();

        userEmail.setText(correo);
        userName.setText(nombreUsuario);

        if (photoUriString != null && !photoUriString.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(photoUriString))
                    .circleCrop()
                    .placeholder(R.drawable.perfil)
                    .into(userProfileImage);
        } else {
            userProfileImage.setImageResource(R.drawable.perfil);
        }
    }

    private void mostrarDetallesReceta(Receta receta) {
        TextView nombreReceta = getView().findViewById(R.id.recipe_name);
        nombreReceta.setText(receta.getTitulo());

        TextView kcl = getView().findViewById(R.id.nutrition_kcl);
        TextView p = getView().findViewById(R.id.nutrition_p);
        TextView c = getView().findViewById(R.id.nutrition_c);
        TextView gt = getView().findViewById(R.id.nutrition_gt);

        kcl.setText(String.valueOf(receta.getCalorias()));
        p.setText(String.valueOf(receta.getProteinas()));
        c.setText(String.valueOf(receta.getCarbohidratos()));
        gt.setText(String.valueOf(receta.getGrasas()));

        ImageView imagenReceta = getView().findViewById(R.id.photoImageDetails);
        Glide.with(requireContext()).load(receta.getImagen()).into(imagenReceta);

        TextView tiempo = getView().findViewById(R.id.tv_tiempo);
        TextView dificultad = getView().findViewById(R.id.tv_dificultad);
        tiempo.setText(String.format(receta.getTiempoPreparacion()));
        dificultad.setText(receta.getDificultad());

        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = getView().findViewById(R.id.lista_ingredientes);
        contenedorIngredientes.removeAllViews();
        for (Ingrediente ingrediente : receta.getIngredientes()) {
            agregarChipIngrediente(contenedorIngredientes, ingrediente.getNombreIngrediente());
        }

        LinearLayout contenedorPasos = getView().findViewById(R.id.lista_pasos);
        contenedorPasos.removeAllViews();

        List<String> pasos = Arrays.asList(receta.getPreparacion().split("\n"));
        int pasoNum = 1;
        for (String paso : pasos) {
            TextView tvPaso = new TextView(getContext());
            tvPaso.setText(String.format("%d. %s", pasoNum++, paso));
            tvPaso.setTextSize(16);
            tvPaso.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            tvPaso.setPadding(0, 0, 0, 16);
            contenedorPasos.addView(tvPaso);
        }
    }

    private void agregarChipIngrediente(com.google.android.flexbox.FlexboxLayout contenedor, String ingrediente) {
        TextView tvIngrediente = new TextView(getContext());
        tvIngrediente.setText(ingrediente);
        tvIngrediente.setPadding(30, 20, 30, 20);
        tvIngrediente.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        tvIngrediente.setBackgroundResource(R.drawable.bg_chip);
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        tvIngrediente.setLayoutParams(params);
        contenedor.addView(tvIngrediente);
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarReceta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarReceta() {
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        recetaApi.deleteReceta(recetaActual.getIdReceta(), "Bearer " + tokenGuardado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ELIMINAR_ERROR", "Error en conexión: " + t.getMessage());
            }
        });
    }
}
