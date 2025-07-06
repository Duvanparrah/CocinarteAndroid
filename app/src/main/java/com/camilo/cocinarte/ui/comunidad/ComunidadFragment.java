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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }

        listView = view.findViewById(R.id.contenedor_recetas);

        setupClickListeners();
        cargarRecetasUsuariosConReacciones(); // ✅ CAMBIO PRINCIPAL
    }

    // ✅ NUEVO MÉTODO: Solo cargar recetas de usuarios regulares
    private void cargarRecetasUsuariosConReacciones() {
        mostrarCargando(true);

        Log.d(TAG, "🔍 Cargando SOLO recetas de usuarios regulares (sin administradores)");

        // ✅ VERIFICAR TOKEN ANTES DE HACER LA LLAMADA
        String tokenGuardado = getValidToken();
        if (tokenGuardado == null) {
            Log.e(TAG, "No se pudo obtener un token válido");
            mostrarCargando(false);
            Toast.makeText(getContext(), "Error de autenticación. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        // ✅ USAR EL ENDPOINT ESPECÍFICO PARA USUARIOS SOLAMENTE
        recetaApi.getRecetasUsuarios("Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> recetas = response.body();

                    Log.d(TAG, "✅ Recetas de USUARIOS cargadas: " + recetas.size());
                    Log.d(TAG, "🚫 Recetas de administradores filtradas automáticamente");

                    // ✅ VERIFICAR QUE TODAS SON DE USUARIOS REGULARES (LOG DE VERIFICACIÓN)
                    int usuariosRegulares = 0;
                    int administradores = 0;
                    for (Receta receta : recetas) {
                        if (receta.getCreador() != null) {
                            if ("usuario".equals(receta.getCreador().getTipo_usuario())) {
                                usuariosRegulares++;
                            } else {
                                administradores++;
                                Log.w(TAG, "⚠️ ADVERTENCIA: Receta de admin detectada: " + receta.getTitulo() +
                                        " - Tipo: " + receta.getCreador().getTipo_usuario());
                            }
                        }
                    }

                    Log.d(TAG, "📊 Estadísticas finales - Usuarios: " + usuariosRegulares + ", Admins: " + administradores);

                    // ✅ CREAR ADAPTER CON VALIDACIÓN DE USUARIO
                    adapter = createAdapterSafely(recetas);

                    if (adapter != null) {
                        listView.setAdapter(adapter);
                        adapter.actualizarReacciones(() -> mostrarCargando(false));

                        // Mostrar mensaje informativo si no hay recetas
                        if (recetas.isEmpty()) {
                            Toast.makeText(getContext(), "No hay recetas de usuarios para mostrar", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "No se pudo crear el adapter");
                        mostrarCargando(false);
                        Toast.makeText(getContext(), "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Error al cargar recetas de usuarios: " + response.code());

                    // Si falla, intentar endpoint general como fallback
                    if (response.code() == 404) {
                        Log.w(TAG, "Endpoint usuarios-only no disponible, usando endpoint general con filtro");
                        cargarRecetasConFiltroLocal();
                    } else {
                        mostrarCargando(false);
                        Toast.makeText(getContext(), "Error al cargar recetas: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al cargar recetas de usuarios: " + t.getMessage());

                // Como fallback, intentar el endpoint general
                Log.w(TAG, "Intentando endpoint general como fallback");
                cargarRecetasConFiltroLocal();
            }
        });
    }

    // ✅ MÉTODO FALLBACK: Usar endpoint general y filtrar localmente
    private void cargarRecetasConFiltroLocal() {
        Log.d(TAG, "🔄 Usando endpoint general y filtrando localmente");

        String tokenGuardado = getValidToken();
        if (tokenGuardado == null) {
            mostrarCargando(false);
            return;
        }

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        recetaApi.getRecetas("Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> todasLasRecetas = response.body();

                    // ✅ FILTRAR LOCALMENTE SOLO USUARIOS REGULARES
                    List<Receta> recetasUsuarios = new ArrayList<>();
                    for (Receta receta : todasLasRecetas) {
                        if (receta.getCreador() != null && "usuario".equals(receta.getCreador().getTipo_usuario())) {
                            recetasUsuarios.add(receta);
                        }
                    }

                    Log.d(TAG, "✅ Filtro local aplicado - Total: " + todasLasRecetas.size() +
                            ", Solo usuarios: " + recetasUsuarios.size());

                    adapter = createAdapterSafely(recetasUsuarios);
                    if (adapter != null) {
                        listView.setAdapter(adapter);
                        adapter.actualizarReacciones(() -> mostrarCargando(false));
                    } else {
                        mostrarCargando(false);
                    }
                } else {
                    Log.e(TAG, "❌ Error al cargar recetas: " + response.code());
                    mostrarCargando(false);
                    Toast.makeText(getContext(), "Error al cargar recetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión: " + t.getMessage());
                mostrarCargando(false);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ MÉTODO PARA OBTENER TOKEN VÁLIDO DESDE CUALQUIER MANAGER
    private String getValidToken() {
        // Intentar primero con SessionManager
        try {
            SessionManager sessionManager = SessionManager.getInstance(requireContext());
            if (sessionManager != null && sessionManager.getAuthToken() != null) {
                Log.d(TAG, "Token obtenido desde SessionManager");
                return sessionManager.getAuthToken();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener token desde SessionManager: " + e.getMessage());
        }

        // Si falla, intentar con LoginManager
        try {
            LoginManager loginManager = new LoginManager(requireContext());
            String token = loginManager.getToken();
            if (token != null && !token.trim().isEmpty()) {
                Log.d(TAG, "Token obtenido desde LoginManager");
                return token;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al obtener token desde LoginManager: " + e.getMessage());
        }

        Log.e(TAG, "No se pudo obtener token desde ningún manager");
        return null;
    }

    // ✅ MÉTODO PARA CREAR ADAPTER DE FORMA SEGURA
    private AdapterComunidad createAdapterSafely(List<Receta> recetas) {
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
                    // ✅ USAR TU IMPLEMENTACIÓN COMPLETA DE COMENTARIOS
                    try {
                        JSONObject cache = ReaccionCache.getReacciones(receta.getIdReceta());
                        JSONArray comentarios = new JSONArray();
                        if (cache != null && cache.has("comentarios")) {
                            comentarios = cache.optJSONArray("comentarios");
                        }

                        // ✅ USAR TU ComentariosBottomSheetFragment DE FAVORITOS
                        com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment modal =
                                com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment.newInstance(comentarios, receta.getIdReceta());

                        modal.setComentariosListener(new com.camilo.cocinarte.ui.favoritos.ComentariosBottomSheetFragment.ComentariosListener() {
                            @Override
                            public void onComentariosCerrados() {
                                // Al cerrar el modal, recargamos reacciones
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
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al crear adapter: " + e.getMessage(), e);
            return null;
        }
    }

    // ✅ MÉTODO DE CARGA SIMPLE (solo usa Toast)
    private void mostrarCargando(boolean mostrar) {
        // Usar Toast como indicador de carga simple
        if (mostrar && getContext() != null) {
            Toast.makeText(getContext(), "Cargando recetas...", Toast.LENGTH_SHORT).show();
        }
        // No hacer nada cuando mostrar=false, ya que el Toast desaparece automáticamente
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());

        // ✅ BUSCAR EL BOTÓN DE MENÚ POR findViewById COMO ALTERNATIVA SEGURA
        View menuButton = getView() != null ? getView().findViewById(R.id.menu_button) : null;
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> openMenu());
        }

        binding.misRecetasTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas));

        binding.comunidadTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Comunidad", Toast.LENGTH_SHORT).show());

        // ✅ CONFIGURAR OTROS POSIBLES BOTONES DE MENÚ CON DIFERENTES NOMBRES
        setupMenuButtons();
    }

    // ✅ MÉTODO PARA CONFIGURAR BOTONES DE MENÚ CON DIFERENTES POSIBLES NOMBRES
    private void setupMenuButtons() {
        if (getView() != null) {
            // Intentar diferentes IDs comunes para botones de menú
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
                            break; // Solo configurar el primero que encontremos
                        }
                    }
                } catch (Exception e) {
                    // Continuar con el siguiente ID si hay error
                }
            }
        }
    }

    // ✅ BÚSQUEDA MEJORADA - Solo en recetas de usuarios
    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Log.d(TAG, "🔍 Buscando '" + query + "' solo en recetas de usuarios");

            String tokenGuardado = getValidToken();
            if (tokenGuardado == null) return;

            mostrarCargando(true);
            RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

            // Usar búsqueda y filtrar resultados
            recetaApi.buscarRecetas(query, "Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
                @Override
                public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Receta> resultados = response.body();

                        // Filtrar solo usuarios regulares
                        List<Receta> resultadosUsuarios = new ArrayList<>();
                        for (Receta receta : resultados) {
                            if (receta.getCreador() != null && "usuario".equals(receta.getCreador().getTipo_usuario())) {
                                resultadosUsuarios.add(receta);
                            }
                        }

                        Log.d(TAG, "✅ Búsqueda completada - Encontrados: " + resultadosUsuarios.size() + " de usuarios");

                        adapter = createAdapterSafely(resultadosUsuarios);
                        if (adapter != null) {
                            listView.setAdapter(adapter);
                            adapter.actualizarReacciones(() -> mostrarCargando(false));
                        } else {
                            mostrarCargando(false);
                        }

                        if (resultadosUsuarios.isEmpty()) {
                            Toast.makeText(getContext(), "No se encontraron recetas de usuarios para: " + query, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mostrarCargando(false);
                        Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
                    mostrarCargando(false);
                    Toast.makeText(getContext(), "Error de conexión en búsqueda", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
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

    // ✅ MÉTODO PÚBLICO PARA RECARGAR RECETAS (llamado después de crear una receta)
    public void recargarRecetas() {
        Log.d(TAG, "🔄 Recargando recetas después de crear una nueva");
        cargarRecetasUsuariosConReacciones();
    }

    // ✅ MÉTODOS PARA NAVEGAR PROGRAMÁTICAMENTE USANDO MAINACTIVITY
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

    // ✅ MÉTODO PARA NAVEGAR DESPUÉS DE CREAR UNA RECETA
    public void onRecetaCreated() {
        Toast.makeText(getContext(), "Receta creada exitosamente", Toast.LENGTH_SHORT).show();
        // Recargar recetas para mostrar la nueva
        recargarRecetas();
        // Navegar a mis recetas
        Navigation.findNavController(requireView())
                .navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumido, actualizando recetas");
        if (adapter != null) {
            mostrarCargando(true);
            adapter.actualizarReacciones(() -> mostrarCargando(false));
        } else {
            // Si no hay adapter, cargar recetas desde cero
            cargarRecetasUsuariosConReacciones();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}