package com.camilo.cocinarte;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.camilo.cocinarte.api.UsersRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConfiguracionActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImage;
    private ImageButton changeProfilePicButton;


    UsersRequest usersRequest = new UsersRequest();
    private EditText user_name;
    private EditText user_password;
    private EditText confirm_password;
    private TextView text_username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        this.usersRequest.UserPrefsManager(this.getApplicationContext());

        // Recuperar datos guardados
        String savedName = this.usersRequest.getSavedName();

        // Si hay nombre guardado, lo mostramos
        if (savedName != null && !savedName.isEmpty()) {
            this.text_username.setText(savedName);
        }



        // Configurar botón para seleccionar imagen
        changeProfilePicButton.setOnClickListener(v -> openGallery());

        //Obtener avatar
        loadAvatarImage();
    }

    private void changeName() {
        this.user_name = findViewById(R.id.user_name);
        String name = user_name.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre de usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        this.usersRequest.changeName(name); // Suponiendo que este método existe
        if (name != null && !name.isEmpty()) {
            this.text_username.setText(name);
        }
        Toast.makeText(this, "Nombre de usuario actualizado", Toast.LENGTH_SHORT).show();
    }

    private void changePassword() {
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

        this.usersRequest.changePassword(password); // Suponiendo que este método existe
        Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show();
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
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImage.setImageBitmap(bitmap);
                saveImageToInternalStorage(bitmap);  // Guardar en almacenamiento interno
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), "avatar.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Imagen guardada como avatar", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAvatarImage() {
        File file = new File(getFilesDir(), "avatar.png");
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            profileImage.setImageBitmap(bitmap);
        }
    }

}