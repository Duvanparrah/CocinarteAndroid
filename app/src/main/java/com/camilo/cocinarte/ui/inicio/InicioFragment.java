package com.camilo.cocinarte.ui.inicio;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiClient;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.models.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InicioFragment extends Fragment {

    private static final String TAG = "InicioFragment";

    private ImageButton menuButton;
    private ImageButton searchButton;
    private EditText searchEditText;
    private DrawerLayout drawerLayout;
    private TextView welcomeMessage;

    private AuthService authService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Inicializar vistas
        initViews(root);

        // Configurar API
        setupApi();

        // Configurar listeners
        setupListeners();

        // Mostrar mensaje de bienvenida con el nombre del usuario
        fetchAndDisplayUserName();

        return root;
    }

    private void initViews(View root) {
        menuButton = root.findViewById(R.id.menu_button);
        searchButton = root.findViewById(R.id.search_button);
        searchEditText = root.findViewById(R.id.search_edit_text);
        welcomeMessage = root.findViewById(R.id.welcomemessage);

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }
    }

    private void setupApi() {
        authService = ApiClient.getClient(requireContext()).create(AuthService.class);
    }

    private void setupListeners() {
        menuButton.setOnClickListener(v -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        searchButton.setOnClickListener(v -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
            } else {
                Toast.makeText(getContext(), "Ingresa algo para buscar", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = searchEditText.getText().toString().trim();
            if (!searchQuery.isEmpty()) {
                performSearch(searchQuery);
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null && getView() != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    private void fetchAndDisplayUserName() {
        // Aquí deberías recuperar el token desde SharedPreferences o cualquier mecanismo que uses para almacenarlo
        String token = "Bearer <TOKEN_DEL_USUARIO>"; // Reemplaza esto con el token real

        Call<Usuario> call = authService.getUserProfile(token);
        call.enqueue(new Callback<Usuario>() {
            @Override
            public void onResponse(@NonNull Call<Usuario> call, @NonNull Response<Usuario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Usuario usuario = response.body();
                    welcomeMessage.setText("¡Bienvenido de nuevo, " + usuario.getNombreUsuario() + "!");
                } else {
                    Log.e(TAG, "Error al obtener el usuario: " + response.message());
                    welcomeMessage.setText("¡Bienvenido de nuevo!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Usuario> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red: ", t);
                welcomeMessage.setText("¡Bienvenido de nuevo!");
            }
        });
    }
}



//package com.camilo.cocinarte.ui.inicio;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.view.GravityCompat;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.fragment.app.Fragment;
//
//import com.camilo.cocinarte.MainActivity;
//import com.camilo.cocinarte.R;
//
//public class InicioFragment extends Fragment {
//
//    private ImageButton menuButton;
//    private ImageButton searchButton;
//    private EditText searchEditText;
//    private DrawerLayout drawerLayout;
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View root = inflater.inflate(R.layout.fragment_inicio, container, false);
//
//        // Inicializar vistas
//        initViews(root);
//
//        // Configurar listeners
//        setupListeners();
//
//        return root;
//    }
//
//    private void initViews(View root) {
//        menuButton = root.findViewById(R.id.menu_button);
//        searchButton = root.findViewById(R.id.search_button);
//        searchEditText = root.findViewById(R.id.search_edit_text);
//
//        // Obtener referencia al DrawerLayout desde MainActivity
//        if (getActivity() instanceof MainActivity) {
//            MainActivity mainActivity = (MainActivity) getActivity();
//            drawerLayout = mainActivity.getDrawerLayout();
//        }
//    }
//
//    private void setupListeners() {
//        // Configurar botón del menú lateral
//        menuButton.setOnClickListener(v -> {
//            if (drawerLayout != null) {
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                } else {
//                    drawerLayout.openDrawer(GravityCompat.START);
//                }
//            }
//        });
//
//        // Configurar botón de búsqueda
//        searchButton.setOnClickListener(v -> {
//            String searchQuery = searchEditText.getText().toString().trim();
//            if (!searchQuery.isEmpty()) {
//                performSearch(searchQuery);
//            } else {
//                Toast.makeText(getContext(), "Ingresa algo para buscar", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Configurar búsqueda al presionar Enter
//        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
//            String searchQuery = searchEditText.getText().toString().trim();
//            if (!searchQuery.isEmpty()) {
//                performSearch(searchQuery);
//                return true;
//            }
//            return false;
//        });
//    }
//
//    private void performSearch(String query) {
//        // Aquí implementas la lógica de búsqueda
//        Toast.makeText(getContext(), "Buscando: " + query, Toast.LENGTH_SHORT).show();
//
//        // Ejemplo: ocultar el teclado después de buscar
//        if (getActivity() != null) {
//            android.view.inputmethod.InputMethodManager imm =
//                    (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
//            if (imm != null && getView() != null) {
//                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
//            }
//        }
//
//        // Limpiar el campo de búsqueda si deseas
//        // searchEditText.setText("");
//    }
//}