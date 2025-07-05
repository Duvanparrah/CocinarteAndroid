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
    private static final String TAG = "InstruccionesFragment";

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
                publicarReceta(datos);
            } else {
                Toast.makeText(getContext(), "No se recibieron los datos necesarios", Toast.LENGTH_LONG).show();
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

    // ✅ MÉTODO PRINCIPAL PARA PUBLICAR LA RECETA
    private void publicarReceta(Bundle datos) {
        // Validar que hay pasos
        if (listaPasos.getChildCount() == 0) {
            Toast.makeText(getContext(), "Agrega al menos un paso para la receta", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar imagen
        imagePath = datos.getString("imagenUri");
        if (imagePath == null || imagePath.isEmpty()) {
            Toast.makeText(getContext(), "No se ha seleccionado una imagen válida", Toast.LENGTH_LONG).show();
            Log.e(TAG, "imagePath es nulo o vacío");
            return;
        }

        // ✅ EXTRAER PASOS DE LA INTERFAZ
        List<String> pasos = new ArrayList<>();
        for (int i = 0; i < listaPasos.getChildCount(); i++) {
            TextView tv = (TextView) listaPasos.getChildAt(i);
            String texto = tv.getText().toString();
            String contenido = texto.substring(texto.indexOf(".") + 1).trim();
            pasos.add(contenido);
        }

        // ✅ CREAR DESCRIPCIÓN/PREPARACIÓN UNIENDO LOS PASOS
        String pasosFormateados = String.join("\n", pasos);

        Log.d(TAG, "📝 Preparando receta con " + pasos.size() + " pasos");

        // ✅ CREAR INSTANCIA DE LOGINMANAGER
        LoginManager loginManager = new LoginManager(requireContext());
        Log.d(TAG, "👤 Usuario: " + loginManager.getUsuario().getNombreUsuario());

        // ✅ CREAR OBJETO RECETA CON DATOS CORRECTOS
        RecetaRequest receta = new RecetaRequest();

        receta.setIdUsuario(loginManager.getUsuario().getIdUsuario());
        receta.setTitulo(datos.getString("nombreReceta"));
        receta.setDescripcion(pasosFormateados);  // ✅ Los pasos como descripción
        receta.setTiempoPreparacion(datos.getString("tiempo"));
        receta.setDificultad(datos.getString("dificultad"));
        receta.setIdCategoria(datos.getInt("categoriaId", 1));
        receta.setIngredientes(datos.getString("ingredientes"));
        receta.setPasos(pasos);
        receta.setPreparacion(pasosFormateados);  // ✅ También como preparación
        receta.setSeccion("comunidad");  // ✅ IMPORTANTE: Para que aparezca en comunidad
        receta.setFechaCreacion(String.valueOf(new Date()));

        // ✅ VALORES NUTRICIONALES (se calcularán automáticamente en el backend)
        receta.setCalorias(0);
        receta.setProteinas(0);
        receta.setCarbohidratos(0);
        receta.setGrasas(0);

        try {
            enviarRecetaAlServidor(receta);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ MÉTODO PARA ENVIAR LA RECETA AL SERVIDOR
    private void enviarRecetaAlServidor(RecetaRequest receta) throws IOException {
        Log.d(TAG, "🚀 Enviando receta al servidor...");

        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        // ✅ PREPARAR ARCHIVO DE IMAGEN
        Uri imageUri = Uri.parse(imagePath);
        File file = createTempFileFromUri(imageUri);
        if (file == null) {
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_LONG).show();
            Log.e(TAG, "No se pudo convertir la URI en archivo");
            return;
        }

        // ✅ CREAR MULTIPART BODY PARTS
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

        // ✅ CREAR REQUEST BODIES PARA CADA CAMPO
        RequestBody _nombre = RequestBody.create(MediaType.parse("text/plain"), receta.getTitulo());
        RequestBody _categoria = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(receta.getIdCategoria()));
        RequestBody _seccion = RequestBody.create(MediaType.parse("text/plain"), receta.getSeccion());
        RequestBody _preparacion = RequestBody.create(MediaType.parse("text/plain"), receta.getPreparacion());
        RequestBody _tiempo = RequestBody.create(MediaType.parse("text/plain"), receta.getTiempoPreparacion());
        RequestBody _descripcion = RequestBody.create(MediaType.parse("text/plain"), receta.getDescripcion());
        RequestBody _dificultad = RequestBody.create(MediaType.parse("text/plain"), receta.getDificultad());
        RequestBody _ingredientes = RequestBody.create(MediaType.parse("text/plain"), receta.getIngredientes());

        // ✅ LOG DE DATOS ENVIADOS
        Log.d(TAG, "📊 Datos de la receta:");
        Log.d(TAG, "   - Título: " + receta.getTitulo());
        Log.d(TAG, "   - Categoría: " + receta.getIdCategoria());
        Log.d(TAG, "   - Sección: " + receta.getSeccion());
        Log.d(TAG, "   - Tiempo: " + receta.getTiempoPreparacion());
        Log.d(TAG, "   - Dificultad: " + receta.getDificultad());
        Log.d(TAG, "   - Ingredientes: " + receta.getIngredientes());
        Log.d(TAG, "   - Descripción: " + receta.getDescripcion().substring(0, Math.min(50, receta.getDescripcion().length())) + "...");

        // ✅ HACER LA LLAMADA AL API
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

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
                    Log.d(TAG, "✅ Receta publicada exitosamente");

                    Toast.makeText(getContext(), "¡Receta publicada correctamente!", Toast.LENGTH_SHORT).show();

                    // ✅ NAVEGAR A MIS RECETAS DESPUÉS DE CREAR
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_instruccionesFragmentReceta_to_navegar_comunidad_mis_recetas);

                } else {
                    manejarErrorRespuesta(response);
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // ✅ MÉTODO PARA MANEJAR ERRORES DE RESPUESTA
    private void manejarErrorRespuesta(Response<Receta> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Respuesta vacía";
            Log.e(TAG, "❌ Error HTTP " + response.code() + ": " + errorBody);

            String mensajeClaro;
            switch (response.code()) {
                case 400:
                    mensajeClaro = "Datos inválidos o incompletos. Verifica todos los campos.";
                    break;
                case 401:
                    mensajeClaro = "Error de autenticación. Inicia sesión nuevamente.";
                    break;
                case 413:
                    mensajeClaro = "La imagen es demasiado grande. Usa una imagen más pequeña.";
                    break;
                case 500:
                    if (errorBody.contains("Named bind parameter")) {
                        mensajeClaro = "Error interno del servidor. Faltan parámetros requeridos.";
                    } else {
                        mensajeClaro = "Error interno del servidor. Intenta nuevamente.";
                    }
                    break;
                default:
                    mensajeClaro = "Error del servidor (código " + response.code() + "). Intenta nuevamente.";
            }

            Toast.makeText(getContext(), mensajeClaro, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e(TAG, "❌ Error al leer respuesta de error", e);
            Toast.makeText(getContext(), "Error al interpretar la respuesta del servidor", Toast.LENGTH_LONG).show();
        }
    }

    // ✅ MÉTODO PARA CREAR ARCHIVO TEMPORAL DESDE URI
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

            Log.d(TAG, "✅ Archivo temporal creado: " + tempFile.getAbsolutePath() + " (" + tempFile.length() + " bytes)");
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "❌ Error al crear archivo temporal", e);
            return null;
        }
    }
}