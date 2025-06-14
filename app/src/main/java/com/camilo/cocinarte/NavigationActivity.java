package com.camilo.cocinarte;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.api.UsersRequest;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class NavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    UsersRequest usersRequest = new UsersRequest();
    NavigationView navigationView;

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
        this.navigationView = findViewById(R.id.nav_view);


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

        this.actionsButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ====== Logica enviar datos al nav_header_perfil ======
        SessionManager sessionManager = new SessionManager(getApplicationContext());

        View headerView = navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.user_name);
        TextView userEmail = headerView.findViewById(R.id.user_email);
        ImageView profile_image = headerView.findViewById(R.id.profile_image);

        userEmail.setText(sessionManager.getEmail());
        userName.setText(sessionManager.getNombre());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Glide.with(this)
                    .load(Uri.parse(sessionManager.getFoto()))
                    .circleCrop()
                    .placeholder(R.drawable.ic_cuenta_configuracion)
                    .error(R.drawable.ic_cuenta_configuracion)
                    .into(profile_image);
        }, 200);

        // ====== fin ======
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


    public void actionsButtons() {
        View signout = findViewById(R.id.nav_cerrar_sesion);
        signout.setOnClickListener(v -> {
            Intent intent = new Intent(this, InicioSesionActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
            this.finish();
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
            Toast.makeText(this, "Soporte", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_cuenta_configuracion) {
            // Iniciar actividad de configuración
            Intent intent = new Intent(this, ConfiguracionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_notificaciones) {
            Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show();
        }
        /*else if (id == R.id.nav_cerrar_sesion) {
            Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
        }*/

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