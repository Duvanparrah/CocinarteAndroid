package com.camilo.cocinarte.ui.comunidad;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.camilo.cocinarte.api.RecetaApi;
import com.camilo.cocinarte.databinding.FragmentCrearRecetaBinding;
import com.camilo.cocinarte.models.Ingrediente;
import com.camilo.cocinarte.models.IngredientesByCategoriaResponse;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CrearRecetaFragment extends Fragment {

    private FragmentCrearRecetaBinding binding;
    private Uri imagenUriSeleccionada;
    List<Ingrediente> _ingredientes = new ArrayList<>();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCrearRecetaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LoginManager loginManager = new LoginManager(requireContext());
        String tokenGuardado = loginManager.getToken();


        /*binding.btnAgregarIngrediente.setOnClickListener(v -> {
            String ingrediente = binding.etIngrediente.getText().toString().trim();
            if (!TextUtils.isEmpty(ingrediente)) {
                agregarIngrediente(ingrediente);
                binding.etIngrediente.setText("");
            }
        }); */

        //Al seleccionar la categoría hace una petición a ingredientes por categoría
        binding.spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String categoria = parentView.getItemAtPosition(position).toString();
                IngredientesService ingredientesService = ApiClient.getClient(getContext()).create(IngredientesService.class);
                Call<IngredientesByCategoriaResponse> call = ingredientesService.obtenerIngredientesPorCategoria(categoria, "Bearer " + tokenGuardado);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<IngredientesByCategoriaResponse> call, Response<IngredientesByCategoriaResponse> response) {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            _ingredientes = response.body().getIngredientes();
                            actualizarSpinner(response.body().getIngredientes());
                        } else {
                            Log.e("|||No successful", "Error al obtener ingredientes: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<IngredientesByCategoriaResponse> call, Throwable t) {
                        Log.e("|||Failure", "Error en conexión: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        binding.btnInstrucciones.setOnClickListener(v -> {

            String nombre = binding.recipeNameInput.getText().toString().trim();
            String kcal = binding.nutritionKcl.getText().toString().trim();
            String proteinas = binding.nutritionP.getText().toString().trim();
            String carbohidratos = binding.nutritionC.getText().toString().trim();
            String grasas = binding.nutritionGt.getText().toString().trim();
            String tiempo = binding.etTiempo.getText().toString().trim();
            int ingredientesCount = binding.listaIngredientes.getChildCount();

            if (nombre.isEmpty() || kcal.isEmpty() || proteinas.isEmpty() || carbohidratos.isEmpty()
                    || grasas.isEmpty() || tiempo.isEmpty() || ingredientesCount == 0 || nombre.length() < 5 || nombre.length() > 100) {
                Toast.makeText(getContext(), "Por favor, completa todos los campos y agrega al menos un ingrediente", Toast.LENGTH_LONG).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("nombreReceta", nombre);
            bundle.putString("kcal", kcal);
            bundle.putString("proteinas", proteinas);
            bundle.putString("carbohidratos", carbohidratos);
            bundle.putString("grasas", grasas);
            bundle.putString("tiempo", tiempo);
            bundle.putString("unidad", binding.spinnerUnidadTiempo.getSelectedItem().toString());
            bundle.putString("dificultad", binding.spinnerDificultad.getSelectedItem().toString());
            bundle.putString("categoria", binding.spinnerCategoria.getSelectedItem().toString());

            StringBuilder ingredientes = new StringBuilder();
            ingredientes.append("[");
            for (int i = 0; i < ingredientesCount; i++) {
                TextView tv = (TextView) binding.listaIngredientes.getChildAt(i);

                Ingrediente ingredienteBuscado  = null;
                for (Ingrediente ingrediente : _ingredientes) {
                    if (ingrediente.getNombreIngrediente().equals(tv.getText().toString())) {
                        ingredienteBuscado  = ingrediente;
                        break; // Si encuentras el ingrediente, puedes salir del bucle
                    }
                }
                ingredientes.append("{\"id\":"+ingredienteBuscado.getIdIngrediente()+",  \"cantidad\":"+  1).append("},");
            }
            // Elimina la última coma si es necesario
            if (ingredientes.length() > 1) {
                ingredientes.deleteCharAt(ingredientes.length() - 1); // Elimina la última coma
            }
            ingredientes.append("]");
            bundle.putString("ingredientes", ingredientes.toString());

            //ingredientes.append(tv.getText().toString()).append(";");

            if (imagenUriSeleccionada != null) {
                bundle.putString("imagenUri", imagenUriSeleccionada.toString());
            }

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_crearRecetaFragment_to_instruccionesFragmentReceta, bundle);
        });

        // Seleccionar imagen
        binding.frameSeleccionImagen.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()
            );
        });

    }



    private void actualizarSpinner(List<Ingrediente> _ingredientes) {
        List<String> ingredientes = new ArrayList<>();

        // Iterar sobre la lista de Ingrediente y extraer los nombres
        for (Ingrediente ingrediente : _ingredientes) {
            ingredientes.add(ingrediente.getNombreIngrediente());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, ingredientes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerIngredientes.setAdapter(adapter);

        // Opcional: si quieres manejar la selección de un ítem
        binding.spinnerIngredientes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String ingrediente = parentView.getItemAtPosition(position).toString();
                if (!TextUtils.isEmpty(ingrediente)) {
                    agregarIngrediente(ingrediente);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Manejar el caso en que no se selecciona nada
            }
        });
    }

    private void agregarIngrediente(String ingrediente) {
        TextView tvIngrediente = new TextView(getContext());
        tvIngrediente.setText(ingrediente);
        tvIngrediente.setTextSize(14);
        tvIngrediente.setTextColor(getResources().getColor(android.R.color.black));
        tvIngrediente.setPadding(30, 20, 30, 20);
        tvIngrediente.setBackgroundResource(R.drawable.bg_chip);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        tvIngrediente.setLayoutParams(params);

        tvIngrediente.setOnClickListener(v -> binding.listaIngredientes.removeView(tvIngrediente));

        binding.listaIngredientes.addView(tvIngrediente);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


