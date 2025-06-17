package com.camilo.cocinarte.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Patrones de validación
    private static final Pattern PASSWORD_PATTERN_UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_PATTERN_LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_PATTERN_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_PATTERN_SPECIAL =
            Pattern.compile(".*[!@#$%^&*()_+=\\-{}\\[\\]:;\"'<>,.?/~`].*");

    /**
     * Valida si un email es válido
     */
    public static boolean isValidEmail(String email) {
        return email != null &&
                !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Valida una contraseña y retorna el mensaje de error si no es válida
     */
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña es requerida";
        }

        if (password.length() < 6) {
            return "La contraseña debe tener al menos 6 caracteres";
        }

        if (password.contains(" ")) {
            return "La contraseña no debe contener espacios";
        }

        if (!PASSWORD_PATTERN_UPPERCASE.matcher(password).matches()) {
            return "Debe contener al menos una letra mayúscula";
        }

        if (!PASSWORD_PATTERN_LOWERCASE.matcher(password).matches()) {
            return "Debe contener al menos una letra minúscula";
        }

        if (!PASSWORD_PATTERN_DIGIT.matcher(password).matches()) {
            return "Debe contener al menos un número";
        }

        if (!PASSWORD_PATTERN_SPECIAL.matcher(password).matches()) {
            return "Debe contener al menos un carácter especial";
        }

        return null; // Contraseña válida
    }

    /**
     * Evalúa la fortaleza de una contraseña
     */
    public static PasswordStrength evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.NONE;
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (PASSWORD_PATTERN_UPPERCASE.matcher(password).matches()) score++;
        if (PASSWORD_PATTERN_LOWERCASE.matcher(password).matches()) score++;
        if (PASSWORD_PATTERN_DIGIT.matcher(password).matches()) score++;
        if (PASSWORD_PATTERN_SPECIAL.matcher(password).matches()) score++;

        if (score <= 2) return PasswordStrength.WEAK;
        else if (score <= 4) return PasswordStrength.MEDIUM;
        else return PasswordStrength.STRONG;
    }

    public enum PasswordStrength {
        NONE(""),
        WEAK("Débil"),
        MEDIUM("Media"),
        STRONG("Fuerte");

        private final String text;

        PasswordStrength(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}