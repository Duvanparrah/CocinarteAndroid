package com.camilo.cocinarte.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.session.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class NavigationHeaderHelper {

    /**
     * Carga la información del usuario en el header del Navigation Drawer
     */
    public static void loadUserInfo(Context context, NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        // Obtener referencias a las vistas del header
        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewInitials = headerView.findViewById(R.id.textViewInitials);
        TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
        TextView textViewUserEmail = headerView.findViewById(R.id.textViewUserEmail);

        // Obtener datos del usuario desde SessionManager
        SessionManager sessionManager = SessionManager.getInstance(context);
        SessionManager.SessionData sessionData = sessionManager.getSessionData();

        // Configurar nombre de usuario
        String userName = sessionData.userName;
        if (userName != null && !userName.trim().isEmpty()) {
            textViewUserName.setText(userName);
        } else {
            textViewUserName.setText("Usuario");
        }

        // Configurar email
        String userEmail = sessionData.email;
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            textViewUserEmail.setText(userEmail);
        } else {
            textViewUserEmail.setText("email@ejemplo.com");
        }

        // Configurar foto de perfil
        String userPhoto = sessionData.userPhoto;
        if (userPhoto != null && !userPhoto.trim().isEmpty() && !userPhoto.equals("null")) {
            // Si hay foto de perfil, cargarla
            loadProfileImage(context, imageViewProfile, textViewInitials, userPhoto);
        } else {
            // Si no hay foto, mostrar iniciales
            showInitials(imageViewProfile, textViewInitials, userName);
        }
    }

    /**
     * Carga la imagen de perfil desde Cloudinary
     */
    private static void loadProfileImage(Context context, ImageView imageView, TextView initialsView, String photoUrl) {
        try {
            // Construir URL completa de Cloudinary si es necesario
            String fullImageUrl = buildCloudinaryUrl(photoUrl);

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

        } catch (Exception e) {
            android.util.Log.e("NavigationHelper", "Error loading profile image", e);
            // En caso de error, mostrar iniciales
            showInitials(imageView, initialsView, null);
        }
    }

    /**
     * Construye la URL completa de Cloudinary
     */
    private static String buildCloudinaryUrl(String photoPath) {
        // Si ya es una URL completa, devolverla tal como está
        if (photoPath.startsWith("http://") || photoPath.startsWith("https://")) {
            return photoPath;
        }

        // Si es solo el path, construir la URL completa de Cloudinary
        // Formato típico: https://res.cloudinary.com/tu-cloud-name/image/upload/path
        String cloudName = "cocinarte"; // Reemplaza con tu cloud name real de Cloudinary
        String baseUrl = "https://res.cloudinary.com/" + cloudName + "/image/upload/";

        // Limpiar path si tiene barras al inicio
        String cleanPath = photoPath.startsWith("/") ? photoPath.substring(1) : photoPath;

        return baseUrl + cleanPath;
    }

    /**
     * Muestra las iniciales del usuario cuando no hay foto
     */
    private static void showInitials(ImageView imageView, TextView initialsView, String userName) {
        imageView.setVisibility(View.GONE);
        initialsView.setVisibility(View.VISIBLE);

        String initials = getUserInitials(userName);
        initialsView.setText(initials);
    }

    /**
     * Obtiene las iniciales del nombre del usuario
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
     * Actualiza solo la foto de perfil
     */
    public static void updateProfilePhoto(Context context, NavigationView navigationView, String newPhotoUrl) {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);
        TextView textViewInitials = headerView.findViewById(R.id.textViewInitials);

        if (newPhotoUrl != null && !newPhotoUrl.trim().isEmpty() && !newPhotoUrl.equals("null")) {
            loadProfileImage(context, imageViewProfile, textViewInitials, newPhotoUrl);

            // Actualizar en SessionManager
            SessionManager sessionManager = SessionManager.getInstance(context);
            sessionManager.updateUserPhoto(newPhotoUrl);
        } else {
            SessionManager sessionManager = SessionManager.getInstance(context);
            String userName = sessionManager.getUserName();
            showInitials(imageViewProfile, textViewInitials, userName);
        }
    }

    /**
     * Refresca la información del usuario (útil después de actualizar perfil)
     */
    public static void refreshUserInfo(Context context, NavigationView navigationView) {
        loadUserInfo(context, navigationView);
    }
}