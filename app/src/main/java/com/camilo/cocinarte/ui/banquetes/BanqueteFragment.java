package com.camilo.cocinarte.ui.banquetes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.BanqueteApi;
import com.camilo.cocinarte.models.Banquete;
import com.camilo.cocinarte.session.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BanqueteFragment extends Fragment implements BanqueteAdapter.OnBanqueteClickListener {

    private static final String TAG = "BanqueteFragment";

    // ✅ VIEWS PRINCIPALES
    private ImageButton menuButton;
    private ImageButton filterButton;
    private EditText searchEditText;
    private RecyclerView banquetesRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;

    // ✅ DATOS Y ADAPTERS - CORREGIDO PARA USAR BanqueteAdapter
    private BanqueteAdapter banqueteAdapter;
    private List<Banquete> todosBanquetes = new ArrayList<>();
    private List<Banquete> banquetesFiltrados = new ArrayList<>();

    // ✅ SERVICIOS
    private BanqueteApi banqueteApi;
    private SessionManager sessionManager;

    // ✅ ESTADO
    private boolean isLoading = false;
    private boolean hasAuthentication = false;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_banquete, container, false);

        // Inicializar vistas
        initViews(root);

        // Configurar API y SessionManager
        setupApi();

        // Verificar autenticación
        checkAuthentication();

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar listeners
        setupListeners();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ✅ CARGAR BANQUETES INMEDIATAMENTE
        cargarBanquetes();
    }

    private void initViews(View root) {
        menuButton = root.findViewById(R.id.menu_button);
        filterButton = root.findViewById(R.id.filter_button);
        searchEditText = root.findViewById(R.id.search_edit_text);
        banquetesRecyclerView = root.findViewById(R.id.banquetesRecyclerView);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }
    }

    private void setupApi() {
        banqueteApi = ApiClient.getClient(requireContext()).create(BanqueteApi.class);
        sessionManager = SessionManager.getInstance(requireContext());
    }

    private void checkAuthentication() {
        String token = sessionManager.getAuthToken();
        hasAuthentication = (token != null && !token.isEmpty());
        Log.d(TAG, "🔐 Verificación de autenticación: " + (hasAuthentication ? "CON AUTH" : "SIN AUTH"));
    }

    private void setupRecyclerView() {
        // ✅ CONFIGURAR ADAPTER SEGÚN AUTENTICACIÓN - CORREGIDO
        banqueteAdapter = new BanqueteAdapter(getContext(), banquetesFiltrados, this, hasAuthentication);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        banquetesRecyclerView.setLayoutManager(layoutManager);
        banquetesRecyclerView.setAdapter(banqueteAdapter);
        banquetesRecyclerView.setHasFixedSize(true);
    }

    private void setupListeners() {
        // Menu button
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    } else {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                }
            });
        }

        // Filter button
        if (filterButton != null) {
            filterButton.setOnClickListener(v -> {
                // TODO: Implementar filtros (por dificultad, cantidad de personas, etc.)
                Toast.makeText(getContext(), "Filtros próximamente", Toast.LENGTH_SHORT).show();
            });
        }

        // Search functionality
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Debounce search con delay
                    if (getView() != null) {
                        getView().removeCallbacks(searchRunnable);
                        getView().postDelayed(searchRunnable, 300);
                    }
                }
            });
        }

        // Swipe to refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshBanquetes);
        }
    }

    // ✅ RUNNABLE PARA BÚSQUEDA CON DELAY
    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!currentSearchQuery.isEmpty()) {
                filtrarBanquetes(currentSearchQuery);
            } else {
                // Mostrar todos los banquetes
                banquetesFiltrados.clear();
                banquetesFiltrados.addAll(todosBanquetes);
                banqueteAdapter.notifyDataSetChanged();
            }
        }
    };

    // ✅ MÉTODO PRINCIPAL PARA CARGAR BANQUETES
    private void cargarBanquetes() {
        if (isLoading) {
            Log.d(TAG, "⚠️ Ya se está cargando, saltando...");
            return;
        }

        Log.d(TAG, "🍽️ Cargando banquetes...");
        Log.d(TAG, "🔐 Modo: " + (hasAuthentication ? "CON AUTENTICACIÓN" : "SIN AUTENTICACIÓN"));

        isLoading = true;

        if (getContext() == null) {
            Log.e(TAG, "❌ Context es null, no se puede cargar");
            isLoading = false;
            return;
        }

        Call<List<Banquete>> call;

        if (hasAuthentication) {
            // ✅ LLAMADA CON AUTENTICACIÓN
            String token = "Bearer " + sessionManager.getAuthToken();
            call = banqueteApi.obtenerTodosBanquetes(token);
            Log.d(TAG, "📞 Llamada CON token");
        } else {
            // ✅ LLAMADA SIN AUTENTICACIÓN
            call = banqueteApi.obtenerTodosBanquetes();
            Log.d(TAG, "📞 Llamada SIN token");
        }

        call.enqueue(new Callback<List<Banquete>>() {
            @Override
            public void onResponse(@NonNull Call<List<Banquete>> call, @NonNull Response<List<Banquete>> response) {
                // ✅ VERIFICAR SI EL FRAGMENT SIGUE ACTIVO
                if (getContext() == null || !isAdded()) {
                    Log.w(TAG, "Fragment no está activo, cancelando actualización");
                    isLoading = false;
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Banquete> banquetes = response.body();
                    Log.d(TAG, "✅ Banquetes obtenidos: " + banquetes.size());

                    // ✅ ACTUALIZAR EN EL HILO PRINCIPAL
                    requireActivity().runOnUiThread(() -> {
                        mostrarBanquetesEnPantalla(banquetes);
                    });
                } else {
                    Log.e(TAG, "❌ Error en respuesta: " + response.code());
                    Log.e(TAG, "❌ Mensaje: " + response.message());

                    requireActivity().runOnUiThread(() -> {
                        mostrarMensajeError("No se pudieron cargar los banquetes (Código: " + response.code() + ")");
                    });
                }

                isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<List<Banquete>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Error de conexión al cargar banquetes: " + t.getMessage());

                // ✅ VERIFICAR SI EL FRAGMENT SIGUE ACTIVO
                if (getContext() == null || !isAdded()) {
                    Log.w(TAG, "Fragment no está activo, cancelando manejo de error");
                    isLoading = false;
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    mostrarMensajeError("Error de conexión. Verifica tu internet.");
                });

                isLoading = false;
            }
        });
    }

    // ✅ MÉTODO PARA MOSTRAR BANQUETES EN LA INTERFAZ
    private void mostrarBanquetesEnPantalla(List<Banquete> banquetes) {
        if (banquetes == null || banquetes.isEmpty()) {
            Log.w(TAG, "⚠️ No hay banquetes para mostrar");
            mostrarMensajeVacio();
            return;
        }

        Log.d(TAG, "📱 Mostrando " + banquetes.size() + " banquetes");

        // Actualizar listas
        todosBanquetes.clear();
        todosBanquetes.addAll(banquetes);

        banquetesFiltrados.clear();
        banquetesFiltrados.addAll(banquetes);

        // Notificar al adapter
        if (banqueteAdapter != null) {
            banqueteAdapter.notifyDataSetChanged();
            Log.d(TAG, "🔄 Adapter notificado con " + banquetes.size() + " banquetes");
        }

        // Detener refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        // Mostrar mensaje de éxito
        Toast.makeText(getContext(), "Banquetes cargados: " + banquetes.size(), Toast.LENGTH_SHORT).show();
    }

    // ✅ FILTRAR BANQUETES POR BÚSQUEDA
    private void filtrarBanquetes(String query) {
        Log.d(TAG, "🔍 Filtrando banquetes con query: " + query);

        if (todosBanquetes.isEmpty()) {
            Toast.makeText(getContext(), "No hay banquetes cargados para buscar", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Banquete> resultados = new ArrayList<>();
        String queryLower = query.toLowerCase();

        for (Banquete banquete : todosBanquetes) {
            // Buscar en nombre
            if (banquete.getNombre() != null &&
                    banquete.getNombre().toLowerCase().contains(queryLower)) {
                resultados.add(banquete);
                continue;
            }

            // Buscar en descripción
            if (banquete.getDescripcionPreparacion() != null &&
                    banquete.getDescripcionPreparacion().toLowerCase().contains(queryLower)) {
                resultados.add(banquete);
                continue;
            }

            // Buscar en dificultad
            if (banquete.getDificultad() != null &&
                    banquete.getDificultad().toLowerCase().contains(queryLower)) {
                resultados.add(banquete);
            }
        }

        Log.d(TAG, "📋 Resultados encontrados: " + resultados.size());

        // Actualizar lista filtrada
        banquetesFiltrados.clear();
        banquetesFiltrados.addAll(resultados);

        // Notificar al adapter
        if (banqueteAdapter != null) {
            banqueteAdapter.notifyDataSetChanged();
        }

        // Mostrar resultado de búsqueda
        if (resultados.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron banquetes con: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Encontrados " + resultados.size() + " banquetes", Toast.LENGTH_SHORT).show();
        }

        // Ocultar teclado
        ocultarTeclado();
    }

    // ✅ REFRESCAR BANQUETES
    private void refreshBanquetes() {
        Log.d(TAG, "🔄 Refrescando banquetes...");

        // Limpiar búsqueda
        currentSearchQuery = "";
        if (searchEditText != null) {
            searchEditText.setText("");
        }

        // Recargar datos
        cargarBanquetes();
    }

    // ✅ MÉTODOS AUXILIARES
    private void mostrarMensajeError(String mensaje) {
        if (getContext() != null) {
            Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        }

        // Detener refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Log.e(TAG, "Error mostrado al usuario: " + mensaje);
    }

    private void mostrarMensajeVacio() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "No hay banquetes disponibles en este momento", Toast.LENGTH_SHORT).show();
        }

        // Detener refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        Log.w(TAG, "No hay banquetes para mostrar");
    }

    private void ocultarTeclado() {
        if (getActivity() != null && getView() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    // ✅ IMPLEMENTACIÓN DE OnBanqueteClickListener
    @Override
    public void onBanqueteClick(Banquete banquete, int position) {
        Log.d(TAG, "🎯 Click en banquete: " + banquete.getNombre());

        Intent intent = new Intent(getContext(), BanqueteDetalleActivity.class);
        intent.putExtra("banquete_id", banquete.getIdBanquete());
        intent.putExtra("has_authentication", hasAuthentication);

        startActivity(intent);
    }

    // ✅ MÉTODOS DE NAVEGACIÓN
    public void navigateToFragment(int fragmentId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(fragmentId);
        }
    }

    // ✅ LIFECYCLE METHODS
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumed");

        // Verificar cambios en autenticación
        boolean authAnterior = hasAuthentication;
        checkAuthentication();

        if (authAnterior != hasAuthentication) {
            Log.d(TAG, "🔄 Cambio en autenticación detectado, reconfigurando adapter");
            setupRecyclerView();
            cargarBanquetes();
        } else if (banquetesFiltrados.isEmpty()) {
            // Solo recargar si no hay datos
            Log.d(TAG, "🔄 No hay datos, recargando banquetes");
            cargarBanquetes();
        } else {
            Log.d(TAG, "📋 Ya hay " + banquetesFiltrados.size() + " banquetes cargados");
        }

        // Actualizar adapter si hay autenticación (para reacciones)
        if (banqueteAdapter != null && hasAuthentication) {
            banqueteAdapter.actualizarDatos();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ Fragment destroyed");

        // Limpiar referencias para evitar memory leaks
        banqueteAdapter = null;
        todosBanquetes.clear();
        banquetesFiltrados.clear();
    }
}