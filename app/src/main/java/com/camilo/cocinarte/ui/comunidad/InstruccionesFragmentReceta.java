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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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


                ArrayList<Ingrediente> _ingredientes = new ArrayList<Ingrediente>();
                ingredientes.forEach(res ->{
                    Log.v(">> >>:ingredientesString",     ""+res);
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

        this.enviarFoto(receta.getImagen(), tokenGuardado).enqueue(new Callback<FotoResponse>() {
            @Override
            public void onResponse(@NonNull Call<FotoResponse> call, @NonNull Response<FotoResponse> response) {
                if (response.isSuccessful()) {
                    FotoResponse fotoResponse = response.body();
                    // Manejar la respuesta exitosa, ej. mostrar un mensaje, actualizar UI
                    assert fotoResponse != null;
                    Log.d("Upload", "Foto subida correctamente. Link: " + fotoResponse.getUrl() );
                    RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
                    receta.setImagen(fotoResponse.getUrl());
                    recetaApi.createReceta(receta, "Bearer " + tokenGuardado).enqueue(new Callback<Receta>() {
                        @Override
                        public void onResponse(Call<Receta> call, Response<Receta> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Receta publicada correctamente", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigate(R.id.action_instruccionesFragmentReceta_to_navegar_comunidad_mis_recetas);
                            } else {
                                Log.e(">> >>error regrofit", "Error en respuesta: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Receta> call, Throwable t) {
                            Log.e(">> >>failuere", "Error en conexión: " + t.getMessage());
                        }
                    });
                } else {
                    // Manejar el error de la respuesta
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("Upload", "Error al subir foto: " + response.code() + " - " + errorBody);
                        // Mostrar mensaje de error al usuario
                    } catch (Exception e) {
                        Log.e("Upload", "Error al leer errorBody: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<FotoResponse> call, Throwable t) {
                // Manejar errores de red o excepciones
                Log.e("Upload", "Fallo al conectar con el servidor: " + t.getMessage(), t);
                // Mostrar mensaje de error de conexión
            }
        });
    }


    private String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String nombre = "archivo.jpg";
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (index != -1) nombre = cursor.getString(index);
            cursor.close();
        }
        return nombre;
    }
    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private MultipartBody.Part prepararImagenParaSubir(Context context, Uri contentUri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        String mimeType = resolver.getType(contentUri);
        String nombreArchivo = getFileName(context, contentUri);

        InputStream inputStream = resolver.openInputStream(contentUri);
        byte[] bytes = getBytes(inputStream);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
        return MultipartBody.Part.createFormData("foto", nombreArchivo, requestFile);
    }


    private Call<FotoResponse> enviarFoto(String imagePath, String token) throws IOException {
        // 1. Crear MultipartBody.Part
        MultipartBody.Part filePart = prepararImagenParaSubir(requireContext(), Uri.parse(imagePath));

        // 2. Obtener tu instancia del api
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        // 3. Realizar la llamada a la API
        return recetaApi.subirFotoReceta(filePart, "Bearer " + token);
    }
}
