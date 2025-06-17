package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.camilo.cocinarte.databinding.ActivityMainBinding;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;

    private final int[] TOP_LEVEL_DESTINATIONS = new int[]{
            R.id.navigation_inicio,
            R.id.navigation_banquetes,
            R.id.navigation_nutricion,
            R.id.navegar_comunidad
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);

        // Verificación de sesión
        if (!sessionManager.isLoggedIn() || !sessionManager.hasValidToken() || sessionManager.isSessionExpired()) {
            Log.d(TAG, "Sesión no válida o expirada. Redirigiendo al login.");
            sessionManager.logout();
            Toast.makeText(this, "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, com.camilo.cocinarte.ui.authentication.InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Sesión válida. Cargando MainActivity...");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // OCULTAR EL ACTION BAR/TOOLBAR
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configurar DrawerLayout
        drawerLayout = binding.drawerLayout;

        // Configurar NavigationView
        NavigationView navigationView = binding.navigationView;
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_profile) {
                // Acción para Mi Perfil
            } else if (id == R.id.nav_favorites) {
                // Acción para Favoritos
            } else if (id == R.id.nav_recipes) {
                // Acción para Mis Recetas
            } else if (id == R.id.nav_notifications) {
                // Acción para Notificaciones
            } else if (id == R.id.nav_account) {
                // Acción para Cuenta y configuración
            } else if (id == R.id.nav_support) {
                // Acción para Soporte
            } else if (id == R.id.nav_sign_out) {
                // Cerrar sesión
                sessionManager.logout();
                Intent intent = new Intent(this, com.camilo.cocinarte.ui.authentication.InicioSesionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Configurar Bottom Navigation
        BottomNavigationView navView = binding.navView;

        // Configuración simplificada sin AppBarConfiguration para toolbar
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Método público para que los fragments puedan acceder al DrawerLayout
    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }
}