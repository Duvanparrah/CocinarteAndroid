package com.camilo.cocinarte.ui.inicio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;

import com.camilo.cocinarte.NavigationActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.FragmentInicioBinding;
import com.camilo.cocinarte.ui.base.BaseFragment;

public class InicioFragment extends BaseFragment {

    private FragmentInicioBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar listeners adicionales específicos de este fragmento
        setupClickListeners();
    }

    @Override
    protected void setupDrawerButton() {
        // Sobreescribimos este método del BaseFragment
        if (binding != null && binding.filterButton != null) {
            binding.filterButton.setOnClickListener(v -> openDrawer());
        }
    }

    private void setupClickListeners() {
        // Configurar el botón de búsqueda si existe
        if (binding.searchButton != null) {
            binding.searchButton.setOnClickListener(v -> performSearch());
        }

        // Si existe el botón de favoritos (filtro con estrella)
        if (binding.filtro != null) {
            binding.filtro.setOnClickListener(v -> toggleFavorites());
        }
    }

    private void performSearch() {
        if (binding.searchEditText != null) {
            String query = binding.searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                // Implementar funcionalidad de búsqueda
                Toast.makeText(requireContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
                // Navegar a resultados de búsqueda o filtrar contenido actual
            } else {
                Toast.makeText(requireContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleFavorites() {
        // Implementar lógica para mostrar favoritos
        Toast.makeText(requireContext(), "Favoritos", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}