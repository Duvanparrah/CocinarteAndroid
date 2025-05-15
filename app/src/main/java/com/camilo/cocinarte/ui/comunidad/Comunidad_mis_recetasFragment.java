package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.FragmentComunidadMisRecetasBinding;

public class Comunidad_mis_recetasFragment extends Fragment {

    private FragmentComunidadMisRecetasBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadMisRecetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Configurar el botón de búsqueda
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Configurar el botón de menú
        binding.menuButton.setOnClickListener(v -> openMenu());

        // Configurar la pestaña de Comunidad - Navegar usando Navigation Component
        binding.comunidadTab.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_navegar_comunidad_mis_recetas_to_navegar_comunidad));

        // Configurar la pestaña de Mis recetas (opcional, ya que estamos en esta pestaña)
        binding.misRecetasTab.setOnClickListener(v -> {
            // No es necesario navegar, ya estamos en Comunidad_mis_recetasFragment
            Toast.makeText(getContext(), "Ya estás en Mis recetas", Toast.LENGTH_SHORT).show();
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