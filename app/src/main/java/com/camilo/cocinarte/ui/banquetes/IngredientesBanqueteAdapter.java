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
import com.camilo.cocinarte.models.BanqueteIngrediente;

import java.util.ArrayList;
import java.util.List;

public class IngredientesBanqueteAdapter extends RecyclerView.Adapter<IngredientesBanqueteAdapter.IngredienteViewHolder> {

    private static final String TAG = "IngredientesBanqueteAdapter";

    private Context context;
    private List<BanqueteIngrediente> ingredientes;
    private OnIngredienteClickListener listener;

    // Interface para clicks en ingredientes (opcional)
    public interface OnIngredienteClickListener {
        void onIngredienteClick(BanqueteIngrediente ingrediente, int position);
    }

    // Constructor principal
    public IngredientesBanqueteAdapter(Context context, List<BanqueteIngrediente> ingredientes) {
        this.context = context;
        this.ingredientes = ingredientes != null ? new ArrayList<>(ingredientes) : new ArrayList<>();

        Log.d(TAG, "🏗️ Adapter creado con " + this.ingredientes.size() + " ingredientes");
        logIngredientesInfo();
    }

    // Constructor con listener
    public IngredientesBanqueteAdapter(Context context, List<BanqueteIngrediente> ingredientes, OnIngredienteClickListener listener) {
        this(context, ingredientes);
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "📦 Creando ViewHolder de ingrediente");
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente_banquete, parent, false);
        return new IngredienteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        if (ingredientes == null || position >= ingredientes.size() || position < 0) {
            Log.e(TAG, "❌ Error: posición inválida (" + position + ") o lista nula. Size: " +
                    (ingredientes != null ? ingredientes.size() : "null"));
            return;
        }

        BanqueteIngrediente ingrediente = ingredientes.get(position);
        if (ingrediente == null) {
            Log.e(TAG, "❌ Error: ingrediente nulo en posición " + position);
            return;
        }

        Log.d(TAG, "🔗 Binding ingrediente " + (position + 1) + "/" + ingredientes.size() + ": " + ingrediente.getNombreIngrediente());

        // Configurar nombre del ingrediente
        configurarNombre(holder, ingrediente);

        // Configurar cantidad
        configurarCantidad(holder, ingrediente);

        // Configurar categoría (si existe el campo)
        configurarCategoria(holder, ingrediente);

        // Configurar imagen (si existe el campo)
        configurarImagen(holder, ingrediente);

        // Configurar información nutricional (si existe)
        configurarInformacionNutricional(holder, ingrediente);

        // Configurar click listener
        configurarClickListener(holder, ingrediente, position);

        Log.d(TAG, "✅ Ingrediente binding completado para posición: " + position);
    }

    private void configurarNombre(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
        if (holder.textNombreIngrediente != null) {
            String nombre = ingrediente.getNombreIngrediente();
            if (nombre != null && !nombre.trim().isEmpty()) {
                holder.textNombreIngrediente.setText(nombre.trim());
                holder.textNombreIngrediente.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ✅ Nombre configurado: " + nombre);
            } else {
                holder.textNombreIngrediente.setText("Ingrediente sin nombre");
                holder.textNombreIngrediente.setVisibility(View.VISIBLE);
                Log.w(TAG, "   ⚠️ Nombre vacío, usando texto por defecto");
            }
        }
    }

    private void configurarCantidad(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
        if (holder.textCantidadIngrediente != null) {
            String cantidad = ingrediente.getCantidad();
            if (cantidad != null && !cantidad.trim().isEmpty()) {
                // Formatear cantidad si es necesario
                String cantidadFormateada = formatearCantidad(cantidad.trim());
                holder.textCantidadIngrediente.setText(cantidadFormateada);
                holder.textCantidadIngrediente.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ✅ Cantidad configurada: " + cantidadFormateada);
            } else {
                holder.textCantidadIngrediente.setText("Al gusto");
                holder.textCantidadIngrediente.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ℹ️ Sin cantidad específica, usando 'Al gusto'");
            }
        }
    }

    private void configurarCategoria(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
        if (holder.textCategoriaIngrediente != null) {
            String categoria = ingrediente.getCategoria();
            if (categoria != null && !categoria.trim().isEmpty()) {
                holder.textCategoriaIngrediente.setText("🏷️ " + categoria.trim());
                holder.textCategoriaIngrediente.setVisibility(View.VISIBLE);
                Log.d(TAG, "   ✅ Categoría configurada: " + categoria);
            } else {
                holder.textCategoriaIngrediente.setVisibility(View.GONE);
                Log.d(TAG, "   ℹ️ Sin categoría, ocultando campo");
            }
        }
    }

    private void configurarImagen(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
        if (holder.imagenIngrediente != null) {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.logo_cocinarte)
                    .error(R.drawable.logo_cocinarte)
                    .centerCrop()
                    .override(200, 150); // Optimizar tamaño de imagen

            if (ingrediente.tieneImagen()) {
                String imageUrl = ingrediente.getImagenUrl();
                Log.d(TAG, "   🖼️ Cargando imagen de ingrediente: " + imageUrl);

                Glide.with(context)
                        .load(imageUrl)
                        .apply(options)
                        .into(holder.imagenIngrediente);
            } else {
                Log.d(TAG, "   🖼️ Sin imagen, usando placeholder");
                holder.imagenIngrediente.setImageResource(R.drawable.logo_cocinarte);
            }

            holder.imagenIngrediente.setVisibility(View.VISIBLE);
        }
    }

    private void configurarInformacionNutricional(IngredienteViewHolder holder, BanqueteIngrediente ingrediente) {
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
                holder.textProteinas.setText(String.format("🥩 %.1fg", proteinas));
                holder.textProteinas.setVisibility(View.VISIBLE);
            } else {
                holder.textProteinas.setVisibility(View.GONE);
            }
        }

        // Carbohidratos
        if (holder.textCarbohidratos != null) {
            double carbohidratos = ingrediente.getCarbohidratos_por_100g();
            if (carbohidratos > 0) {
                holder.textCarbohidratos.setText(String.format("🌾 %.1fg", carbohidratos));
                holder.textCarbohidratos.setVisibility(View.VISIBLE);
            } else {
                holder.textCarbohidratos.setVisibility(View.GONE);
            }
        }

        // Grasas
        if (holder.textGrasas != null) {
            double grasas = ingrediente.getGrasas_totales_por_100g();
            if (grasas > 0) {
                holder.textGrasas.setText(String.format("🥑 %.1fg", grasas));
                holder.textGrasas.setVisibility(View.VISIBLE);
            } else {
                holder.textGrasas.setVisibility(View.GONE);
            }
        }

        // Panel de información nutricional
        boolean hayInfoNutricional = (holder.textCalorias != null && holder.textCalorias.getVisibility() == View.VISIBLE) ||
                (holder.textProteinas != null && holder.textProteinas.getVisibility() == View.VISIBLE) ||
                (holder.textCarbohidratos != null && holder.textCarbohidratos.getVisibility() == View.VISIBLE) ||
                (holder.textGrasas != null && holder.textGrasas.getVisibility() == View.VISIBLE);

        if (holder.panelNutricional != null) {
            holder.panelNutricional.setVisibility(hayInfoNutricional ? View.VISIBLE : View.GONE);
        }
    }

    private void configurarClickListener(IngredienteViewHolder holder, BanqueteIngrediente ingrediente, int position) {
        holder.itemView.setOnClickListener(v -> {
            // Efecto visual al click
            v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100);

                        if (listener != null) {
                            listener.onIngredienteClick(ingrediente, position);
                        } else {
                            Log.d(TAG, "👆 Click en ingrediente: " + ingrediente.getNombreIngrediente());
                        }
                    });
        });
    }

    // ✅ MÉTODO AUXILIAR PARA FORMATEAR CANTIDAD
    private String formatearCantidad(String cantidad) {
        if (cantidad == null || cantidad.trim().isEmpty()) {
            return "Al gusto";
        }

        String cantidadLower = cantidad.toLowerCase().trim();

        // Si ya tiene unidades comunes, devolverla tal como está
        if (cantidadLower.contains("gr") || cantidadLower.contains("ml") ||
                cantidadLower.contains("kg") || cantidadLower.contains("lt") ||
                cantidadLower.contains("taza") || cantidadLower.contains("cdta") ||
                cantidadLower.contains("cda") || cantidadLower.contains("pizca") ||
                cantidadLower.contains("unidad") || cantidadLower.contains("diente")) {
            return cantidad;
        }

        // Si es solo número, agregar contexto
        try {
            double numero = Double.parseDouble(cantidadLower);
            if (numero == (int) numero) {
                return (int) numero + " unidades";
            } else {
                return cantidad + " unidades";
            }
        } catch (NumberFormatException e) {
            // No es un número, devolverla tal como está
            return cantidad;
        }
    }

    @Override
    public int getItemCount() {
        int count = ingredientes != null ? ingredientes.size() : 0;
        Log.d(TAG, "📊 getItemCount llamado: " + count + " ingredientes");
        return count;
    }

    // ✅ MÉTODOS PÚBLICOS PARA GESTIÓN DINÁMICA DE DATOS

    /**
     * Actualiza completamente la lista de ingredientes
     */
    public void updateIngredientes(List<BanqueteIngrediente> nuevosIngredientes) {
        Log.d(TAG, "🔄 Actualizando ingredientes: " +
                (nuevosIngredientes != null ? nuevosIngredientes.size() : 0) + " nuevos ingredientes");

        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }

        this.ingredientes.clear();
        if (nuevosIngredientes != null) {
            this.ingredientes.addAll(nuevosIngredientes);
        }

        notifyDataSetChanged();
        logIngredientesInfo();
    }

    /**
     * Agrega nuevos ingredientes al final de la lista
     */
    public void addIngredientes(List<BanqueteIngrediente> nuevosIngredientes) {
        if (nuevosIngredientes == null || nuevosIngredientes.isEmpty()) {
            Log.d(TAG, "⚠️ No hay ingredientes nuevos para agregar");
            return;
        }

        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }

        int startPosition = this.ingredientes.size();
        this.ingredientes.addAll(nuevosIngredientes);

        notifyItemRangeInserted(startPosition, nuevosIngredientes.size());
        Log.d(TAG, "➕ Agregados " + nuevosIngredientes.size() + " ingredientes en posición " + startPosition);
    }

    /**
     * Agrega un solo ingrediente
     */
    public void addIngrediente(BanqueteIngrediente ingrediente) {
        if (ingrediente == null) {
            Log.w(TAG, "⚠️ Intentando agregar ingrediente nulo");
            return;
        }

        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }

        int position = this.ingredientes.size();
        this.ingredientes.add(ingrediente);
        notifyItemInserted(position);

        Log.d(TAG, "➕ Ingrediente agregado: " + ingrediente.getNombreIngrediente() + " en posición " + position);
    }

    /**
     * Remueve un ingrediente por posición
     */
    public void removeIngrediente(int position) {
        if (this.ingredientes == null || position < 0 || position >= this.ingredientes.size()) {
            Log.w(TAG, "⚠️ No se puede remover ingrediente en posición " + position);
            return;
        }

        BanqueteIngrediente removed = this.ingredientes.remove(position);
        notifyItemRemoved(position);

        Log.d(TAG, "➖ Ingrediente removido: " + (removed != null ? removed.getNombreIngrediente() : "null") +
                " de posición " + position);
    }

    /**
     * Limpia todos los ingredientes
     */
    public void clearIngredientes() {
        if (this.ingredientes != null) {
            int size = this.ingredientes.size();
            this.ingredientes.clear();
            notifyItemRangeRemoved(0, size);
            Log.d(TAG, "🗑️ Limpiados " + size + " ingredientes");
        }
    }

    /**
     * Obtiene un ingrediente por posición
     */
    public BanqueteIngrediente getIngrediente(int position) {
        if (ingredientes != null && position >= 0 && position < ingredientes.size()) {
            return ingredientes.get(position);
        }
        Log.w(TAG, "⚠️ No se puede obtener ingrediente en posición " + position);
        return null;
    }

    /**
     * Obtiene la lista completa de ingredientes
     */
    public List<BanqueteIngrediente> getAllIngredientes() {
        return ingredientes != null ? new ArrayList<>(ingredientes) : new ArrayList<>();
    }

    /**
     * Verifica si la lista está vacía
     */
    public boolean isEmpty() {
        return ingredientes == null || ingredientes.isEmpty();
    }

    /**
     * Fuerza la actualización completa del adapter
     */
    public void forceUpdate() {
        Log.d(TAG, "🔄 Forzando actualización completa del adapter");
        notifyDataSetChanged();
    }

    /**
     * Busca ingredientes por nombre
     */
    public List<BanqueteIngrediente> buscarPorNombre(String query) {
        List<BanqueteIngrediente> resultados = new ArrayList<>();

        if (ingredientes == null || query == null || query.trim().isEmpty()) {
            return resultados;
        }

        String queryLower = query.toLowerCase().trim();

        for (BanqueteIngrediente ingrediente : ingredientes) {
            if (ingrediente != null && ingrediente.getNombreIngrediente() != null &&
                    ingrediente.getNombreIngrediente().toLowerCase().contains(queryLower)) {
                resultados.add(ingrediente);
            }
        }

        Log.d(TAG, "🔍 Búsqueda '" + query + "': " + resultados.size() + " resultados");
        return resultados;
    }

    /**
     * Filtra ingredientes por categoría
     */
    public List<BanqueteIngrediente> filtrarPorCategoria(String categoria) {
        List<BanqueteIngrediente> resultados = new ArrayList<>();

        if (ingredientes == null || categoria == null || categoria.trim().isEmpty()) {
            return resultados;
        }

        String categoriaLower = categoria.toLowerCase().trim();

        for (BanqueteIngrediente ingrediente : ingredientes) {
            if (ingrediente != null && ingrediente.getCategoria() != null &&
                    ingrediente.getCategoria().toLowerCase().equals(categoriaLower)) {
                resultados.add(ingrediente);
            }
        }

        Log.d(TAG, "🏷️ Filtro por categoría '" + categoria + "': " + resultados.size() + " resultados");
        return resultados;
    }

    // ✅ MÉTODOS AUXILIARES

    private void logIngredientesInfo() {
        if (ingredientes != null && !ingredientes.isEmpty()) {
            Log.d(TAG, "📋 Lista de ingredientes actual:");
            for (int i = 0; i < Math.min(ingredientes.size(), 5); i++) { // Solo los primeros 5
                BanqueteIngrediente ing = ingredientes.get(i);
                if (ing != null) {
                    Log.d(TAG, "   " + (i + 1) + ". " + ing.getNombreIngrediente() +
                            " (" + ing.getCantidad() + ")");
                } else {
                    Log.d(TAG, "   " + (i + 1) + ". null");
                }
            }
            if (ingredientes.size() > 5) {
                Log.d(TAG, "   ... y " + (ingredientes.size() - 5) + " más");
            }
        } else {
            Log.d(TAG, "📋 Lista de ingredientes vacía");
        }
    }

    // ✅ VIEWHOLDER MEJORADO
    static class IngredienteViewHolder extends RecyclerView.ViewHolder {
        // Campos principales
        TextView textNombreIngrediente;
        TextView textCantidadIngrediente;

        // Campos opcionales (pueden no existir en algunos layouts)
        TextView textCategoriaIngrediente;
        ImageView imagenIngrediente;

        // Información nutricional opcional
        TextView textCalorias;
        TextView textProteinas;
        TextView textCarbohidratos;
        TextView textGrasas;
        View panelNutricional; // Contenedor para la info nutricional

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);

            // Campos principales (requeridos)
            textNombreIngrediente = itemView.findViewById(R.id.textNombreIngrediente);
            textCantidadIngrediente = itemView.findViewById(R.id.textCantidadIngrediente);

            // Campos opcionales (pueden ser null si no están en el layout)
            textCategoriaIngrediente = itemView.findViewById(R.id.textCategoriaIngrediente);
            imagenIngrediente = itemView.findViewById(R.id.imagenIngrediente);

            // Información nutricional opcional
            textCalorias = itemView.findViewById(R.id.textCalorias);
            textProteinas = itemView.findViewById(R.id.textProteinas);
            textCarbohidratos = itemView.findViewById(R.id.textCarbohidratos);
            textGrasas = itemView.findViewById(R.id.textGrasas);
            panelNutricional = itemView.findViewById(R.id.panelNutricional);

            // Log para debugging
            Log.d("IngredienteViewHolder", "🏗️ ViewHolder creado");
            Log.d("IngredienteViewHolder", "   - textNombreIngrediente: " + (textNombreIngrediente != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textCantidadIngrediente: " + (textCantidadIngrediente != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textCategoriaIngrediente: " + (textCategoriaIngrediente != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - imagenIngrediente: " + (imagenIngrediente != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textCalorias: " + (textCalorias != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textProteinas: " + (textProteinas != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textCarbohidratos: " + (textCarbohidratos != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - textGrasas: " + (textGrasas != null ? "✅" : "❌"));
            Log.d("IngredienteViewHolder", "   - panelNutricional: " + (panelNutricional != null ? "✅" : "❌"));
        }
    }
}