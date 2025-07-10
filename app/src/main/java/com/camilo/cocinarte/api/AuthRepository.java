package com.camilo.cocinarte.api;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.camilo.cocinarte.models.*;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class AuthRepository {
    private static final String TAG = "AuthRepository";

    private final AuthService authService;
    private final SessionManager sessionManager;
    private final Application application;

    // Singleton instance
    private static AuthRepository instance;

    private AuthRepository(Application application) {
        this.application = application;
        this.authService = ApiClient.getClient(application).create(AuthService.class);
        try {
            this.sessionManager = SessionManager.getInstance(application);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized AuthRepository getInstance(Application application) {
        if (instance == null) {
            instance = new AuthRepository(application);
        }
        return instance;
    }

    /**
     * Login del usuario
     */
    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        MutableLiveData<Resource<LoginResponse>> result = new MutableLiveData<>();

        // Verificar conexión a internet
        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("Sin conexión a internet", null));
            return result;
        }

        result.setValue(Resource.loading(null));

        LoginRequest request = new LoginRequest(email.toLowerCase().trim(), password);

        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Guardar datos de sesión con información completa del usuario
                    if (loginResponse.getToken() != null) {
                        saveUserSessionData(loginResponse, email, password);
                        result.setValue(Resource.success(loginResponse));
                    } else {
                        result.setValue(Resource.error("No se recibió token de autenticación", null));
                    }
                } else {
                    String errorMessage = parseErrorResponse(response);
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String errorMessage = parseNetworkError(t);
                result.setValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Guarda los datos del usuario después del login exitoso
     */
    private void saveUserSessionData(LoginResponse loginResponse, String email, String password) {
        try {
            // Verificar si la respuesta incluye datos del usuario
            if (loginResponse.getUser() != null) {
                LoginResponse.UserData userData = loginResponse.getUser();

                // Guardar sesión completa con todos los datos del usuario
                sessionManager.saveCompleteUserSession(
                        email,
                        password,
                        loginResponse.getToken(),
                        String.valueOf(userData.getId()),
                        userData.getNombre(),
                        userData.getFoto(),
                        userData.getTipo_usuario(),
                        userData.isVerified()
                );

                android.util.Log.d(TAG, "Datos completos del usuario guardados: " + userData.getNombre());
                android.util.Log.d(TAG, "Foto del usuario: " + userData.getFoto());

            } else {
                // Si no hay datos del usuario en la respuesta, guardar sesión básica
                sessionManager.saveUserSession(email, password, loginResponse.getToken());
                android.util.Log.w(TAG, "Solo se guardó sesión básica, falta información del usuario");

                // Opcionalmente, hacer una llamada adicional para obtener datos del usuario
                fetchUserProfile();
            }

        } catch (Exception e) {
            android.util.Log.e(TAG, "Error al guardar datos de sesión: " + e.getMessage());
            // En caso de error, al menos guardar la sesión básica
            sessionManager.saveUserSession(email, password, loginResponse.getToken());
        }
    }

    /**
     * Obtiene los datos del perfil del usuario (llamada adicional si es necesario)
     */
    private void fetchUserProfile() {
        // Implementar solo si tu API tiene un endpoint separado para obtener el perfil
        // Por ejemplo: GET /api/user/profile
        /*
        authService.getUserProfile().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    sessionManager.saveUserInfo(
                        String.valueOf(user.getId_usuario()),
                        user.getNombre_usuario(),
                        user.getFoto_perfil(),
                        user.getTipo_usuario(),
                        user.isVerified()
                    );
                    android.util.Log.d(TAG, "Perfil del usuario obtenido: " + user.getNombre_usuario());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                android.util.Log.e(TAG, "Error al obtener perfil del usuario: " + t.getMessage());
            }
        });
        */
    }

    /**
     * Registro de usuario
     */
    public LiveData<Resource<RegisterResponse>> register(String email, String password) {
        MutableLiveData<Resource<RegisterResponse>> result = new MutableLiveData<>();

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("Sin conexión a internet", null));
            return result;
        }

        result.setValue(Resource.loading(null));

        RegisterRequest request = new RegisterRequest(email.toLowerCase().trim(), password);

        authService.registerUser(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    // Guardar datos después del registro exitoso
                    sessionManager.saveUser(email, password);
                    if (registerResponse.getToken() != null) {
                        sessionManager.saveAuthToken(registerResponse.getToken());
                    }

                    result.setValue(Resource.success(registerResponse));
                } else {
                    String errorMessage = parseErrorResponse(response);
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                String errorMessage = parseNetworkError(t);
                result.setValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Solicitar recuperación de contraseña
     */
    public LiveData<Resource<ApiResponse>> forgotPassword(String email) {
        MutableLiveData<Resource<ApiResponse>> result = new MutableLiveData<>();

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("Sin conexión a internet", null));
            return result;
        }

        result.setValue(Resource.loading(null));

        ForgotPasswordRequest request = new ForgotPasswordRequest(email.toLowerCase().trim());

        authService.forgotPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    String errorMessage = parseErrorResponse(response);
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                String errorMessage = parseNetworkError(t);
                result.setValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Verificar código de recuperación
     */
    public LiveData<Resource<ApiResponse>> verifyResetCode(String email, String code) {
        MutableLiveData<Resource<ApiResponse>> result = new MutableLiveData<>();

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("Sin conexión a internet", null));
            return result;
        }

        result.setValue(Resource.loading(null));

        VerifyCodeRequest request = new VerifyCodeRequest(email, code);

        authService.verifyRecoveryCode(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        result.setValue(Resource.success(apiResponse));
                    } else {
                        // Si no hay body pero la respuesta es exitosa, crear respuesta por defecto
                        ApiResponse defaultResponse = new ApiResponse();
                        defaultResponse.setMessage("Código verificado correctamente");
                        result.setValue(Resource.success(defaultResponse));
                    }
                } else {
                    String errorMessage = parseErrorResponse(response);
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                String errorMessage = parseNetworkError(t);
                result.setValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Restablecer contraseña
     */
    public LiveData<Resource<ApiResponse>> resetPassword(String email, String newPassword) {
        MutableLiveData<Resource<ApiResponse>> result = new MutableLiveData<>();

        if (!NetworkUtils.isNetworkAvailable(application)) {
            result.setValue(Resource.error("Sin conexión a internet", null));
            return result;
        }

        result.setValue(Resource.loading(null));

        ResetPasswordRequest request = new ResetPasswordRequest(email, newPassword);

        authService.resetPassword(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.setValue(Resource.success(response.body()));
                } else {
                    String errorMessage = parseErrorResponse(response);
                    result.setValue(Resource.error(errorMessage, null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                String errorMessage = parseNetworkError(t);
                result.setValue(Resource.error(errorMessage, null));
            }
        });

        return result;
    }

    /**
     * Cerrar sesión
     */
    public void logout() {
        sessionManager.logout();
    }

    /**
     * Verificar si el usuario está logueado
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn() &&
                sessionManager.hasValidToken() &&
                !sessionManager.isSessionExpired();
    }

    /**
     * Obtener el token actual
     */
    public String getCurrentToken() {
        return sessionManager.getAuthToken();
    }

    /**
     * Obtener datos del usuario actual
     */
    public SessionManager.SessionData getCurrentUserData() {
        return sessionManager.getSessionData();
    }

    /**
     * Parser de errores de respuesta HTTP
     */
    private String parseErrorResponse(Response<?> response) {
        String errorMessage = "Error desconocido";

        switch (response.code()) {
            case 400:
                errorMessage = "Datos inválidos";
                break;
            case 401:
                errorMessage = "Credenciales incorrectas";
                break;
            case 404:
                errorMessage = "Usuario no encontrado";
                break;
            case 422:
                errorMessage = "Error de validación";
                break;
            case 500:
                errorMessage = "Error del servidor";
                break;
            default:
                try {
                    if (response.errorBody() != null) {
                        String errorBody = response.errorBody().string();
                        // Aquí podrías parsear el JSON de error si tu API lo retorna
                        errorMessage = "Error: " + response.code();
                    }
                } catch (IOException e) {
                    errorMessage = "Error al procesar la respuesta";
                }
        }

        return errorMessage;
    }

    /**
     * Parser de errores de red
     */
    private String parseNetworkError(Throwable t) {
        if (t.getMessage() != null) {
            if (t.getMessage().contains("Unable to resolve host")) {
                return "No se puede conectar al servidor";
            } else if (t.getMessage().contains("timeout")) {
                return "Tiempo de espera agotado";
            } else if (t.getMessage().contains("SSL")) {
                return "Error de seguridad en la conexión";
            }
        }
        return "Error de conexión";
    }
}