package com.camilo.cocinarte;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class NavigationActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            Intent intent = null;
            if (id == R.id.nav_favorites) {
                // Acción para Favoritos
                //intent = new Intent(this, Favoritos.class);
            } else if (id == R.id.nav_recipes) {
                // Acción para Mis Recetas
                //intent = new Intent(this, M.class);
            } else if (id == R.id.nav_notifications) {
                // Acción para Notificaciones
                //intent = new Intent(this, NotificacionesActivity.class);
            } else if (id == R.id.nav_account) {
                // Acción para Cuenta y configuración
                //intent = new Intent(this, NotificacionesActivity.class);
            } else if (id == R.id.nav_support) {
                // Acción para Soporte
                //intent = new Intent(this, NotificacionesActivity.class);
            } else if (id == R.id.nav_sign_out) {
                // Acción para Cerrar sesión
                //intent = new Intent(this, NotificacionesActivity.class);
            }
            if(intent != null){
                startActivity(intent);
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        /*ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
