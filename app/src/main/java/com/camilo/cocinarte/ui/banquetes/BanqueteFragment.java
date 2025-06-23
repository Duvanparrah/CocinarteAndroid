package com.camilo.cocinarte.ui.banquetes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.databinding.FragmentBanqueteBinding;

public class BanqueteFragment extends Fragment {

    private FragmentBanqueteBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBanqueteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());
        binding.menuButton.setOnClickListener(v -> openMenu());
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(getContext(), "Buscando banquete: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        Toast.makeText(getContext(), "Menú de banquetes abierto", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
