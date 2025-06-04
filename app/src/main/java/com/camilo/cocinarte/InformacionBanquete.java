package com.camilo.cocinarte;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.camilo.cocinarte.databinding.ActivityInformacionBanqueteBinding;

public class InformacionBanquete extends AppCompatActivity {

    private ActivityInformacionBanqueteBinding binding;
    private int numPersonas = 1; // Inicializa con el número predeterminado de personas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding
        binding = ActivityInformacionBanqueteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupMenuButton();
        setupPersonasButtons();
        updateTenerEnCuentaText(); // Actualiza el texto inicial en "Tener en cuenta"

        // Agregar dinámicamente un ítem de banquete
        addBanqueteItem("Banquete 1", "Este es el primer banquete de prueba.");
    }

    /**
     * Configura el botón de menú
     */
    private void setupMenuButton() {
        binding.menuButton.setOnClickListener(view -> {
            Toast.makeText(this, "Menú presionado", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Configura los botones para ajustar el número de personas
     */
    private void setupPersonasButtons() {
        // Actualizar el texto inicial de número de personas
        binding.numPersonas.setText(String.valueOf(numPersonas));

        // Botón de incrementar
        binding.btnIncrementPersonas.setOnClickListener(v -> {
            numPersonas++;
            binding.numPersonas.setText(String.valueOf(numPersonas));
            updateTenerEnCuentaText();
        });

        // Botón de decrementar
        binding.btnDecrementPersonas.setOnClickListener(v -> {
            if (numPersonas > 0) {
                numPersonas--;
                binding.numPersonas.setText(String.valueOf(numPersonas));
                updateTenerEnCuentaText();
            }
        });

        // Botón de aplicar
        binding.btnAplicar.setOnClickListener(v -> {
            Toast.makeText(this, "Número de personas actualizado: " + numPersonas, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Actualiza el texto en "Tener en cuenta" con el número actual de personas
     */
    private void updateTenerEnCuentaText() {
        String textoTenerEnCuenta = "TENER EN CUENTA: Este banquete es para " + numPersonas + " personas.\n" +
                "Puedes aumentar o disminuir la cantidad de invitados y los ingredientes se ajustarán automáticamente.";
        binding.descriptiveText.setText(textoTenerEnCuenta);
    }

    /**
     * Agrega dinámicamente un ítem de banquete al contenedor
     */
    private void addBanqueteItem(String title, String description) {
        // Inflar la vista del ítem desde el layout XML
        View banqueteItem = LayoutInflater.from(this).inflate(R.layout.item_banquete, binding.banquetesContainer, false);

        TextView likesText = banqueteItem.findViewById(R.id.banquete_likes);
        ImageView likeIcon = banqueteItem.findViewById(R.id.corazon_icon);
        ImageView commentIcon = banqueteItem.findViewById(R.id.comentar_icon);
        ImageView shareIcon = banqueteItem.findViewById(R.id.compartir_icon);

        // Configurar funcionalidad de "Me gusta"
        final int[] likes = {0};
        likeIcon.setOnClickListener(v -> {
            likes[0]++;
            likesText.setText(likes[0] + " likes");
        });

        // Configurar funcionalidad de "Comentar"
        commentIcon.setOnClickListener(v -> {
            showCommentDialog(this);
        });

        // Configurar funcionalidad de "Compartir"
        shareIcon.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mira este banquete");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "¡Revisa este banquete increíble en CocinArte!");
            startActivity(Intent.createChooser(shareIntent, "Compartir vía"));
        });

        // Agregar el ítem al contenedor
        binding.banquetesContainer.addView(banqueteItem);
    }

    /**
     * Muestra un diálogo para agregar un comentario
     */
    private void showCommentDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Escribe tu comentario");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String comentario = input.getText().toString();
            Toast.makeText(context, "Comentario enviado: " + comentario, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
        }
}
