package com.camilo.cocinarte.ui.banquetes;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.camilo.cocinarte.InformacionBanquete;
import com.camilo.cocinarte.R;


public class BanqueteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_banquete, container, false);

        Button btnNuevoBanquete = rootView.findViewById(R.id.btn_nuevo_banquete);
        LinearLayout banquetesContainer = rootView.findViewById(R.id.banquetes_container);

        btnNuevoBanquete.setOnClickListener(v -> agregarBanquete(banquetesContainer));

        return rootView;
    }

    private void agregarBanquete(LinearLayout container) {
        View banqueteView = LayoutInflater.from(getContext()).inflate(R.layout.item_banquete, container, false);

        int numeroBanquete = container.getChildCount() + 1;

        TextView banqueteTitle = banqueteView.findViewById(R.id.banquete_title);
        banqueteTitle.setText("Nuevo Banquete - " + numeroBanquete);

        // Like
        ImageView corazon = banqueteView.findViewById(R.id.corazon_icon); // ID corregido
        TextView likesText = banqueteView.findViewById(R.id.banquete_likes);
        final int[] likes = {0};
        corazon.setOnClickListener(v -> {
            likes[0]++;
            likesText.setText(likes[0] + " likes");
        });

        // Comentar
        ImageView comentar = banqueteView.findViewById(R.id.comentar_icon);
        comentar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Escribe tu comentario");

            final EditText input = new EditText(requireContext());
            builder.setView(input);

            builder.setPositiveButton("Enviar", (dialog, which) -> {
                String comentario = input.getText().toString();
                Toast.makeText(requireContext(), "Comentario enviado: " + comentario, Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        // Compartir
        ImageView compartir = banqueteView.findViewById(R.id.compartir_icon);
        compartir.setOnClickListener(v -> {
            Intent compartirIntent = new Intent(Intent.ACTION_SEND);
            compartirIntent.setType("text/plain");
            compartirIntent.putExtra(Intent.EXTRA_SUBJECT, "Mira este banquete");
            compartirIntent.putExtra(Intent.EXTRA_TEXT, "¡Revisa este banquete increíble en CocinArte!");
            startActivity(Intent.createChooser(compartirIntent, "Compartir vía"));
        });

        // Descripción - Navegación a una nueva actividad
        banqueteView.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(),InformacionBanquete.class);
            startActivity(intent);
        });

        container.addView(banqueteView);
        }
}
