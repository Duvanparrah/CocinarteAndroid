package com.camilo.cocinarte.ui.banquetes;

import android.content.Context;
import android.util.Log;
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
import com.camilo.cocinarte.models.BanquetePlatillo;

import java.util.ArrayList;
import java.util.List;

public class PlatillosBanqueteAdapter extends RecyclerView.Adapter<PlatillosBanqueteAdapter.PlatilloViewHolder> {

    private static final String TAG = "PlatillosBanqueteAdapter";

    private Context context;
    private List<BanquetePlatillo> platillos;
    private OnPlatilloClickListener listener;

    // Interface para clicks en platillos (opcional)
    public interface OnPlatilloClickListener {
        void onPlatilloClick(BanquetePlatillo platillo, int position);
    }

    // Constructor principal
    public PlatillosBanqueteAdapter(Context context, List<BanquetePlatillo> platillos) {
        this.context = context;
        this.platillos = platillos != null ? new ArrayList<>(platillos) : new ArrayList<>();

        Log.d(TAG, "🏗️ Adapter creado con " + this.platillos.size() + " platillos");
        logPlatillosInfo();
    }

    // Constructor con listener
    public PlatillosBanqueteAdapter(Context context, List<BanquetePlatillo> platillos, OnPlatilloClickListener listener) {
        this(context, platillos);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlatilloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "📦 Creando ViewHolder");
        View view = LayoutInflater.from(context).inflate(R.layout.item_platillo_banquete, parent, false);
        return new PlatilloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatilloViewHolder holder, int position) {
        if (platillos == null || position >= platillos.size() || position < 0) {
            Log.e(TAG, "❌ Error: posición inválida (" + position + ") o lista nula. Size: " +
                    (platillos != null ? platillos.size() : "null"));
            return;
        }

        BanquetePlatillo platillo = platillos.get(position);
        if (platillo == null) {
            Log.e(TAG, "❌ Error: platillo nulo en posición " + position);
            return;
        }

        Log.d(TAG, "🔗 Binding platillo " + (position + 1) + "/" + platillos.size() + ": " + platillo.getNombre());

        // Configurar nombre del platillo
        configurarNombre(holder, platillo);

        // Configurar descripción (si existe el campo)
        configurarDescripcion(holder, platillo);

        // Configurar imagen (si existe el campo)
        configurarImagen(holder, platillo);

        // Configurar click listener
        configurarClickListener(holder, platillo, position);

        Log.d(TAG, "✅ Platillo binding completado para posición: " + position);
    }

    private void configurarNombre(PlatilloViewHolder holder, BanquetePlatillo platillo) {
        if (holder.textNombrePlatillo != null) {
            String nombre = platillo.getNombre();
            if (nombre != null && !nombre.trim().isEmpty()) {
                holder.textNombrePlatillo.setText(nombre.trim());
                holder.textNombrePlatillo.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ✅ Nombre configurado: " + nombre);
            } else {
                holder.textNombrePlatillo.setText("Platillo sin nombre");
                holder.textNombrePlatillo.setVisibility(View.VISIBLE);
                Log.w(TAG, "   ⚠️ Nombre vacío, usando texto por defecto");
            }
        }
    }

    private void configurarDescripcion(PlatilloViewHolder holder, BanquetePlatillo platillo) {
        if (holder.textDescripcionPlatillo != null) {
            String descripcion = platillo.getDescripcion();
            if (descripcion != null && !descripcion.trim().isEmpty()) {
                // Limitar descripción si es muy larga
                String descripcionMostrar = descripcion.trim();
                if (descripcionMostrar.length() > 150) {
                    descripcionMostrar = descripcionMostrar.substring(0, 147) + "...";
                }

                holder.textDescripcionPlatillo.setText(descripcionMostrar);
                holder.textDescripcionPlatillo.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ✅ Descripción configurada");
            } else {
                holder.textDescripcionPlatillo.setVisibility(View.GONE);
                Log.d(TAG, "   ℹ️ Sin descripción, ocultando campo");
            }
        }
    }

    private void configurarImagen(PlatilloViewHolder holder, BanquetePlatillo platillo) {
        if (holder.imagenPlatillo != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.logo_cocinarte)
                    .error(R.drawable.logo_cocinarte)
                    .centerCrop()
                    .override(300, 200); // Optimizar tamaño de imagen

            if (platillo.tieneImagen()) {
                String imageUrl = platillo.getImagenUrl();
                Log.d(TAG, "   🖼️ Cargando imagen: " + imageUrl);

                Glide.with(context)
                        .load(imageUrl)
                        .apply(options)
                        .into(holder.imagenPlatillo);
            } else {
                Log.d(TAG, "   🖼️ Sin imagen, usando placeholder");
                holder.imagenPlatillo.setImageResource(R.drawable.logo_cocinarte);
            }

            holder.imagenPlatillo.setVisibility(View.VISIBLE);
        }
    }

    private void configurarClickListener(PlatilloViewHolder holder, BanquetePlatillo platillo, int position) {
        holder.itemView.setOnClickListener(v -> {
            // Agregar efecto visual al click
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100);

                        if (listener != null) {
                            listener.onPlatilloClick(platillo, position);
                        } else {
                            Log.d(TAG, "👆 Click en platillo: " + platillo.getNombre());
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        int count = platillos != null ? platillos.size() : 0;
        Log.d(TAG, "📊 getItemCount llamado: " + count + " platillos");
        return count;
    }

    // ✅ MÉTODOS PÚBLICOS PARA GESTIÓN DINÁMICA DE DATOS

    /**
     * Actualiza completamente la lista de platillos
     */
    public void updatePlatillos(List<BanquetePlatillo> nuevosPlatillos) {
        Log.d(TAG, "🔄 Actualizando platillos: " +
                (nuevosPlatillos != null ? nuevosPlatillos.size() : 0) + " nuevos platillos");

        if (this.platillos == null) {
            this.platillos = new ArrayList<>();
        }

        this.platillos.clear();
        if (nuevosPlatillos != null) {
            this.platillos.addAll(nuevosPlatillos);
        }

        notifyDataSetChanged();
        logPlatillosInfo();
    }

    /**
     * Agrega nuevos platillos al final de la lista
     */
    public void addPlatillos(List<BanquetePlatillo> nuevosPlatillos) {
        if (nuevosPlatillos == null || nuevosPlatillos.isEmpty()) {
            Log.d(TAG, "⚠️ No hay platillos nuevos para agregar");
            return;
        }

        if (this.platillos == null) {
            this.platillos = new ArrayList<>();
        }

        int startPosition = this.platillos.size();
        this.platillos.addAll(nuevosPlatillos);

        notifyItemRangeInserted(startPosition, nuevosPlatillos.size());
        Log.d(TAG, "➕ Agregados " + nuevosPlatillos.size() + " platillos en posición " + startPosition);
    }

    /**
     * Agrega un solo platillo
     */
    public void addPlatillo(BanquetePlatillo platillo) {
        if (platillo == null) {
            Log.w(TAG, "⚠️ Intentando agregar platillo nulo");
            return;
        }

        if (this.platillos == null) {
            this.platillos = new ArrayList<>();
        }

        int position = this.platillos.size();
        this.platillos.add(platillo);
        notifyItemInserted(position);

        Log.d(TAG, "➕ Platillo agregado: " + platillo.getNombre() + " en posición " + position);
    }

    /**
     * Remueve un platillo por posición
     */
    public void removePlatillo(int position) {
        if (this.platillos == null || position < 0 || position >= this.platillos.size()) {
            Log.w(TAG, "⚠️ No se puede remover platillo en posición " + position);
            return;
        }

        BanquetePlatillo removed = this.platillos.remove(position);
        notifyItemRemoved(position);

        Log.d(TAG, "➖ Platillo removido: " + (removed != null ? removed.getNombre() : "null") +
                " de posición " + position);
    }

    /**
     * Limpia todos los platillos
     */
    public void clearPlatillos() {
        if (this.platillos != null) {
            int size = this.platillos.size();
            this.platillos.clear();
            notifyItemRangeRemoved(0, size);
            Log.d(TAG, "🗑️ Limpiados " + size + " platillos");
        }
    }

    /**
     * Obtiene un platillo por posición
     */
    public BanquetePlatillo getPlatillo(int position) {
        if (platillos != null && position >= 0 && position < platillos.size()) {
            return platillos.get(position);
        }
        Log.w(TAG, "⚠️ No se puede obtener platillo en posición " + position);
        return null;
    }

    /**
     * Obtiene la lista completa de platillos
     */
    public List<BanquetePlatillo> getAllPlatillos() {
        return platillos != null ? new ArrayList<>(platillos) : new ArrayList<>();
    }

    /**
     * Verifica si la lista está vacía
     */
    public boolean isEmpty() {
        return platillos == null || platillos.isEmpty();
    }

    /**
     * Fuerza la actualización completa del adapter
     */
    public void forceUpdate() {
        Log.d(TAG, "🔄 Forzando actualización completa del adapter");
        notifyDataSetChanged();
    }

    // ✅ MÉTODOS AUXILIARES

    private void logPlatillosInfo() {
        if (platillos != null && !platillos.isEmpty()) {
            Log.d(TAG, "📋 Lista de platillos actual:");
            for (int i = 0; i < Math.min(platillos.size(), 5); i++) { // Solo los primeros 5
                BanquetePlatillo p = platillos.get(i);
                Log.d(TAG, "   " + (i + 1) + ". " + (p != null ? p.getNombre() : "null"));
            }
            if (platillos.size() > 5) {
                Log.d(TAG, "   ... y " + (platillos.size() - 5) + " más");
            }
        } else {
            Log.d(TAG, "📋 Lista de platillos vacía");
        }
    }

    // ✅ VIEWHOLDER SIMPLIFICADO - SOLO CAMPOS BÁSICOS
    static class PlatilloViewHolder extends RecyclerView.ViewHolder {
        // Campos principales (que SÍ existen en tu layout)
        TextView textNombrePlatillo;
        TextView textDescripcionPlatillo; // Si existe en tu layout
        ImageView imagenPlatillo; // Si existe en tu layout

        public PlatilloViewHolder(@NonNull View itemView) {
            super(itemView);

            // ✅ SOLO INICIALIZAR CAMPOS QUE EXISTEN EN TU LAYOUT
            textNombrePlatillo = itemView.findViewById(R.id.textNombrePlatillo);

            // ✅ ESTOS SON OPCIONALES - Solo se inicializan si existen en el layout
            textDescripcionPlatillo = itemView.findViewById(R.id.textDescripcionPlatillo);
            imagenPlatillo = itemView.findViewById(R.id.imagenPlatillo);

            // Log para debugging
            Log.d("PlatilloViewHolder", "🏗️ ViewHolder creado");
            Log.d("PlatilloViewHolder", "   - textNombrePlatillo: " + (textNombrePlatillo != null ? "✅" : "❌"));
            Log.d("PlatilloViewHolder", "   - textDescripcionPlatillo: " + (textDescripcionPlatillo != null ? "✅" : "❌"));
            Log.d("PlatilloViewHolder", "   - imagenPlatillo: " + (imagenPlatillo != null ? "✅" : "❌"));
        }
    }
}