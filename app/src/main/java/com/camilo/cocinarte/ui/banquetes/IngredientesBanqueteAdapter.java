// ==========================================
// IngredientesBanqueteAdapter.java
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
import com.camilo.cocinarte.models.BanqueteIngrediente;

import java.util.List;

public class IngredientesBanqueteAdapter extends RecyclerView.Adapter<IngredientesBanqueteAdapter.IngredienteViewHolder> {

    private static final String TAG = "IngredientesBanqueteAdapter";

    private Context context;
    private List<BanqueteIngrediente> ingredientes;

    public IngredientesBanqueteAdapter(Context context, List<BanqueteIngrediente> ingredientes) {
        this.context = context;
        this.ingredientes = ingredientes;
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente_banquete, parent, false);
        return new IngredienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        BanqueteIngrediente ingrediente = ingredientes.get(position);

        // Nombre del ingrediente
        if (ingrediente.getNombreIngrediente() != null) {
            holder.textNombreIngrediente.setText(ingrediente.getNombreIngrediente());
        } else {
            holder.textNombreIngrediente.setText("Sin nombre");
        }

        // Cantidad del ingrediente
        if (holder.textCantidadIngrediente != null) {
            String cantidad = ingrediente.getCantidad();
            if (cantidad != null && !cantidad.isEmpty()) {
                holder.textCantidadIngrediente.setText(cantidad);
                holder.textCantidadIngrediente.setVisibility(View.VISIBLE);
            } else {
                holder.textCantidadIngrediente.setText("1 unidad");
                holder.textCantidadIngrediente.setVisibility(View.VISIBLE);
            }
        }

        // Categoría del ingrediente (si existe en el layout)
        if (holder.textCategoriaIngrediente != null) {
            String categoria = ingrediente.getCategoria();
            if (categoria != null && !categoria.isEmpty()) {
                holder.textCategoriaIngrediente.setText(categoria);
                holder.textCategoriaIngrediente.setVisibility(View.VISIBLE);
            } else {
                holder.textCategoriaIngrediente.setVisibility(View.GONE);
            }
        }

        // Imagen del ingrediente (si existe en el layout)
        if (holder.imagenIngrediente != null) {
            if (ingrediente.tieneImagen()) {
                RequestOptions options = new RequestOptions()
                        .placeholder(R.drawable.logo_cocinarte)
                        .error(R.drawable.logo_cocinarte)
                        .centerCrop();

                Glide.with(context)
                        .load(ingrediente.getImagenUrl())
                        .apply(options)
                        .into(holder.imagenIngrediente);
            } else {
                holder.imagenIngrediente.setImageResource(R.drawable.logo_cocinarte);
            }
        }

        // Información nutricional (si existe en el layout)
        mostrarInformacionNutricional(holder, ingrediente);
    }

    private void mostrarInformacionNutricional(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
        // Calorías
        if (holder.textCalorias != null) {
            double calorias = ingrediente.getCalorias_por_100g();
            if (calorias > 0) {
                holder.textCalorias.setText(String.format("%.0f kcal/100g", calorias));
                holder.textCalorias.setVisibility(View.VISIBLE);
            } else {
                holder.textCalorias.setVisibility(View.GONE);
            }
        }

        // Proteínas
        if (holder.textProteinas != null) {
            double proteinas = ingrediente.getProteinas_por_100g();
            if (proteinas > 0) {
                holder.textProteinas.setText(String.format("P: %.1fg", proteinas));
                holder.textProteinas.setVisibility(View.VISIBLE);
            } else {
                holder.textProteinas.setVisibility(View.GONE);
            }
        }

        // Carbohidratos
        if (holder.textCarbohidratos != null) {
            double carbohidratos = ingrediente.getCarbohidratos_por_100g();
            if (carbohidratos > 0) {
                holder.textCarbohidratos.setText(String.format("C: %.1fg", carbohidratos));
                holder.textCarbohidratos.setVisibility(View.VISIBLE);
            } else {
                holder.textCarbohidratos.setVisibility(View.GONE);
            }
        }

        // Grasas
        if (holder.textGrasas != null) {
            double grasas = ingrediente.getGrasas_totales_por_100g();
            if (grasas > 0) {
                holder.textGrasas.setText(String.format("G: %.1fg", grasas));
                holder.textGrasas.setVisibility(View.VISIBLE);
            } else {
                holder.textGrasas.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        TextView textNombreIngrediente;
        TextView textCantidadIngrediente;
        TextView textCategoriaIngrediente; // Opcional
        ImageView imagenIngrediente; // Opcional

        // Información nutricional (opcional)
        TextView textCalorias;
        TextView textProteinas;
        TextView textCarbohidratos;
        TextView textGrasas;

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombreIngrediente = itemView.findViewById(R.id.textNombreIngrediente);

            // Campos opcionales (pueden no existir en algunos layouts)
            textCantidadIngrediente = itemView.findViewById(R.id.textCantidadIngrediente);
            textCategoriaIngrediente = itemView.findViewById(R.id.textCategoriaIngrediente);
            imagenIngrediente = itemView.findViewById(R.id.imagenIngrediente);

            // Información nutricional opcional
            textCalorias = itemView.findViewById(R.id.textCalorias);
            textProteinas = itemView.findViewById(R.id.textProteinas);
            textCarbohidratos = itemView.findViewById(R.id.textCarbohidratos);
            textGrasas = itemView.findViewById(R.id.textGrasas);
        }
    }
}