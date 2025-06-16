package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.gson.Gson;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComunidadFragment extends Fragment {
    private FragmentComunidadBinding binding;
    ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        getDataRecetas();
        listView = view.findViewById(R.id.contenedor_recetas);

    }

    private void getDataRecetas(){
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        recetaApi.getRecetas("Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful()) {
                    List<Receta> recetas = response.body();

                    assert recetas != null;

                    AdapterComunidad adapter = new AdapterComunidad(getContext(), recetas, receta -> {
                        Bundle bundle = new Bundle();
                        String recetaGson = new Gson().toJson(receta);
                        bundle.putString("receta", recetaGson);
                        bundle.putString("origen", "comunidad");
                        Navigation.findNavController(requireView()).navigate(R.id.detalleRecetaFragment, bundle);
                    });
                    listView.setAdapter(adapter);
                } else {
                    Log.e(">> >>error retrofit", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.e(">> >>failure", "Error en conexión: " + t.getMessage());
            }
        });
    }

    private void setupClickListeners() {
        // Configurar el botón de búsqueda
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Configurar el botón de menú
        binding.menuButton.setOnClickListener(v -> openMenu());

        // Configurar la pestaña de Mis recetas - Navegar usando Navigation Component
        binding.misRecetasTab.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_navegar_comunidad_to_navegar_comunidad_mis_recetas));

        // Configurar la pestaña de Comunidad (opcional, ya que estamos en esta pestaña)
        binding.comunidadTab.setOnClickListener(v -> {
            // No es necesario navegar, ya estamos en ComunidadFragment
            Toast.makeText(getContext(), "Ya estás en Comunidad", Toast.LENGTH_SHORT).show();
        });
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            // Implementar funcionalidad de búsqueda
            Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
            // Navegar a resultados de búsqueda o filtrar contenido actual
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        // Implementar lógica para abrir el menú
        // Esto podría ser abrir un DrawerLayout o mostrar un menú popup
        Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}