package com.camilo.cocinarte.ui.authentication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.MainActivity;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.auth.AuthService;
import com.camilo.cocinarte.api.auth.UsuarioService;
import com.camilo.cocinarte.databinding.ActivityCuentaConfiguracionBinding;
import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.ProfileImageResponse;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.models.UsuarioUpdateRequest;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.utils.CloudinaryUploader;
import com.camilo.cocinarte.utils.NavigationHeaderHelper;
import com.camilo.cocinarte.utils.Resource;
import com.camilo.cocinarte.utils.ValidationUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CuentaConfiguracionActivity extends AppCompatActivity {

    private static final String TAG = "CuentaConfig";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ActivityCuentaConfiguracionBinding binding;
    private SessionManager sessionManager;
    private CloudinaryUploader cloudinaryUploader;

    // Variables para manejar la imagen
    private Uri selectedImageUri;
    private String currentPhotoUrl;
    private boolean isImageChanged = false;

    // Launchers para seleccionar imagen
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCuentaConfiguracionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        cloudinaryUploader = new CloudinaryUploader(this);

        setupViews();
        loadCurrentUserData();
        setupImageSelectionLaunchers();
        setupTextWatchers();
    }

    private void setupViews() {
        // Configurar toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cuenta y configuración");
        }

        // Listeners para botones
        binding.imageViewEditPhoto.setOnClickListener(v -> showImageSelectionDialog());
        binding.buttonSaveChanges.setOnClickListener(v -> saveChanges());
        binding.buttonChangePass.setOnClickListener(v -> changePassword());

        // Listener para toolbar back button
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadCurrentUserData() {
        SessionManager.SessionData sessionData = sessionManager.getSessionData();

        // Cargar nombre
        if (sessionData.userName != null && !sessionData.userName.trim().isEmpty()) {
            binding.editTextName.setText(sessionData.userName);
        }

        // Cargar email (solo lectura)
        if (sessionData.email != null && !sessionData.email.trim().isEmpty()) {
            binding.textViewEmail.setText(sessionData.email);
        }

        // Cargar foto
        currentPhotoUrl = sessionData.userPhoto;
        loadProfileImage(currentPhotoUrl);

        Log.d(TAG, "Datos cargados - Nombre: " + sessionData.userName + ", Email: " + sessionData.email);
    }

    private void loadProfileImage(String photoUrl) {
        if (photoUrl != null && !photoUrl.trim().isEmpty() && !photoUrl.equals("null")) {
            // Cargar imagen desde URL
            Glide.with(this)
                    .load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.perfil_chef)
                    .error(R.drawable.perfil_chef)
                    .into(binding.imageViewProfile);

            binding.imageViewProfile.setVisibility(View.VISIBLE);
            binding.textViewInitials.setVisibility(View.GONE);
        } else {
            // Mostrar iniciales
            showInitials();
        }
    }

    private void showInitials() {
        binding.imageViewProfile.setVisibility(View.GONE);
        binding.textViewInitials.setVisibility(View.VISIBLE);

        String name = binding.editTextName.getText() != null ?
                binding.editTextName.getText().toString() :
                sessionManager.getUserName();

        String initials = getInitials(name);
        binding.textViewInitials.setText(initials);
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }

    private void setupImageSelectionLaunchers() {
        // Launcher para galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            loadSelectedImage();
                            isImageChanged = true;
                        }
                    }
                }
        );

        // Launcher para cámara
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            if (imageBitmap != null) {
                                displayCameraBitmap(imageBitmap);
                                isImageChanged = true;
                            }
                        }
                    }
                }
        );
    }

    private void loadSelectedImage() {
        Glide.with(this)
                .load(selectedImageUri)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(binding.imageViewProfile);

        binding.imageViewProfile.setVisibility(View.VISIBLE);
        binding.textViewInitials.setVisibility(View.GONE);
    }

    private void displayCameraBitmap(Bitmap bitmap) {
        Glide.with(this)
                .load(bitmap)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(binding.imageViewProfile);

        binding.imageViewProfile.setVisibility(View.VISIBLE);
        binding.textViewInitials.setVisibility(View.GONE);
    }

    private void setupTextWatchers() {
        binding.editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textInputLayoutName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showImageSelectionDialog() {
        String[] options = {"Tomar foto", "Seleccionar de galería", "Cancelar"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar imagen");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    checkCameraPermissionAndTakePhoto();
                    break;
                case 1:
                    openGallery();
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CODE);
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void saveChanges() {
        String newName = binding.editTextName.getText() != null ?
                binding.editTextName.getText().toString().trim() : "";

        // Validar nombre
        if (newName.isEmpty()) {
            binding.textInputLayoutName.setError("El nombre no puede estar vacío");
            return;
        }

        // Mostrar progreso
        showLoading(true);

        // Si hay imagen nueva, subirla primero
        if (isImageChanged && selectedImageUri != null) {
            //TODO: OBSOLETO? uploadImageAndSaveChanges(newName);

            AuthService authService = ApiConfig.getClient(getApplicationContext()).create(AuthService.class);
            String token = "Bearer " + sessionManager.getAuthToken();

            Uri imageUri = selectedImageUri;
            File file = createTempFileFromUri(imageUri);
            if (file == null) return;

            // Crear RequestBody para el archivo de la imagen
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);

            // Crear MultipartBody.Part para la imagen (debe coincidir con lo que espera el backend, "foto")
            MultipartBody.Part imagenPart = MultipartBody.Part.createFormData(
                    "profileImage",              // Nombre del campo para el archivo (backend espera "foto")
                    file.getName(),
                    requestFile
            );


            authService.updateProfileImage(imagenPart, token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Imagen subida exitosamente: " + response.body().getProfileImageUrl());
                        saveUserChanges(newName, response.body().getProfileImageUrl());
                    } else {
                        Toast.makeText(getApplicationContext(), "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });




        } else {
            // Solo actualizar nombre
            Log.d("|||actulizar nombre", newName+" ----   "+sessionManager.getUserName());

            if(newName.equals(sessionManager.getUserName())){
                Toast.makeText(CuentaConfiguracionActivity.this, "Sin cambios", Toast.LENGTH_LONG).show();
                showLoading(false);
            }else{
                saveUserChanges(newName, currentPhotoUrl);
            }
        }
    }

    private void uploadImageAndSaveChanges(String newName) {
        cloudinaryUploader.uploadImage(selectedImageUri, new CloudinaryUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                Log.d(TAG, "Imagen subida exitosamente: " + imageUrl);
                saveUserChanges(newName, imageUrl);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error al subir imagen: " + error);
                showLoading(false);
                Toast.makeText(CuentaConfiguracionActivity.this,
                        "Error al subir imagen: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserChanges(String newName, String photoUrl) {
        try {
            UsuarioService usuarioService = ApiConfig.getClient(getApplicationContext()).create(UsuarioService.class);
            String token = "Bearer " + sessionManager.getAuthToken();
            UsuarioUpdateRequest usuarioRequest = new UsuarioUpdateRequest(
                    sessionManager.getEmail(),
                    newName,
                    "usuario", // o "admin"
                    true,
                    photoUrl
            );

            usuarioService.actualizarUsuario(sessionManager.getUserId(), usuarioRequest, token).enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        //Usuario loginResponse = response.body();

                        // Actualizar SessionManager
                        sessionManager.updateUserName(newName);
                        if (photoUrl != null) {
                            sessionManager.updateUserPhoto(photoUrl);
                        }

                        // Aquí puedes hacer llamada a API para actualizar en el servidor
                        // updateUserProfileOnServer(newName, photoUrl);

                        showLoading(false);
                        Toast.makeText(getApplicationContext(), "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();

                        // Regresar a MainActivity y actualizar header
                        getOnBackPressedDispatcher().onBackPressed();

                        finish();

                        Toast.makeText(getApplicationContext(), "Usuario actualizada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error al actualizar el usuario", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    t.printStackTrace();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error al guardar cambios: " + e.getMessage());
            showLoading(false);
            Toast.makeText(this, "Error al guardar cambios", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String newPassword = Objects.requireNonNull(binding.eTChangePassword.getText()).toString();
        AuthService authService = ApiConfig.getClient(getApplicationContext()).create(AuthService.class);
        ResetPasswordRequest request = new ResetPasswordRequest(sessionManager.getEmail(), newPassword);
        authService.resetPassword(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse loginResponse = response.body();
                    Toast.makeText(getApplicationContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.buttonSaveChanges.setEnabled(!show);
        binding.buttonSaveChanges.setText(show ? "Guardando..." : "Guardar cambios");

        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
            String fileName = "imagen_" + System.currentTimeMillis() + ".jpg";

            File tempFile = new File(getApplicationContext().getCacheDir(), fileName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}