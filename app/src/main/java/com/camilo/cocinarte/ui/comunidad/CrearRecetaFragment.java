package com.camilo.cocinarte.ui.comunidad;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.IngredientesService;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.databinding.FragmentCrearRecetaBinding;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearRecetaFragment extends Fragment {

    private FragmentCrearRecetaBinding binding;
    private Uri imagenUriSeleccionada;
    private final List<Ingrediente> _ingredientes = new ArrayList<>();
    private final List<String> ingredientesSeleccionados = new ArrayList<>();

    private Uri imagenUriCamara;


    private final ActivityResultLauncher<Intent> camaraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (imagenUriCamara != null) {
                        imagenUriSeleccionada = imagenUriCamara;
                        binding.photoImage.setImageURI(imagenUriSeleccionada);
                        binding.photoImage.setVisibility(View.VISIBLE);
                        binding.contenedorIcono.setVisibility(View.GONE);
                    }
                }
            }
    );


    private final ActivityResultLauncher<String> galeriaLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imagenUriSeleccionada = uri;
                    binding.photoImage.setImageURI(uri);
                    binding.photoImage.setVisibility(View.VISIBLE);
                    binding.contenedorIcono.setVisibility(View.GONE);
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearRecetaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.nutritionKcl.setEnabled(false);
        binding.nutritionP.setEnabled(false);
        binding.nutritionC.setEnabled(false);
        binding.nutritionGt.setEnabled(false);

        cargarIngredientesDesdeAPI();

        binding.searchIngrediente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarSugerencias(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnInstrucciones.setOnClickListener(v -> prepararDatosParaInstrucciones());

        binding.frameSeleccionImagen.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
    }

    private void mostrarDialogoSeleccionImagen() {
        String[] opciones = {"Cámara", "Galería"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        abrirCamara();
                    } else {
                        galeriaLauncher.launch("image/*");
                    }
                }).show();
    }


    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = crearArchivoImagen();
            if (photoFile != null) {
                imagenUriCamara = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".provider",
                        photoFile
                );
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUriCamara);
                camaraLauncher.launch(intent);
            }
        }
    }


    private void cargarIngredientesDesdeAPI() {
        LoginManager loginManager = new LoginManager(requireContext());
        String token = loginManager.getToken();
        IngredientesService service = ApiClient.getClient(getContext()).create(IngredientesService.class);

        service.obtenerTodosLosIngredientes("Bearer " + token).enqueue(new Callback<IngredientesByCategoriaResponse>() {
            @Override
            public void onResponse(Call<IngredientesByCategoriaResponse> call, Response<IngredientesByCategoriaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _ingredientes.clear();
                    _ingredientes.addAll(response.body().getIngredientes());
                } else {
                    Log.e("API", "Error al obtener ingredientes: código " + response.code());
                }
            }

            @Override
            public void onFailure(Call<IngredientesByCategoriaResponse> call, Throwable t) {
                Log.e("Fallo de red", t.toString());
            }
        });
    }

    private File crearArchivoImagen() {
        String nombreArchivo = "foto_" + System.currentTimeMillis();
        File directorio = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            return File.createTempFile(nombreArchivo, ".jpg", directorio);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarSugerencias(String texto) {
        binding.sugerenciasContainer.removeAllViews();
        if (texto.isEmpty()) return;

        for (Ingrediente ingrediente : _ingredientes) {
            if (ingrediente.getNombreIngrediente().toLowerCase().contains(texto.toLowerCase()) &&
                    !ingredientesSeleccionados.contains(ingrediente.getNombreIngrediente())) {

                TextView chip = crearChipVisual(ingrediente.getNombreIngrediente(), false);
                chip.setOnClickListener(v -> {
                    agregarIngredienteASeleccionados(ingrediente.getNombreIngrediente());
                    binding.searchIngrediente.setText("");
                });

                binding.sugerenciasContainer.addView(chip);
            }
        }
    }

    private void agregarIngredienteASeleccionados(String nombreIngrediente) {
        if (!ingredientesSeleccionados.contains(nombreIngrediente)) {
            ingredientesSeleccionados.add(nombreIngrediente);
            Toast.makeText(getContext(), nombreIngrediente + " agregado a la receta", Toast.LENGTH_SHORT).show();

            TextView chip = crearChipVisual(nombreIngrediente, true);
            chip.setOnClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(nombreIngrediente);
            });

            binding.listaIngredientes.addView(chip);
        }
    }

    private Chip crearChipVisual(String texto, boolean esSeleccionado) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setTextSize(14);
        chip.setChipBackgroundColorResource(R.color.chip_background);
        chip.setTextColor(getResources().getColor(android.R.color.black));

        // Estilo de chip
        chip.setChipCornerRadius(50f);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        chip.setLayoutParams(params);

        if (esSeleccionado) {
            chip.setCloseIconVisible(true);
            chip.setCloseIconResource(R.drawable.ic_chip_delete);
            chip.setOnCloseIconClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(texto);
            });
        }

        return chip;
    }


    private void prepararDatosParaInstrucciones() {
        String nombre = binding.recipeNameInput.getText().toString().trim();
        String kcal = binding.nutritionKcl.getText().toString().trim();
        String proteinas = binding.nutritionP.getText().toString().trim();
        String carbohidratos = binding.nutritionC.getText().toString().trim();
        String grasas = binding.nutritionGt.getText().toString().trim();

        String tiempoValor = binding.etTiempo.getText().toString().trim();
        String unidadTiempo = binding.spinnerUnidadTiempo.getSelectedItem().toString();
        String tiempo = tiempoValor + " " + unidadTiempo;
        String dificultad = binding.spinnerDificultad.getSelectedItem().toString();

        if (nombre.isEmpty() || tiempoValor.isEmpty() || ingredientesSeleccionados.isEmpty()
                || nombre.length() < 5 || nombre.length() > 100) {
            Toast.makeText(getContext(), "Completa todos los campos correctamente y agrega ingredientes", Toast.LENGTH_LONG).show();
            return;
        }

        if (imagenUriSeleccionada == null) {
            Toast.makeText(getContext(), "Debes seleccionar una imagen para tu receta", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("nombreReceta", nombre);
        bundle.putString("kcal", "0");
        bundle.putString("proteinas", "0");
        bundle.putString("carbohidratos", "0");
        bundle.putString("grasas", "0");
        bundle.putString("tiempo", tiempo);
        bundle.putString("unidad", unidadTiempo);
        bundle.putString("dificultad", dificultad);
        bundle.putString("imagenUri", imagenUriSeleccionada.toString());

        bundle.putInt("categoriaId", 1);

        StringBuilder ingredientesJson = new StringBuilder("[");
        for (String nombreIngrediente : ingredientesSeleccionados) {
            for (Ingrediente ing : _ingredientes) {
                if (ing.getNombreIngrediente().equals(nombreIngrediente)) {
                    ingredientesJson.append(ing.getIdIngrediente()).append(",");
                    break;
                }
            }
        }
        if (ingredientesJson.length() > 1) {
            ingredientesJson.deleteCharAt(ingredientesJson.length() - 1);
        }
        ingredientesJson.append("]");
        bundle.putString("ingredientes", ingredientesJson.toString());

        Navigation.findNavController(requireView())
                .navigate(R.id.action_crearRecetaFragment_to_instruccionesFragmentReceta, bundle);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


