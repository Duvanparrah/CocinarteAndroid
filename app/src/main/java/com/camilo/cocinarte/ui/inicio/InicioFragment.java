package com.camilo.cocinarte.ui.inicio;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.DetalleRecetaActivity;
import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentInicioBinding;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.session.SessionManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.core.view.GravityCompat;

public class InicioFragment extends Fragment {
    private static final String TAG = "InicioFragment";

    private FragmentInicioBinding binding;
    private RecyclerView recyclerView;
    private RecetasAdapter adapter;
    private List<Receta> recetasList = new ArrayList<>();
    private SessionManager sessionManager;
    private LoginManager loginManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            sessionManager = SessionManager.getInstance(requireContext());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loginManager = new LoginManager(requireContext());

        setupRecyclerView();
        setupListeners();
        cargarRecetasDeAdministradores();
    }

    private void setupRecyclerView() {
        recyclerView = binding.webRecipesRecycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RecetasAdapter(getContext(), recetasList, receta -> abrirDetalleReceta(receta), true);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        // Búsqueda
        binding.searchButton.setOnClickListener(v -> realizarBusqueda());

        // Mensaje de bienvenida
        actualizarMensajeBienvenida();

        // 👉 Abrir Drawer con botón del menú
        ImageButton menuButton = binding.getRoot().findViewById(R.id.menu_button);
        DrawerLayout drawerLayout = ((MainActivity) requireActivity()).getDrawerLayout();

        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void actualizarMensajeBienvenida() {
        try {
            String nombreUsuario = "Usuario";

            if (loginManager.getUsuario() != null && loginManager.getUsuario().getNombreUsuario() != null) {
                nombreUsuario = loginManager.getUsuario().getNombreUsuario();
            } else if (sessionManager.getUserName() != null) {
                nombreUsuario = sessionManager.getUserName();
            }

            binding.welcomeMessage.setText("¡Bienvenido de nuevo, " + nombreUsuario + "!");
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar mensaje de bienvenida", e);
            binding.welcomeMessage.setText("¡Bienvenido de nuevo!");
        }
    }

    private void cargarRecetasDeAdministradores() {
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        boolean tieneAutenticacion = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        if (tieneAutenticacion) {
            recetaApi.obtenerTodasLasRecetasConAuth("Bearer " + sessionManager.getAuthToken())
                    .enqueue(new CallbackRecetasConFiltro(true));
        } else {
            recetaApi.obtenerTodasLasRecetas().enqueue(new CallbackRecetasConFiltro(false));
        }
    }

    private class CallbackRecetasConFiltro implements Callback<List<Receta>> {
        private final boolean conAutenticacion;

        public CallbackRecetasConFiltro(boolean conAutenticacion) {
            this.conAutenticacion = conAutenticacion;
        }

        @Override
        public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Receta> recetasAdmin = new ArrayList<>();
                for (Receta receta : response.body()) {
                    if (receta.getCreador() != null) {
                        String tipoUsuario = receta.getCreador().getTipo_usuario();
                        if ("administrador".equals(tipoUsuario) || "administrador_lider".equals(tipoUsuario)) {
                            recetasAdmin.add(receta);
                        }
                    }
                }

                recetasList.clear();
                recetasList.addAll(recetasAdmin);
                adapter.notifyDataSetChanged();

                if (recetasAdmin.isEmpty()) {
                    Toast.makeText(getContext(), "No hay recetas de administradores disponibles", Toast.LENGTH_SHORT).show();
                }

            } else {
                manejarErrorCarga("Error al cargar recetas: " + response.code());
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
            manejarErrorCarga("Error de conexión: " + t.getMessage());
        }
    }

    private void realizarBusqueda() {
        String query = binding.searchEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            return;
        }

        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        boolean tieneAutenticacion = sessionManager.isLoggedIn() && sessionManager.hasValidToken();

        if (tieneAutenticacion) {
            recetaApi.buscarRecetasConAuth(query, "Bearer " + sessionManager.getAuthToken())
                    .enqueue(new CallbackBusquedaConFiltro(query));
        } else {
            recetaApi.buscarRecetas(query).enqueue(new CallbackBusquedaConFiltro(query));
        }
    }

    private class CallbackBusquedaConFiltro implements Callback<List<Receta>> {
        private final String query;

        public CallbackBusquedaConFiltro(String query) {
            this.query = query;
        }

        @Override
        public void onResponse(@NonNull Call<List<Receta>> call, @NonNull Response<List<Receta>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Receta> resultadosAdmin = new ArrayList<>();
                for (Receta receta : response.body()) {
                    if (receta.getCreador() != null) {
                        String tipoUsuario = receta.getCreador().getTipo_usuario();
                        if ("administrador".equals(tipoUsuario) || "administrador_lider".equals(tipoUsuario)) {
                            resultadosAdmin.add(receta);
                        }
                    }
                }

                recetasList.clear();
                recetasList.addAll(resultadosAdmin);
                adapter.notifyDataSetChanged();

                if (resultadosAdmin.isEmpty()) {
                    Toast.makeText(getContext(), "No se encontraron recetas de administradores para: " + query, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), resultadosAdmin.size() + " recetas encontradas", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Error en la búsqueda", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Receta>> call, @NonNull Throwable t) {
            Toast.makeText(getContext(), "Error de conexión en búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirDetalleReceta(Receta receta) {
        Intent intent = new Intent(getContext(), DetalleRecetaActivity.class);
        intent.putExtra("receta_id", receta.getIdReceta());
        intent.putExtra("from_inicio", true);
        startActivity(intent);
    }

    private void manejarErrorCarga(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
        recetasList.clear();
        adapter.notifyDataSetChanged();
    }

    public void recargarRecetas() {
        cargarRecetasDeAdministradores();
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarMensajeBienvenida();
        if (adapter != null) {
            adapter.actualizarDatos();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
