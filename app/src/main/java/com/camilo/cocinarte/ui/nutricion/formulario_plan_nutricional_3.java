package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class formulario_plan_nutricional_3 extends AppCompatActivity {

    private ImageView backArrow;
    private Button btnContinuar;
    private Spinner spinnerSexo;
    private EditText editTextEdad, editTextAltura, editTextPeso;

    // Variables para validación
    private String sexoSeleccionado = "";
    private boolean todosLosCamposCompletos = false;

    // Variables para recibir datos de formularios anteriores
    private String metodoPago;
    private String nombreUsuario;
    private String numeroNequi;
    private String tipoPlan;
    private String objetivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario_plan_nutricional3);

        // Sin configuración de insets para evitar errores de compatibilidad

        // Obtener datos del intent
        obtenerDatosIntent();

        // Inicializar vistas
        initViews();

        // Configurar listeners
        setupListeners();

        // Configurar Spinner de sexo
        configurarSpinnerSexo();

        // Deshabilitar botón continuar inicialmente
        deshabilitarBotonContinuar();

        // Mostrar información recibida
        mostrarInformacionRecibida();
    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            metodoPago = intent.getStringExtra("metodo_pago");
            nombreUsuario = intent.getStringExtra("nombre_usuario");
            numeroNequi = intent.getStringExtra("numero_nequi");
            tipoPlan = intent.getStringExtra("tipo_plan");
            objetivo = intent.getStringExtra("objetivo");
        }
    }

    private void initViews() {
        backArrow = findViewById(R.id.backArrow);
        btnContinuar = findViewById(R.id.btnContinuar);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        editTextEdad = findViewById(R.id.editTextEdad);
        editTextAltura = findViewById(R.id.editTextAltura);
        editTextPeso = findViewById(R.id.editTextPeso);
    }

    private void configurarSpinnerSexo() {
        // Crear array con opciones de sexo
        String[] opcionesSexo = {"Seleccionar sexo", "Masculino", "Femenino"};

        // Crear adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, opcionesSexo);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Asignar adapter al spinner
        spinnerSexo.setAdapter(adapter);

        // Configurar listener para detectar selección
        spinnerSexo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Si no es la opción por defecto
                    sexoSeleccionado = opcionesSexo[position];
                } else {
                    sexoSeleccionado = "";
                }
                validarCampos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sexoSeleccionado = "";
                validarCampos();
            }
        });
    }

    private void setupListeners() {
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarTodosLosCampos()) {
                    continuarSiguienteFormulario();
                }
            }
        });

        // Configurar listeners para EditTexts para validación en tiempo real
        configurarListenersEditText();
    }

    private void configurarListenersEditText() {
        // TextWatcher para validación en tiempo real mientras el usuario escribe
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Validar campos cada vez que el texto cambie
                validarCampos();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos hacer nada aquí
            }
        };

        // Aplicar el TextWatcher a todos los EditText
        editTextEdad.addTextChangedListener(textWatcher);
        editTextAltura.addTextChangedListener(textWatcher);
        editTextPeso.addTextChangedListener(textWatcher);

        // También mantener OnFocusChangeListener como validación adicional
        editTextEdad.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validarCampos();
                }
            }
        });

        editTextAltura.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validarCampos();
                }
            }
        });

        editTextPeso.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validarCampos();
                }
            }
        });
    }

    private void validarCampos() {
        boolean sexoCompleto = !sexoSeleccionado.isEmpty();
        boolean edadCompleta = !TextUtils.isEmpty(editTextEdad.getText().toString().trim()) &&
                editTextEdad.getText().toString().trim().length() > 0;
        boolean alturaCompleta = !TextUtils.isEmpty(editTextAltura.getText().toString().trim()) &&
                editTextAltura.getText().toString().trim().length() > 0;
        boolean pesoCompleto = !TextUtils.isEmpty(editTextPeso.getText().toString().trim()) &&
                editTextPeso.getText().toString().trim().length() > 0;

        todosLosCamposCompletos = sexoCompleto && edadCompleta && alturaCompleta && pesoCompleto;

        // Ejecutar en el hilo principal para evitar problemas de UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (todosLosCamposCompletos) {
                    habilitarBotonContinuar();
                } else {
                    deshabilitarBotonContinuar();
                }
            }
        });
    }

    private void habilitarBotonContinuar() {
        btnContinuar.setEnabled(true);
        btnContinuar.setAlpha(1.0f);
    }

    private void deshabilitarBotonContinuar() {
        btnContinuar.setEnabled(false);
        btnContinuar.setAlpha(0.5f);
    }

    private boolean validarTodosLosCampos() {
        // Validar sexo
        if (sexoSeleccionado.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona tu sexo", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar edad
        String edadStr = editTextEdad.getText().toString().trim();
        if (TextUtils.isEmpty(edadStr)) {
            Toast.makeText(this, "Por favor, ingresa tu edad", Toast.LENGTH_SHORT).show();
            editTextEdad.requestFocus();
            return false;
        }

        try {
            int edad = Integer.parseInt(edadStr);
            if (edad < 18 || edad > 99) {
                Toast.makeText(this, "Por favor, ingresa una edad válida (18-99 años)", Toast.LENGTH_SHORT).show();
                editTextEdad.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa una edad válida", Toast.LENGTH_SHORT).show();
            editTextEdad.requestFocus();
            return false;
        }

        // Validar altura
        String alturaStr = editTextAltura.getText().toString().trim();
        if (TextUtils.isEmpty(alturaStr)) {
            Toast.makeText(this, "Por favor, ingresa tu altura en centímetros", Toast.LENGTH_SHORT).show();
            editTextAltura.requestFocus();
            return false;
        }

        try {
            int altura = Integer.parseInt(alturaStr);
            if (altura < 100 || altura > 250) {
                Toast.makeText(this, "Por favor, ingresa una altura válida (100-250 cm)", Toast.LENGTH_SHORT).show();
                editTextAltura.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa una altura válida", Toast.LENGTH_SHORT).show();
            editTextAltura.requestFocus();
            return false;
        }

        // Validar peso
        String pesoStr = editTextPeso.getText().toString().trim();
        if (TextUtils.isEmpty(pesoStr)) {
            Toast.makeText(this, "Por favor, ingresa tu peso en kilogramos", Toast.LENGTH_SHORT).show();
            editTextPeso.requestFocus();
            return false;
        }

        try {
            float peso = Float.parseFloat(pesoStr);
            if (peso < 30 || peso > 300) {
                Toast.makeText(this, "Por favor, ingresa un peso válido (30-300 kg)", Toast.LENGTH_SHORT).show();
                editTextPeso.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un peso válido", Toast.LENGTH_SHORT).show();
            editTextPeso.requestFocus();
            return false;
        }

        return true;
    }

    private void mostrarInformacionRecibida() {
        String mensaje = "Datos recibidos - ";
        mensaje += "Plan: " + (tipoPlan != null ? tipoPlan : "No especificado") + " | ";
        mensaje += "Objetivo: " + (objetivo != null ? obtenerNombreObjetivo(objetivo) : "No seleccionado");

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private String obtenerNombreObjetivo(String obj) {
        switch (obj) {
            case "perder_grasa":
                return "Perder Grasa";
            case "ganar_musculo":
                return "Ganar Masa Muscular";
            case "mantener_peso":
                return "Mantener Peso";
            default:
                return obj;
        }
    }

    private void continuarSiguienteFormulario() {
        // Recopilar todos los datos
        String edad = editTextEdad.getText().toString().trim();
        String altura = editTextAltura.getText().toString().trim();
        String peso = editTextPeso.getText().toString().trim();

        Toast.makeText(this, "Datos guardados: " + sexoSeleccionado + ", " + edad + " años, "
                + altura + " cm, " + peso + " kg", Toast.LENGTH_LONG).show();

        // Navegar al siguiente formulario
        Intent intent = new Intent(this, formulario_plan_nutricional_4.class);
        intent.putExtra("metodo_pago", metodoPago);
        intent.putExtra("nombre_usuario", nombreUsuario);
        intent.putExtra("numero_nequi", numeroNequi);
        intent.putExtra("tipo_plan", tipoPlan);
        intent.putExtra("objetivo", objetivo);
        intent.putExtra("sexo", sexoSeleccionado);
        intent.putExtra("edad", edad);
        intent.putExtra("altura", altura);
        intent.putExtra("peso", peso);

        startActivity(intent);
    }
}