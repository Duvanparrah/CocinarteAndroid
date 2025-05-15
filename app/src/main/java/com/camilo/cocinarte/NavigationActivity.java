package com.camilo.cocinarte;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Ocultar título de la ActionBar si existe
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Inicializar el DrawerLayout y NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Obtener el NavController correctamente desde el NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Configurar BottomNavigationView si existe
            BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
            if (bottomNav != null) {
                NavigationUI.setupWithNavController(bottomNav, navController);
            }

            // Configurar el AppBarConfiguration con los IDs de los destinos de nivel superior
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_inicio, R.id.navigation_banquetes,
                    R.id.navigation_nutricion, R.id.navegar_comunidad)
                    .setOpenableLayout(drawerLayout)
                    .build();

            // Configurar la ActionBar con el NavController
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Configurar NavigationView con el NavController para navegación automática
            NavigationUI.setupWithNavController(navigationView, navController);

            // También configuramos el listener personalizado para manejar items no definidos en el grafo
            navigationView.setNavigationItemSelectedListener(this);
        } else {
            // Si no se encuentra el NavHostFragment, mostrar un mensaje de error
            Toast.makeText(this, "Error: No se pudo encontrar el NavHostFragment",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Configura el botón de menú de cualquier fragmento para abrir el drawer
     */
    public void setupMenuButton(View menuButton) {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Manejar la navegación basada en los elementos del menú del drawer
        int id = item.getItemId();

        if (id == R.id.nav_soporte) {
            // Implementar navegación a soporte
            Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_cuenta_configuracion) {
            // Implementar navegación a cuenta y configuración
            Toast.makeText(this, "Cuenta y configuración", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_notificaciones) {
            // Implementar navegación a notificaciones
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_cerrar_sesion) {
            // Implementar lógica de cierre de sesión
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
            // Aquí irá la lógica para cerrar sesión
        }

        // Cerrar el drawer después de la selección
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Cerrar el drawer si está abierto antes de salir de la app
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}