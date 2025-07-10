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

import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.databinding.ActivityMainBinding;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.CuentaConfiguracionActivity;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;
import com.camilo.cocinarte.ui.favoritos.FavoritosActivity;
import com.camilo.cocinarte.utils.NavigationHeaderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private NavController navController;
    private SessionManager sessionManager;
    private LoginManager loginManager;
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

        // ✅ INICIALIZAR AMBOS MANAGERS
        try {
            sessionManager = SessionManager.getInstance(this);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loginManager = new LoginManager(this);

        // ✅ DEBUG: Mostrar información disponible
        Log.d(TAG, "=== INICIO MainActivity ===");
        NavigationHeaderHelper.debugUserInfo(this);

        // ✅ VALIDACIÓN DE SESIÓN MEJORADA
        if (!isValidSession()) {
            Log.d(TAG, "Sesión no válida o expirada. Redirigiendo al login.");
            redirectToLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente.");
            return;
        }

        Log.d(TAG, "Sesión válida. Cargando MainActivity...");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializar componentes de navegación
        initializeNavigation();

        // Cargar información del usuario
        loadUserInfoInNavigationHeader();

        // Configurar listeners del drawer
        setupDrawerListeners();

        // Manejar intent de navegación
        handleNavigationIntent(getIntent());
    }

    /**
     * ✅ MÉTODO MEJORADO: Validación robusta de sesión
     */
    private boolean isValidSession() {
        try {
            // Verificar si LoginManager tiene sesión activa válida
            boolean loginManagerValid = loginManager.hasActiveSession();

            if (loginManagerValid) {
                Log.d(TAG, "✅ Sesión válida en LoginManager");
                return true;
            }

            // Si LoginManager no tiene sesión válida, verificar SessionManager
            boolean sessionManagerValid = sessionManager.isLoggedIn() &&
                    sessionManager.hasValidToken() &&
                    !sessionManager.isSessionExpired();

            if (sessionManagerValid) {
                Log.d(TAG, "✅ Sesión válida en SessionManager, migrando a LoginManager...");
                loginManager.migrarDesdeSessionManager(this);

                // Verificar nuevamente si la migración fue exitosa
                if (loginManager.hasActiveSession()) {
                    Log.d(TAG, "✅ Migración exitosa, sesión válida en LoginManager");
                    return true;
                } else {
                    Log.w(TAG, "⚠️ Migración fallida, sesión inválida tras migrar");
                    clearSessions();
                    return false;
                }
            }

            // Si no hay sesión válida en ninguno
            Log.w(TAG, "⚠️ No hay sesión válida en LoginManager ni en SessionManager");
            clearSessions();
            return false;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al validar sesión: " + e.getMessage(), e);
            clearSessions();
            return false;
        }
    }

    /**
     * Limpia las sesiones en ambos managers y logs para debug.
     */
    private void clearSessions() {
        Log.d(TAG, "🧹 Limpiando datos de sesión en LoginManager y SessionManager...");
        try {
            loginManager.clear();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error limpiando LoginManager: " + e.getMessage(), e);
        }
        try {
            sessionManager.logout();
        } catch (Exception e) {
            Log.e(TAG, "❌ Error limpiando SessionManager: " + e.getMessage(), e);
        }
    }


    /**
     * ✅ MÉTODO PARA REDIRIGIR AL LOGIN
     */
    private void redirectToLogin(String message) {
        try {
            // Limpiar sesiones
            sessionManager.logout();
            loginManager.clear();

            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            Intent intent = new Intent(this, InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e(TAG, "Error al redirigir al login: " + e.getMessage());
            finish(); // Al menos cerrar esta actividad
        }
    }

    /**
     * ✅ INICIALIZAR COMPONENTES DE NAVEGACIÓN
     */
    private void initializeNavigation() {
        drawerLayout = binding.drawerLayout;
        navigationView = findViewById(R.id.navigation_view);

        // Bottom Navigation
        BottomNavigationView navView = binding.navView;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);

        // ✅ CONFIGURAR LISTENER CON IDs QUE COINCIDAN ENTRE MENU Y NAVIGATION
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // ✅ USAR LOS IDs QUE COINCIDEN EN AMBOS ARCHIVOS
            if (itemId == R.id.navigation_inicio) {
                navController.navigate(R.id.navigation_inicio);
                return true;
            } else if (itemId == R.id.navigation_banquetes) {  // ✅ MISMO ID EN MENU Y NAVIGATION
                navController.navigate(R.id.navigation_banquetes);
                return true;
            } else if (itemId == R.id.navigation_nutricion) {
                navController.navigate(R.id.navigation_nutricion);
                return true;
            } else if (itemId == R.id.navegar_comunidad) {
                navController.navigate(R.id.navegar_comunidad);
                return true;
            }

            // ✅ FALLBACK: Usar NavigationUI por defecto si no coincide ningún ID
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        Log.d(TAG, "Navegación inicializada correctamente");
    }

    /**
     * ✅ CONFIGURAR LISTENERS DEL DRAWER
     */
    // ✅ MÉTODO CORREGIDO: setupDrawerListeners()
// Reemplaza solo este método en tu MainActivity.java

    private void setupDrawerListeners() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(menuItem -> {
                int id = menuItem.getItemId();
                Intent intent = null;

                if (id == R.id.nav_favorites) {
                    Log.d(TAG, "Navegando a Favoritos");
                    intent = new Intent(this, FavoritosActivity.class);
                } else if (id == R.id.nav_account) {
                    Log.d(TAG, "Navegando a Cuenta y configuración");
                    intent = new Intent(this, CuentaConfiguracionActivity.class);
                } else if (id == R.id.nav_notifications) {
                    Log.d(TAG, "Navegando a Notificaciones");
                    intent = new Intent(this, NotificacionesActivity.class);
                } else if (id == R.id.nav_support) {
                    Log.d(TAG, "Navegando a Soporte");
                    intent = new Intent(this, SoporteActivity.class);
                } else if (id == R.id.nav_sign_out) {
                    Log.d(TAG, "Cerrando sesión...");
                    sessionManager.logout();
                    loginManager.clear();
                    getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply();
                    intent = new Intent(this, InicioSesionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                } else {
                    Log.d(TAG, "Elemento del menú no manejado: " + id);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNavigationIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ✅ VALIDAR SESIÓN EN onResume TAMBIÉN
        if (!isValidSession()) {
            Log.w(TAG, "Sesión inválida detectada en onResume");
            redirectToLogin("Tu sesión ha expirado");
            return;
        }

        refreshUserInfo();

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("refresh_user_info", false)) {
            refreshUserInfo();
            intent.removeExtra("refresh_user_info");
        }
    }

    /**
     * ✅ MANEJAR INTENTS DE NAVEGACIÓN
     */
    private void handleNavigationIntent(Intent intent) {
        if (intent != null && intent.hasExtra("fragment_to_show")) {
            String fragmentToShow = intent.getStringExtra("fragment_to_show");
            Log.d(TAG, "Navegando a fragment: " + fragmentToShow);

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
                    Log.d(TAG, "Fragment desconocido, navegando a inicio");
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

    /**
     * ✅ MÉTODOS PÚBLICOS PARA NAVEGACIÓN PROGRAMÁTICA
     */
    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public NavController getNavController() {
        return navController;
    }

    // ✅ MÉTODO GENERAL PARA NAVEGAR A CUALQUIER FRAGMENT
    public void navigateToFragment(int fragmentId) {
        if (navController != null) {
            try {
                navController.navigate(fragmentId);
                Log.d(TAG, "Navegación exitosa a fragmentId: " + fragmentId);
            } catch (Exception e) {
                Log.e(TAG, "Error al navegar a fragmentId: " + fragmentId, e);
                Toast.makeText(this, "Error de navegación", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "NavController es null, no se puede navegar");
        }
    }

    // ✅ MÉTODOS ESPECÍFICOS PARA CADA FRAGMENT
    public void navigateToInicio() {
        Log.d(TAG, "Navegando a Inicio");
        navigateToFragment(R.id.navigation_inicio);
    }

    public void navigateToBanquetes() {
        Log.d(TAG, "Navegando a Banquetes");
        navigateToFragment(R.id.navigation_banquetes);
    }

    public void navigateToNutricion() {
        Log.d(TAG, "Navegando a Nutrición");
        navigateToFragment(R.id.navigation_nutricion);
    }

    public void navigateToComunidad() {
        Log.d(TAG, "Navegando a Comunidad");
        navigateToFragment(R.id.navegar_comunidad);
    }

    // ✅ MÉTODO PARA NAVEGAR CON BUNDLE DE DATOS
    public void navigateToFragmentWithBundle(int fragmentId, Bundle bundle) {
        if (navController != null) {
            try {
                navController.navigate(fragmentId, bundle);
                Log.d(TAG, "Navegación con bundle exitosa a fragmentId: " + fragmentId);
            } catch (Exception e) {
                Log.e(TAG, "Error al navegar con bundle a fragmentId: " + fragmentId, e);
                Toast.makeText(this, "Error de navegación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ✅ MÉTODO PARA VERIFICAR SI ESTAMOS EN UN FRAGMENT ESPECÍFICO
    public boolean isCurrentDestination(int fragmentId) {
        if (navController != null && navController.getCurrentDestination() != null) {
            return navController.getCurrentDestination().getId() == fragmentId;
        }
        return false;
    }

    // ✅ MÉTODO PARA OBTENER EL FRAGMENT ACTUAL
    public int getCurrentFragmentId() {
        if (navController != null && navController.getCurrentDestination() != null) {
            return navController.getCurrentDestination().getId();
        }
        return -1;
    }

    /**
     * ✅ GESTIÓN DE INFORMACIÓN DE USUARIO CORREGIDA
     */
    private void loadUserInfoInNavigationHeader() {
        if (navigationView != null) {
            Log.d(TAG, "Cargando información del usuario en el header...");

            try {
                // ✅ USAR EL NavigationHeaderHelper CORREGIDO
                NavigationHeaderHelper.loadUserInfo(this, navigationView);
                Log.d(TAG, "✅ Información del usuario cargada exitosamente");

            } catch (Exception e) {
                Log.e(TAG, "❌ Error al cargar información del usuario: " + e.getMessage());
            }
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
            try {
                NavigationHeaderHelper.refreshUserInfo(this, navigationView);
                Log.d(TAG, "✅ Información del usuario refrescada");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error al refrescar información del usuario: " + e.getMessage());
            }
        }
    }

    /**
     * ✅ MÉTODO PARA MANEJO DE ERRORES DE NAVEGACIÓN
     */
    public void handleNavigationError(Exception error) {
        Log.e(TAG, "Error de navegación: ", error);
        Toast.makeText(this, "Error de navegación. Reintentando...", Toast.LENGTH_SHORT).show();

        // En caso de error, navegar a inicio como fallback
        try {
            navController.navigate(R.id.navigation_inicio);
        } catch (Exception fallbackError) {
            Log.e(TAG, "Error crítico de navegación: ", fallbackError);
        }
    }

    /**
     * ✅ MÉTODO PARA RESETEAR NAVEGACIÓN
     */
    public void resetNavigation() {
        Log.d(TAG, "Reseteando navegación a inicio");
        if (navController != null) {
            navController.popBackStack(R.id.navigation_inicio, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        navController = null;
        Log.d(TAG, "MainActivity destruida");
    }
}