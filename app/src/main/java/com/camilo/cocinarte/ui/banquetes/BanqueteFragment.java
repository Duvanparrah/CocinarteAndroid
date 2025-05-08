package com.camilo.cocinarte.ui.banquetes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.databinding.FragmentBanqueteBinding;

public class BanqueteFragment extends Fragment {

    private FragmentBanqueteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBanqueteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Aquí puedes usar binding.<tuVista> para manipular tus vistas directamente.
        // Por ejemplo:
        // binding.textTitulo.setText("Sección de Banquetes");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
