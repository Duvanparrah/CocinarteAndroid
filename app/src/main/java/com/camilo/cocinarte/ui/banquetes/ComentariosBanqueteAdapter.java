package com.camilo.cocinarte.ui.banquetes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.models.ComentarioBanquete;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComentariosBanqueteAdapter extends RecyclerView.Adapter<ComentariosBanqueteAdapter.ComentarioViewHolder> {

    private static final String TAG = "ComentariosBanqueteAdapter";
    private List<ComentarioBanquete> comentarios;

    public ComentariosBanqueteAdapter(List<ComentarioBanquete> comentarios) {
        this.comentarios = comentarios;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario_banquete, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        ComentarioBanquete comentario = comentarios.get(position);

        // Nombre del usuario
        holder.textNombreUsuario.setText(comentario.getNombreUsuario());

        // Contenido del comentario
        holder.textContenido.setText(comentario.getContenido());

        // Fecha del comentario
        holder.textFecha.setText(formatearFecha(comentario.getFechaCreacion()));

        // Mostrar si fue editado
        if (comentario.isEditado()) {
            holder.textEditado.setVisibility(View.VISIBLE);
        } else {
            holder.textEditado.setVisibility(View.GONE);
        }

        // Cargar foto de perfil
        cargarFotoPerfil(holder.imageViewPerfil, comentario.getFotoPerfil());
    }

    private void cargarFotoPerfil(ImageView imageView, String fotoPerfil) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.perfil)  // ✅ Cambio aquí
                .error(R.drawable.perfil)        // ✅ Cambio aquí
                .circleCrop();

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(fotoPerfil)
                    .apply(options)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.perfil);  // ✅ Cambio aquí
        }
    }

    private String formatearFecha(String fechaString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date fecha = inputFormat.parse(fechaString);
            return outputFormat.format(fecha);
        } catch (ParseException e) {
            return fechaString; // Devolver tal como está si no se puede parsear
        }
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPerfil;
        TextView textNombreUsuario;
        TextView textContenido;
        TextView textFecha;
        TextView textEditado;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPerfil = itemView.findViewById(R.id.imageViewPerfil);
            textNombreUsuario = itemView.findViewById(R.id.textNombreUsuario);
            textContenido = itemView.findViewById(R.id.textContenido);
            textFecha = itemView.findViewById(R.id.textFecha);
            textEditado = itemView.findViewById(R.id.textEditado);
        }
    }
}