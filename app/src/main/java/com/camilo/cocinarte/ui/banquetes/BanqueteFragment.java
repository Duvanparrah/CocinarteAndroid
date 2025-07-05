package com.camilo.cocinarte.ui.banquetes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.FragmentBanqueteBinding;

public class BanqueteFragment extends Fragment {

    private FragmentBanqueteBinding binding;
    private DrawerLayout drawerLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBanqueteBinding.inflate(inflater, container, false);
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

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Configurar botón de menú para abrir/cerrar drawer
        binding.menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // ✅ EJEMPLO: Si tienes algún botón específico en tu binding, puedes usarlo así:
        // binding.tuBotonEspecifico.setOnClickListener(v -> navigateToInicio());
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(getContext(), "Buscando banquete: " + query, Toast.LENGTH_SHORT).show();

            // Aquí podrías agregar lógica de búsqueda
            // Por ejemplo, filtrar una lista de banquetes

            // ✅ EJEMPLO: Navegar después de una búsqueda exitosa
            // navigateToInicio(); // Si quieres navegar después de buscar

        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
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

    public void navigateToNutricion() {
        navigateToFragment(R.id.navigation_nutricion);
    }

    public void navigateToComunidad() {
        navigateToFragment(R.id.navegar_comunidad);
    }

    // ✅ MÉTODO PARA USAR LA NAVEGACIÓN DEL BOTTOM NAVIGATION
    public void navigateViaBottomNavigation(int destinationId) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.navigateToFragment(destinationId);
        }
    }

    // ✅ MÉTODOS DE EJEMPLO PARA LLAMAR DESDE BOTONES ESPECÍFICOS
    public void onInicioButtonClick() {
        navigateToInicio();
    }

    public void onNutricionButtonClick() {
        navigateToNutricion();
    }

    public void onComunidadButtonClick() {
        navigateToComunidad();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}