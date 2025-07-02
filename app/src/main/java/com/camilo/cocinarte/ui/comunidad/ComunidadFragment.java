package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentComunidadBinding;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.utils.ReaccionCache;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadFragment extends Fragment {
    private FragmentComunidadBinding binding;
    private ListView listView;
    private FrameLayout loadingContainer;
    private AdapterComunidad adapter;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.contenedor_recetas);
        loadingContainer = view.findViewById(R.id.loading_container);
        setupClickListeners();
        cargarRecetasConReacciones();
    }

    private void cargarRecetasConReacciones() {
        mostrarCargando(true);
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        recetaApi.getRecetas("Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> recetas = response.body();
                    adapter = new AdapterComunidad(getContext(), recetas, new AdapterComunidad.OnRecetaClickListener() {
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
                            JSONObject cache = ReaccionCache.getReacciones(receta.getIdReceta());
                            JSONArray comentarios = new JSONArray();
                            if (cache != null && cache.has("comentarios")) {
                                comentarios = cache.optJSONArray("comentarios");
                            }

                            ComentariosBottomSheetFragment modal = ComentariosBottomSheetFragment.newInstance(comentarios, receta.getIdReceta());
                            modal.setComentariosListener(() -> {
                                // Al cerrar el modal, recargamos reacciones
                                adapter.actualizarReacciones(null);
                            });
                            modal.show(getParentFragmentManager(), "ComentariosBottomSheet");
                        }
                    });

                    listView.setAdapter(adapter);
                    adapter.actualizarReacciones(() -> mostrarCargando(false));
                } else {
                    Log.e("RETROFIT_ERROR", "Respuesta fallida: " + response.code());
                    mostrarCargando(false);
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.e("RETROFIT_FAILURE", "Error en conexión: " + t.getMessage());
                mostrarCargando(false);
            }
        });
    }

    private void mostrarCargando(boolean mostrar) {
        if (loadingContainer != null) {
            loadingContainer.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());

//        binding.menuButton.setOnClickListener(v -> openMenu());

        binding.misRecetasTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas));

        binding.comunidadTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Comunidad", Toast.LENGTH_SHORT).show());
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            mostrarCargando(true);
            adapter.actualizarReacciones(() -> mostrarCargando(false));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
