package com.camilo.cocinarte.ui.comunidad;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.databinding.FragmentComunidadBinding;

public class ComunidadFragment extends Fragment {

    private FragmentComunidadBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentComunidadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Aquí puedes trabajar con tus vistas directamente
        // Por ejemplo:
        // binding.tituloComunidad.setText("Comunidad");

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
