package com.camilo.cocinarte.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.models.Banquete;

import java.util.List;

public class BanqueteAdapter extends RecyclerView.Adapter<BanqueteAdapter.BanqueteViewHolder> {

    private Context context;
    private List<Banquete> banquetes;
    private OnBanqueteClickListener listener;

    public BanqueteAdapter(Context context, List<Banquete> banquetes, OnBanqueteClickListener listener) {
        this.context = context;
        this.banquetes = banquetes;
        this.listener = listener;
    }

    public interface OnBanqueteClickListener {
        void onBanqueteClick(Banquete banquete);
    }

    @NonNull
    @Override
    public BanqueteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banquete, parent, false);
        return new BanqueteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BanqueteViewHolder holder, int position) {
        Banquete banquete = banquetes.get(position);

        holder.textNombre.setText(banquete.getNombre());
        holder.textPersonas.setText(String.format("%d personas", banquete.getCantidadPersonas()));
        holder.textTiempo.setText(banquete.getTiempoPreparacion());

        Glide.with(context)
                .load(banquete.getImagenUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.imageBanquete);

        holder.itemView.setOnClickListener(v -> listener.onBanqueteClick(banquete));
    }

    @Override
    public int getItemCount() {
        return banquetes != null ? banquetes.size() : 0;
    }

    public void updateBanquetes(List<Banquete> newBanquetes) {
        this.banquetes = newBanquetes;
        notifyDataSetChanged();
    }

    static class BanqueteViewHolder extends RecyclerView.ViewHolder {
        ImageView imageBanquete;
        TextView textNombre;
        TextView textPersonas;
        TextView textTiempo;

        public BanqueteViewHolder(@NonNull View itemView) {
            super(itemView);
            imageBanquete = itemView.findViewById(R.id.imageBanquete);
            textNombre = itemView.findViewById(R.id.nombre_banquete);  // ID cambiado
            textPersonas = itemView.findViewById(R.id.cantidad_personas);  // ID cambiado
            textTiempo = itemView.findViewById(R.id.tiempo_preparacion);  // ID cambiado
        }
    }
}