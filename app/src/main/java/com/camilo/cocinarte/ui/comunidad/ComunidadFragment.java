package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentComunidadBinding;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.ReaccionCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadFragment extends Fragment {
    private static final String TAG = "ComunidadFragment";

    private FragmentComunidadBinding binding;
    private ListView listView;
    private AdapterComunidad adapter;
    private Handler handler = new Handler();
    private DrawerLayout drawerLayout;
    private SessionManager sessionManager;
    private LoginManager loginManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadBinding.inflate(inflater, container, false);
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

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }

        listView = view.findViewById(R.id.contenedor_recetas);

        setupClickListeners();
        cargarRecetasDeUsuariosRegulares(); // ✅ MÉTODO PRINCIPAL ACTUALIZADO
    }

    /**
     * ✅ MÉTODO ACTUALIZADO: Usar endpoints existentes y filtrar del lado del cliente
     */
    private void cargarRecetasDeUsuariosRegulares() {
        mostrarCargando(true);

        Log.d(TAG, "👥 Cargando SOLO recetas de USUARIOS REGULARES para pantalla de COMUNIDAD");

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        // ✅ VERIFICAR AUTENTICACIÓN
        boolean tieneAutenticacion = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        if (tieneAutenticacion) {
            Log.d(TAG, "🔐 Usuario autenticado: Cargando todas las recetas y filtrando usuarios");
            String token = sessionManager.getAuthToken();

            // ✅ USAR ENDPOINT EXISTENTE
            recetaApi.obtenerTodasLasRecetasConAuth("Bearer " + token)
                    .enqueue(new CallbackRecetasConFiltro(true));
        } else {
            Log.d(TAG, "🌐 Usuario NO autenticado: Cargando todas las recetas y filtrando usuarios");

            // ✅ USAR ENDPOINT EXISTENTE SIN AUTH
            recetaApi.obtenerTodasLasRecetas()
                    .enqueue(new CallbackRecetasConFiltro(false));
        }
    }

    /**
     * ✅ CALLBACK CON FILTRADO DEL LADO DEL CLIENTE PARA USUARIOS REGULARES
     */
    private class CallbackRecetasConFiltro implements Callback<List<Receta>> {
        private final boolean conAutenticacion;

        public CallbackRecetasConFiltro(boolean conAutenticacion) {
            this.conAutenticacion = conAutenticacion;
        }

        @Override
        public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Receta> todasLasRecetas = response.body();

                Log.d(TAG, "✅ Todas las recetas cargadas: " + todasLasRecetas.size());

                // ✅ FILTRAR SOLO USUARIOS REGULARES
                List<Receta> recetasUsuarios = new ArrayList<>();

                for (Receta receta : todasLasRecetas) {
                    if (receta.getCreador() != null) {
                        String tipoUsuario = receta.getCreador().getTipo_usuario();

                        // ✅ FILTRAR SOLO USUARIOS REGULARES
                        if ("usuario".equals(tipoUsuario)) {
                            recetasUsuarios.add(receta);
                        }
                    }
                }

                Log.d(TAG, "✅ Recetas de USUARIOS REGULARES filtradas: " + recetasUsuarios.size());

                // ✅ VERIFICAR VALORES NUTRICIONALES
                int conNutricion = 0;
                for (Receta receta : recetasUsuarios) {
                    if (receta.getCalorias() > 0 || (receta.getNutricion() != null && receta.getNutricion().getCalorias() > 0)) {
                        conNutricion++;
                    }
                }

                Log.d(TAG, "📊 Estadísticas COMUNIDAD - Recetas usuarios: " + recetasUsuarios.size() + ", Con nutrición: " + conNutricion);

                // ✅ CREAR ADAPTER ESPECÍFICO PARA COMUNIDAD
                adapter = createAdapterSafely(recetasUsuarios, conAutenticacion);

                if (adapter != null) {
                    listView.setAdapter(adapter);

                    if (conAutenticacion) {
                        adapter.actualizarReacciones(() -> mostrarCargando(false));
                    } else {
                        mostrarCargando(false);
                    }

                    // ✅ MENSAJE INFORMATIVO
                    String mensaje = "👥 " + recetasUsuarios.size() + " recetas de usuarios cargadas";
                    if (conNutricion > 0) {
                        mensaje += " (" + conNutricion + " con nutrición)";
                    }
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();

                } else {
                    Log.e(TAG, "No se pudo crear el adapter para comunidad");
                    mostrarCargando(false);
                    Toast.makeText(getContext(), "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
                }

                // Mostrar mensaje si no hay recetas
                if (recetasUsuarios.isEmpty()) {
                    Toast.makeText(getContext(), "No hay recetas de usuarios para mostrar", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e(TAG, "❌ Error al cargar recetas: " + response.code());
                manejarErrorCarga("Error al cargar recetas: " + response.code());
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
            Log.e(TAG, "❌ Error de conexión al cargar recetas", t);
            manejarErrorCarga("Error de conexión: " + t.getMessage());
        }
    }

    /**
     * ✅ BÚSQUEDA CON FILTRADO DEL LADO DEL CLIENTE PARA USUARIOS
     */
    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();

        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔍 Buscando '" + query + "' en recetas de USUARIOS REGULARES");

        mostrarCargando(true);
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        boolean tieneAutenticacion = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        if (tieneAutenticacion) {
            String token = sessionManager.getAuthToken();
            // ✅ USAR ENDPOINT EXISTENTE DE BÚSQUEDA
            recetaApi.buscarRecetasConAuth(query, "Bearer " + token)
                    .enqueue(new CallbackBusquedaConFiltro(query));
        } else {
            // ✅ USAR ENDPOINT EXISTENTE DE BÚSQUEDA SIN AUTH
            recetaApi.buscarRecetas(query)
                    .enqueue(new CallbackBusquedaConFiltro(query));
        }
    }

    /**
     * ✅ CALLBACK PARA BÚSQUEDA CON FILTRADO DE USUARIOS
     */
    private class CallbackBusquedaConFiltro implements Callback<List<Receta>> {
        private final String query;

        public CallbackBusquedaConFiltro(String query) {
            this.query = query;
        }

        @Override
        public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Receta> todosLosResultados = response.body();

                // ✅ FILTRAR SOLO USUARIOS REGULARES
                List<Receta> resultadosUsuarios = new ArrayList<>();
                for (Receta receta : todosLosResultados) {
                    if (receta.getCreador() != null) {
                        String tipoUsuario = receta.getCreador().getTipo_usuario();
                        if ("usuario".equals(tipoUsuario)) {
                            resultadosUsuarios.add(receta);
                        }
                    }
                }

                Log.d(TAG, "✅ Búsqueda completada: " + resultadosUsuarios.size() + " resultados de usuarios para '" + query + "'");

                // ✅ CREAR ADAPTER CON RESULTADOS
                boolean tieneAuth = sessionManager.isLoggedIn() && sessionManager.hasValidToken();
                adapter = createAdapterSafely(resultadosUsuarios, tieneAuth);

                if (adapter != null) {
                    listView.setAdapter(adapter);

                    if (tieneAuth) {
                        adapter.actualizarReacciones(() -> mostrarCargando(false));
                    } else {
                        mostrarCargando(false);
                    }
                }

                // ✅ MOSTRAR MENSAJE
                if (resultadosUsuarios.isEmpty()) {
                    Toast.makeText(getContext(), "No se encontraron recetas de usuarios para: " + query, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "✅ " + resultadosUsuarios.size() + " recetas de usuarios encontradas", Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e(TAG, "❌ Error en búsqueda: " + response.code());
                mostrarCargando(false);
                Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
            Log.e(TAG, "❌ Error de conexión en búsqueda", t);
            mostrarCargando(false);
            Toast.makeText(getContext(), "Error de conexión en búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ MÉTODO PARA CREAR ADAPTER DE FORMA SEGURA
     */
    private AdapterComunidad createAdapterSafely(List<Receta> recetas, boolean conAutenticacion) {
        try {
            return new AdapterComunidad(getContext(), recetas, new AdapterComunidad.OnRecetaClickListener() {
                @Override
                public void onRecetaClick(Receta receta) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("id_receta", receta.getIdReceta());
                    bundle.putString("origen", "comunidad");
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_navegar_comunidad_to_detalleRecetaFragment, bundle);
                }

                @Override
                public void onComentariosClick(Receta receta) {
                    if (conAutenticacion) {
                        abrirComentarios(receta);
                    } else {
                        Toast.makeText(getContext(), "Inicia sesión para ver comentarios", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al crear adapter: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * ✅ ABRIR COMENTARIOS (solo si hay autenticación)
     */
    private void abrirComentarios(Receta receta) {
        try {
            JSONObject cache = ReaccionCache.getReacciones(receta.getIdReceta());
            JSONArray comentarios = new JSONArray();
            if (cache != null && cache.has("comentarios")) {
                comentarios = cache.optJSONArray("comentarios");
            }

            com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment modal =
                    com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment.newInstance(comentarios, receta.getIdReceta());

            modal.setComentariosListener(new com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment.ComentariosListener() {
                @Override
                public void onComentariosCerrados() {
                    if (adapter != null) {
                        adapter.actualizarReacciones(null);
                    }
                }
            });
            modal.show(getParentFragmentManager(), "ComentariosBottomSheet");
        } catch (Exception e) {
            Log.e(TAG, "Error al abrir comentarios: " + e.getMessage());
            Toast.makeText(getContext(), "Error al cargar comentarios", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ MANEJAR ERRORES DE CARGA
     */
    private void manejarErrorCarga(String mensaje) {
        mostrarCargando(false);
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    /**
     * ✅ MOSTRAR INDICADOR DE CARGA
     */
    private void mostrarCargando(boolean mostrar) {
        if (mostrar && getContext() != null) {
            Toast.makeText(getContext(), "👥 Cargando recetas de usuarios...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Botón de menú
        View menuButton = getView() != null ? getView().findViewById(R.id.menu_button) : null;
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> openMenu());
        }

        // Tabs
        binding.misRecetasTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas));

        binding.comunidadTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Comunidad", Toast.LENGTH_SHORT).show());

        setupMenuButtons();
    }

    private void setupMenuButtons() {
        if (getView() != null) {
            String[] menuButtonIds = {
                    "menu_button", "btn_menu", "menuBtn", "hamburger_button",
                    "drawer_button", "navigation_button", "toolbar_menu"
            };

            for (String buttonId : menuButtonIds) {
                try {
                    int resourceId = getResources().getIdentifier(buttonId, "id", requireContext().getPackageName());
                    if (resourceId != 0) {
                        View button = getView().findViewById(resourceId);
                        if (button != null) {
                            button.setOnClickListener(v -> openMenu());
                            Log.d(TAG, "Botón de menú configurado con ID: " + buttonId);
                            break;
                        }
                    }
                } catch (Exception e) {
                    // Continuar con el siguiente ID si hay error
                }
            }
        }
    }

    private void openMenu() {
        if (drawerLayout != null) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        } else {
            Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ MÉTODO PÚBLICO: Recargar recetas (llamado desde MainActivity)
     */
    public void recargarRecetas() {
        Log.d(TAG, "🔄 Recargando recetas de usuarios regulares");
        cargarRecetasDeUsuariosRegulares();
    }

    /**
     * ✅ MÉTODO PÚBLICO: Navegar a fragment específico
     */
    public void navigateToFragment(int fragmentId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(fragmentId);
        }
    }

    public void navigateToInicio() {
        navigateToFragment(R.id.navigation_inicio);
    }

    public void navigateToBanquetes() {
        navigateToFragment(R.id.navigation_banquetes);
    }

    public void navigateToNutricion() {
        navigateToFragment(R.id.navigation_nutricion);
    }

    /**
     * ✅ MÉTODO PARA CUANDO SE CREA UNA RECETA NUEVA
     */
    public void onRecetaCreated() {
        Toast.makeText(getContext(),
                "🎉 Receta creada exitosamente\n📊 Con valores nutricionales calculados por IA",
                Toast.LENGTH_LONG).show();

        // Recargar recetas para mostrar la nueva
        recargarRecetas();

        // Navegar a mis recetas
        Navigation.findNavController(requireView())
                .navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumido, actualizando recetas de usuarios");

        if (adapter != null) {
            mostrarCargando(true);
            boolean tieneAuth = sessionManager.isLoggedIn() && sessionManager.hasValidToken();
            if (tieneAuth) {
                adapter.actualizarReacciones(() -> mostrarCargando(false));
            } else {
                mostrarCargando(false);
            }
        } else {
            // Si no hay adapter, cargar recetas desde cero
            cargarRecetasDeUsuariosRegulares();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}