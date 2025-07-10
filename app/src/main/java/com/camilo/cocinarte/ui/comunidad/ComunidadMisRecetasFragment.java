package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentComunidadMisRecetasBinding;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadMisRecetasFragment extends Fragment {
    private static final String TAG = "MisRecetasFragment";

    private FragmentComunidadMisRecetasBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadMisRecetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        cargarDatosUsuario();
        cargarMisRecetasConNutricion(); // ✅ MÉTODO ACTUALIZADO
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());
        binding.menuButton.setOnClickListener(v -> openMenu());

        binding.comunidadTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_navegar_comunidad));

        binding.misRecetasTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Mis recetas", Toast.LENGTH_SHORT).show());

        binding.btnCrearReceta.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_crearRecetaFragment));
    }

    // ✅ MÉTODO CORREGIDO CON SessionManager REAL Y CARGA DE FOTO
    private void cargarDatosUsuario() {
        Log.d(TAG, "📱 Cargando datos del usuario...");

        // ✅ INTENTAR PRIMERO LoginManager, LUEGO SessionManager como fallback
        LoginManager loginManager = new LoginManager(requireContext());
        Usuario usuario = loginManager.getUsuario();
        String fotoPerfil = null;

        if (usuario != null) {
            // ✅ DATOS ENCONTRADOS EN LoginManager
            binding.userEmail.setText(usuario.getCorreo());
            binding.userName.setText(usuario.getNombreUsuario());
            fotoPerfil = usuario.getFotoPerfil();

            Log.d(TAG, "✅ Datos del usuario cargados desde LoginManager:");
            Log.d(TAG, "   - ID: " + usuario.getIdUsuario());
            Log.d(TAG, "   - Email: " + usuario.getCorreo());
            Log.d(TAG, "   - Nombre: " + usuario.getNombreUsuario());
            Log.d(TAG, "   - Foto: " + fotoPerfil);
        } else {
            // ✅ FALLBACK: Usar SessionManager si LoginManager está vacío
            Log.w(TAG, "⚠️ LoginManager vacío, intentando SessionManager...");

            try {
                SessionManager sessionManager = SessionManager.getInstance(requireContext());

                // ✅ USAR MÉTODOS CORRECTOS DE SessionManager
                String email = sessionManager.getEmail();
                String nombre = sessionManager.getUserName();
                String userId = String.valueOf(sessionManager.getUserId());
                String token = sessionManager.getAuthToken();
                fotoPerfil = sessionManager.getUserPhoto();

                if (email != null && nombre != null && userId != null && token != null) {
                    // ✅ TRANSFERIR DATOS DE SessionManager A LoginManager
                    Log.d(TAG, "📋 Transfiriendo datos de SessionManager a LoginManager...");

                    // Crear objeto UserData temporal para transferir
                    LoginResponse.UserData userData = new LoginResponse.UserData();
                    userData.setId(Integer.parseInt(userId));
                    userData.setEmail(email);
                    userData.setNombre(nombre);
                    userData.setFoto(fotoPerfil);
                    userData.setTipo_usuario(sessionManager.getUserType());

                    // Guardar en LoginManager
                    loginManager.saveToken(token);
                    loginManager.saveUser(userData);

                    // Mostrar en interfaz
                    binding.userEmail.setText(email);
                    binding.userName.setText(nombre);

                    Log.d(TAG, "✅ Datos transferidos y mostrados:");
                    Log.d(TAG, "   - ID: " + userId);
                    Log.d(TAG, "   - Email: " + email);
                    Log.d(TAG, "   - Nombre: " + nombre);
                    Log.d(TAG, "   - Foto: " + fotoPerfil);

                } else {
                    // ✅ NO HAY DATOS EN NINGÚN LADO
                    binding.userEmail.setText("Correo no disponible");
                    binding.userName.setText("Usuario desconocido");

                    Log.e(TAG, "❌ No se encontraron datos en SessionManager tampoco");
                    Log.d(TAG, "SessionManager datos:");
                    Log.d(TAG, "   - Email: " + email);
                    Log.d(TAG, "   - Nombre: " + nombre);
                    Log.d(TAG, "   - UserID: " + userId);
                    Log.d(TAG, "   - Token: " + (token != null ? "presente" : "ausente"));

                    Toast.makeText(getContext(), "Error: Sesión expirada, inicia sesión nuevamente", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Error al acceder a SessionManager: " + e.getMessage());
                binding.userEmail.setText("Error de sesión");
                binding.userName.setText("Reinicia la app");
            }
        }

        // ✅ CARGAR FOTO DE PERFIL
        cargarFotoPerfilUsuario(fotoPerfil);
    }

    // ✅ NUEVO MÉTODO: Cargar foto de perfil del usuario
    private void cargarFotoPerfilUsuario(String fotoPerfil) {
        // Primero intentar cargar imagen guardada localmente
        SharedPreferences preferences = requireContext()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedImageUri = preferences.getString("profile_image_uri", null);

        if (savedImageUri != null) {
            try {
                cargarImagenPerfil(Uri.parse(savedImageUri));
                Log.d(TAG, "✅ Imagen de perfil cargada desde preferencias locales");
                return; // Si carga local exitosa, no cargar desde servidor
            } catch (Exception e) {
                Log.w(TAG, "⚠️ Error al cargar imagen local: " + e.getMessage());
            }
        }

        // Si no hay imagen local, cargar desde servidor
        if (fotoPerfil != null && !fotoPerfil.trim().isEmpty() && !fotoPerfil.equals("null")) {
            Log.d(TAG, "🌐 Cargando foto de perfil desde servidor: " + fotoPerfil);

            Glide.with(this)
                    .load(fotoPerfil)
                    .circleCrop()
                    .placeholder(R.drawable.perfil)
                    .error(R.drawable.perfil)
                    .into(binding.userProfileImage);

            Log.d(TAG, "✅ Foto de perfil del usuario cargada desde servidor");
        } else {
            // Usar imagen por defecto
            binding.userProfileImage.setImageResource(R.drawable.perfil);
            Log.d(TAG, "📷 Usando imagen de perfil por defecto");
        }
    }

    private void cargarImagenPerfil(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
                .into(binding.userProfileImage);
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(getContext(), "Buscando en mis recetas: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();
    }

    // ✅ MÉTODO PRINCIPAL ACTUALIZADO: Cargar mis recetas CON nutrición calculada
    private void cargarMisRecetasConNutricion() {
        Log.d(TAG, "📋 Cargando MIS recetas con valores nutricionales calculados por IA");

        // ✅ OBTENER USUARIO Y TOKEN DE MANERA ROBUSTA
        LoginManager loginManager = new LoginManager(getContext());
        Usuario usuarioInicial = loginManager.getUsuario();
        String tokenInicial = loginManager.getToken();

        // ✅ Variables finales para usar en callbacks
        final Usuario[] usuarioFinal = {usuarioInicial};
        final String[] tokenFinal = {tokenInicial};

        // ✅ SI NO HAY DATOS EN LoginManager, OBTENER DE SessionManager
        if (usuarioFinal[0] == null || tokenFinal[0] == null) {
            Log.w(TAG, "⚠️ Datos faltantes en LoginManager, obteniendo de SessionManager...");

            try {
                SessionManager sessionManager = SessionManager.getInstance(getContext());

                // ✅ USAR MÉTODOS CORRECTOS DE SessionManager
                String userId = String.valueOf(sessionManager.getUserId());
                String token = sessionManager.getAuthToken();

                if (userId != null && token != null) {
                    // Crear usuario temporal
                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(Integer.parseInt(userId));
                    usuario.setCorreo(sessionManager.getEmail());
                    usuario.setNombreUsuario(sessionManager.getUserName());

                    usuarioFinal[0] = usuario;
                    tokenFinal[0] = token;

                    Log.d(TAG, "✅ Datos obtenidos de SessionManager para cargar recetas");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Error al obtener datos de SessionManager: " + e.getMessage());
            }
        }

        // ✅ VALIDAR QUE TENEMOS LOS DATOS NECESARIOS
        if (usuarioFinal[0] == null) {
            Log.e(TAG, "❌ No se pudo obtener el usuario actual de ninguna fuente");
            Toast.makeText(getContext(), "Error: No se pudo identificar el usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tokenFinal[0] == null || tokenFinal[0].trim().isEmpty()) {
            Log.e(TAG, "❌ Token de autenticación no encontrado en ninguna fuente");
            Toast.makeText(getContext(), "Error de autenticación, inicia sesión nuevamente", Toast.LENGTH_SHORT).show();
            return;
        }

        final int idUsuarioActual = usuarioFinal[0].getIdUsuario();
        Log.d(TAG, "👤 Cargando recetas del usuario ID: " + idUsuarioActual);

        // ✅ USAR EL NUEVO ENDPOINT ESPECÍFICO PARA MIS RECETAS CON NUTRICIÓN
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        // ✅ INTENTAR PRIMERO EL ENDPOINT ESPECÍFICO /recetas/usuario (CON NUTRICIÓN)
        recetaApi.obtenerMisRecetas("Bearer " + tokenFinal[0]).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> misRecetas = response.body();
                    Log.d(TAG, "✅ MIS recetas con NUTRICIÓN cargadas desde endpoint específico: " + misRecetas.size());

                    // ✅ VERIFICAR QUE TODAS TIENEN VALORES NUTRICIONALES
                    int recetasConNutricion = 0;
                    for (Receta receta : misRecetas) {
                        if (receta.getCalorias() > 0) {
                            recetasConNutricion++;
                            Log.d(TAG, "✅ " + receta.getTitulo() + " - Cal: " + receta.getCalorias() +
                                    ", Prot: " + receta.getProteinas() + "g");
                        }
                    }

                    Log.d(TAG, "📊 Mis recetas con nutrición: " + recetasConNutricion + "/" + misRecetas.size());

                    mostrarRecetasConNutricion(misRecetas);
                } else {
                    Log.w(TAG, "⚠️ Endpoint /recetas/usuario no disponible (código " + response.code() + "), usando método alternativo");
                    // Fallback: usar endpoint general y filtrar
                    cargarMisRecetasConFiltro(idUsuarioActual, tokenFinal[0]);
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.w(TAG, "⚠️ Error en endpoint /recetas/usuario: " + t.getMessage() + ", usando método alternativo");
                // Fallback: usar endpoint general y filtrar
                cargarMisRecetasConFiltro(idUsuarioActual, tokenFinal[0]);
            }
        });
    }

    // ✅ MÉTODO FALLBACK: Usar endpoint general y filtrar por usuario
    private void cargarMisRecetasConFiltro(int idUsuarioActual, String token) {
        Log.d(TAG, "🔄 Usando endpoint general y filtrando por usuario ID: " + idUsuarioActual);

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        Call<List<Receta>> call = recetaApi.obtenerTodasLasRecetasConAuth("Bearer " + token);

        call.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> todasLasRecetas = response.body();
                    List<Receta> recetasDelUsuario = new ArrayList<>();

                    // ✅ FILTRAR SOLO LAS RECETAS DEL USUARIO ACTUAL
                    for (Receta receta : todasLasRecetas) {
                        if (receta.getIdUsuario() == idUsuarioActual) {
                            recetasDelUsuario.add(receta);
                        }
                    }

                    Log.d(TAG, "✅ Filtrado completado - Total: " + todasLasRecetas.size() +
                            ", Mis recetas: " + recetasDelUsuario.size());

                    mostrarRecetasConNutricion(recetasDelUsuario);
                } else {
                    Log.e(TAG, "❌ Error al cargar recetas: " + response.code());
                    Toast.makeText(getContext(), "Error al cargar tus recetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.e(TAG, "❌ Error de conexión: " + t.getMessage());
                Toast.makeText(getContext(), "Error de conexión al cargar tus recetas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ MÉTODO ACTUALIZADO: Mostrar las recetas CON información nutricional en la interfaz
    private void mostrarRecetasConNutricion(List<Receta> recetas) {
        LinearLayout contenedorPrincipal = binding.contenedorRecetas;
        contenedorPrincipal.removeAllViews();
        contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);

        if (recetas.isEmpty()) {
            // Mostrar mensaje cuando no hay recetas
            TextView mensajeVacio = new TextView(getContext());
            mensajeVacio.setText("¡Aún no tienes recetas!\n\n" +
                    "Toca el botón 'CREAR RECETA' para empezar a compartir tus deliciosas creaciones.\n\n" +
                    "📊 Todas tus recetas incluirán valores nutricionales calculados automáticamente con IA.");
            mensajeVacio.setTextSize(16);
            mensajeVacio.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            mensajeVacio.setPadding(32, 64, 32, 64);
            mensajeVacio.setTextColor(getResources().getColor(android.R.color.darker_gray));
            contenedorPrincipal.addView(mensajeVacio);

            Log.d(TAG, "📝 Mostrando mensaje de recetas vacías");
            return;
        }

        // Calcular dimensiones para layout en grid
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int mitadAncho = screenWidth / 2;

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // ✅ CREAR LAYOUT EN GRID DE 2 COLUMNAS CON INFORMACIÓN NUTRICIONAL
        for (int i = 0; i < recetas.size(); i += 2) {
            LinearLayout fila = new LinearLayout(getContext());
            fila.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            fila.setOrientation(LinearLayout.HORIZONTAL);

            // Primera receta de la fila
            View item1 = inflarItemRecetaConNutricion(inflater, fila, recetas.get(i), mitadAncho);
            fila.addView(item1);

            // Segunda receta de la fila (si existe)
            if (i + 1 < recetas.size()) {
                View item2 = inflarItemRecetaConNutricion(inflater, fila, recetas.get(i + 1), mitadAncho);
                fila.addView(item2);
            }

            contenedorPrincipal.addView(fila);
        }

        // ✅ MOSTRAR ESTADÍSTICAS NUTRICIONALES
        int recetasConNutricion = 0;
        for (Receta receta : recetas) {
            if (receta.getCalorias() > 0) {
                recetasConNutricion++;
            }
        }

        String mensaje = "✅ " + recetas.size() + " recetas cargadas";
        if (recetasConNutricion > 0) {
            mensaje += "\n📊 " + recetasConNutricion + " con valores nutricionales calculados por IA";
        }

        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

        Log.d(TAG, "✅ " + recetas.size() + " recetas mostradas en interfaz (" + recetasConNutricion + " con nutrición)");
    }

    // ✅ MÉTODO ACTUALIZADO: Crear cada item de receta CON información nutricional
    private View inflarItemRecetaConNutricion(LayoutInflater inflater, ViewGroup parent, Receta receta, int ancho) {
        // ✅ USAR EL LAYOUT NORMAL SI NO EXISTE EL LAYOUT CON NUTRICIÓN
        View item;
        try {
            item = inflater.inflate(R.layout.item_receta_con_nutricion, parent, false);
        } catch (Exception e) {
            // Fallback al layout normal si no existe el layout con nutrición
            Log.w(TAG, "Layout item_receta_con_nutricion no encontrado, usando layout normal");
            item = inflater.inflate(R.layout.item_receta, parent, false);
        }

        // Configurar ancho del item
        ViewGroup.LayoutParams params = item.getLayoutParams();
        params.width = ancho;
        item.setLayoutParams(params);

        // Encontrar vistas del item
        ImageView ivImagen = item.findViewById(R.id.iv_imagen_receta);
        TextView tvNombre = item.findViewById(R.id.tv_nombre_receta);

        // ✅ NUEVOS CAMPOS PARA INFORMACIÓN NUTRICIONAL (pueden no existir en el layout normal)
        TextView tvCalorias = item.findViewById(R.id.tv_calorias);
        TextView tvProteinas = item.findViewById(R.id.tv_proteinas);
        TextView tvCarbohidratos = item.findViewById(R.id.tv_carbohidratos);
        TextView tvGrasas = item.findViewById(R.id.tv_grasas);

        // Cargar imagen de la receta
        if (ivImagen != null) {
            Glide.with(this)
                    .load(receta.getImagen())
                    .centerCrop()
                    .placeholder(R.drawable.temp_plato)
                    .error(R.drawable.temp_plato)
                    .into(ivImagen);
        }

        // Establecer nombre de la receta
        if (tvNombre != null) {
            tvNombre.setText(receta.getTitulo());
        }

        // ✅ CONFIGURAR INFORMACIÓN NUTRICIONAL SI ESTÁ DISPONIBLE Y LOS CAMPOS EXISTEN
        if (receta.getCalorias() > 0) {
            // Mostrar valores nutricionales calculados por IA
            if (tvCalorias != null) {
                tvCalorias.setText(receta.getCalorias() + " kcal");
                tvCalorias.setVisibility(View.VISIBLE);
            }

            if (tvProteinas != null) {
                tvProteinas.setText(String.format("%.1fg P", receta.getProteinas()));
                tvProteinas.setVisibility(View.VISIBLE);
            }

            if (tvCarbohidratos != null) {
                tvCarbohidratos.setText(String.format("%.1fg C", receta.getCarbohidratos()));
                tvCarbohidratos.setVisibility(View.VISIBLE);
            }

            if (tvGrasas != null) {
                tvGrasas.setText(String.format("%.1fg G", receta.getGrasas()));
                tvGrasas.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "📊 " + receta.getTitulo() + " - Nutrición: " + receta.getCalorias() + " kcal, " +
                    receta.getProteinas() + "g P, " + receta.getCarbohidratos() + "g C, " + receta.getGrasas() + "g G");
        } else {
            // Ocultar campos nutricionales si no hay datos
            if (tvCalorias != null) tvCalorias.setVisibility(View.GONE);
            if (tvProteinas != null) tvProteinas.setVisibility(View.GONE);
            if (tvCarbohidratos != null) tvCarbohidratos.setVisibility(View.GONE);
            if (tvGrasas != null) tvGrasas.setVisibility(View.GONE);

            Log.w(TAG, "⚠️ " + receta.getTitulo() + " - Sin datos nutricionales");
        }

        // ✅ CONFIGURAR CLICK PARA NAVEGAR AL DETALLE
        item.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id_receta", receta.getIdReceta());
            bundle.putString("origen", "mis_recetas"); // ✅ IMPORTANTE: Indicar origen

            Log.d(TAG, "📱 Navegando al detalle de receta: " + receta.getTitulo() + " (ID: " + receta.getIdReceta() + ")");

            Navigation.findNavController(v)
                    .navigate(R.id.action_navegar_comunidad_mis_recetas_to_detalleRecetaFragment, bundle);
        });

        return item;
    }

    // ✅ MÉTODO PÚBLICO ACTUALIZADO: Recargar recetas con nutrición (llamado después de crear una)
    public void recargarMisRecetas() {
        Log.d(TAG, "🔄 Recargando mis recetas con nutrición después de crear una nueva");
        cargarMisRecetasConNutricion();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumido, recargando mis recetas con nutrición");
        // Recargar las recetas cada vez que se regresa a este fragment
        cargarMisRecetasConNutricion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}