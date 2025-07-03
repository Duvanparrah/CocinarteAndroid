package com.camilo.cocinarte.ui.comunidad;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.IngredientesService;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.databinding.FragmentCrearRecetaBinding;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearRecetaFragment extends Fragment {

    private FragmentCrearRecetaBinding binding;
    private Uri imagenUriSeleccionada;
    private List<Ingrediente> _ingredientes = new ArrayList<>();
    private final List<String> ingredientesSeleccionados = new ArrayList<>();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    imagenUriSeleccionada = uri;
                    binding.photoImage.setImageURI(uri);
                    binding.photoImage.setVisibility(View.VISIBLE);
                    binding.contenedorIcono.setVisibility(View.GONE);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCrearRecetaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();

        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String categoria = parent.getItemAtPosition(position).toString();
                cargarIngredientesPorCategoria(categoria, tokenGuardado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.searchIngrediente.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarSugerencias(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.searchIngrediente.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String categoria = binding.spinnerCategoria.getSelectedItem().toString();
                if (categoria.equals("Selecciona una categoría")) {
                    binding.searchIngrediente.clearFocus(); // Evita que el teclado aparezca
                    Toast.makeText(getContext(), "Primero selecciona una categoría", Toast.LENGTH_SHORT).show();
                }
            }
        });


        binding.btnInstrucciones.setOnClickListener(v -> prepararDatosParaInstrucciones());

        binding.frameSeleccionImagen.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
    }

    private void cargarIngredientesPorCategoria(String categoria, String token) {
        IngredientesService service = ApiClient.getClient(getContext()).create(IngredientesService.class);
        service.obtenerIngredientesPorCategoria(categoria, "Bearer " + token)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<IngredientesByCategoriaResponse> call, Response<IngredientesByCategoriaResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            _ingredientes = response.body().getIngredientes();
                        } else {
                            Log.e("CATEGORIA ERROR", "Código: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<IngredientesByCategoriaResponse> call, Throwable t) {
                        Log.e("API FAIL", t.getMessage(), t);
                    }
                });
    }

    private void mostrarSugerencias(String texto) {
        binding.sugerenciasContainer.removeAllViews();
        if (texto.isEmpty()) return;

        for (Ingrediente ingrediente : _ingredientes) {
            if (ingrediente.getNombreIngrediente().toLowerCase().contains(texto.toLowerCase()) &&
                    !ingredientesSeleccionados.contains(ingrediente.getNombreIngrediente())) {

                TextView chip = crearChipVisual(ingrediente.getNombreIngrediente());
                chip.setOnClickListener(v -> {
                    agregarIngredienteASeleccionados(ingrediente.getNombreIngrediente());
                    binding.sugerenciasContainer.removeAllViews();
                    binding.searchIngrediente.setText("");
                });

                binding.sugerenciasContainer.addView(chip);
            }
        }
    }

    private TextView crearChipVisual(String texto) {
        TextView tv = new TextView(getContext());
        tv.setText(texto);
        tv.setTextSize(14);
        tv.setTextColor(getResources().getColor(android.R.color.black));
        tv.setPadding(30, 20, 30, 20);
        tv.setBackgroundResource(R.drawable.bg_chip);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        tv.setLayoutParams(params);

        return tv;
    }

    private void agregarIngredienteASeleccionados(String nombreIngrediente) {
        if (!ingredientesSeleccionados.contains(nombreIngrediente)) {
            ingredientesSeleccionados.add(nombreIngrediente);

            TextView chip = crearChipVisual(nombreIngrediente);
            chip.setOnClickListener(v -> {
                binding.listaIngredientes.removeView(chip);
                ingredientesSeleccionados.remove(nombreIngrediente);
            });

            binding.listaIngredientes.addView(chip);
        }
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

        String categoriaSeleccionada = binding.spinnerCategoria.getSelectedItem().toString();
        String dificultadSeleccionada = binding.spinnerDificultad.getSelectedItem().toString();

        if (nombre.isEmpty() || kcal.isEmpty() || proteinas.isEmpty() || carbohidratos.isEmpty()
                || grasas.isEmpty() || tiempoValor.isEmpty() || ingredientesSeleccionados.isEmpty()
                || nombre.length() < 5 || nombre.length() > 100) {
            Toast.makeText(getContext(), "Por favor, completa todos los campos y agrega al menos un ingrediente", Toast.LENGTH_LONG).show();
            return;
        }

        if (categoriaSeleccionada.equals("Selecciona una categoría")) {
            Toast.makeText(getContext(), "Por favor selecciona una categoría válida", Toast.LENGTH_LONG).show();
            return;
        }

        if (imagenUriSeleccionada == null) {
            Toast.makeText(getContext(), "Debes seleccionar una imagen para tu receta", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("nombreReceta", nombre);
        bundle.putString("kcal", kcal);
        bundle.putString("proteinas", proteinas);
        bundle.putString("carbohidratos", carbohidratos);
        bundle.putString("grasas", grasas);
        bundle.putString("tiempo", tiempo);
        bundle.putString("unidad", unidadTiempo);
        bundle.putString("dificultad", dificultadSeleccionada);
        bundle.putString("categoria", categoriaSeleccionada);
        bundle.putString("imagenUri", imagenUriSeleccionada.toString());

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
            ingredientesJson.deleteCharAt(ingredientesJson.length() - 1); // eliminar última coma
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
