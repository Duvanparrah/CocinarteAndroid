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
import android.util.Log;
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
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentComunidadMisRecetasBinding;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

            SharedPreferences preferences = requireContext()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            preferences.edit().putString("profile_image_uri", imageUri.toString()).apply();


            cargarImagenPerfil(imageUri);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void cargarDatosUsuario() {

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

        } else {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMenu() {
        Toast.makeText(getContext(), "Menú abierto", Toast.LENGTH_SHORT).show();

    }

    private void cargarRecetasGuardadas() {
        RecetaApi recetaApi = ApiClient.getClient(getContext()).create(RecetaApi.class);

        LoginManager loginManager = new LoginManager(getContext());
        String tokenGuardado = loginManager.getToken();
        recetaApi.getRecetas("Bearer " + tokenGuardado).enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {

                if (response.isSuccessful()) {
                    List<Receta> recetas = response.body();

                    LinearLayout contenedorPrincipal = binding.contenedorRecetas;
                    contenedorPrincipal.removeAllViews();
                    contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);


                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    int mitadAncho = screenWidth / 2;

                    LayoutInflater inflater = LayoutInflater.from(getContext());

                    for (int i = 0; i < Objects.requireNonNull(recetas).size(); i += 2) {
                        LinearLayout fila = new LinearLayout(getContext());
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        fila.setOrientation(LinearLayout.HORIZONTAL);

                        if (i < recetas.size()) {
                            View item = inflarItemReceta(inflater, fila, recetas.get(i), mitadAncho);
                            fila.addView(item);
                        }

                        if (i + 1 < recetas.size()) {
                            View item = inflarItemReceta(inflater, fila, recetas.get(i + 1), mitadAncho);
                            fila.addView(item);
                        }

                        contenedorPrincipal.addView(fila);
                    }
                } else {
                    Log.e(">> >>error regrofit", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.e(">> >>failuere", "Error en conexión: " + t.getMessage());
            }
        });
    }

    private View inflarItemReceta(LayoutInflater inflater, ViewGroup parent, Receta receta, int ancho) {
        View item = inflater.inflate(R.layout.item_receta, parent, false);

        // Configurar tamaño del item
        ViewGroup.LayoutParams params = item.getLayoutParams();
        params.width = ancho;
        item.setLayoutParams(params);

        ImageView ivImagen = item.findViewById(R.id.iv_imagen_receta);
        TextView tvNombre = item.findViewById(R.id.tv_nombre_receta);

        //tvNombre.setText(receta.getTitulo());
        Glide.with(this)
                .load(receta.getImagen())
                .fitCenter()
                .into(ivImagen);

        tvNombre.setText(receta.getTitulo());

        item.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("receta", new Gson().toJson(receta));
            bundle.putString("origen", "mis_recetas");
            Navigation.findNavController(v).navigate(R.id.action_navegar_comunidad_mis_recetas_to_detalleRecetaFragment, bundle);
        });

        return item;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}