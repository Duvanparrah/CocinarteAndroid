package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstruccionesFragmentReceta extends Fragment {

    private EditText etPaso;
    private ImageButton btnAgregarPaso;
    private LinearLayout listaPasos;

    public InstruccionesFragmentReceta() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instrucciones_receta, container, false);

        etPaso = view.findViewById(R.id.et_paso);
        btnAgregarPaso = view.findViewById(R.id.btn_agregar_paso);
        listaPasos = view.findViewById(R.id.lista_pasos);
        Button btnPublicar = view.findViewById(R.id.btn_publicar_receta);

        actualizarHint();

        btnAgregarPaso.setOnClickListener(v -> {
            String pasoTexto = etPaso.getText().toString().trim();
            if (!TextUtils.isEmpty(pasoTexto)) {
                agregarPaso(pasoTexto);
                etPaso.setText("");
                actualizarHint();
            }
        });

        btnPublicar.setOnClickListener(v -> {
            Bundle datos = getArguments();
            if (datos != null) {

                List<String> pasos = new ArrayList<>();
                for (int i = 0; i < listaPasos.getChildCount(); i++) {
                    TextView tv = (TextView) listaPasos.getChildAt(i);
                    String texto = tv.getText().toString();
                    String contenido = texto.substring(texto.indexOf(".") + 1).trim();
                    pasos.add(contenido);
                }


                String ingredientesString = datos.getString("ingredientes", "");
                List<String> ingredientes = Arrays.asList(ingredientesString.split(";"));

                Receta receta = new Receta(
                        datos.getString("nombreReceta"),
                        datos.getString("imagenUri"),
                        datos.getString("tiempo"),
                        datos.getString("unidad"),
                        datos.getString("dificultad"),
                        ingredientes,
                        pasos,
                        Integer.parseInt(datos.getString("kcal", "0")),
                        Integer.parseInt(datos.getString("proteinas", "0")),
                        Integer.parseInt(datos.getString("carbohidratos", "0")),
                        Integer.parseInt(datos.getString("grasas", "0"))
                );


                guardarReceta(receta);
                Toast.makeText(getContext(), "Receta publicada correctamente", Toast.LENGTH_SHORT).show();


            }
        });

        return view;
    }

    private void agregarPaso(String textoPaso) {
        TextView paso = new TextView(getContext());
        paso.setTextSize(16);
        paso.setTextColor(getResources().getColor(android.R.color.black));
        paso.setPadding(20, 20, 20, 20);
        paso.setText(listaPasos.getChildCount() + 1 + ". " + textoPaso);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        paso.setLayoutParams(params);

        listaPasos.addView(paso);

        paso.setOnClickListener(v -> {
            listaPasos.removeView(paso);
            actualizarNumeracion();
            actualizarHint();
        });
    }

    private void actualizarNumeracion() {
        int count = listaPasos.getChildCount();
        for (int i = 0; i < count; i++) {
            TextView paso = (TextView) listaPasos.getChildAt(i);
            String texto = paso.getText().toString();
            String contenido = texto.contains(".") ? texto.substring(texto.indexOf(".") + 1).trim() : texto;
            paso.setText((i + 1) + ". " + contenido);
        }
    }

    private void actualizarHint() {
        int siguientePaso = listaPasos.getChildCount() + 1;
        etPaso.setHint(siguientePaso + ". Agrega los pasos de tu receta");
    }

    private void guardarReceta(Receta receta) {
        SharedPreferences prefs = requireContext().getSharedPreferences("recetas", Context.MODE_PRIVATE);
        Gson gson = new Gson();


        String jsonActual = prefs.getString("lista_recetas", null);
        List<Receta> lista = new ArrayList<>();

        if (jsonActual != null) {
            lista = gson.fromJson(jsonActual, new TypeToken<List<Receta>>() {}.getType());
        }


        lista.add(receta);
        String jsonNuevo = gson.toJson(lista);
        prefs.edit().putString("lista_recetas", jsonNuevo).apply();
    }
}
