package com.camilo.cocinarte.ui.comunidad;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.FotoResponse;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InstruccionesFragmentReceta extends Fragment {

    private EditText etPaso;
    private ImageButton btnAgregarPaso;
    private LinearLayout listaPasos;
    private String imagePath;

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
                imagePath = datos.getString("imagenUri");
                receta.setTiempoPreparacion(datos.getString("tiempo"));
                receta.setDificultad(datos.getString("dificultad"));
                receta.setCalorias(Integer.parseInt(datos.getString("kcal", "0")));
                receta.setProteinas(Integer.parseInt(datos.getString("proteinas", "0")));
                receta.setCarbohidratos(Integer.parseInt(datos.getString("carbohidratos", "0")));
                receta.setGrasas(Integer.parseInt(datos.getString("grasas", "0")));
                //receta.setAzucar(Integer azucar);


                ArrayList<Ingrediente> _ingredientes = new ArrayList<Ingrediente>();
                ingredientes.forEach(res ->{
                    Ingrediente ingrediente = new Ingrediente();
                    ingrediente.setNombreIngrediente(res);
                    //ingrediente.setImagen(null);
                    ingrediente.setCategoria("Verduras");
                    ingrediente.setCaloriasPor100g(0);
                    ingrediente.setProteinasPor100g(0);
                    ingrediente.setCarbohidratosPor100g(0);
                    ingrediente.setGrasasTotalesPor100g(0);
                    ingrediente.setAzucarPor100g(0);
                    ingrediente.setUnidad("gramos");
                    _ingredientes.add(ingrediente);
                });

                receta.setIngredientes(_ingredientes);

                receta.setPasos(pasos);
                receta.setSeccion("comunidad");
                //receta.setIdCategoria(Integer idCategoria);
                receta.setFechaCreacion(String.valueOf(new Date()));
                //receta.setFechaEdicion(String.valueOf(new Date()));
                //receta.setEditado(Boolean editado);
                //TODO: falta en app azucar, idCategoria, editado
                //TODO: falta en db ingredientes, pasos, datos.getString("unidad")

                try {
                    guardarReceta(receta);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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

    private void guardarReceta(Receta receta) throws IOException {
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        Uri imageUri = Uri.parse(imagePath);

        File file = createTempFileFromUri(imageUri);
        if (file == null) return;

        // Crear RequestBody del archivo
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                file
        );

        // Crear MultipartBody.Part con el nombre esperado por el backend: "file"
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData(
                "foto",              // este nombre debe coincidir con el que espera tu backend: req.file
                file.getName(),
                requestFile
        );

        // Manejar la respuesta exitosa, ej. mostrar un mensaje, actualizar UI
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        // Convierte el objeto Receta en JSON usando Gson
        Gson gson = new Gson();
        receta.setCategoria("Comida Internacional");
        receta.setIngredientes(null);
        receta.setDificultad(receta.getDificultad().toLowerCase());
        String preparacion = receta.getPasos().stream().collect(Collectors.joining("\n"));
        receta.setPreparacion(preparacion);

        String recetaJson = gson.toJson(receta);
        RequestBody recetaRequestBody = RequestBody.create(
                MediaType.parse("application/json"),
                recetaJson
        );

        Log.d("|||gson receta", recetaJson);

        recetaApi.createReceta(imagenPart, recetaRequestBody, "Bearer " + tokenGuardado).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Receta publicada correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_instruccionesFragmentReceta_to_navegar_comunidad_mis_recetas);
                } else {
                    Log.e("|||No successful", "Error creear receta: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("|||Failure", "Error en conexión: " + t.getMessage());
            }
        });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            String fileName = "imagen_perfil_" + System.currentTimeMillis() + ".jpg";

            File tempFile = new File(getContext().getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
