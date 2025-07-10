package com.camilo.cocinarte.ui.nutricion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiService;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.api.TokenRefreshService;
import com.camilo.cocinarte.databinding.FragmentNutricionBinding;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class NutricionFragment extends Fragment {

    private static final String TAG = "NutricionFragment";
    private FragmentNutricionBinding binding;
    private DrawerLayout drawerLayout;
    private ApiService apiService;
    private SessionManager sessionManager;
    private LoginManager loginManager;
    private TokenRefreshService tokenRefreshService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNutricionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar servicios
        initializeServices();

        // Obtener referencia al DrawerLayout desde MainActivity
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            drawerLayout = mainActivity.getDrawerLayout();
        }

        // Configurar eventos
        setupEventListeners();

        // Verificar plan activo solo si hay autenticación
        if (isUserLoggedIn()) {
            verificarPlanActivo();
        }
    }

    private void initializeServices() {
        try {
            apiService = new ApiService(getContext());
            loginManager = new LoginManager(getContext());
            tokenRefreshService = new TokenRefreshService(getContext());

            // ✅ Auto-sincronizar datos desde SessionManager si es necesario
            loginManager.autoSincronizarConSessionManager(getContext());

            // ✅ MANTENER SessionManager para compatibilidad con ApiService
            try {
                sessionManager = SessionManager.getInstance(requireContext());
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "❌ Error inicializando SessionManager: " + e.getMessage(), e);
                sessionManager = null;
            }

            Log.d(TAG, "✅ Servicios inicializados correctamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inicializando servicios: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error inicializando servicios", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupEventListeners() {
        // Botón Plan Gratuito
        binding.btnGratis.setOnClickListener(view -> {
            Log.d(TAG, "🆓 Plan Gratuito seleccionado");
            procesarPlanGratuito();
        });

        // Botón Plan Pro
        binding.btnPro.setOnClickListener(view -> {
            Log.d(TAG, "💎 Plan Pro seleccionado");
            procesarPlanPro();
        });
    }

    private void procesarPlanGratuito() {
        // ✅ USAR LoginManager para verificar autenticación
        if (!isUserLoggedIn()) {
            Log.d(TAG, "❌ Usuario NO logueado - Redirigiendo a login para Plan Gratuito");
            mostrarDialogoLogin("Para acceder al Plan Gratuito necesitas crear una cuenta o iniciar sesión");
            return;
        }

        // ✅ VERIFICAR Y RENOVAR TOKEN ANTES DE HACER LA PETICIÓN
        verificarYRenovarToken(new TokenRefreshService.TokenRefreshCallback() {
            @Override
            public void onSuccess(String validToken) {
                Log.d(TAG, "🔑 Token válido obtenido, procediendo con plan gratuito");
                procesarPlanGratuitoConToken(validToken);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Error obteniendo token válido: " + error);
                Toast.makeText(getContext(), "Error de autenticación: " + error, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTokenExpired() {
                Log.e(TAG, "❌ Sesión completamente expirada");
                mostrarDialogoLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente");
            }
        });
    }

    private void procesarPlanGratuitoConToken(String token) {
        Toast.makeText(getContext(), "Activando Plan Gratuito...", Toast.LENGTH_SHORT).show();

        apiService.activarPlanGratis(token, new ApiService.PagoCallback() {
            @Override
            public void onSuccess(String message, String referencia) {
                Log.d(TAG, "✅ Plan gratuito activado: " + referencia);

                Intent intent = new Intent(getActivity(), formulario_plan_nutricional.class);
                intent.putExtra("tipo_plan", "gratuito");
                intent.putExtra("metodo_pago", "gratuito");
                intent.putExtra("referencia_pago", referencia);
                startActivity(intent);

                Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error activando plan gratuito: " + error);

                if (error.contains("expirado") || error.contains("401")) {
                    mostrarDialogoLogin("Tu sesión ha expirado. Por favor, inicia sesión nuevamente");
                } else {
                    Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void procesarPlanPro() {
        // ✅ USAR LoginManager para verificar autenticación
        if (!isUserLoggedIn()) {
            Log.d(TAG, "❌ Usuario NO logueado - Redirigiendo a login para Plan Pro");
            mostrarDialogoLogin("Para acceder al Plan Pro necesitas iniciar sesión");
            return;
        }

        Log.d(TAG, "✅ Usuario autenticado - Navegando a métodos de pago");
        Toast.makeText(getContext(), "Has seleccionado el Plan Pro", Toast.LENGTH_SHORT).show();

        // Navegar a métodos de pago
        try {
            Intent intent = new Intent(getActivity(), Metodo_de_pago_Activity.class);
            intent.putExtra("tipo_plan", "pro");
            startActivity(intent);
            Log.d(TAG, "🚀 Navegación a Metodo_de_pago_Activity iniciada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Error navegando a métodos de pago: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error al abrir métodos de pago", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🔄 Verificar y renovar token si es necesario
     */
    private void verificarYRenovarToken(TokenRefreshService.TokenRefreshCallback callback) {
        if (loginManager == null) {
            Log.e(TAG, "❌ LoginManager es null");
            callback.onFailure("Error interno de autenticación");
            return;
        }

        // Verificar si necesita renovación
        if (loginManager.needsTokenRefresh()) {
            Log.d(TAG, "🔄 Token necesita renovación, renovando...");
            tokenRefreshService.refreshToken(callback);
        } else {
            // Token aún válido
            String currentToken = loginManager.getToken();
            if (currentToken != null) {
                Log.d(TAG, "✅ Token aún válido, usando token actual");
                callback.onSuccess(currentToken);
            } else {
                Log.e(TAG, "❌ No hay token disponible");
                callback.onTokenExpired();
            }
        }
    }

    private void mostrarDialogoLogin(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

        // Limpiar datos de sesión
        if (loginManager != null) {
            loginManager.clear();
        }

        // Navegar al login
        Intent intent = new Intent(getActivity(), InicioSesionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // ✅ MÉTODO CORREGIDO: Usar métodos que EXISTEN en LoginManager
    private boolean isUserLoggedIn() {
        if (loginManager == null) {
            Log.d(TAG, "❌ LoginManager es null");
            return false;
        }

        try {
            // ✅ USAR MÉTODO QUE EXISTE: hasActiveSession()
            boolean hasActiveSession = loginManager.hasActiveSession();

            Log.d(TAG, "🔍 Verificación de sesión con LoginManager:");
            Log.d(TAG, "   - hasActiveSession: " + hasActiveSession);

            if (hasActiveSession) {
                // ✅ USAR MÉTODO QUE EXISTE: getUsuario()
                Usuario usuario = loginManager.getUsuario();
                if (usuario != null) {
                    Log.d(TAG, "   - Usuario: " + usuario.getNombreUsuario());
                    Log.d(TAG, "   - Email: " + usuario.getCorreo());
                } else {
                    Log.d(TAG, "   - Usuario es null");
                }
            }

            Log.d(TAG, "   - RESULTADO FINAL: " + hasActiveSession);
            return hasActiveSession;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error verificando sesión con LoginManager: " + e.getMessage(), e);
            return false;
        }
    }

    // ✅ MÉTODO CORREGIDO: Usar método que EXISTE en LoginManager
    private String getAuthToken() {
        if (loginManager == null) {
            Log.e(TAG, "❌ LoginManager es null");
            return null;
        }

        try {
            // ✅ USAR MÉTODO QUE EXISTE: getToken()
            String token = loginManager.getToken();
            Log.d(TAG, "🔑 Token obtenido: " + (token != null ? "presente" : "ausente"));
            return token;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error obteniendo token: " + e.getMessage(), e);
            return null;
        }
    }

    private void verificarPlanActivo() {
        if (!isUserLoggedIn()) {
            Log.d(TAG, "Usuario no logueado, saltando verificación de plan");
            return;
        }

        // ✅ USAR EL NUEVO SISTEMA DE VERIFICACIÓN DE TOKENS
        verificarYRenovarToken(new TokenRefreshService.TokenRefreshCallback() {
            @Override
            public void onSuccess(String validToken) {
                Log.d(TAG, "🔑 Token válido para verificar plan activo");
                verificarPlanActivoConToken(validToken);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "❌ Error obteniendo token para verificar plan: " + error);
            }

            @Override
            public void onTokenExpired() {
                Log.d(TAG, "❌ Sesión expirada, saltando verificación de plan");
            }
        });
    }

    private void verificarPlanActivoConToken(String token) {
        apiService.obtenerPlanActivo(token, new ApiService.PlanNutricionalCallback() {
            @Override
            public void onSuccess(org.json.JSONObject planGenerado) {
                Log.d(TAG, "📊 Usuario tiene plan activo");

                try {
                    org.json.JSONObject data = planGenerado.getJSONObject("data");
                    String tipoPlan = data.getString("tipo_plan");
                    String objetivo = data.getString("objetivo");

                    String mensaje = "Ya tienes un plan " + tipoPlan + " activo (" + objetivo + ")";
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();

                    // Opcional: Modificar UI para mostrar que ya tiene plan
                    // binding.btnGratis.setText("Ver Plan Activo");
                    // binding.btnPro.setEnabled(false);

                } catch (org.json.JSONException e) {
                    Log.e(TAG, "Error parseando plan activo: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "No tiene plan activo: " + error);
                // No hacer nada, es normal no tener plan
            }
        });
    }

    // Métodos de navegación con MainActivity
    public void navigateToFragment(int fragmentId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToFragment(fragmentId);
        }
    }

    public void navigateToInicio() {
        navigateToFragment(R.id.navigation_inicio);
    }

    public void navigateToBanquetes() {
        navigateToFragment(R.id.navigation_banquetes);
    }

    public void navigateToComunidad() {
        navigateToFragment(R.id.navegar_comunidad);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Fragment resumed - Verificando sesión");

        // ✅ Auto-sincronizar en onResume por si acaso
        if (loginManager != null) {
            loginManager.autoSincronizarConSessionManager(getContext());
        }

        // Revalidar sesión cuando el fragment vuelva a estar activo
        if (isUserLoggedIn()) {
            Log.d(TAG, "✅ Sesión válida en onResume");
            verificarPlanActivo();
        } else {
            Log.d(TAG, "❌ No hay sesión válida en onResume");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🗑️ Fragment destroyed");
        binding = null;
    }
}