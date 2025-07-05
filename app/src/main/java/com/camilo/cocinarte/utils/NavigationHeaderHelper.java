package com.camilo.cocinarte.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.LoginManager;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class NavigationHeaderHelper {
    private static final String TAG = "NavigationHeaderHelper";

    /**
     * ✅ MÉTODO CORREGIDO: Carga la información del usuario desde LoginManager o SessionManager
     */
    public static void loadUserInfo(Context context, NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) {
            Log.w(TAG, "HeaderView es null, no se puede cargar información del usuario");
            return;
        }

        // Obtener referencias a las vistas del header
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewInitials = headerView.findViewById(R.id.textViewInitials);
        TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
        TextView textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

        // ✅ OBTENER DATOS DESDE LoginManager PRIMERO, LUEGO SessionManager
        String userName = null;
        String userEmail = null;
        String userPhoto = null;

        try {
            // Intentar obtener desde LoginManager primero
            LoginManager loginManager = new LoginManager(context);
            Usuario usuario = loginManager.getUsuario();

            if (usuario != null) {
                userName = usuario.getNombreUsuario();
                userEmail = usuario.getCorreo();
                userPhoto = usuario.getFotoPerfil();
                Log.d(TAG, "✅ Datos obtenidos desde LoginManager:");
                Log.d(TAG, "   - Nombre: " + userName);
                Log.d(TAG, "   - Email: " + userEmail);
                Log.d(TAG, "   - Foto: " + userPhoto);
            } else {
                Log.w(TAG, "⚠️ No hay datos en LoginManager, intentando SessionManager...");

                // Fallback: obtener desde SessionManager
                SessionManager sessionManager = SessionManager.getInstance(context);
                SessionManager.SessionData sessionData = sessionManager.getSessionData();

                if (sessionData != null) {
                    userName = sessionData.userName;
                    userEmail = sessionData.email;
                    userPhoto = sessionData.userPhoto;
                    Log.d(TAG, "✅ Datos obtenidos desde SessionManager:");
                    Log.d(TAG, "   - Nombre: " + userName);
                    Log.d(TAG, "   - Email: " + userEmail);
                    Log.d(TAG, "   - Foto: " + userPhoto);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al obtener datos del usuario: " + e.getMessage());
        }

        // ✅ CONFIGURAR NOMBRE DE USUARIO
        if (userName != null && !userName.trim().isEmpty()) {
            textViewUserName.setText(userName);
            Log.d(TAG, "📝 Nombre configurado: " + userName);
        } else {
            textViewUserName.setText("Usuario");
            Log.d(TAG, "📝 Usando nombre por defecto: Usuario");
        }

        // ✅ CONFIGURAR EMAIL
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            textViewUserEmail.setText(userEmail);
            Log.d(TAG, "📧 Email configurado: " + userEmail);
        } else {
            textViewUserEmail.setText("email@ejemplo.com");
            Log.d(TAG, "📧 Usando email por defecto: email@ejemplo.com");
        }

        // ✅ CONFIGURAR FOTO DE PERFIL
        if (userPhoto != null && !userPhoto.trim().isEmpty() && !userPhoto.equals("null")) {
            Log.d(TAG, "🖼️ Cargando foto de perfil: " + userPhoto);
            loadProfileImage(context, imageViewProfile, textViewInitials, userPhoto);
        } else {
            Log.d(TAG, "🖼️ No hay foto de perfil, mostrando iniciales");
            showInitials(imageViewProfile, textViewInitials, userName);
        }
    }

    /**
     * ✅ MÉTODO MEJORADO: Carga la imagen de perfil con mejor manejo de errores
     */
    private static void loadProfileImage(Context context, ImageView imageView, TextView initialsView, String photoUrl) {
        try {
            // Construir URL completa de Cloudinary si es necesario
            String fullImageUrl = buildCloudinaryUrl(photoUrl);
            Log.d(TAG, "🌐 URL completa de imagen: " + fullImageUrl);

            // Cargar imagen con Glide
            Glide.with(context)
                    .load(fullImageUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.perfil_chef)
                    .error(R.drawable.perfil_chef)
                    .into(imageView);

            // Mostrar ImageView y ocultar iniciales
            imageView.setVisibility(View.VISIBLE);
            initialsView.setVisibility(View.GONE);

            Log.d(TAG, "✅ Imagen de perfil cargada exitosamente");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al cargar imagen de perfil: " + e.getMessage());
            // En caso de error, mostrar iniciales
            showInitials(imageView, initialsView, null);
        }
    }

    /**
     * ✅ MÉTODO MEJORADO: Construye la URL completa de Cloudinary
     */
    private static String buildCloudinaryUrl(String photoPath) {
        // Si ya es una URL completa, devolverla tal como está
        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
            Log.d(TAG, "🔗 URL ya es completa: " + photoPath);
            return photoPath;
        }

        // Si es solo el path, construir la URL completa de Cloudinary
        String cloudName = "dryumffhy"; // ✅ Cloud name correcto basado en los logs
        String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload/";

        // Limpiar path si tiene barras al inicio
        String cleanPath = photoPath.startsWith("/") ? photoPath.substring(1) : photoPath;
        String fullUrl = baseUrl + cleanPath;

        Log.d(TAG, "🔧 URL construida: " + fullUrl);
        return fullUrl;
    }

    /**
     * ✅ MÉTODO MEJORADO: Muestra las iniciales del usuario cuando no hay foto
     */
    private static void showInitials(ImageView imageView, TextView initialsView, String userName) {
        imageView.setVisibility(View.GONE);
        initialsView.setVisibility(View.VISIBLE);

        String initials = getUserInitials(userName);
        initialsView.setText(initials);

        Log.d(TAG, "🔤 Mostrando iniciales: " + initials);
    }

    /**
     * ✅ MÉTODO MEJORADO: Obtiene las iniciales del nombre del usuario
     */
    private static String getUserInitials(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return "U"; // Usuario por defecto
        }

        String[] parts = userName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return userName.substring(0, Math.min(2, userName.length())).toUpperCase();
        }
    }

    /**
     * ✅ MÉTODO CORREGIDO: Actualiza solo la foto de perfil en ambos sistemas
     */
    public static void updateProfilePhoto(Context context, NavigationView navigationView, String newPhotoUrl) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewInitials = headerView.findViewById(R.id.textViewInitials);

        if (newPhotoUrl != null && !newPhotoUrl.trim().isEmpty() && !newPhotoUrl.equals("null")) {
            loadProfileImage(context, imageViewProfile, textViewInitials, newPhotoUrl);

            // ✅ ACTUALIZAR EN AMBOS SISTEMAS
            try {
                // Actualizar en SessionManager
                SessionManager sessionManager = SessionManager.getInstance(context);
                sessionManager.updateUserPhoto(newPhotoUrl);

                // TODO: Si LoginManager tiene método para actualizar foto, agregarlo aquí
                // loginManager.updateUserPhoto(newPhotoUrl);

                Log.d(TAG, "✅ Foto de perfil actualizada en ambos sistemas");
            } catch (Exception e) {
                Log.e(TAG, "❌ Error al actualizar foto en sistemas: " + e.getMessage());
            }
        } else {
            // ✅ OBTENER NOMBRE DE USUARIO DESDE LoginManager O SessionManager
            String userName = null;
            try {
                LoginManager loginManager = new LoginManager(context);
                Usuario usuario = loginManager.getUsuario();
                if (usuario != null) {
                    userName = usuario.getNombreUsuario();
                } else {
                    SessionManager sessionManager = SessionManager.getInstance(context);
                    userName = sessionManager.getUserName();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al obtener nombre de usuario: " + e.getMessage());
            }

            showInitials(imageViewProfile, textViewInitials, userName);
        }
    }

    /**
     * ✅ MÉTODO CORREGIDO: Refresca la información del usuario
     */
    public static void refreshUserInfo(Context context, NavigationView navigationView) {
        Log.d(TAG, "🔄 Refrescando información del usuario...");
        loadUserInfo(context, navigationView);
    }

    /**
     * ✅ MÉTODO ADICIONAL: Debug para verificar qué datos están disponibles
     */
    public static void debugUserInfo(Context context) {
        Log.d(TAG, "=== DEBUG: INFORMACIÓN DE USUARIO ===");

        try {
            // Debug LoginManager
            LoginManager loginManager = new LoginManager(context);
            Usuario usuario = loginManager.getUsuario();
            if (usuario != null) {
                Log.d(TAG, "LoginManager - Nombre: " + usuario.getNombreUsuario());
                Log.d(TAG, "LoginManager - Email: " + usuario.getCorreo());
                Log.d(TAG, "LoginManager - Foto: " + usuario.getFotoPerfil());
            } else {
                Log.d(TAG, "LoginManager - No hay datos");
            }

            // Debug SessionManager
            SessionManager sessionManager = SessionManager.getInstance(context);
            SessionManager.SessionData sessionData = sessionManager.getSessionData();
            Log.d(TAG, "SessionManager - Nombre: " + sessionData.userName);
            Log.d(TAG, "SessionManager - Email: " + sessionData.email);
            Log.d(TAG, "SessionManager - Foto: " + sessionData.userPhoto);

        } catch (Exception e) {
            Log.e(TAG, "Error en debug: " + e.getMessage());
        }

        Log.d(TAG, "=====================================");
    }
}