package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginApi;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

                if (listaPasos.getChildCount() == 0) {
                    Toast.makeText(getContext(), "Agrega al menos un paso para la receta", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> pasos = new ArrayList<>();
                for (int i = 0; i < listaPasos.getChildCount(); i++) {
                    TextView tv = (TextView) listaPasos.getChildAt(i);
                    String texto = tv.getText().toString();
                    String contenido = texto.substring(texto.indexOf(".") + 1).trim();
                    pasos.add(contenido);
                }

                String ingredientesString = datos.getString("ingredientes", "");
                List<String> ingredientes = Arrays.asList(ingredientesString.split(";"));

                Receta receta = new Receta();
                LoginManager loginManager = new LoginManager(getContext());

                receta.setIdUsuario(loginManager.getUsuario().getIdUsuario());
                receta.setTitulo(datos.getString("nombreReceta"));
                receta.setDescripcion("-o-123456789");
                receta.setImagen(datos.getString("imagenUri"));
                receta.setTiempoPreparacion(datos.getString("tiempo"));
                receta.setDificultad(datos.getString("dificultad"));
                receta.setCalorias(Integer.parseInt(datos.getString("kcal", "0")));
                receta.setProteinas(Integer.parseInt(datos.getString("proteinas", "0")));
                receta.setCarbohidratos(Integer.parseInt(datos.getString("carbohidratos", "0")));
                receta.setGrasas(Integer.parseInt(datos.getString("grasas", "0")));
                //receta.setAzucar(Integer azucar);
                //receta.setSeccion(String seccion);
                //receta.setIdCategoria(Integer idCategoria);
                //receta.setFechaCreacion(String fechaCreacion);
                //receta.setFechaEdicion(String fechaEdicion);
                //receta.setEditado(Boolean editado);
                //TODO: falta en app azucar, seccion, idCategoria, fechaCreacion, fechaEdicion, editado
                //TODO: falta en db ingredientes, pasos, datos.getString("unidad")

                guardarReceta(receta);
                Toast.makeText(getContext(), "Receta publicada correctamente", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(requireView()).navigate(R.id.action_instruccionesFragmentReceta_to_navegar_comunidad_mis_recetas);
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
        Gson gson = new Gson();

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        LoginManager loginManager = new LoginManager(getContext());
        String tokenGuardado = loginManager.getToken();

        receta.setSeccion("comunidad");
        String jsonNuevo = gson.toJson(receta);
        Log.v(">> >>:JSON crate",     jsonNuevo);

        recetaApi.createReceta(receta, "Bearer " + tokenGuardado).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {

                if (response.isSuccessful()) {
                    //respuesta = response.body();
                } else {
                    Log.e(">> >>error regrofit", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e(">> >>failuere", "Error en conexión: " + t.getMessage());
            }
        });
        //prefs.edit().putString("lista_recetas", jsonNuevo).apply();
    }
}
