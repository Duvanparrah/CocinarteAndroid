package com.camilo.cocinarte.ui.comunidad;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.camilo.cocinarte.databinding.FragmentCrearRecetaBinding;

public class CrearRecetaFragment extends Fragment {

    private FragmentCrearRecetaBinding binding;
    private Uri imagenUriSeleccionada;

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


        binding.btnAgregarIngrediente.setOnClickListener(v -> {
            String ingrediente = binding.etIngrediente.getText().toString().trim();
            if (!TextUtils.isEmpty(ingrediente)) {
                agregarIngrediente(ingrediente);
                binding.etIngrediente.setText("");
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
                    || grasas.isEmpty() || tiempo.isEmpty() || ingredientesCount == 0) {
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

            StringBuilder ingredientes = new StringBuilder();
            for (int i = 0; i < ingredientesCount; i++) {
                TextView tv = (TextView) binding.listaIngredientes.getChildAt(i);
                ingredientes.append(tv.getText().toString()).append(",");
            }
            bundle.putString("ingredientes", ingredientes.toString());

            if (imagenUriSeleccionada != null) {
                bundle.putString("imagenUri", imagenUriSeleccionada.toString());
            }

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_crearRecetaFragment_to_instruccionesFragmentReceta, bundle);
        });

        // Seleccionar imagen
        binding.frameSeleccionImagen.setOnClickListener(v -> {
            pickMedia.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
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


