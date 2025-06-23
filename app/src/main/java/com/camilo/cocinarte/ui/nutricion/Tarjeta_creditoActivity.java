package com.camilo.cocinarte.ui.nutricion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.camilo.cocinarte.R;

public class Tarjeta_creditoActivity extends AppCompatActivity {

    private EditText edtNombre, edtCC, edtNumeroTarjeta, edtFechaExpiracion, edtCVC;
    private TextView txtPais;
    private Button btnFinalizarCompra;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tarjeta_credito);

        // Configurar insets para el modo edge-to-edge de forma segura
        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        inicializarVistas();

        // Configurar eventos
        configurarEventos();

        // Configurar formateador para fecha de expiración
        configurarFormatoFecha();
    }

    private void inicializarVistas() {
        edtNombre = findViewById(R.id.edtNombre);
        edtCC = findViewById(R.id.edtCC);
        edtNumeroTarjeta = findViewById(R.id.edtNumeroTarjeta);
        edtFechaExpiracion = findViewById(R.id.edtFechaExpiracion);
        edtCVC = findViewById(R.id.edtCVC);
        txtPais = findViewById(R.id.txtPais);
        btnFinalizarCompra = findViewById(R.id.btnFinalizarCompra);
        btnBack = findViewById(R.id.btnBack);
    }

    private void configurarEventos() {
        // Botón regresar
        btnBack.setOnClickListener(v -> finish());

        // Botón finalizar compra
        btnFinalizarCompra.setOnClickListener(v -> {
            if (validarDatos()) {
                finalizarCompra();
            }
        });
    }

    private void configurarFormatoFecha() {
        edtFechaExpiracion.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;
            String separador = "/";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario para el formateo
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No necesario para el formateo
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }

                isFormatting = true;

                // Eliminar caracteres no numéricos
                String texto = s.toString().replaceAll("[^\\d]", "");

                // Formatear MM/AA
                StringBuilder formateado = new StringBuilder();
                for (int i = 0; i < texto.length() && i < 4; i++) {
                    if (i == 2) {
                        formateado.append(separador);
                    }
                    formateado.append(texto.charAt(i));
                }

                s.replace(0, s.length(), formateado.toString());

                isFormatting = false;
            }
        });
    }

    private boolean validarDatos() {
        // Validar nombre
        if (TextUtils.isEmpty(edtNombre.getText())) {
            mostrarError("Por favor, ingrese su nombre");
            return false;
        }

        // Validar CC
        if (TextUtils.isEmpty(edtCC.getText())) {
            mostrarError("Por favor, ingrese su número de CC");
            return false;
        }

        // Validar número de tarjeta
        String numeroTarjeta = edtNumeroTarjeta.getText().toString();
        if (TextUtils.isEmpty(numeroTarjeta) || numeroTarjeta.length() < 13 || numeroTarjeta.length() > 16) {
            mostrarError("Por favor, ingrese un número de tarjeta válido");
            return false;
        }

        // Validar fecha de expiración
        String fechaExp = edtFechaExpiracion.getText().toString();
        if (TextUtils.isEmpty(fechaExp) || fechaExp.length() != 5 || !fechaExp.contains("/")) {
            mostrarError("Por favor, ingrese una fecha de expiración válida (MM/AA)");
            return false;
        }

        // Validar mes
        try {
            int mes = Integer.parseInt(fechaExp.substring(0, 2));
            if (mes < 1 || mes > 12) {
                mostrarError("Mes de expiración inválido");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Formato de fecha inválido");
            return false;
        }

        // Validar CVC
        String cvc = edtCVC.getText().toString();
        if (TextUtils.isEmpty(cvc) || cvc.length() < 3 || cvc.length() > 4) {
            mostrarError("Por favor, ingrese un código CVC válido");
            return false;
        }

        return true;
    }

    private void mostrarError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void finalizarCompra() {
        // Aquí recogemos y procesamos la información
        String nombre = edtNombre.getText().toString();
        String cc = edtCC.getText().toString();
        String numeroTarjeta = edtNumeroTarjeta.getText().toString();
        String fechaExpiracion = edtFechaExpiracion.getText().toString();
        String cvc = edtCVC.getText().toString();
        String pais = txtPais.getText().toString();

        // Aquí iría el código para procesar el pago con la información recolectada
        // Por ejemplo, crear un objeto con los datos o enviarlos a un servicio

        // Mostrar mensaje de confirmación
        Toast.makeText(this, "Pago procesado con éxito", Toast.LENGTH_LONG).show();

        // Cerrar la actividad
        finish();
    }
}