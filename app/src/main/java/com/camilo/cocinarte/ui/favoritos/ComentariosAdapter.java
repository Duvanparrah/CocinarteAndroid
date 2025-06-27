package com.camilo.cocinarte.ui.favoritos;

import android.app.AlertDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.api.ApiConfig;
import com.camilo.cocinarte.api.ReaccionApi;
import com.camilo.cocinarte.models.Comentario;
import com.camilo.cocinarte.session.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComentariosAdapter extends RecyclerView.Adapter<ComentariosAdapter.ComentarioViewHolder> {

    private final ArrayList<Comentario> comentarios;
    private final Context context;
    private final int idUsuarioActual;

    public ComentariosAdapter(ArrayList<Comentario> comentarios, Context context, int idUsuarioActual) {
        this.comentarios = comentarios;
        this.context = context;
        this.idUsuarioActual = idUsuarioActual;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = comentarios.get(position);

        holder.nombreUsuario.setText(comentario.getNombreUsuario());
        holder.contenido.setText(comentario.getContenido());


        holder.fecha.setText(formatearFecha(comentario.isEditado() ? comentario.getFechaEdicion() : comentario.getFechaCreacion(), comentario.isEditado()));

        if (comentario.getFotoPerfil() != null && !comentario.getFotoPerfil().isEmpty()) {
            Glide.with(context)
                    .load(comentario.getFotoPerfil())
                    .placeholder(R.drawable.ic_cuenta_configuracion)
                    .circleCrop()
                    .into(holder.imagenPerfil);
        } else {
            holder.imagenPerfil.setImageResource(R.drawable.ic_cuenta_configuracion);
        }

        if (comentario.getUsuarioId() == idUsuarioActual) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
        }

        holder.btnEliminar.setOnClickListener(v -> eliminarComentario(comentario, position));
        holder.btnEditar.setOnClickListener(v -> editarComentario(comentario, position));
    }

    @Override
    public int getItemCount() {
        return comentarios.size();
    }

    public static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        ImageView imagenPerfil;
        TextView nombreUsuario, contenido, fecha;
        ImageButton btnEditar, btnEliminar;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenPerfil = itemView.findViewById(R.id.img_perfil_comentario);
            nombreUsuario = itemView.findViewById(R.id.tv_nombre_usuario_comentario);
            contenido = itemView.findViewById(R.id.tv_contenido_comentario);
            fecha = itemView.findViewById(R.id.tv_fecha_comentario);
            btnEditar = itemView.findViewById(R.id.btn_editar_comentario);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_comentario);
        }
    }

    private void eliminarComentario(Comentario comentario, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar comentario")
                .setMessage("¿Estás seguro de que deseas eliminar este comentario?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    ReaccionApi api = ApiConfig.getClient(context).create(ReaccionApi.class);
                    SessionManager sessionManager = SessionManager.getInstance(context.getApplicationContext());
                    String token = "Bearer " + sessionManager.getAuthToken();

                    api.eliminarComentario(token, comentario.getId()).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                comentarios.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Comentario eliminado", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void editarComentario(Comentario comentario, int position) {
        EditText input = new EditText(context);
        input.setText(comentario.getContenido());

        new AlertDialog.Builder(context)
                .setTitle("Editar comentario")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoTexto = input.getText().toString().trim();
                    if (!nuevoTexto.isEmpty()) {
                        ReaccionApi api =   ApiConfig.getClient(context).create(ReaccionApi.class);
                        SessionManager sessionManager = SessionManager.getInstance(context);
                        String token = "Bearer " + sessionManager.getAuthToken();

                        api.editarComentario(token, comentario.getId(), nuevoTexto).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                if (response.isSuccessful()) {
                                    comentario.setContenido(nuevoTexto);
                                    comentario.setEditado(true);
                                    comentario.setFechaEdicion(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));
                                    notifyItemChanged(position);
                                    Toast.makeText(context, "Comentario actualizado", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Error al editar", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String formatearFecha(String fechaIso, boolean editado) {
        try {
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date fecha = formatoEntrada.parse(fechaIso);

            if (fecha != null) {
                String fechaFormateada = DateFormat.format("dd MMM yyyy HH:mm", fecha).toString();
                return editado ? "Editado: " + fechaFormateada : fechaFormateada;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
