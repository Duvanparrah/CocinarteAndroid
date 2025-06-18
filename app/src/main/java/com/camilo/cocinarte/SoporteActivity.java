package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.ExpandableListAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class SoporteActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    ImageButton btnBack;
    TextView tvTitulo;
    HashMap<String, List<String>> listaPreguntas;
    List<String> listaTitulos;
    ExpandableListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soporte);

        expandableListView = findViewById(R.id.expandableListView);
        btnBack = findViewById(R.id.btnBack);
        tvTitulo = findViewById(R.id.tvTitulo);

        // Acción al presionar el botón de retroceso personalizado
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SoporteActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Inicializamos títulos y respuestas
        listaTitulos = new ArrayList<>();
        listaPreguntas = new HashMap<>();

        listaTitulos.add("¿Cómo puedo recuperar mi contraseña?");
        listaTitulos.add("¿Dónde puedo ver mis compras?");
        listaTitulos.add("¿Cómo contacto al soporte?");

        List<String> respuesta1 = Collections.singletonList("Para recuperar tu contraseña, ve a 'Iniciar sesión' y presiona '¿Olvidaste tu contraseña?'.");
        List<String> respuesta2 = Collections.singletonList("Tus compras están en el menú principal, sección 'Mis pedidos'.");
        List<String> respuesta3 = Collections.singletonList("Puedes escribirnos al correo soporte@ejemplo.com o desde la app en 'Contáctanos'.");

        listaPreguntas.put(listaTitulos.get(0), respuesta1);
        listaPreguntas.put(listaTitulos.get(1), respuesta2);
        listaPreguntas.put(listaTitulos.get(2), respuesta3);

        // Adaptador para mostrar preguntas y respuestas
        listAdapter = new SimpleExpandableListAdapter(
                this,
                crearGrupo(listaTitulos),
                android.R.layout.simple_expandable_list_item_1,
                new String[]{"titulo"},
                new int[]{android.R.id.text1},
                crearHijos(listaPreguntas),
                android.R.layout.simple_list_item_1,
                new String[]{"contenido"},
                new int[]{android.R.id.text1}
        );

        expandableListView.setAdapter(listAdapter);
    }

    // Grupo de preguntas (títulos)
    private List<Map<String, String>> crearGrupo(List<String> titulos) {
        List<Map<String, String>> data = new ArrayList<>();
        for (String titulo : titulos) {
            Map<String, String> item = new HashMap<>();
            item.put("titulo", titulo);
            data.add(item);
        }
        return data;
    }

    // Hijos: respuestas asociadas a cada pregunta
    private List<List<Map<String, String>>> crearHijos(HashMap<String, List<String>> datos) {
        List<List<Map<String, String>>> data = new ArrayList<>();
        for (String clave : listaTitulos) {
            List<Map<String, String>> children = new ArrayList<>();
            for (String respuesta : datos.get(clave)) {
                Map<String, String> item = new HashMap<>();
                item.put("contenido", respuesta);
                children.add(item);
            }
            data.add(children);
        }
        return data;
    }

    // Opción adicional: botón físico de "atrás" también te regresa
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SoporteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
