package com.camilo.cocinarte.ui.nutricion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        // Aquí puedes trabajar con tus vistas directamente usando binding
        // Ejemplo: binding.tituloNutricion.setText("Plan Nutricional");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
