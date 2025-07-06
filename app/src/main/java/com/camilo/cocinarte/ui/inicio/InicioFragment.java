package com.camilo.cocinarte.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.DetalleRecetaActivity;
import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private static final String TAG = "InicioFragment";

    // Views básicas
    private ImageButton menuButton;
    private ImageButton searchButton;
    private EditText searchEditText;
    private DrawerLayout drawerLayout;
    private TextView welcomeMessage;
    private ProgressBar progressBar;

    // Solo el RecyclerView de recetas de la web
    private RecyclerView webRecipesRecycler;

    // Solo un adapter
    private RecetasAdapter webRecipesAdapter;

    // Services
    private AuthService authService;
    private RecetaApi recetaApi;
    private SessionManager sessionManager;

    // Data - Solo recetas de la web
    private List<Receta> todasLasRecetas = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Inicializar vistas
        initViews(root);

        // Configurar API y SessionManager
        setupApi();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        // Configurar navegación entre fragments
        setupNavigationListeners(root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar vistas
        setupViews(view);

        // ✅ CARGAR RECETAS INMEDIATAMENTE (SIN VERIFICAR TOKEN)
        cargarRecetasInicio();
    }

    private void initViews(View root) {
        menuButton = root.findViewById(R.id.menu_button);
        searchButton = root.findViewById(R.id.search_button);
        searchEditText = root.findViewById(R.id.search_edit_text);
        welcomeMessage = root.findViewById(R.id.welcome_message);

        // ✅ NO HAY PROGRESSBAR EN EL LAYOUT - No inicializar
        progressBar = null;

        // Solo el RecyclerView de recetas de la web
        webRecipesRecycler = root.findViewById(R.id.web_recipes_recycler);

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }
    }

    private void setupViews(View view) {
        // Método auxiliar para configurar vistas adicionales si es necesario
        // Mostrar mensaje de bienvenida
        displayWelcomeMessage();
    }

    private void setupApi() {
        authService = ApiClient.getClient(requireContext()).create(AuthService.class);
        recetaApi = ApiClient.getClient(requireContext()).create(RecetaApi.class);
        sessionManager = SessionManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        // ✅ CONFIGURAR EL ADAPTER CON EL FLAG DE INICIO (CAMBIO PRINCIPAL)
        webRecipesAdapter = new RecetasAdapter(getContext(), todasLasRecetas, new RecetasAdapter.OnRecetaClickListener() {
            @Override
            public void onRecetaClick(Receta receta) {
                abrirDetalleReceta(receta);
            }
        }, true); // ✅ TRUE = ES PANTALLA DE INICIO

        // ✅ USAR LINEARLAYOUTMANAGER PARA FEED ESTILO INSTAGRAM
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        webRecipesRecycler.setLayoutManager(layoutManager);

        webRecipesRecycler.setAdapter(webRecipesAdapter);
        webRecipesRecycler.setHasFixedSize(true);
    }

    private void setupListeners() {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            } else {
                Toast.makeText(getContext(), "Ingresa algo para buscar", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
                return true;
            }
            return false;
        });
    }

    // ✅ FUNCIÓN PARA CONFIGURAR NAVEGACIÓN ENTRE FRAGMENTS
    private void setupNavigationListeners(View root) {
        // La navegación principal se maneja mediante el Bottom Navigation
        // Estos métodos pueden ser llamados programáticamente

        // Ejemplo: Si tienes algún botón específico en el layout
        // View banquetesButton = root.findViewById(R.id.btn_banquetes_especial);
        // if (banquetesButton != null) {
        //     banquetesButton.setOnClickListener(v -> navigateToFragment(R.id.navigation_banquetes));
        // }
    }

    // ✅ MÉTODOS DE NAVEGACIÓN USANDO MAINACTIVITY
    public void navigateToFragment(int fragmentId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(fragmentId);
        }
    }

    public void navigateToBanquetes() {
        navigateToFragment(R.id.navigation_banquetes);
    }

    public void navigateToNutricion() {
        navigateToFragment(R.id.navigation_nutricion);
    }

    public void navigateToComunidad() {
        navigateToFragment(R.id.navegar_comunidad);
    }

    // ✅ MÉTODO PRINCIPAL PARA CARGAR RECETAS DE INICIO
    private void cargarRecetasInicio() {
        Log.d(TAG, "🏠 Cargando recetas para pantalla de INICIO...");
        Log.d(TAG, "👑 Solo recetas de administradores");

        // Mostrar indicador de carga
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (getContext() == null) {
            Log.e(TAG, "❌ Context es null, no se puede cargar");
            return;
        }

        try {
            // ✅ USAR EL NUEVO ENDPOINT ESPECÍFICO PARA INICIO (SIN TOKEN REQUERIDO)
            RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

            // ✅ LLAMADA SIN AUTENTICACIÓN REQUERIDA
            Call<List<Receta>> call = recetaApi.obtenerRecetasInicio();

            call.enqueue(new Callback<List<Receta>>() {
                @Override
                public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
                    // ✅ VERIFICAR SI EL FRAGMENT SIGUE ACTIVO
                    if (getContext() == null || !isAdded()) {
                        Log.w(TAG, "Fragment no está activo, cancelando actualización");
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        List<Receta> recetas = response.body();
                        Log.d(TAG, "✅ Recetas de INICIO obtenidas: " + recetas.size());

                        // ✅ ACTUALIZAR EN EL HILO PRINCIPAL
                        requireActivity().runOnUiThread(() -> {
                            mostrarRecetasEnPantalla(recetas);
                        });
                    } else {
                        Log.e(TAG, "❌ Error en respuesta: " + response.code());
                        Log.e(TAG, "❌ Mensaje: " + response.message());

                        requireActivity().runOnUiThread(() -> {
                            mostrarMensajeError("No se pudieron cargar las recetas (Código: " + response.code() + ")");
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Error de conexión al cargar recetas de INICIO: " + t.getMessage());

                    // ✅ VERIFICAR SI EL FRAGMENT SIGUE ACTIVO
                    if (getContext() == null || !isAdded()) {
                        Log.w(TAG, "Fragment no está activo, cancelando manejo de error");
                        return;
                    }

                    requireActivity().runOnUiThread(() -> {
                        mostrarMensajeError("Error de conexión. Verifica tu internet.");
                    });
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inesperado al cargar recetas: " + e.getMessage());
            mostrarMensajeError("Error inesperado");
        }
    }

    // ✅ MÉTODO PARA MOSTRAR RECETAS EN LA INTERFAZ
    private void mostrarRecetasEnPantalla(List<Receta> recetas) {
        if (recetas == null || recetas.isEmpty()) {
            Log.w(TAG, "⚠️ No hay recetas de administradores para mostrar");
            // Mostrar mensaje de "no hay contenido"
            mostrarMensajeVacio();
            return;
        }

        Log.d(TAG, "📱 Mostrando " + recetas.size() + " recetas de administradores en INICIO");

        // ✅ FILTRAR SOLO RECETAS DE ADMINISTRADORES (EXTRA SEGURIDAD)
        List<Receta> recetasAdmin = new ArrayList<>();
        for (Receta receta : recetas) {
            // Verificar si es de administrador
            if (receta.esDeAdministrador()) {
                recetasAdmin.add(receta);
                Log.d(TAG, "✅ Receta de admin añadida: " + receta.getTitulo());
            } else {
                Log.d(TAG, "⚠️ Receta de usuario regular filtrada: " + receta.getTitulo());
            }
        }

        // Actualizar la lista de recetas
        todasLasRecetas.clear();
        todasLasRecetas.addAll(recetasAdmin); // Usar la lista filtrada

        // Notificar al adapter
        if (webRecipesAdapter != null) {
            webRecipesAdapter.notifyDataSetChanged();
            Log.d(TAG, "🔄 Adapter notificado con " + todasLasRecetas.size() + " recetas");
        }

        // Ocultar indicadores de carga
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Mostrar mensaje de éxito
        Toast.makeText(getContext(), "Recetas cargadas: " + recetasAdmin.size(), Toast.LENGTH_SHORT).show();
    }

    // ✅ MÉTODOS AUXILIARES
    private void mostrarMensajeError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }

        // Ocultar indicadores de carga
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        Log.e(TAG, "Error mostrado al usuario: " + mensaje);
    }

    private void mostrarMensajeVacio() {
        // Mostrar un mensaje de que no hay contenido
        if (getContext() != null) {
            Toast.makeText(getContext(), "No hay recetas disponibles en este momento", Toast.LENGTH_SHORT).show();
        }

        // Ocultar indicadores de carga
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        Log.w(TAG, "No hay recetas para mostrar en inicio");
    }

    private void displayWelcomeMessage() {
        // ✅ VERIFICAR QUE WELCOMEMESSAGE NO SEA NULL
        if (welcomeMessage == null) {
            Log.w(TAG, "welcomeMessage es null, no se puede mostrar mensaje");
            return;
        }

        // Primero intentar con SessionManager
        if (sessionManager != null) {
            SessionManager.SessionData sessionData = sessionManager.getSessionData();
            if (sessionData != null && sessionData.userName != null && !sessionData.userName.isEmpty()) {
                welcomeMessage.setText("¡Bienvenido de nuevo, " + sessionData.userName + "!");
                return;
            }
        }

        // Si no hay datos en SessionManager, obtenerlos del servidor
        fetchAndDisplayUserName();
    }

    private void fetchAndDisplayUserName() {
        if (welcomeMessage == null) {
            Log.w(TAG, "welcomeMessage es null en fetchAndDisplayUserName");
            return;
        }

        if (sessionManager == null || sessionManager.getAuthToken() == null) {
            welcomeMessage.setText("¡Bienvenido de nuevo!");
            return;
        }

        String token = "Bearer " + sessionManager.getAuthToken();

        Call<Usuario> call = authService.getUserProfile(token);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                // ✅ VERIFICAR SI EL FRAGMENT SIGUE ACTIVO
                if (getContext() == null || !isAdded() || welcomeMessage == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();
                    String nombreUsuario = usuario.getNombreUsuario();

                    if (nombreUsuario != null && !nombreUsuario.isEmpty()) {
                        welcomeMessage.setText("¡Bienvenido de nuevo, " + nombreUsuario + "!");

                        // Actualizar SessionManager con los datos del usuario
                        if (sessionManager != null) {
                            sessionManager.saveUserInfo(
                                    String.valueOf(usuario.getIdUsuario()),
                                    nombreUsuario,
                                    usuario.getFotoPerfil(),
                                    usuario.getTipoUsuario(),
                                    true
                            );
                        }
                    } else {
                        welcomeMessage.setText("¡Bienvenido de nuevo!");
                    }
                } else {
                    Log.e(TAG, "Error al obtener el perfil del usuario: " + response.code() + " - " + response.message());
                    welcomeMessage.setText("¡Bienvenido de nuevo!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener perfil del usuario: ", t);
                if (welcomeMessage != null) {
                    welcomeMessage.setText("¡Bienvenido de nuevo!");
                }
            }
        });
    }

    private void performSearch(String query) {
        Log.d(TAG, "Realizando búsqueda: " + query);

        // ✅ IMPLEMENTAR BÚSQUEDA LOCAL EN LAS RECETAS CARGADAS
        if (todasLasRecetas.isEmpty()) {
            Toast.makeText(getContext(), "No hay recetas cargadas para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Receta> resultados = new ArrayList<>();
        for (Receta receta : todasLasRecetas) {
            if (receta.getTitulo() != null &&
                    receta.getTitulo().toLowerCase().contains(query.toLowerCase())) {
                resultados.add(receta);
            }
        }

        if (resultados.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron recetas con: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Encontradas " + resultados.size() + " recetas", Toast.LENGTH_SHORT).show();
            // ✅ ACTUALIZAR ADAPTER CON RESULTADOS
            if (webRecipesAdapter != null) {
                webRecipesAdapter.updateRecetas(resultados);
            }
        }

        // Ocultar teclado
        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    // ✅ MÉTODO ACTUALIZADO: Añadir flag para indicar que viene de inicio
    private void abrirDetalleReceta(Receta receta) {
        Log.d(TAG, "Abriendo detalle de receta: " + receta.getTitulo());

        Intent intent = new Intent(getContext(), DetalleRecetaActivity.class);
        intent.putExtra("receta_id", receta.getIdReceta());

        // ✅ AÑADIR FLAG PARA INDICAR QUE VIENE DE INICIO
        intent.putExtra("from_inicio", true);

        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumed - Recargando recetas de INICIO");

        // ✅ SOLO RECARGAR SI NO HAY RECETAS CARGADAS (EVITAR LLAMADAS EXCESIVAS)
        if (todasLasRecetas.isEmpty()) {
            cargarRecetasInicio();
        } else {
            Log.d(TAG, "📋 Ya hay " + todasLasRecetas.size() + " recetas cargadas, no recargando");
        }

        // ✅ ACTUALIZAR DATOS DE REACCIONES cuando se regresa del detalle
        if (webRecipesAdapter != null) {
            webRecipesAdapter.actualizarDatos();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Fragment destroyed");

        // ✅ LIMPIAR REFERENCIAS PARA EVITAR MEMORY LEAKS
        webRecipesAdapter = null;
        progressBar = null;
        welcomeMessage = null;
    }
}



//package com.camilo.cocinarte.ui.inicio;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//
//import com.camilo.cocinarte.MainActivity;
//import com.camilo.cocinarte.R;
//
//public class InicioFragment extends Fragment {
//
//    private ImageButton menuButton;
//    private ImageButton searchButton;
//    private EditText searchEditText;
//    private DrawerLayout drawerLayout;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_inicio, container, false);
//
//        // Inicializar vistas
//        initViews(root);
//
//        // Configurar listeners
//        setupListeners();
//
//        return root;
//    }
//
//    private void initViews(View root) {
//        menuButton = root.findViewById(R.id.menu_button);
//        searchButton = root.findViewById(R.id.search_button);
//        searchEditText = root.findViewById(R.id.search_edit_text);
//
//        // Obtener referencia al DrawerLayout desde MainActivity
//        if (getActivity() instanceof MainActivity) {
//            MainActivity mainActivity = (MainActivity) getActivity();
//            drawerLayout = mainActivity.getDrawerLayout();
//        }
//    }
//
//    private void setupListeners() {
//        // Configurar botón del menú lateral
//        menuButton.setOnClickListener(v -> {
//            if (drawerLayout != null) {
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    drawerLayout.openDrawer(GravityCompat.START);
//                }
//            }
//        });
//
//        // Configurar botón de búsqueda
//        searchButton.setOnClickListener(v -> {
//            String searchQuery = searchEditText.getText().toString().trim();
//            if (!searchQuery.isEmpty()) {
//                performSearch(searchQuery);
//            } else {
//                Toast.makeText(getContext(), "Ingresa algo para buscar", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Configurar búsqueda al presionar Enter
//        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
//            String searchQuery = searchEditText.getText().toString().trim();
//            if (!searchQuery.isEmpty()) {
//                performSearch(searchQuery);
//                return true;
//            }
//            return false;
//        });
//    }
//
//    private void performSearch(String query) {
//        // Aquí implementas la lógica de búsqueda
//        Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
//
//        // Ejemplo: ocultar el teclado después de buscar
//        if (getActivity() != null) {
//            android.view.inputmethod.InputMethodManager imm =
//                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
//            if (imm != null && getView() != null) {
//                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
//            }
//        }
//
//        // Limpiar el campo de búsqueda si deseas
//        // searchEditText.setText("");
//    }
//}