package com.camilo.cocinarte.ui.comunidad;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.databinding.FragmentComunidadMisRecetasBinding;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Comunidad_mis_recetasFragment extends Fragment {

    private FragmentComunidadMisRecetasBinding binding;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadMisRecetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupImagePickerLauncher();
        setupPermissionLauncher();
        setupClickListeners();
        cargarDatosUsuario();
        cargarRecetasGuardadas();
    }

    private void setupImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            guardarYMostrarImagen(imageUri);
                        }
                    }
                });
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        abrirSelectorImagenes();
                    } else {
                        Toast.makeText(requireContext(),
                                "Permiso necesario para cambiar la imagen de perfil",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupClickListeners() {
        // Botón para cambiar imagen de perfil
        binding.btnChangeImage.setOnClickListener(v -> verificarPermisos());

        // Botón de búsqueda
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Botón de menú
        binding.menuButton.setOnClickListener(v -> openMenu());

        // Pestaña Comunidad
        binding.comunidadTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_navegar_comunidad));

        // Pestaña Mis Recetas
        binding.misRecetasTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Mis recetas", Toast.LENGTH_SHORT).show());

        // Botón Crear Receta
        binding.btnCrearReceta.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_crearRecetaFragment));
    }

    private void verificarPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                abrirSelectorImagenes();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                mostrarExplicacionPermisos(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 6.0+ (API 23+)
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                abrirSelectorImagenes();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                mostrarExplicacionPermisos(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void mostrarExplicacionPermisos(String permiso) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permiso necesario")
                .setMessage("Para cambiar tu foto de perfil, necesitamos acceso a tus imágenes")
                .setPositiveButton("Entendido", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirSelectorImagenes() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al abrir la galería", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void guardarYMostrarImagen(Uri imageUri) {
        try {
            // Guardar la URI en SharedPreferences
            SharedPreferences preferences = requireContext()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            preferences.edit().putString("profile_image_uri", imageUri.toString()).apply();

            // Cargar la nueva imagen
            cargarImagenPerfil(imageUri);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void cargarDatosUsuario() {
        // Cargar imagen de perfil guardada
        SharedPreferences preferences = requireContext()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedImageUri = preferences.getString("profile_image_uri", null);

        if (savedImageUri != null) {
            try {
                cargarImagenPerfil(Uri.parse(savedImageUri));
            } catch (Exception e) {
                binding.userProfileImage.setImageResource(R.drawable.ic_cuenta_configuracion);
            }
        } else {
            binding.userProfileImage.setImageResource(R.drawable.ic_cuenta_configuracion);
        }

        // Configurar datos de usuario
        binding.userEmail.setText("cristiancl7@gmail.com");
        binding.userName.setText("cristian015");
    }

    private void cargarImagenPerfil(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .placeholder(R.drawable.ic_cuenta_configuracion)
                .error(R.drawable.ic_cuenta_configuracion)
                .into(binding.userProfileImage);
    }

    private void performSearch() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
            // Aquí implementarías la lógica de búsqueda real
        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();
        // Aquí implementarías la lógica para abrir el menú
    }

    private void cargarRecetasGuardadas() {
        LinearLayout contenedorPrincipal = binding.contenedorRecetas;
        contenedorPrincipal.removeAllViews();
        contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);

        // Obtener el ancho de pantalla
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int mitadAncho = screenWidth / 2;

        // Obtener recetas guardadas
        String json = requireContext()
                .getSharedPreferences("recetas", Context.MODE_PRIVATE)
                .getString("lista_recetas", null);

        List<Receta> recetas = new ArrayList<>();
        if (json != null) {
            recetas = new Gson().fromJson(json, new TypeToken<List<Receta>>() {}.getType());
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Mostrar recetas en grid de 2 columnas
        for (int i = 0; i < recetas.size(); i += 2) {
            LinearLayout fila = new LinearLayout(getContext());
            fila.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            fila.setOrientation(LinearLayout.HORIZONTAL);

            // Primera receta (izquierda)
            if (i < recetas.size()) {
                View item = inflarItemReceta(inflater, fila, recetas.get(i), mitadAncho);
                fila.addView(item);
            }

            // Segunda receta (derecha)
            if (i + 1 < recetas.size()) {
                View item = inflarItemReceta(inflater, fila, recetas.get(i + 1), mitadAncho);
                fila.addView(item);
            }

            contenedorPrincipal.addView(fila);
        }
    }

    private View inflarItemReceta(LayoutInflater inflater, ViewGroup parent, Receta receta, int ancho) {
        View item = inflater.inflate(R.layout.item_receta, parent, false);

        // Configurar tamaño del item
        ViewGroup.LayoutParams params = item.getLayoutParams();
        params.width = ancho;
        item.setLayoutParams(params);

        // Configurar vista
        ImageView ivImagen = item.findViewById(R.id.iv_imagen_receta);
        TextView tvNombre = item.findViewById(R.id.tv_nombre_receta);

        tvNombre.setText(receta.getNombre());
        Glide.with(this)
                .load(receta.getImagenUri())
                .override(ancho, 180)
                .centerCrop()
                .into(ivImagen);

        // Configurar click listener
        item.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("receta", new Gson().toJson(receta));
            Navigation.findNavController(v)
                    .navigate(R.id.action_navegar_comunidad_mis_recetas_to_detalleRecetaFragment, bundle);
        });

        return item;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}