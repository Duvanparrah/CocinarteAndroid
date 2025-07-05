package com.camilo.cocinarte.ui.comunidad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.IngredientesService;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentCrearRecetaBinding;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.chip.Chip;

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

public class CrearRecetaFragment extends Fragment {
    private static final String TAG = "CrearRecetaFragment";

    private FragmentCrearRecetaBinding binding;
    private Uri imagenUriSeleccionada;
    private final List<Ingrediente> _ingredientes = new ArrayList<>();
    private final List<String> ingredientesSeleccionados = new ArrayList<>();
    private final List<String> pasosReceta = new ArrayList<>();
    private Uri imagenUriCamara;

    private final ActivityResultLauncher<Intent> camaraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (imagenUriCamara != null) {
                        imagenUriSeleccionada = imagenUriCamara;
                        binding.photoImage.setImageURI(imagenUriSeleccionada);
                        binding.photoImage.setVisibility(View.VISIBLE);
                        binding.contenedorIcono.setVisibility(View.GONE);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imagenUriSeleccionada = uri;
                    binding.photoImage.setImageURI(uri);
                    binding.photoImage.setVisibility(View.VISIBLE);
                    binding.contenedorIcono.setVisibility(View.GONE);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearRecetaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Deshabilitar campos nutricionales (se calculan automáticamente)
        binding.nutritionKcl.setEnabled(false);
        binding.nutritionP.setEnabled(false);
        binding.nutritionC.setEnabled(false);
        binding.nutritionGt.setEnabled(false);

        cargarIngredientesDesdeAPI();
        setupEventListeners();
    }

    private void setupEventListeners() {
        // Búsqueda de ingredientes
        binding.searchIngrediente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarSugerencias(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Selección de imagen
        binding.frameSeleccionImagen.setOnClickListener(v -> mostrarDialogoSeleccionImagen());

        // Funcionalidad de pasos
        binding.btnAgregarPaso.setOnClickListener(v -> agregarPaso());

        // ✅ NUEVO: Botón publicar receta llama directamente al método de publicación
        binding.btnPublicarReceta.setOnClickListener(v -> publicarRecetaDirectamente());
    }

    // Método para agregar pasos
    private void agregarPaso() {
        String paso = binding.etPaso.getText().toString().trim();
        if (!paso.isEmpty()) {
            pasosReceta.add(paso);
            binding.etPaso.setText("");

            // Crear vista del paso
            TextView pasoView = new TextView(getContext());
            pasoView.setText((pasosReceta.size()) + ". " + paso);
            pasoView.setTextSize(14);
            pasoView.setTextColor(getResources().getColor(android.R.color.black));
            pasoView.setPadding(16, 8, 16, 8);
            pasoView.setBackground(getResources().getDrawable(R.drawable.bg_edittext));

            // Configurar parámetros de layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            pasoView.setLayoutParams(params);

            // Agregar click listener para eliminar paso
            pasoView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar paso")
                        .setMessage("¿Deseas eliminar este paso?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            int index = binding.listaPasos.indexOfChild(pasoView);
                            if (index >= 0 && index < pasosReceta.size()) {
                                pasosReceta.remove(index);
                                actualizarListaPasos();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });

            binding.listaPasos.addView(pasoView);

            // Actualizar hint
            actualizarHint();

            Toast.makeText(getContext(), "Paso agregado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Escribe un paso para agregar", Toast.LENGTH_SHORT).show();
        }
    }

    // ✅ NUEVO: Actualizar hint del EditText
    private void actualizarHint() {
        int siguientePaso = pasosReceta.size() + 1;
        binding.etPaso.setHint(siguientePaso + ". Agrega los pasos de tu receta");
    }

    // Actualizar lista de pasos después de eliminar uno
    private void actualizarListaPasos() {
        binding.listaPasos.removeAllViews();
        for (int i = 0; i < pasosReceta.size(); i++) {
            TextView pasoView = new TextView(getContext());
            pasoView.setText((i + 1) + ". " + pasosReceta.get(i));
            pasoView.setTextSize(14);
            pasoView.setTextColor(getResources().getColor(android.R.color.black));
            pasoView.setPadding(16, 8, 16, 8);
            pasoView.setBackground(getResources().getDrawable(R.drawable.bg_edittext));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 8, 0, 8);
            pasoView.setLayoutParams(params);

            // Re-agregar click listener
            final int index = i;
            pasoView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Eliminar paso")
                        .setMessage("¿Deseas eliminar este paso?")
                        .setPositiveButton("Eliminar", (dialog, which) -> {
                            pasosReceta.remove(index);
                            actualizarListaPasos();
                            actualizarHint();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            });

            binding.listaPasos.addView(pasoView);
        }
    }

    // ✅ NUEVO: Método principal para publicar la receta directamente
    private void publicarRecetaDirectamente() {
        String nombre = binding.recipeNameInput.getText().toString().trim();
        String tiempoValor = binding.etTiempo.getText().toString().trim();
        String unidadTiempo = binding.spinnerUnidadTiempo.getSelectedItem().toString();
        String tiempo = tiempoValor + " " + unidadTiempo;
        String dificultad = binding.spinnerDificultad.getSelectedItem().toString();

        // ✅ VALIDACIONES COMPLETAS
        if (nombre.isEmpty() || nombre.length() < 3) {
            Toast.makeText(getContext(), "El nombre de la receta debe tener al menos 3 caracteres", Toast.LENGTH_LONG).show();
            return;
        }

        if (tiempoValor.isEmpty()) {
            Toast.makeText(getContext(), "Debes especificar el tiempo de preparación", Toast.LENGTH_LONG).show();
            return;
        }

        if (ingredientesSeleccionados.isEmpty()) {
            Toast.makeText(getContext(), "Debes agregar al menos un ingrediente", Toast.LENGTH_LONG).show();
            return;
        }

        if (pasosReceta.isEmpty()) {
            Toast.makeText(getContext(), "Debes agregar al menos un paso de preparación", Toast.LENGTH_LONG).show();
            return;
        }

        if (imagenUriSeleccionada == null) {
            Toast.makeText(getContext(), "Debes seleccionar una imagen para tu receta", Toast.LENGTH_LONG).show();
            return;
        }

        // Obtener usuario
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            Toast.makeText(getContext(), "Error al obtener información del usuario", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "📝 Preparando receta con " + pasosReceta.size() + " pasos");
        Log.d(TAG, "👤 Usuario: " + usuario.getNombreUsuario());

        // ✅ CREAR OBJETO CON TODOS LOS DATOS
        try {
            enviarRecetaAlServidor(nombre, tiempo, dificultad, usuario);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ NUEVO: Método para enviar la receta al servidor
    private void enviarRecetaAlServidor(String nombre, String tiempo, String dificultad, Usuario usuario) throws IOException {
        Log.d(TAG, "🚀 Enviando receta al servidor...");

        String token = obtenerToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(getContext(), "Error de autenticación", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ PREPARAR ARCHIVO DE IMAGEN
        File file = createTempFileFromUri(imagenUriSeleccionada);
        if (file == null) {
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ CREAR DESCRIPCIÓN/PREPARACIÓN UNIENDO LOS PASOS
        String pasosFormateados = String.join("\n", pasosReceta);

        // ✅ CREAR INGREDIENTES JSON
        StringBuilder ingredientesJson = new StringBuilder("[");
        for (String nombreIngrediente : ingredientesSeleccionados) {
            for (Ingrediente ing : _ingredientes) {
                if (ing.getNombreIngrediente().equals(nombreIngrediente)) {
                    ingredientesJson.append(ing.getIdIngrediente()).append(",");
                    break;
                }
            }
        }
        if (ingredientesJson.length() > 1) {
            ingredientesJson.deleteCharAt(ingredientesJson.length() - 1);
        }
        ingredientesJson.append("]");

        // ✅ CREAR MULTIPART BODY PARTS
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

        // ✅ CREAR REQUEST BODIES PARA CADA CAMPO
        RequestBody _nombre = RequestBody.create(MediaType.parse("text/plain"), nombre);
        RequestBody _categoria = RequestBody.create(MediaType.parse("text/plain"), "1"); // Categoría por defecto
        RequestBody _seccion = RequestBody.create(MediaType.parse("text/plain"), "comunidad");
        RequestBody _preparacion = RequestBody.create(MediaType.parse("text/plain"), pasosFormateados);
        RequestBody _tiempo = RequestBody.create(MediaType.parse("text/plain"), tiempo);
        RequestBody _descripcion = RequestBody.create(MediaType.parse("text/plain"), pasosFormateados);
        RequestBody _dificultad = RequestBody.create(MediaType.parse("text/plain"), dificultad);
        RequestBody _ingredientes = RequestBody.create(MediaType.parse("text/plain"), ingredientesJson.toString());

        // ✅ LOG DE DATOS ENVIADOS
        Log.d(TAG, "📊 Datos de la receta:");
        Log.d(TAG, "   - Título: " + nombre);
        Log.d(TAG, "   - Tiempo: " + tiempo);
        Log.d(TAG, "   - Dificultad: " + dificultad);
        Log.d(TAG, "   - Ingredientes: " + ingredientesJson.toString());
        Log.d(TAG, "   - Pasos: " + pasosFormateados.substring(0, Math.min(50, pasosFormateados.length())) + "...");

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
                "Bearer " + token
        ).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Receta publicada exitosamente");
                    Toast.makeText(getContext(), "¡Receta publicada correctamente!", Toast.LENGTH_SHORT).show();

                    // ✅ LIMPIAR FORMULARIO
                    limpiarFormulario();

                    // ✅ NAVEGAR A MIS RECETAS
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navegar_comunidad_mis_recetas);

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

    // ✅ NUEVO: Método para limpiar el formulario después de publicar
    private void limpiarFormulario() {
        binding.recipeNameInput.setText("");
        binding.etTiempo.setText("");
        binding.searchIngrediente.setText("");
        binding.etPaso.setText("");

        // Limpiar imagen
        binding.photoImage.setVisibility(View.GONE);
        binding.contenedorIcono.setVisibility(View.VISIBLE);
        imagenUriSeleccionada = null;

        // Limpiar ingredientes
        ingredientesSeleccionados.clear();
        binding.listaIngredientes.removeAllViews();
        binding.sugerenciasContainer.removeAllViews();

        // Limpiar pasos
        pasosReceta.clear();
        binding.listaPasos.removeAllViews();
        actualizarHint();

        // Resetear spinners
        binding.spinnerUnidadTiempo.setSelection(0);
        binding.spinnerDificultad.setSelection(0);
    }

    // ✅ NUEVO: Método para manejar errores de respuesta
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
                    mensajeClaro = "Error interno del servidor. Intenta nuevamente.";
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

    // ✅ NUEVO: Método para crear archivo temporal desde URI
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

    // ✅ MÉTODO CORREGIDO: Obtener usuario actual de forma robusta
    private Usuario obtenerUsuarioActual() {
        try {
            // Intentar con LoginManager primero
            LoginManager loginManager = new LoginManager(requireContext());
            Usuario usuario = loginManager.getUsuario();
            if (usuario != null) {
                Log.d(TAG, "Usuario obtenido desde LoginManager");
                return usuario;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener usuario desde LoginManager: " + e.getMessage());
        }

        try {
            // Fallback a SessionManager
            SessionManager sessionManager = SessionManager.getInstance(requireContext());
            if (sessionManager != null && sessionManager.isLoggedIn() && sessionManager.getUserId() != null) {
                Log.d(TAG, "Usuario obtenido desde SessionManager");
                // Crear objeto Usuario con los datos del SessionManager
                Usuario usuario = new Usuario();

                // Convertir String a int para setIdUsuario
                try {
                    int userId = Integer.parseInt(sessionManager.getUserId());
                    usuario.setIdUsuario(userId);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al convertir userId a int: " + e.getMessage());
                    return null;
                }

                if (sessionManager.getUserName() != null) {
                    usuario.setNombreUsuario(sessionManager.getUserName());
                }

                return usuario;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener usuario desde SessionManager: " + e.getMessage());
        }

        Log.e(TAG, "No se pudo obtener usuario desde ningún manager");
        return null;
    }

    private void cargarIngredientesDesdeAPI() {
        // ✅ OBTENER TOKEN DE FORMA ROBUSTA
        String token = obtenerToken();

        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "No se pudo obtener token para cargar ingredientes");
            Toast.makeText(getContext(), "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        IngredientesService service = ApiClient.getClient(getContext()).create(IngredientesService.class);

        service.obtenerTodosLosIngredientes("Bearer " + token).enqueue(new Callback<IngredientesByCategoriaResponse>() {
            @Override
            public void onResponse(Call<IngredientesByCategoriaResponse> call, Response<IngredientesByCategoriaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _ingredientes.clear();
                    _ingredientes.addAll(response.body().getIngredientes());
                    Log.d(TAG, "✅ Ingredientes cargados: " + _ingredientes.size());
                } else {
                    Log.e("API", "Error al obtener ingredientes: código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<IngredientesByCategoriaResponse> call, Throwable t) {
                Log.e("Fallo de red", t.toString());
            }
        });
    }

    // ✅ MÉTODO PARA OBTENER TOKEN DE FORMA ROBUSTA
    private String obtenerToken() {
        try {
            // Intentar con LoginManager primero
            LoginManager loginManager = new LoginManager(requireContext());
            String token = loginManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                Log.d(TAG, "Token obtenido desde LoginManager");
                return token;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener token desde LoginManager: " + e.getMessage());
        }

        try {
            // Fallback a SessionManager
            SessionManager sessionManager = SessionManager.getInstance(requireContext());
            if (sessionManager != null && sessionManager.getAuthToken() != null) {
                Log.d(TAG, "Token obtenido desde SessionManager");
                return sessionManager.getAuthToken();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener token desde SessionManager: " + e.getMessage());
        }

        Log.e(TAG, "No se pudo obtener token desde ningún manager");
        return null;
    }

    // MÉTODOS EXISTENTES SIN CAMBIOS...
    private void mostrarDialogoSeleccionImagen() {
        String[] opciones = {"Cámara", "Galería"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirCamara();
                    } else {
                        galeriaLauncher.launch("image/*");
                    }
                }).show();
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = crearArchivoImagen();
            if (photoFile != null) {
                imagenUriCamara = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
                camaraLauncher.launch(intent);
            }
        }
    }

    private File crearArchivoImagen() {
        String nombreArchivo = "foto_" + System.currentTimeMillis();
        File directorio = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(nombreArchivo, ".jpg", directorio);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarSugerencias(String texto) {
        binding.sugerenciasContainer.removeAllViews();
        if (texto.isEmpty()) return;

        for (Ingrediente ingrediente : _ingredientes) {
            if (ingrediente.getNombreIngrediente().toLowerCase().contains(texto.toLowerCase()) &&
                    !ingredientesSeleccionados.contains(ingrediente.getNombreIngrediente())) {

                TextView chip = crearChipVisual(ingrediente.getNombreIngrediente(), false);
                chip.setOnClickListener(v -> {
                    agregarIngredienteASeleccionados(ingrediente.getNombreIngrediente());
                    binding.searchIngrediente.setText("");
                });

                binding.sugerenciasContainer.addView(chip);
            }
        }
    }

    private void agregarIngredienteASeleccionados(String nombreIngrediente) {
        if (!ingredientesSeleccionados.contains(nombreIngrediente)) {
            ingredientesSeleccionados.add(nombreIngrediente);
            Toast.makeText(getContext(), nombreIngrediente + " agregado a la receta", Toast.LENGTH_SHORT).show();

            TextView chip = crearChipVisual(nombreIngrediente, true);
            chip.setOnClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(nombreIngrediente);
            });

            binding.listaIngredientes.addView(chip);
        }
    }

    private Chip crearChipVisual(String texto, boolean esSeleccionado) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setTextSize(14);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setTextColor(getResources().getColor(android.R.color.black));
        chip.setChipCornerRadius(50f);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        chip.setLayoutParams(params);

        if (esSeleccionado) {
            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(R.drawable.ic_chip_delete);
            chip.setOnCloseIconClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(texto);
            });
        }

        return chip;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}