package com.camilo.cocinarte.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.R;

import java.util.List;

public class PlatilloBanqueteAdapter extends RecyclerView.Adapter<PlatilloBanqueteAdapter.PlatilloViewHolder> {

    private Context context;
    private List<String> platillos;

    public PlatilloBanqueteAdapter(Context context) {
        this.context = context;
    }

    public void updatePlatillos(List<String> platillos) {
        this.platillos = platillos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlatilloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_platillo_banquete, parent, false);
        return new PlatilloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlatilloViewHolder holder, int position) {
        String platillo = platillos.get(position);
        holder.textNombrePlatillo.setText(platillo);
    }

    @Override
    public int getItemCount() {
        return platillos != null ? platillos.size() : 0;
    }

    static class PlatilloViewHolder extends RecyclerView.ViewHolder {
        TextView textNombrePlatillo;

        public PlatilloViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombrePlatillo = itemView.findViewById(R.id.textNombrePlatillo);
        }
    }
}