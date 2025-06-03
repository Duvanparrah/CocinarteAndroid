package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.databinding.FragmentNutricionBinding;

public class NutricionFragment extends Fragment {

    private FragmentNutricionBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNutricionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar oyentes de eventos
        setupEventListeners();

        return root;
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
            Toast.makeText(getContext(), "Filtrar planes (funcionalidad pendiente)", Toast.LENGTH_SHORT).show();
            // Aquí puedes abrir un diálogo de filtros, si lo deseas
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}