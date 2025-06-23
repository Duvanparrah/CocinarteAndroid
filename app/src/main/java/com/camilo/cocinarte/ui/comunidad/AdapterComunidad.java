package com.camilo.cocinarte.ui.comunidad;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.camilo.cocinarte.R;
import com.camilo.cocinarte.models.Receta;
import com.google.gson.Gson;

import java.util.List;




public class AdapterComunidad extends BaseAdapter {

    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
    }

    private OnRecetaClickListener listener;

    private final Context context;
    private final List<Receta> items;
    private final LayoutInflater inflater;

    public AdapterComunidad(Context context, List<Receta> items, OnRecetaClickListener listener) {
        this.listener = listener;
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ? convertView : inflater.inflate(R.layout.card_comunidad, parent, false);

        ImageView iVReceta = view.findViewById(R.id.iVReceta);
        //TextView textView = view.findViewById(R.id.tVNameUser);
        TextView tVTitle = view.findViewById(R.id.tVTitle);

        Receta item = items.get(position);

        Glide.with(context)
                .load(item.getImagen())
                .fitCenter()
                .into(iVReceta);

        tVTitle.setText(item.getTitulo());


        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecetaClick(item);
            }
        });

        return view;
    }
}