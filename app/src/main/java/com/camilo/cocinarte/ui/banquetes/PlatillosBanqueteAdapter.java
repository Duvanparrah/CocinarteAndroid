
// ==========================================
// PlatillosBanqueteAdapter.java
// ==========================================
package com.camilo.cocinarte.ui.banquetes;

import android.content.Context;
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

import java.util.List;

public class PlatillosBanqueteAdapter extends RecyclerView.Adapter<PlatillosBanqueteAdapter.PlatilloViewHolder> {

    private static final String TAG = "PlatillosBanqueteAdapter";

    private Context context;
    private List<BanquetePlatillo> platillos;

    public PlatillosBanqueteAdapter(Context context, List<BanquetePlatillo> platillos) {
        this.context = context;
        this.platillos = platillos;
    }

    @NonNull
    @Override
    public PlatilloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_platillo_banquete, parent, false);
        return new PlatilloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatilloViewHolder holder, int position) {
        BanquetePlatillo platillo = platillos.get(position);

        // Nombre del platillo
        if (platillo.getNombre() != null) {
            holder.textNombrePlatillo.setText(platillo.getNombre());
        } else {
            holder.textNombrePlatillo.setText("Sin nombre");
        }

        // Descripción/preparación (si existe en el layout)
        if (holder.textDescripcionPlatillo != null) {
            String descripcion = platillo.getDescripcion();
            if (descripcion != null && !descripcion.isEmpty()) {
                holder.textDescripcionPlatillo.setText(descripcion);
                holder.textDescripcionPlatillo.setVisibility(View.VISIBLE);
            } else {
                holder.textDescripcionPlatillo.setVisibility(View.GONE);
            }
        }

        // Imagen del platillo (si existe en el layout)
        if (holder.imagenPlatillo != null) {
            if (platillo.tieneImagen()) {
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.logo_cocinarte)
                        .error(R.drawable.logo_cocinarte)
                        .centerCrop();

                Glide.with(context)
                        .load(platillo.getImagenUrl())
                        .apply(options)
                        .into(holder.imagenPlatillo);
            } else {
                holder.imagenPlatillo.setImageResource(R.drawable.logo_cocinarte);
            }
        }
    }

    @Override
    public int getItemCount() {
        return platillos != null ? platillos.size() : 0;
    }

    static class PlatilloViewHolder extends RecyclerView.ViewHolder {
        TextView textNombrePlatillo;
        TextView textDescripcionPlatillo; // Opcional
        ImageView imagenPlatillo; // Opcional

        public PlatilloViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombrePlatillo = itemView.findViewById(R.id.textNombrePlatillo);

            // Campos opcionales (pueden no existir en algunos layouts)
            textDescripcionPlatillo = itemView.findViewById(R.id.textDescripcionPlatillo);
            imagenPlatillo = itemView.findViewById(R.id.imagenPlatillo);
        }
    }
}
