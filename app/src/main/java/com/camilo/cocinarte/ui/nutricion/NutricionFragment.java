package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
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
import com.camilo.cocinarte.databinding.FragmentNutricionBinding;

public class NutricionFragment extends Fragment {

    private FragmentNutricionBinding binding;
    private DrawerLayout drawerLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNutricionBinding.inflate(inflater, container, false);
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

        // Configurar oyentes de eventos
        setupEventListeners();

        // Configurar navegación entre fragments
        setupNavigationListeners(view);
    }

    private void setupEventListeners() {
        // Botón Plan Gratuito
        binding.btnGratis.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Has seleccionado el Plan Gratuito", Toast.LENGTH_SHORT).show();
            // Aquí podrías abrir una nueva actividad, enviar a un WebView, etc.
        });

        // Botón Plan Pro
        binding.btnPro.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Has seleccionado el Plan Pro", Toast.LENGTH_SHORT).show();
            // Iniciar la actividad de método de pago
            Intent intent = new Intent(getActivity(), Metodo_de_pago_Activity.class);
            startActivity(intent);
        });

        // Botón de filtro (ícono de menú)
        binding.filterButton.setOnClickListener(view -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            } else {
                Toast.makeText(getContext(), "Filtrar planes (funcionalidad pendiente)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ FUNCIÓN PARA CONFIGURAR NAVEGACIÓN ENTRE FRAGMENTS
    private void setupNavigationListeners(View view) {
        // La navegación principal se maneja mediante el Bottom Navigation

        // Ejemplo: Navegar después de seleccionar plan gratuito
        binding.btnGratis.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Plan Gratuito activado. Redirigiendo a Inicio...", Toast.LENGTH_SHORT).show();

            // Navegar a inicio después de un pequeño delay
            v.postDelayed(() -> {
                navigateToInicio();
            }, 1500);
        });
    }

    // ✅ MÉTODOS DE NAVEGACIÓN USANDO MAINACTIVITY
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

    public void navigateToComunidad() {
        navigateToFragment(R.id.navegar_comunidad);
    }

    // ✅ MÉTODO PARA MANEJAR LA SELECCIÓN DE PLANES CON NAVEGACIÓN
    private void handlePlanSelection(String planType) {
        switch (planType) {
            case "gratuito":
                Toast.makeText(getContext(), "Plan Gratuito activado", Toast.LENGTH_SHORT).show();
                // Redirigir a inicio o mantener en nutrición
                navigateToInicio();
                break;
            case "pro":
                Toast.makeText(getContext(), "Procesando Plan Pro...", Toast.LENGTH_SHORT).show();
                // Iniciar actividad de pago y después redirigir
                Intent intent = new Intent(getActivity(), Metodo_de_pago_Activity.class);
                startActivity(intent);
                break;
        }
    }

    // ✅ MÉTODOS DE EJEMPLO PARA LLAMAR DESDE ACCIONES ESPECÍFICAS
    public void onPlanGratuitoSelected() {
        handlePlanSelection("gratuito");
    }

    public void onPlanProSelected() {
        handlePlanSelection("pro");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}