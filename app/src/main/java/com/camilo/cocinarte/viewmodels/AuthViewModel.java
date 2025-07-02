package com.camilo.cocinarte.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.camilo.cocinarte.api.AuthRepository;
import com.camilo.cocinarte.models.*;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.utils.SingleLiveEvent;
import com.camilo.cocinarte.utils.ValidationUtils;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    // LiveData para los estados de UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // LiveData para navegación (eventos únicos)
    private final SingleLiveEvent<String> _navigationEvent = new SingleLiveEvent<>();
    public LiveData<String> navigationEvent = _navigationEvent;

    // LiveData para mensajes de error
    private final SingleLiveEvent<String> _errorMessage = new SingleLiveEvent<>();
    public LiveData<String> errorMessage = _errorMessage;

    // LiveData para los campos del formulario
    private final MutableLiveData<String> _emailError = new MutableLiveData<>();
    public LiveData<String> emailError = _emailError;

    private final MutableLiveData<String> _passwordError = new MutableLiveData<>();
    public LiveData<String> passwordError = _passwordError;

    public AuthViewModel(Application application) {
        super(application);
        authRepository = AuthRepository.getInstance(application);
    }

    /**
     * Login del usuario
     */
    public LiveData<Resource<LoginResponse>> login(String email, String password) {
        // Limpiar errores previos
        _emailError.setValue(null);
        _passwordError.setValue(null);

        // Validar campos
        if (!validateLoginFields(email, password)) {
            MutableLiveData<Resource<LoginResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("Por favor corrige los errores", null));
            return errorResult;
        }

        return authRepository.login(email, password);
    }

    /**
     * Registro de usuario
     */
    public LiveData<Resource<RegisterResponse>> register(String email, String password, String confirmPassword) {
        // Limpiar errores previos
        _emailError.setValue(null);
        _passwordError.setValue(null);

        // Validar campos
        if (!validateRegisterFields(email, password, confirmPassword)) {
            MutableLiveData<Resource<RegisterResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("Por favor corrige los errores", null));
            return errorResult;
        }

        return authRepository.register(email, password);
    }

    /**
     * Solicitar recuperación de contraseña
     */
    public LiveData<Resource<ApiResponse>> forgotPassword(String email) {
        // Validar email
        if (!ValidationUtils.isValidEmail(email)) {
            _emailError.setValue("Por favor ingresa un email válido");
            MutableLiveData<Resource<ApiResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("Email inválido", null));
            return errorResult;
        }

        return authRepository.forgotPassword(email);
    }

    /**
     * Verificar código de recuperación
     */
    public LiveData<Resource<ApiResponse>> verifyResetCode(String email, String code) {
        // Validar código
        if (code == null || code.length() != 6 || !code.matches("\\d+")) {
            MutableLiveData<Resource<ApiResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("Código inválido", null));
            return errorResult;
        }

        return authRepository.verifyResetCode(email, code);
    }

    /**
     * Restablecer contraseña
     */
    public LiveData<Resource<ApiResponse>> resetPassword(String email, String newPassword, String confirmPassword) {
        // Validar contraseñas
        String passwordValidation = ValidationUtils.validatePassword(newPassword);
        if (passwordValidation != null) {
            _passwordError.setValue(passwordValidation);
            MutableLiveData<Resource<ApiResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error(passwordValidation, null));
            return errorResult;
        }

        if (!newPassword.equals(confirmPassword)) {
            MutableLiveData<Resource<ApiResponse>> errorResult = new MutableLiveData<>();
            errorResult.setValue(Resource.error("Las contraseñas no coinciden", null));
            return errorResult;
        }

        return authRepository.resetPassword(email, newPassword);
    }

    /**
     * Cerrar sesión
     */
    public void logout() {
        authRepository.logout();
        _navigationEvent.setValue("login");
    }

    /**
     * Verificar si el usuario está logueado
     */
    public boolean isUserLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * Validar campos de login
     */
    private boolean validateLoginFields(String email, String password) {
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            _emailError.setValue("Por favor ingresa un email válido");
            isValid = false;
        }

        if (password == null || password.isEmpty()) {
            _passwordError.setValue("Por favor ingresa tu contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            _passwordError.setValue("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validar campos de registro
     */
    private boolean validateRegisterFields(String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (!ValidationUtils.isValidEmail(email)) {
            _emailError.setValue("Por favor ingresa un email válido");
            isValid = false;
        }

        String passwordValidation = ValidationUtils.validatePassword(password);
        if (passwordValidation != null) {
            _passwordError.setValue(passwordValidation);
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            _errorMessage.setValue("Las contraseñas no coinciden");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Limpiar errores
     */
    public void clearErrors() {
        _emailError.setValue(null);
        _passwordError.setValue(null);
        _errorMessage.setValue(null);
    }
}