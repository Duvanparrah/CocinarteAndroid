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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
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
    private SessionManager sessionManager;
    private LoginManager loginManager;

    // Variables para cálculo nutricional
    private boolean mostrandoNutricionCalculada = false;
    private int caloriasCalculadas = 0;
    private double proteinasCalculadas = 0.0;
    private double carbohidratosCalculados = 0.0;
    private double grasasCalculadas = 0.0;

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

        // Inicializar managers
        try {
            sessionManager = SessionManager.getInstance(requireContext());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loginManager = new LoginManager(requireContext());

        // ✅ VALIDAR QUE EL USUARIO PUEDE CREAR RECETAS
        if (!validarPermisoCrearReceta()) {
            return;
        }

        // Configurar campos nutricionales
        configurarCamposNutricionales();

        cargarIngredientesDesdeAPI();
        setupEventListeners();
    }

    /**
     * ✅ VALIDAR QUE SOLO USUARIOS REGULARES PUEDEN CREAR RECETAS
     */
    private boolean validarPermisoCrearReceta() {
        try {
            // Verificar en LoginManager
            Usuario usuario = loginManager.getUsuario();
            if (usuario != null) {
                String tipoUsuario = usuario.getTipoUsuario();
                Log.d(TAG, "Usuario desde LoginManager: " + usuario.getNombreUsuario() + " - Tipo: " + tipoUsuario);

                if (!"usuario".equals(tipoUsuario)) {
                    Log.w(TAG, "❌ Usuario tipo '" + tipoUsuario + "' no puede crear recetas en comunidad");
                    mostrarErrorPermisos(tipoUsuario);
                    return false;
                }
                return true;
            }

            // Verificar en SessionManager
            String tipoUsuario = sessionManager.getUserType();
            Log.d(TAG, "Usuario desde SessionManager - Tipo: " + tipoUsuario);

            if (!"usuario".equals(tipoUsuario)) {
                Log.w(TAG, "❌ Usuario tipo '" + tipoUsuario + "' no puede crear recetas en comunidad");
                mostrarErrorPermisos(tipoUsuario);
                return false;
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error al validar permisos: " + e.getMessage());
            Toast.makeText(getContext(), "Error al validar permisos de usuario", Toast.LENGTH_LONG).show();
            Navigation.findNavController(requireView()).popBackStack();
            return false;
        }
    }

    /**
     * ✅ MOSTRAR ERROR DE PERMISOS
     */
    private void mostrarErrorPermisos(String tipoUsuario) {
        String mensaje;
        if ("administrador".equals(tipoUsuario) || "administrador_lider".equals(tipoUsuario)) {
            mensaje = "Los administradores no pueden crear recetas en la sección de comunidad.\n\n" +
                    "Las recetas de administradores se gestionan desde el panel de administración.";
        } else {
            mensaje = "No tienes permisos para crear recetas en esta sección.";
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("🚫 Acceso Restringido")
                .setMessage(mensaje)
                .setPositiveButton("Entendido", (dialog, which) -> {
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setCancelable(false)
                .show();
    }

    private void configurarCamposNutricionales() {
        // Campos de solo lectura pero visibles
        binding.nutritionKcl.setEnabled(false);
        binding.nutritionP.setEnabled(false);
        binding.nutritionC.setEnabled(false);
        binding.nutritionGt.setEnabled(false);

        // Configurar texto de ayuda
        binding.nutritionKcl.setHint("Se calculará automáticamente con IA");
        binding.nutritionP.setHint("Se calculará automáticamente con IA");
        binding.nutritionC.setHint("Se calculará automáticamente con IA");
        binding.nutritionGt.setHint("Se calculará automáticamente con IA");

        // Valores iniciales
        actualizarCamposNutricionales(0, 0.0, 0.0, 0.0);
    }

    private void actualizarCamposNutricionales(int calorias, double proteinas, double carbohidratos, double grasas) {
        binding.nutritionKcl.setText(String.valueOf(calorias));
        binding.nutritionP.setText(String.format("%.1f", proteinas));
        binding.nutritionC.setText(String.format("%.1f", carbohidratos));
        binding.nutritionGt.setText(String.format("%.1f", grasas));

        // Guardar valores calculados
        caloriasCalculadas = calorias;
        proteinasCalculadas = proteinas;
        carbohidratosCalculados = carbohidratos;
        grasasCalculadas = grasas;
        mostrandoNutricionCalculada = true;
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

        // ✅ BOTÓN PUBLICAR RECETA - Solo para usuarios regulares
        binding.btnPublicarReceta.setOnClickListener(v -> publicarRecetaUsuarioRegular());
    }

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
            actualizarHint();
            Toast.makeText(getContext(), "Paso agregado", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Escribe un paso para agregar", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarHint() {
        int siguientePaso = pasosReceta.size() + 1;
        binding.etPaso.setHint(siguientePaso + ". Agrega los pasos de tu receta");
    }

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

    /**
     * ✅ MÉTODO PRINCIPAL: Publicar receta SOLO para usuarios regulares
     */
    private void publicarRecetaUsuarioRegular() {
        String nombre = binding.recipeNameInput.getText().toString().trim();
        String tiempoValor = binding.etTiempo.getText().toString().trim();
        String unidadTiempo = binding.spinnerUnidadTiempo.getSelectedItem().toString();
        String tiempo = tiempoValor + " " + unidadTiempo;
        String dificultad = binding.spinnerDificultad.getSelectedItem().toString();

        // ✅ VALIDACIONES COMPLETAS
        if (nombre.isEmpty() || nombre.length() < 3) {
            Toast.makeText(getContext(), "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_LONG).show();
            return;
        }

        if (tiempoValor.isEmpty()) {
            Toast.makeText(getContext(), "Especifica el tiempo de preparación", Toast.LENGTH_LONG).show();
            return;
        }

        if (ingredientesSeleccionados.isEmpty()) {
            Toast.makeText(getContext(), "Agrega al menos un ingrediente", Toast.LENGTH_LONG).show();
            return;
        }

        if (pasosReceta.isEmpty()) {
            Toast.makeText(getContext(), "Agrega al menos un paso", Toast.LENGTH_LONG).show();
            return;
        }

        if (imagenUriSeleccionada == null) {
            Toast.makeText(getContext(), "Selecciona una imagen", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ VALIDAR USUARIO NUEVAMENTE
        Usuario usuario = obtenerUsuarioActual();
        if (usuario == null) {
            Toast.makeText(getContext(), "Error: No se pudo identificar el usuario", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ VALIDAR QUE SEA USUARIO REGULAR
        String tipoUsuario = usuario.getTipoUsuario();
        if (!"usuario".equals(tipoUsuario)) {
            Log.e(TAG, "❌ Intento de crear receta por usuario no autorizado: " + tipoUsuario);
            Toast.makeText(getContext(), "Error: Solo usuarios regulares pueden crear recetas en comunidad", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "✅ Preparando receta de usuario regular: " + usuario.getNombreUsuario());
        Log.d(TAG, "📝 Pasos: " + pasosReceta.size() + ", Ingredientes: " + ingredientesSeleccionados.size());

        // Mostrar mensaje de procesamiento
        mostrarMensajeProcesamiento();

        // Enviar al servidor
        try {
            enviarRecetaUsuarioRegular(nombre, tiempo, dificultad, usuario);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al procesar la imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarMensajeProcesamiento() {
        Toast.makeText(getContext(),
                "🧠 Calculando valores nutricionales con IA...\n" +
                        "⏳ Tu receta se publicará en la comunidad",
                Toast.LENGTH_LONG).show();
    }

    /**
     * ✅ ENVIAR RECETA ESPECÍFICAMENTE PARA USUARIOS REGULARES
     */
    private void enviarRecetaUsuarioRegular(String nombre, String tiempo, String dificultad, Usuario usuario) throws IOException {
        Log.d(TAG, "🚀 Enviando receta de usuario regular al servidor...");

        String token = obtenerToken();
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(getContext(), "Error de autenticación", Toast.LENGTH_LONG).show();
            return;
        }

        // Preparar archivo de imagen
        File file = createTempFileFromUri(imagenUriSeleccionada);
        if (file == null) {
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear descripción/preparación uniendo los pasos
        String pasosFormateados = String.join("\n", pasosReceta);

        // Crear ingredientes JSON array con IDs
        JSONArray ingredientesJsonArray = new JSONArray();
        for (String nombreIngrediente : ingredientesSeleccionados) {
            for (Ingrediente ing : _ingredientes) {
                if (ing.getNombreIngrediente().equals(nombreIngrediente)) {
                    ingredientesJsonArray.put(ing.getIdIngrediente());
                    break;
                }
            }
        }

        // ✅ CREAR MULTIPART BODY PARTS
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("foto", file.getName(), requestFile);

        RequestBody _nombre = RequestBody.create(MediaType.parse("text/plain"), nombre);
        RequestBody _preparacion = RequestBody.create(MediaType.parse("text/plain"), pasosFormateados);
        RequestBody _tiempo = RequestBody.create(MediaType.parse("text/plain"), tiempo);
        RequestBody _descripcion = RequestBody.create(MediaType.parse("text/plain"), pasosFormateados);
        RequestBody _dificultad = RequestBody.create(MediaType.parse("text/plain"), dificultad);
        RequestBody _ingredientes = RequestBody.create(MediaType.parse("application/json"), ingredientesJsonArray.toString());

        // ✅ LOG DE DATOS ENVIADOS
        Log.d(TAG, "📊 Datos de receta de usuario regular:");
        Log.d(TAG, "   - Título: " + nombre);
        Log.d(TAG, "   - Usuario: " + usuario.getNombreUsuario() + " (ID: " + usuario.getIdUsuario() + ")");
        Log.d(TAG, "   - Tipo: " + usuario.getTipoUsuario());
        Log.d(TAG, "   - Tiempo: " + tiempo);
        Log.d(TAG, "   - Dificultad: " + dificultad);
        Log.d(TAG, "   - Ingredientes: " + ingredientesJsonArray.toString());
        Log.d(TAG, "   - Pasos: " + pasosFormateados.substring(0, Math.min(50, pasosFormateados.length())) + "...");
        Log.d(TAG, "   - Destino: SOLO COMUNIDAD (usuarios regulares)");

        // ✅ USAR ENDPOINT ESPECÍFICO PARA USUARIOS REGULARES
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        recetaApi.crearRecetaUsuario(
                imagenPart,
                _nombre,
                _ingredientes,
                _preparacion,
                _tiempo,
                _dificultad,
                _descripcion,
                "Bearer " + token
        ).enqueue(new Callback<Receta>() {
            @Override
            public void onResponse(Call<Receta> call, Response<Receta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Receta recetaCreada = response.body();

                    Log.d(TAG, "✅ Receta de usuario regular creada exitosamente:");
                    Log.d(TAG, "   - ID: " + recetaCreada.getIdReceta());
                    Log.d(TAG, "   - Título: " + recetaCreada.getTitulo());
                    Log.d(TAG, "   - Sección: " + recetaCreada.getSeccion());
                    Log.d(TAG, "   - Calorías: " + recetaCreada.getCalorias());

                    // ✅ VERIFICAR QUE LA RECETA SE CREÓ CORRECTAMENTE
                    if (recetaCreada.getCreador() != null) {
                        String tipoCreador = recetaCreada.getCreador().getTipo_usuario();
                        Log.d(TAG, "   - Tipo creador: " + tipoCreador);

                        if (!"usuario".equals(tipoCreador)) {
                            Log.w(TAG, "⚠️ ADVERTENCIA: Receta creada con tipo incorrecto: " + tipoCreador);
                        }
                    }

                    // Mostrar información nutricional
                    mostrarNutricionCalculada(recetaCreada);

                    Toast.makeText(getContext(),
                            "🎉 ¡Receta publicada en la comunidad!\n" +
                                    "📊 Valores nutricionales calculados por IA",
                            Toast.LENGTH_LONG).show();

                    // Limpiar formulario
                    limpiarFormulario();

                    // Navegar a mis recetas
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navegar_comunidad_mis_recetas);

                } else {
                    manejarErrorRespuesta(response);
                }
            }

            @Override
            public void onFailure(Call<Receta> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión al crear receta de usuario", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarNutricionCalculada(Receta receta) {
        // Crear mensaje informativo con los valores calculados
        String mensajeNutricional = String.format(
                "📊 Valores nutricionales calculados:\n\n" +
                        "🔥 Calorías: %d kcal\n" +
                        "🥩 Proteínas: %.1f g\n" +
                        "🍞 Carbohidratos: %.1f g\n" +
                        "🥑 Grasas: %.1f g\n\n" +
                        "✅ Tu receta aparecerá SOLO en la sección de Comunidad",
                receta.getCalorias(),
                receta.getProteinas(),
                receta.getCarbohidratos(),
                receta.getGrasas()
        );

        // Mostrar en la UI actual
        actualizarCamposNutricionales(
                receta.getCalorias(),
                receta.getProteinas(),
                receta.getCarbohidratos(),
                receta.getGrasas()
        );

        Log.d(TAG, mensajeNutricional);

        // Mostrar dialog informativo
        new AlertDialog.Builder(getContext())
                .setTitle("🎉 Receta Publicada en Comunidad")
                .setMessage(mensajeNutricional)
                .setPositiveButton("¡Perfecto!", null)
                .show();
    }

    private void agregarIngredienteASeleccionados(String nombreIngrediente) {
        if (!ingredientesSeleccionados.contains(nombreIngrediente)) {
            ingredientesSeleccionados.add(nombreIngrediente);
            Toast.makeText(getContext(), nombreIngrediente + " agregado", Toast.LENGTH_SHORT).show();

            TextView chip = crearChipVisual(nombreIngrediente, true);
            chip.setOnClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(nombreIngrediente);
            });

            binding.listaIngredientes.addView(chip);
        }
    }

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

        // Limpiar campos nutricionales
        actualizarCamposNutricionales(0, 0.0, 0.0, 0.0);
        mostrandoNutricionCalculada = false;
    }

    private void manejarErrorRespuesta(Response<Receta> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Respuesta vacía";
            Log.e(TAG, "❌ Error HTTP " + response.code() + ": " + errorBody);

            String mensajeClaro;
            switch (response.code()) {
                case 400:
                    if (errorBody.contains("ingredientes")) {
                        mensajeClaro = "Error con los ingredientes. Verifica que sean válidos.";
                    } else if (errorBody.contains("usuario")) {
                        mensajeClaro = "Error: Solo usuarios regulares pueden crear recetas en comunidad.";
                    } else {
                        mensajeClaro = "Datos inválidos. Verifica todos los campos.";
                    }
                    break;
                case 401:
                    mensajeClaro = "Error de autenticación. Inicia sesión nuevamente.";
                    break;
                case 403:
                    mensajeClaro = "Sin permisos. Solo usuarios regulares pueden crear recetas.";
                    break;
                case 413:
                    mensajeClaro = "Imagen demasiado grande. Usa una imagen más pequeña.";
                    break;
                case 422:
                    mensajeClaro = "Error al calcular valores nutricionales. Revisa los ingredientes.";
                    break;
                case 500:
                    mensajeClaro = "Error del servidor. Intenta nuevamente en unos minutos.";
                    break;
                default:
                    mensajeClaro = "Error del servidor (" + response.code() + "). Intenta nuevamente.";
            }

            Toast.makeText(getContext(), mensajeClaro, Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e(TAG, "❌ Error al leer respuesta de error", e);
            Toast.makeText(getContext(), "Error al procesar respuesta del servidor", Toast.LENGTH_LONG).show();
        }
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            String fileName = "receta_usuario_" + System.currentTimeMillis() + ".jpg";
            File tempFile = new File(getContext().getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "✅ Archivo temporal creado: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "❌ Error al crear archivo temporal", e);
            return null;
        }
    }

    private Usuario obtenerUsuarioActual() {
        try {
            // Intentar con LoginManager primero
            Usuario usuario = loginManager.getUsuario();
            if (usuario != null) {
                Log.d(TAG, "Usuario obtenido desde LoginManager: " + usuario.getNombreUsuario());
                return usuario;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener usuario desde LoginManager: " + e.getMessage());
        }

        try {
            // Fallback a SessionManager
            if (sessionManager != null && sessionManager.isLoggedIn() && sessionManager.getUserId() != -1) {
                Log.d(TAG, "Usuario obtenido desde SessionManager");
                Usuario usuario = new Usuario();

                try {
                    int userId = sessionManager.getUserId();
                    usuario.setIdUsuario(userId);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error al convertir userId: " + e.getMessage());
                    return null;
                }

                if (sessionManager.getUserName() != null) {
                    usuario.setNombreUsuario(sessionManager.getUserName());
                }

                if (sessionManager.getUserType() != null) {
                    usuario.setTipoUsuario(sessionManager.getUserType());
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
        Log.d(TAG, "🥕 Cargando ingredientes para usuario regular...");

        String token = obtenerToken();
        if (token == null || token.trim().isEmpty()) {
            Log.e(TAG, "No se pudo obtener token para cargar ingredientes");
            Toast.makeText(getContext(), "Error de autenticación", Toast.LENGTH_SHORT).show();
            return;
        }

        IngredientesService service = ApiClient.getClient(getContext()).create(IngredientesService.class);

        service.obtenerTodosLosIngredientes("Bearer " + token).enqueue(new Callback<IngredientesByCategoriaResponse>() {
            @Override
            public void onResponse(@NonNull Call<IngredientesByCategoriaResponse> call, @NonNull Response<IngredientesByCategoriaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IngredientesByCategoriaResponse responseBody = response.body();

                    if (responseBody.hasIngredientes()) {
                        _ingredientes.clear();
                        _ingredientes.addAll(responseBody.getIngredientes());
                        Log.d(TAG, "✅ Ingredientes cargados para usuario regular: " + _ingredientes.size());

                        Toast.makeText(getContext(),
                                "Ingredientes cargados: " + _ingredientes.size() +
                                        "\n🧠 Listos para cálculo nutricional",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "⚠️ No se encontraron ingredientes");
                        Toast.makeText(getContext(), "No se encontraron ingredientes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error al obtener ingredientes: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar ingredientes", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<IngredientesByCategoriaResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al cargar ingredientes", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private String obtenerToken() {
        try {
            // Intentar con LoginManager primero
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

    // MÉTODOS EXISTENTES PARA INTERFAZ...
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
        String nombreArchivo = "receta_usuario_" + System.currentTimeMillis();
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