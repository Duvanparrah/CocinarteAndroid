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
import com.camilo.cocinarte.utils.NavigationHeaderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView; // Guardar referencia

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

        // Configurar NavigationView - USAR EL ID CORRECTO
        navigationView = findViewById(R.id.navigation_view); // Cambio: era R.id.nav_view

        // Cargar información del usuario en el header
        loadUserInfoInNavigationHeader();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_profile) {
                Log.d(TAG, "Navegando a Mi Perfil");
            } else if (id == R.id.nav_favorites) {
                Log.d(TAG, "Navegando a Favoritos");
            } else if (id == R.id.nav_recipes) {
                Log.d(TAG, "Navegando a Mis Recetas");
            } else if (id == R.id.nav_notifications) {
                Log.d(TAG, "Navegando a Notificaciones");
            } else if (id == R.id.nav_account) {
                Log.d(TAG, "Navegando a Cuenta y configuración");
                Intent intent = new Intent(this, com.camilo.cocinarte.ui.authentication.CuentaConfiguracionActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_support) {
                Log.d(TAG, "Navegando a Soporte");
            } else if (id == R.id.nav_sign_out) {
                Log.d(TAG, "Cerrando sesión...");
                sessionManager.logout();
                Intent intent = new Intent(this, com.camilo.cocinarte.ui.authentication.InicioSesionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.d(TAG, "Elemento desconocido seleccionado");
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

@Override
protected void onResume() {
    super.onResume();
    // Refrescar información del usuario cada vez que se regresa a la actividad
    // Útil si se actualizó el perfil en otra pantalla
    refreshUserInfo();

    // Verificar si se debe refrescar la información del usuario
    Intent intent = getIntent();
    if (intent != null && intent.getBooleanExtra("refresh_user_info", false)) {
        refreshUserInfo();
        // Limpiar el extra para evitar refrescos innecesarios
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

// Método público para que los fragments puedan acceder al DrawerLayout
public DrawerLayout getDrawerLayout() {
    return drawerLayout;
}

/**
 * Carga la información del usuario en el header del Navigation Drawer
 */
private void loadUserInfoInNavigationHeader() {
    if (navigationView != null) {
        Log.d(TAG, "Cargando información del usuario en el header...");
        NavigationHeaderHelper.loadUserInfo(this, navigationView);

        // Debug: Mostrar información actual del usuario
        SessionManager.SessionData sessionData = sessionManager.getSessionData();
        Log.d(TAG, "Usuario: " + sessionData.userName);
        Log.d(TAG, "Email: " + sessionData.email);
        Log.d(TAG, "Foto: " + sessionData.userPhoto);
    } else {
        Log.e(TAG, "NavigationView es null, no se puede cargar información del usuario");
    }
}

/**
 * Método para actualizar la información del usuario (útil para cuando se edite el perfil)
 */
public void updateUserInfo() {
    refreshUserInfo();
}

/**
 * Refresca la información del usuario en el navigation header
 */
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