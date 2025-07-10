package com.camilo.cocinarte.ui.banquetes;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
// ✅ CAMBIAR ESTE IMPORT:
// import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.camilo.cocinarte.R;
import com.camilo.cocinarte.models.BanqueteEscalado;

import java.util.ArrayList;
import java.util.List;

public class IngredientesEscaladosAdapter extends RecyclerView.Adapter<IngredientesEscaladosAdapter.IngredienteEscaladoViewHolder> {

    private static final String TAG = "IngredientesEscaladosAdapter";

    private Context context;
    private List<BanqueteEscalado.IngredienteEscalado> ingredientes;
    private OnIngredienteEscaladoClickListener listener;

    // Interface para clicks (opcional)
    public interface OnIngredienteEscaladoClickListener {
        void onIngredienteClick(BanqueteEscalado.IngredienteEscalado ingrediente, int position);
    }

    // Constructor
    public IngredientesEscaladosAdapter(Context context) {
        this.context = context;
        this.ingredientes = new ArrayList<>();
        Log.d(TAG, "🏗️ Adapter creado");
    }

    public IngredientesEscaladosAdapter(Context context, OnIngredienteEscaladoClickListener listener) {
        this(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredienteEscaladoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente_escalado, parent, false);
        return new IngredienteEscaladoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredienteEscaladoViewHolder holder, int position) {
        if (ingredientes == null || position >= ingredientes.size()) {
            Log.e(TAG, "❌ Posición inválida: " + position);
            return;
        }

        BanqueteEscalado.IngredienteEscalado ingrediente = ingredientes.get(position);
        if (ingrediente == null) {
            Log.e(TAG, "❌ Ingrediente nulo en posición: " + position);
            return;
        }

        Log.d(TAG, "🔗 Binding ingrediente: " + ingrediente.getName());

        // Configurar información básica
        configurarInformacionBasica(holder, ingrediente);

        // Configurar cantidades
        configurarCantidades(holder, ingrediente);

        // Configurar factores de escalado
        configurarFactoresEscalado(holder, ingrediente);

        // Configurar información de IA
        configurarInformacionIA(holder, ingrediente);

        // Configurar información nutricional
        configurarInformacionNutricional(holder, ingrediente);

        // Configurar colores según el tipo de procesamiento
        configurarColoresCard(holder, ingrediente);

        // Configurar click listener
        configurarClickListener(holder, ingrediente, position);
    }

    // ✅ CONFIGURAR INFORMACIÓN BÁSICA
    private void configurarInformacionBasica(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        // Nombre del ingrediente
        if (holder.textNombreIngrediente != null) {
            holder.textNombreIngrediente.setText(ingrediente.getName());
        }

        // Categoría
        if (holder.textCategoria != null) {
            String categoria = ingrediente.getCategory();
            if (categoria != null && !categoria.isEmpty()) {
                holder.textCategoria.setText("🏷️ " + categoria);
                holder.textCategoria.setVisibility(View.VISIBLE);
            } else {
                holder.textCategoria.setVisibility(View.GONE);
            }
        }

        // Unidad
        if (holder.textUnidad != null) {
            String unidad = ingrediente.getUnit();
            if (unidad != null && !unidad.isEmpty()) {
                holder.textUnidad.setText("📏 " + unidad);
                holder.textUnidad.setVisibility(View.VISIBLE);
            } else {
                holder.textUnidad.setVisibility(View.GONE);
            }
        }
    }

    // ✅ CONFIGURAR CANTIDADES
    private void configurarCantidades(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        // Cantidad original
        if (holder.textCantidadOriginal != null) {
            String cantidadOriginal = ingrediente.getOriginalQuantity();
            holder.textCantidadOriginal.setText("Original: " + (cantidadOriginal != null ? cantidadOriginal : "N/A"));
        }

        // Cantidad escalada
        if (holder.textCantidadEscalada != null) {
            String cantidadEscalada = ingrediente.getScaledQuantity();
            holder.textCantidadEscalada.setText("Escalado: " + (cantidadEscalada != null ? cantidadEscalada : "N/A"));
        }
    }

    // ✅ CONFIGURAR FACTORES DE ESCALADO
    private void configurarFactoresEscalado(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        // Factor de escala
        if (holder.textFactorEscala != null) {
            holder.textFactorEscala.setText("Factor: " + ingrediente.getFactorFormateado());
        }

        // Factor de ajuste IA
        if (holder.textFactorAjuste != null) {
            double factorAjuste = ingrediente.getAdjustmentFactor();
            if (factorAjuste != 1.0) {
                holder.textFactorAjuste.setText("Ajuste IA: " + ingrediente.getAjusteFormateado());
                holder.textFactorAjuste.setVisibility(View.VISIBLE);

                // Colorear según el tipo de ajuste
                if (factorAjuste > 1.0) {
                    holder.textFactorAjuste.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                } else if (factorAjuste < 1.0) {
                    holder.textFactorAjuste.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                }
            } else {
                holder.textFactorAjuste.setVisibility(View.GONE);
            }
        }
    }

    // ✅ CONFIGURAR INFORMACIÓN DE IA
    private void configurarInformacionIA(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        // Indicador de procesamiento con IA
        if (holder.textProcesadoIA != null) {
            if (ingrediente.isAiProcessed()) {
                holder.textProcesadoIA.setText("🤖 Procesado con IA");
                holder.textProcesadoIA.setTextColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
                holder.textProcesadoIA.setVisibility(View.VISIBLE);
            } else if (ingrediente.isFallback()) {
                holder.textProcesadoIA.setText("⚠️ Fallback");
                holder.textProcesadoIA.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.textProcesadoIA.setVisibility(View.VISIBLE);
            } else {
                holder.textProcesadoIA.setVisibility(View.GONE);
            }
        }

        // Razonamiento de IA
        if (holder.textRazonamientoIA != null) {
            String razonamiento = ingrediente.getAiReasoning();
            if (razonamiento != null && !razonamiento.isEmpty()) {
                holder.textRazonamientoIA.setText("💡 " + razonamiento);
                holder.textRazonamientoIA.setVisibility(View.VISIBLE);
            } else {
                holder.textRazonamientoIA.setVisibility(View.GONE);
            }
        }
    }

    // ✅ CONFIGURAR INFORMACIÓN NUTRICIONAL
    private void configurarInformacionNutricional(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        BanqueteEscalado.NutritionInfo nutrition = ingrediente.getNutrition();

        if (nutrition != null && nutrition.tieneInformacion()) {
            // Calorías
            if (holder.textCalorias != null) {
                holder.textCalorias.setText("⚡ " + nutrition.getCaloriesFormateadas());
                holder.textCalorias.setVisibility(View.VISIBLE);
            }

            // Proteínas
            if (holder.textProteinas != null) {
                holder.textProteinas.setText("🥩 " + nutrition.getProteinFormateada());
                holder.textProteinas.setVisibility(View.VISIBLE);
            }

            // Carbohidratos
            if (holder.textCarbohidratos != null) {
                holder.textCarbohidratos.setText("🌾 " + nutrition.getCarbsFormateados());
                holder.textCarbohidratos.setVisibility(View.VISIBLE);
            }

            // Grasas
            if (holder.textGrasas != null) {
                holder.textGrasas.setText("🥑 " + nutrition.getFatsFormateadas());
                holder.textGrasas.setVisibility(View.VISIBLE);
            }

            // Mostrar panel nutricional
            if (holder.panelNutricional != null) {
                holder.panelNutricional.setVisibility(View.VISIBLE);
            }
        } else {
            // Ocultar información nutricional
            if (holder.textCalorias != null) holder.textCalorias.setVisibility(View.GONE);
            if (holder.textProteinas != null) holder.textProteinas.setVisibility(View.GONE);
            if (holder.textCarbohidratos != null) holder.textCarbohidratos.setVisibility(View.GONE);
            if (holder.textGrasas != null) holder.textGrasas.setVisibility(View.GONE);
            if (holder.panelNutricional != null) holder.panelNutricional.setVisibility(View.GONE);
        }
    }

    // ✅ CONFIGURAR COLORES DE CARD - CORREGIDO
    private void configurarColoresCard(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente) {
        if (holder.cardContainer != null) {
            int strokeColor;
            int strokeWidth = 2;

            if (ingrediente.isAiProcessed()) {
                // Verde para procesado con IA
                strokeColor = ContextCompat.getColor(context, android.R.color.holo_green_light);
            } else if (ingrediente.isFallback()) {
                // Naranja para fallback
                strokeColor = ContextCompat.getColor(context, android.R.color.holo_orange_light);
            } else {
                // Gris para escalado normal
                strokeColor = ContextCompat.getColor(context, android.R.color.darker_gray);
                strokeWidth = 1;
            }

            // ✅ AHORA FUNCIONARÁ CON MaterialCardView
            holder.cardContainer.setStrokeColor(strokeColor);
            holder.cardContainer.setStrokeWidth(strokeWidth);
        }
    }

    // ✅ CONFIGURAR CLICK LISTENER
    private void configurarClickListener(IngredienteEscaladoViewHolder holder, BanqueteEscalado.IngredienteEscalado ingrediente, int position) {
        // Efecto visual al click
        holder.itemView.setOnClickListener(v -> {
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
                            Log.d(TAG, "👆 Click en ingrediente: " + ingrediente.getName());
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return ingredientes != null ? ingredientes.size() : 0;
    }

    // ✅ MÉTODOS PÚBLICOS PARA GESTIÓN DE DATOS
    public void updateIngredientes(List<BanqueteEscalado.IngredienteEscalado> nuevosIngredientes) {
        Log.d(TAG, "🔄 Actualizando ingredientes: " +
                (nuevosIngredientes != null ? nuevosIngredientes.size() : 0));

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

    public void addIngrediente(BanqueteEscalado.IngredienteEscalado ingrediente) {
        if (ingrediente == null) return;

        if (this.ingredientes == null) {
            this.ingredientes = new ArrayList<>();
        }

        int position = this.ingredientes.size();
        this.ingredientes.add(ingrediente);
        notifyItemInserted(position);

        Log.d(TAG, "➕ Ingrediente agregado: " + ingrediente.getName());
    }

    public void clearIngredientes() {
        if (this.ingredientes != null) {
            int size = this.ingredientes.size();
            this.ingredientes.clear();
            notifyItemRangeRemoved(0, size);
            Log.d(TAG, "🗑️ Limpiados " + size + " ingredientes");
        }
    }

    public boolean isEmpty() {
        return ingredientes == null || ingredientes.isEmpty();
    }

    public List<BanqueteEscalado.IngredienteEscalado> getAllIngredientes() {
        return ingredientes != null ? new ArrayList<>(ingredientes) : new ArrayList<>();
    }

    // ✅ MÉTODO AUXILIAR PARA LOGGING
    private void logIngredientesInfo() {
        if (ingredientes != null && !ingredientes.isEmpty()) {
            Log.d(TAG, "📋 Ingredientes escalados actuales:");
            for (int i = 0; i < Math.min(ingredientes.size(), 3); i++) {
                BanqueteEscalado.IngredienteEscalado ing = ingredientes.get(i);
                Log.d(TAG, "   " + (i + 1) + ". " + ing.getName() +
                        " (" + ing.getOriginalQuantity() + " → " + ing.getScaledQuantity() + ")");
            }
            if (ingredientes.size() > 3) {
                Log.d(TAG, "   ... y " + (ingredientes.size() - 3) + " más");
            }
        }
    }

    // ✅ VIEWHOLDER CORREGIDO
    static class IngredienteEscaladoViewHolder extends RecyclerView.ViewHolder {
        // ✅ CAMBIAR CardView por MaterialCardView
        MaterialCardView cardContainer;
        TextView textNombreIngrediente;
        TextView textCategoria;
        TextView textUnidad;
        TextView textCantidadOriginal;
        TextView textCantidadEscalada;
        TextView textFactorEscala;
        TextView textFactorAjuste;
        TextView textProcesadoIA;
        TextView textRazonamientoIA;

        // Información nutricional
        View panelNutricional;
        TextView textCalorias;
        TextView textProteinas;
        TextView textCarbohidratos;
        TextView textGrasas;

        public IngredienteEscaladoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Elementos principales
            cardContainer = itemView.findViewById(R.id.card_container);
            textNombreIngrediente = itemView.findViewById(R.id.text_nombre_ingrediente);
            textCategoria = itemView.findViewById(R.id.text_categoria);
            textUnidad = itemView.findViewById(R.id.text_unidad);

            // Cantidades
            textCantidadOriginal = itemView.findViewById(R.id.text_cantidad_original);
            textCantidadEscalada = itemView.findViewById(R.id.text_cantidad_escalada);

            // Factores
            textFactorEscala = itemView.findViewById(R.id.text_factor_escala);
            textFactorAjuste = itemView.findViewById(R.id.text_factor_ajuste);

            // Información de IA
            textProcesadoIA = itemView.findViewById(R.id.text_procesado_ia);
            textRazonamientoIA = itemView.findViewById(R.id.text_razonamiento_ia);

            // Información nutricional
            panelNutricional = itemView.findViewById(R.id.panel_nutricional);
            textCalorias = itemView.findViewById(R.id.text_calorias);
            textProteinas = itemView.findViewById(R.id.text_proteinas);
            textCarbohidratos = itemView.findViewById(R.id.text_carbohidratos);
            textGrasas = itemView.findViewById(R.id.text_grasas);

            Log.d("IngredienteEscaladoViewHolder", "🏗️ ViewHolder creado");
        }
    }
}