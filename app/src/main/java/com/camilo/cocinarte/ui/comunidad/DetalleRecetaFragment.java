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
import com.google.gson.Gson;
import java.util.ArrayList;
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

        cargarDatosUsuario();

        if (getArguments() != null) {
            String recetaJson = getArguments().getString("receta");
            recetaActual = new Gson().fromJson(recetaJson, Receta.class);
            mostrarDetallesReceta(recetaActual);
        }

        String origen = getArguments().getString("origen", "mis_recetas");
        ImageView btnEliminar = view.findViewById(R.id.btn_delete_recipe);
        if(origen == "comunidad"){
            btnEliminar.setVisibility(View.GONE);
        }else{
            btnEliminar.setOnClickListener(v -> confirmarEliminacion());
        }
    }

    private void cargarDatosUsuario() {
        ImageView userProfileImage = getView().findViewById(R.id.user_profile_image);
        TextView userEmail = getView().findViewById(R.id.user_email);
        TextView userName = getView().findViewById(R.id.user_name);


        String email = "cristiancl7@gmail.com";
        String name = "cristian015";

        // Leer la URI de la imagen guardada en SharedPreferences
        SharedPreferences preferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String photoUriString = preferences.getString("profile_image_uri", null);

        userEmail.setText(email);
        userName.setText(name);

        if (photoUriString != null && !photoUriString.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(photoUriString))
                    .circleCrop()
                    .placeholder(R.drawable.ic_cuenta_configuracion)
                    .error(R.drawable.ic_cuenta_configuracion)
                    .into(userProfileImage);
        } else {
            userProfileImage.setImageResource(R.drawable.ic_cuenta_configuracion);
        }
    }



    private void mostrarDetallesReceta(Receta receta) {
        ImageView logo = getView().findViewById(R.id.logo_image);
        Glide.with(this).load(R.drawable.logo_cocinarte).into(logo);

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
        if (receta.getImagen() != null && !receta.getImagen().isEmpty()) {
            Glide.with(requireContext())
                    .load(receta.getImagen())
                    .into(imagenReceta);
        }

        TextView tiempo = getView().findViewById(R.id.tv_tiempo);
        TextView dificultad = getView().findViewById(R.id.tv_dificultad);

        tiempo.setText(String.format("%s %s", receta.getTiempoPreparacion(), "m" /*NO EXISTE receta.getUnidad()*/));
        dificultad.setText(receta.getDificultad());


        com.google.android.flexbox.FlexboxLayout contenedorIngredientes = getView().findViewById(R.id.lista_ingredientes);
        contenedorIngredientes.removeAllViews();

        List<String> ingredientes = new ArrayList<>();
        if (receta.getIngredientes() != null) {
            for (Ingrediente ingrediente : receta.getIngredientes()) {
                ingredientes.add(ingrediente.getNombreIngrediente());
            }
        }

        if (ingredientes.size() == 1 && ingredientes.get(0).contains(",")) {
            String[] separados = ingredientes.get(0).split(",");
            for (String ingrediente : separados) {
                agregarChipIngrediente(contenedorIngredientes, ingrediente.trim());
            }
        } else {
            for (String ingrediente : ingredientes) {
                agregarChipIngrediente(contenedorIngredientes, ingrediente.trim());
            }
        }

        LinearLayout contenedorPasos = getView().findViewById(R.id.lista_pasos);
        contenedorPasos.removeAllViews();

        int pasoNum = 1;
        ArrayList<String> pasos = new ArrayList<>(receta.getPasos());

        for (String paso : pasos /*NO EXISTE receta.getPasos()*/) {
            TextView tvPaso = new TextView(getContext());
            tvPaso.setText(String.format("%d. %s", pasoNum++, paso));
            tvPaso.setTextSize(16);
            tvPaso.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            tvPaso.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvPaso.setPadding(0, 0, 0, 16);
            contenedorPasos.addView(tvPaso);
        }
    }


    private void agregarChipIngrediente(com.google.android.flexbox.FlexboxLayout contenedor, String ingrediente) {
        TextView tvIngrediente = new TextView(getContext());
        tvIngrediente.setText(ingrediente);
        tvIngrediente.setTextSize(14);
        tvIngrediente.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        tvIngrediente.setPadding(30, 20, 30, 20);
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

        LoginManager loginManager = new LoginManager(getContext());
        String tokenGuardado = loginManager.getToken();
        recetaApi.deleteReceta(recetaActual.getIdReceta(),"Bearer " + tokenGuardado).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(">> >>failuere", "Error en conexión: " + t.getMessage());
            }
        });
    }
}
