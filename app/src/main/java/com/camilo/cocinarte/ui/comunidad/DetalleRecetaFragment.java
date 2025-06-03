package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
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
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;

import java.util.List;

public class DetalleRecetaFragment extends Fragment {

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
            Receta receta = new Gson().fromJson(recetaJson, Receta.class);
            mostrarDetallesReceta(receta);
        }
    }

    private void cargarDatosUsuario() {
        // Obtener referencias a las vistas
        ImageView userProfileImage = getView().findViewById(R.id.user_profile_image);
        TextView userEmail = getView().findViewById(R.id.user_email);
        TextView userName = getView().findViewById(R.id.user_name);

        // Datos de ejemplo (reemplazar con tus datos reales)
        String email = "cristiancl7@gmail.com";
        String name = "cristian015";
        String photoUrl = ""; // URL de la imagen si está disponible

        // Establecer datos
        userEmail.setText(email);
        userName.setText(name);

        // Cargar imagen de perfil
        if (photoUrl != null && !photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(userProfileImage);
        } else {
            userProfileImage.setImageResource(R.drawable.ic_cuenta_configuracion);
        }
    }

    private void mostrarDetallesReceta(Receta receta) {
        // Configurar logo (opcional)
        ImageView logo = getView().findViewById(R.id.logo_image);
        Glide.with(this).load(R.drawable.logo_cocinarte).into(logo);

        // Nombre de la receta
        TextView nombreReceta = getView().findViewById(R.id.recipe_name);
        nombreReceta.setText(receta.getNombre());

        // Tabla nutricional
        TextView kcl = getView().findViewById(R.id.nutrition_kcl);
        TextView p = getView().findViewById(R.id.nutrition_p);
        TextView c = getView().findViewById(R.id.nutrition_c);
        TextView gt = getView().findViewById(R.id.nutrition_gt);

        kcl.setText(String.valueOf(receta.getKcal()));
        p.setText(String.valueOf(receta.getProteinas()));
        c.setText(String.valueOf(receta.getCarbohidratos()));
        gt.setText(String.valueOf(receta.getGrasa()));

        // Imagen de la receta
        ImageView imagenReceta = getView().findViewById(R.id.photoImage);
        if (receta.getImagenUri() != null && !receta.getImagenUri().isEmpty()) {
            Glide.with(this)
                    .load(receta.getImagenUri())

                    .into(imagenReceta);


        // Tiempo y dificultad
        TextView tiempo = getView().findViewById(R.id.tv_tiempo);
        TextView dificultad = getView().findViewById(R.id.tv_dificultad);

        tiempo.setText(String.format("%s %s", receta.getTiempo(), receta.getUnidad()));
        dificultad.setText(receta.getDificultad());

        // Ingredientes
        LinearLayout contenedorIngredientes = getView().findViewById(R.id.lista_ingredientes);
        for (String ingrediente : receta.getIngredientes()) {
            TextView tvIngrediente = new TextView(getContext());
            tvIngrediente.setText(String.format("• %s", ingrediente));
            tvIngrediente.setTextSize(16);
            tvIngrediente.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            tvIngrediente.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            contenedorIngredientes.addView(tvIngrediente);
        }

        // Pasos de preparación
        LinearLayout contenedorPasos = getView().findViewById(R.id.lista_pasos);
        int pasoNum = 1;
        for (String paso : receta.getPasos()) {
            TextView tvPaso = new TextView(getContext());
            tvPaso.setText(String.format("%d. %s", pasoNum++, paso));
            tvPaso.setTextSize(16);
            tvPaso.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
            tvPaso.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            tvPaso.setPadding(0, 0, 0, 16); // Espacio entre pasos
            contenedorPasos.addView(tvPaso);
        }
    }
}
}