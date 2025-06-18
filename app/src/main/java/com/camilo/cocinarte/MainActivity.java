package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.camilo.cocinarte.databinding.ActivityMainBinding;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.CuentaConfiguracionActivity;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;
import com.camilo.cocinarte.utils.NavigationHeaderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

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

        // Validar sesión
        if (!sessionManager.isLoggedIn() || !sessionManager.hasValidToken() || sessionManager.isSessionExpired()) {
            Log.d(TAG, "Sesión no válida o expirada. Redirigiendo al login.");
            sessionManager.logout();
            Toast.makeText(this, "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Sesión válida. Cargando MainActivity...");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        drawerLayout = binding.drawerLayout;
        navigationView = findViewById(R.id.navigation_view);

        loadUserInfoInNavigationHeader();

        // Manejar clics del Drawer
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            Intent intent = null;

            if (id == R.id.nav_favorites) {
                Log.d(TAG, "Navegando a Favoritos");
                // TODO: Implementar pantalla de favoritos
            } else if (id == R.id.nav_recipes) {
                Log.d(TAG, "Navegando a Mis Recetas");
                intent = new Intent(this, MisRecetasActivity.class);
            } else if (id == R.id.nav_notifications) {
                Log.d(TAG, "Navegando a Notificaciones");
                intent = new Intent(this, NotificacionesActivity.class);
            } else if (id == R.id.nav_account) {
                Log.d(TAG, "Navegando a Cuenta y configuración");
                intent = new Intent(this, CuentaConfiguracionActivity.class);
            } else if (id == R.id.nav_support) {
                Log.d(TAG, "Navegando a Soporte");
                intent = new Intent(this, SoporteActivity.class);
            } else if (id == R.id.nav_sign_out) {
                Log.d(TAG, "Cerrando sesión...");
                sessionManager.logout();
                intent = new Intent(this, InicioSesionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            } else {
                Log.d(TAG, "Elemento desconocido seleccionado");
            }

            if (intent != null) {
                startActivity(intent);
                // ❌ Ya no se cierra MainActivity innecesariamente
                // finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Opcional: transición suave
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Bottom Navigation
        BottomNavigationView navView = binding.navView;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        handleNavigationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigationIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserInfo();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("refresh_user_info", false)) {
            refreshUserInfo();
            intent.removeExtra("refresh_user_info");
        }
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

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    private void loadUserInfoInNavigationHeader() {
        if (navigationView != null) {
            Log.d(TAG, "Cargando información del usuario en el header...");
            NavigationHeaderHelper.loadUserInfo(this, navigationView);

            SessionManager.SessionData sessionData = sessionManager.getSessionData();
            Log.d(TAG, "Usuario: " + sessionData.userName);
            Log.d(TAG, "Email: " + sessionData.email);
            Log.d(TAG, "Foto: " + sessionData.userPhoto);
        } else {
            Log.e(TAG, "NavigationView es null, no se puede cargar información del usuario");
        }
    }

    public void updateUserInfo() {
        refreshUserInfo();
    }

    public void refreshUserInfo() {
        if (navigationView != null) {
            Log.d(TAG, "Refrescando información del usuario...");
            NavigationHeaderHelper.refreshUserInfo(this, navigationView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
