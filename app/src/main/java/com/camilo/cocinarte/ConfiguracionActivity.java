package com.camilo.cocinarte;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.api.AuthService;
import com.camilo.cocinarte.api.UsersRequest;
import com.camilo.cocinarte.api.UsuarioService;
import com.camilo.cocinarte.models.ApiResponse;
import com.camilo.cocinarte.models.LoginRequest;
import com.camilo.cocinarte.models.LoginResponse;
import com.camilo.cocinarte.models.ResetPasswordRequest;
import com.camilo.cocinarte.models.UpdatePhotoResponse;
import com.camilo.cocinarte.models.Usuario;
import com.camilo.cocinarte.session.SessionManager;
import com.camilo.cocinarte.ui.authentication.CambioContrasenaActivity;
import com.camilo.cocinarte.ui.authentication.CorreoRecuperarContrasenaActivity;
import com.camilo.cocinarte.ui.authentication.InicioSesionActivity;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ConfiguracionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private ImageButton changeProfilePicButton;


    UsersRequest usersRequest = new UsersRequest();
    private EditText user_name;
    private EditText user_password;
    private EditText confirm_password;
    private TextView text_username;
    private String BASE_URL =  "";
    SessionManager sessionManager;
    AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.BASE_URL = "https://"+ this.getString(R.string.myhost) +"/api/";
        setContentView(R.layout.activity_configuracion);
        findViewById(R.id.save_name_button).setOnClickListener(v -> changeName());
        findViewById(R.id.save_password_button).setOnClickListener(v -> changePassword());
        profileImage = findViewById(R.id.profile_image);
        changeProfilePicButton = findViewById(R.id.change_profile_pic_button);
        this.user_name = findViewById(R.id.user_name);
        this.text_username = findViewById(R.id.text_username);
        this.user_password = findViewById(R.id.user_password);
        this.confirm_password = findViewById(R.id.confirm_password);
        this.confirm_password = findViewById(R.id.confirm_password);

        this.sessionManager = new SessionManager(getApplicationContext());

        //this.usersRequest.UserPrefsManager(this.getApplicationContext());


        Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        authService = retrofit.create(AuthService.class);

        // Recuperar datos guardados

        // Configurar botón para seleccionar imagen
        changeProfilePicButton.setOnClickListener(v -> openGallery());

        cargarDatosEnVista();
    }

    void cargarDatosEnVista(){
        //Obtener avatar
        if(!sessionManager.getFoto().isBlank() || !sessionManager.getFoto().isEmpty()){
            Glide.with(this)
                    .load(Uri.parse(sessionManager.getFoto()))
                    .circleCrop()
                    .placeholder(R.drawable.ic_cuenta_configuracion)
                    .error(R.drawable.ic_cuenta_configuracion)
                    .into(profileImage);
        }

        // Si hay nombre guardado, lo mostramos
        if (sessionManager.getNombre() != null && !sessionManager.getNombre().isEmpty()) {
            this.text_username.setText(sessionManager.getNombre());
        }
    }

    private void changeName() {
        this.user_name = findViewById(R.id.user_name);
        String name = user_name.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthService.Name _name = new AuthService.Name(name);

        // Llamar a la API
        Call<UpdatePhotoResponse> call = authService.updateNameProfile(_name, "Bearer " + sessionManager.getToken());
        call.enqueue(new Callback<UpdatePhotoResponse>() {
            @Override
            public void onResponse(Call<UpdatePhotoResponse> call, Response<UpdatePhotoResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "✅ Nombre actualizado: " + response.body().getMessage());
                    sessionManager.setNombre(response.body().getUser().getNombre());
                    cargarDatosEnVista();
                    text_username.setText(name);
                    Toast.makeText(getApplicationContext(), "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API", "❌ Error actulizar en nombre de perfil: " + response.code() + response.message());
                }
            }

            @Override
            public void onFailure(Call<UpdatePhotoResponse> call, Throwable t) {
                Log.e("API", "⚠️ Falló la conexión", t);
            }
        });
    }

    private void changePassword() {
        Intent intent = new Intent(getApplicationContext(), CorreoRecuperarContrasenaActivity.class);
        startActivity(intent);


        /*return;

        String password = user_password.getText().toString().trim();
        String confirm = confirm_password.getText().toString().trim();

        if (password.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Por favor, completa ambos campos de contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        ResetPasswordRequest request = new ResetPasswordRequest(sessionManager.getEmail(), "", password);

        Call<ApiResponse> call = authService.resetPassword(request);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "✅ Contraseña actualizada: " + response.body().getMessage());
                    Toast.makeText(getApplicationContext(), "Contraseña actualizada", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("API", "❌ Error al actualizar la contraseña: " + response.code() + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API", "⚠️ Falló la conexión", t);
            }
        });*/

    }


    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            String fileName = "imagen_perfil_" + System.currentTimeMillis() + ".jpg";

            File tempFile = new File(getCacheDir(), fileName);
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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            File file = createTempFileFromUri(imageUri);
            if (file == null) return;

            // Crear RequestBody del archivo
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    file
            );

            // Crear MultipartBody.Part con el nombre esperado por el backend: "file"
            MultipartBody.Part imagenPart = MultipartBody.Part.createFormData(
                    "profileImage",              // este nombre debe coincidir con el que espera tu backend: req.file
                    file.getName(),
                    requestFile
            );


            // Llamar a la API
            Call<UpdatePhotoResponse> call = authService.subirImagen(imagenPart, "Bearer " + sessionManager.getToken());
            call.enqueue(new Callback<UpdatePhotoResponse>() {
                @Override
                public void onResponse(Call<UpdatePhotoResponse> call, Response<UpdatePhotoResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("API", "✅ Imagen subida: " + response.body().getMessage() + ""+response.body().getUser().getFoto_perfil());

                        sessionManager.setFoto(response.body().getUser().getFoto_perfil());
                        cargarDatosEnVista();

                    } else {
                        Log.e("API", "❌ Error al subir: " + response.code() + response.message());
                    }
                }

                @Override
                public void onFailure(Call<UpdatePhotoResponse> call, Throwable t) {
                    Log.e("API", "⚠️ Falló la conexión", t);
                }
            });
        }
    }

}