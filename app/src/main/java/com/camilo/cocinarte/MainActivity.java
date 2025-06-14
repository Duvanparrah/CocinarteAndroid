package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.camilo.cocinarte.databinding.ActivityMainBinding;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SessionManager sessionManager;

    private final int[] TOP_LEVEL_DESTINATIONS = new int[]{
            R.id.navigation_inicio,
            R.id.navigation_banquetes,
            R.id.navigation_nutricion,
            R.id.navegar_comunidad
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // 🔒 Redirigir si no hay sesión válida o si ha expirado
        if (!sessionManager.isLoggedIn() || !sessionManager.hasValidToken() || sessionManager.isSessionExpired()) {
            Log.d(TAG, "Sesión no válida o expirada. Redirigiendo al login.");

            // 🧹 Limpiar datos de sesión
            sessionManager.logout();

            // 🛑 Mostrar mensaje
            Toast.makeText(this, "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

            // 🚪 Redirigir al login
            Intent intent = new Intent(this, com.camilo.cocinarte.ui.authentication.InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Evita volver con el botón atrás
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Sesión válida. Cargando MainActivity...");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        BottomNavigationView navView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(TOP_LEVEL_DESTINATIONS).build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        handleNavigationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigationIntent(intent);
    }

    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("fragment_to_show")) {
            String fragmentToShow = intent.getStringExtra("fragment_to_show");

            switch (fragmentToShow) {
                case "inicio":
                    navController.navigate(R.id.navigation_inicio);
                    break;
                case "banquetes":
                    navController.navigate(R.id.navigation_banquetes);
                    break;
                case "nutricion":
                    navController.navigate(R.id.navigation_nutricion);
                    break;
                case "comunidad":
                    navController.navigate(R.id.navegar_comunidad);
                    break;
                default:
                    navController.navigate(R.id.navigation_inicio);
                    break;
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
