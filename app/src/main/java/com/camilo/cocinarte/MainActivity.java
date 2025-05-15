package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.camilo.cocinarte.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // Tag para logs

    
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    // Definir los destinos principales como IDs para facilitar la navegación
    private final int[] TOP_LEVEL_DESTINATIONS = new int[]{
            R.id.navigation_inicio,
            R.id.navigation_banquetes,
            R.id.navigation_nutricion,
            R.id.navegar_comunidad
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando MainActivity");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la Toolbar como ActionBar
        if (binding.toolbar != null) {
            setSupportActionBar(binding.toolbar);
        } else {
            Log.e(TAG, "onCreate: Toolbar no encontrada en el layout");
            // Si no hay toolbar, puedes crear una al vuelo o usar NoActionBar en el tema
            Toolbar toolbar = new Toolbar(this);
            setSupportActionBar(toolbar);
        }

        // Es mejor usar el binding que findViewById para coherencia
        BottomNavigationView navView = binding.navView;

        // Configuración de la navegación
        appBarConfiguration = new AppBarConfiguration.Builder(TOP_LEVEL_DESTINATIONS)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Ahora es seguro configurar la ActionBar con el NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Verificar si hay un intent con información de navegación
        Log.d(TAG, "onCreate: Verificando si hay intent con información de navegación");

        // Verificar que el intent no sea nulo
        if (getIntent() != null) {
            // Verificar si tiene el extra "fragment_to_show"
            if (getIntent().hasExtra("fragment_to_show")) {
                Log.d(TAG, "onCreate: Intent contiene extra 'fragment_to_show': " +
                        getIntent().getStringExtra("fragment_to_show"));
            } else {
                Log.d(TAG, "onCreate: Intent NO contiene extra 'fragment_to_show'");
            }
        } else {
            Log.d(TAG, "onCreate: Intent es nulo");
        }

        // Verificar si se debe navegar a un fragmento específico
        handleNavigationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: Recibido nuevo intent");
        // También manejar intents nuevos que podrían venir de notificaciones u otras partes
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("fragment_to_show")) {
            String fragmentToShow = intent.getStringExtra("fragment_to_show");
            Log.d(TAG, "handleNavigationIntent: Fragment a mostrar: " + fragmentToShow);

            if (fragmentToShow == null) {
                Log.e(TAG, "handleNavigationIntent: fragmentToShow es null");
                return;
            }

            // Navegar al fragmento correspondiente según el valor
            try {
                switch (fragmentToShow) {
                    case "inicio":
                        Log.d(TAG, "handleNavigationIntent: Navegando a inicio");
                        navController.navigate(R.id.navigation_inicio);
                        break;
                    case "banquetes":
                        Log.d(TAG, "handleNavigationIntent: Navegando a banquetes");
                        navController.navigate(R.id.navigation_banquetes);
                        break;
                    case "nutricion":
                        Log.d(TAG, "handleNavigationIntent: Navegando a nutrición");
                        navController.navigate(R.id.navigation_nutricion);
                        break;
                    case "comunidad":
                        Log.d(TAG, "handleNavigationIntent: Navegando a comunidad");
                        navController.navigate(R.id.navegar_comunidad);
                        break;
                    default:
                        // Manejar caso de valor no reconocido
                        Log.w(TAG, "handleNavigationIntent: Valor no reconocido: " + fragmentToShow + ", navegando a inicio por defecto");
                        navController.navigate(R.id.navigation_inicio); // Navegar al inicio por defecto
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, "handleNavigationIntent: Error al navegar", e);
            }
        } else {
            if (intent == null) {
                Log.d(TAG, "handleNavigationIntent: Intent es nulo");
            } else {
                Log.d(TAG, "handleNavigationIntent: No hay extra 'fragment_to_show'");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Permite navegar hacia atrás cuando se presiona el botón de la barra de acción
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    // Añadimos un método para depurar el estado del NavController
    private void printNavControllerInfo() {
        if (navController != null) {
            Log.d(TAG, "NavController - Destino actual: " +
                    navController.getCurrentDestination().getLabel());
        } else {
            Log.e(TAG, "NavController es nulo");
        }
    }
}