package com.camilo.cocinarte.ui.comunidad;

import android.net.Uri;
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
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.RecetaRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

                String pasosFormateados = String.join("\n", pasos); // ✅ Formato correcto

                String ingredientesString = datos.getString("ingredientes", "");
                imagePath = datos.getString("imagenUri");

                if (imagePath == null || imagePath.isEmpty()) {
                    Toast.makeText(getContext(), "No se ha seleccionado una imagen válida", Toast.LENGTH_LONG).show();
                    Log.e("|||Error", "imagePath es nulo o vacío");
                    return;
                }

                RecetaRequest receta = new RecetaRequest();
                LoginManager loginManager = new LoginManager(requireContext());

                receta.setIdUsuario(loginManager.getUsuario().getIdUsuario());
                receta.setTitulo(datos.getString("nombreReceta"));
                receta.setDescripcion(pasosFormateados); // ✅ Los pasos ahora son la descripción
                receta.setTiempoPreparacion(datos.getString("tiempo"));
                receta.setDificultad(datos.getString("dificultad"));

                String nombreCategoria = datos.getString("categoria", "");
                int idCategoria = obtenerIdCategoriaDesdeNombre(nombreCategoria);
                receta.setIdCategoria(idCategoria);

                receta.setCalorias(Integer.parseInt(datos.getString("kcal", "0")));
                receta.setProteinas(Integer.parseInt(datos.getString("proteinas", "0")));
                receta.setCarbohidratos(Integer.parseInt(datos.getString("carbohidratos", "0")));
                receta.setGrasas(Integer.parseInt(datos.getString("grasas", "0")));
                receta.setIngredientes(ingredientesString);
                receta.setPasos(pasos);
                receta.setPreparacion(pasosFormateados);
                receta.setSeccion("comunidad");
                receta.setFechaCreacion(String.valueOf(new Date()));

                try {
                    guardarReceta(receta);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "No se recibieron los datos necesarios", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private int obtenerIdCategoriaDesdeNombre(String nombre) {
        String[] categorias = getResources().getStringArray(R.array.categorias);
        for (int i = 1; i < categorias.length; i++) {
            if (categorias[i].equalsIgnoreCase(nombre.trim())) {
                return i;
            }
        }
        return 1;
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

    private void guardarReceta(RecetaRequest receta) throws IOException {
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        Uri imageUri = Uri.parse(imagePath);
        File file = createTempFileFromUri(imageUri);
        if (file == null) {
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_LONG).show();
            Log.e("|||Error", "No se pudo convertir la URI en archivo");
            return;
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        RequestBody _nombre = RequestBody.create(MediaType.parse("text/plain"), receta.getTitulo());
        RequestBody _categoria = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(receta.getIdCategoria()));
        RequestBody _seccion = RequestBody.create(MediaType.parse("text/plain"), receta.getSeccion());
        RequestBody _preparacion = RequestBody.create(MediaType.parse("text/plain"), receta.getPreparacion());
        RequestBody _tiempo = RequestBody.create(MediaType.parse("text/plain"), receta.getTiempoPreparacion());
        RequestBody _descripcion = RequestBody.create(MediaType.parse("text/plain"), receta.getDescripcion());
        RequestBody _dificultad = RequestBody.create(MediaType.parse("text/plain"), receta.getDificultad());
        RequestBody _ingredientes = RequestBody.create(MediaType.parse("text/plain"), receta.getIngredientes());

        recetaApi.createReceta(
                imagenPart,
                _nombre,
                _categoria,
                _seccion,
                _ingredientes,
                _preparacion,
                _tiempo,
                _dificultad,
                _descripcion,
                "Bearer " + tokenGuardado
        ).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Receta publicada correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.action_instruccionesFragmentReceta_to_navegar_comunidad_mis_recetas);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Respuesta vacía";
                        Log.e("|||Error", "Código: " + response.code() + "\n" + errorBody);

                        String mensajeClaro;
                        if (response.code() == 400) {
                            mensajeClaro = "Faltan campos obligatorios o hay datos inválidos:\n" + errorBody;
                        } else if (response.code() == 500 && errorBody.contains("Named bind parameter")) {
                            mensajeClaro = "Error interno al crear la receta: falta un parámetro en la base de datos.\nRevisa si estás enviando todos los campos necesarios.";
                        } else {
                            mensajeClaro = "Error del servidor:\n" + errorBody;
                        }

                        Toast.makeText(getContext(), mensajeClaro, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("|||IOException", "Error al leer cuerpo del error", e);
                        Toast.makeText(getContext(), "Error al interpretar la respuesta del servidor", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e("|||Failure", "Fallo en la conexión: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            String fileName = "imagen_" + System.currentTimeMillis() + ".jpg";
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

