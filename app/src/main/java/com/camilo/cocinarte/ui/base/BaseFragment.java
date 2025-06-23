package com.camilo.cocinarte.ui.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.NavigationActivity;

/**
 * Fragmento base que proporciona funcionalidad común para todos los fragmentos de la app
 */
public abstract class BaseFragment extends Fragment {

    protected Context context;
    protected DrawerLayout drawerLayout;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtener referencia al DrawerLayout desde la Activity
        if (getActivity() instanceof NavigationActivity) {
            drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        }

        setupDrawerButton();
    }

    /**
     * Configura el botón del drawer si existe en el fragmento
     * Las subclases pueden sobrescribir este método si necesitan
     * una implementación personalizada
     */
    protected void setupDrawerButton() {
        // La implementación básica se deja a las subclases
        // Este método puede ser sobrescrito si se necesita configurar
        // el botón del drawer de forma específica
    }

    /**
     * Método para abrir el drawer
     */
    protected void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}