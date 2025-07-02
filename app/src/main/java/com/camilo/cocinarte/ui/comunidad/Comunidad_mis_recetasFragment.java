package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentComunidadMisRecetasBinding;
import com.camilo.cocinarte.models.Receta;
import com.camilo.cocinarte.models.Usuario;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comunidad_mis_recetasFragment extends Fragment {

    private FragmentComunidadMisRecetasBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentComunidadMisRecetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
        cargarDatosUsuario();
        cargarRecetasGuardadas();
    }

    private void setupClickListeners() {
        binding.searchButton.setOnClickListener(v -> performSearch());
        binding.menuButton.setOnClickListener(v -> openMenu());

        binding.comunidadTab.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_navegar_comunidad));

        binding.misRecetasTab.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ya estás en Mis recetas", Toast.LENGTH_SHORT).show());

        binding.btnCrearReceta.setOnClickListener(v ->
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_navegar_comunidad_mis_recetas_to_crearRecetaFragment));
    }

    private void cargarDatosUsuario() {
        SharedPreferences preferences = requireContext()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String savedImageUri = preferences.getString("profile_image_uri", null);

        if (savedImageUri != null) {
            try {
                cargarImagenPerfil(Uri.parse(savedImageUri));
            } catch (Exception e) {
                binding.userProfileImage.setImageResource(R.drawable.perfil);
            }
        } else {
            binding.userProfileImage.setImageResource(R.drawable.perfil);
        }

        LoginManager loginManager = new LoginManager(requireContext());
        Usuario usuario = loginManager.getUsuario();

        if (usuario != null) {
            binding.userEmail.setText(usuario.getCorreo());
            binding.userName.setText(usuario.getNombreUsuario());
        } else {
            binding.userEmail.setText("Correo no disponible");
            binding.userName.setText("Usuario desconocido");
        }
    }

    private void cargarImagenPerfil(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .circleCrop()
                .placeholder(R.drawable.perfil)
                .error(R.drawable.perfil)
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
        Usuario usuario = loginManager.getUsuario();

        if (usuario == null) {
            Toast.makeText(getContext(), "No se pudo obtener el usuario actual", Toast.LENGTH_SHORT).show();
            return;
        }

        int idUsuarioActual = usuario.getIdUsuario();

        Call<List<Receta>> call = recetaApi.getRecetas("Bearer " + tokenGuardado);

        call.enqueue(new Callback<List<Receta>>() {
            @Override
            public void onResponse(Call<List<Receta>> call, Response<List<Receta>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Receta> recetas = response.body();
                    List<Receta> recetasDelUsuario = new ArrayList<>();
                    for (Receta r : recetas) {
                        if (r.getIdUsuario() == idUsuarioActual) {
                            recetasDelUsuario.add(r);
                        }
                    }

                    LinearLayout contenedorPrincipal = binding.contenedorRecetas;
                    contenedorPrincipal.removeAllViews();
                    contenedorPrincipal.setOrientation(LinearLayout.VERTICAL);

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int screenWidth = displayMetrics.widthPixels;
                    int mitadAncho = screenWidth / 2;

                    LayoutInflater inflater = LayoutInflater.from(getContext());

                    for (int i = 0; i < recetasDelUsuario.size(); i += 2) {
                        LinearLayout fila = new LinearLayout(getContext());
                        fila.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        fila.setOrientation(LinearLayout.HORIZONTAL);

                        View item1 = inflarItemReceta(inflater, fila, recetasDelUsuario.get(i), mitadAncho);
                        fila.addView(item1);

                        if (i + 1 < recetasDelUsuario.size()) {
                            View item2 = inflarItemReceta(inflater, fila, recetasDelUsuario.get(i + 1), mitadAncho);
                            fila.addView(item2);
                        }

                        contenedorPrincipal.addView(fila);
                    }
                } else {
                    Log.e("RECETAS_ERROR", "Respuesta fallida: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Receta>> call, Throwable t) {
                Log.e("RECETAS_ERROR", "Error al obtener recetas: " + t.getMessage());
            }
        });
    }

    private View inflarItemReceta(LayoutInflater inflater, ViewGroup parent, Receta receta, int ancho) {
        View item = inflater.inflate(R.layout.item_receta, parent, false);

        ViewGroup.LayoutParams params = item.getLayoutParams();
        params.width = ancho;
        item.setLayoutParams(params);

        ImageView ivImagen = item.findViewById(R.id.iv_imagen_receta);
        TextView tvNombre = item.findViewById(R.id.tv_nombre_receta);

        Glide.with(this)
                .load(receta.getImagen())
                .fitCenter()
                .into(ivImagen);

        tvNombre.setText(receta.getTitulo());

        item.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("id_receta", receta.getIdReceta());
            bundle.putString("origen", "mis_recetas");
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