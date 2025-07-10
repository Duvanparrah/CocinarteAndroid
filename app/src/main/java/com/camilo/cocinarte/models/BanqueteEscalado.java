package com.camilo.cocinarte.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BanqueteEscalado implements Serializable {

    private int banqueteId;
    private int originalPeople;
    private int newPeople;
    private double scaleFactor;
    private List<IngredienteEscalado> scaledIngredients;
    private List<String> recommendations;
    private String adjustedPreparation;
    private boolean aiProcessed;
    private String aiVersion;
    private long processingTimeMs;

    // Constructores
    public BanqueteEscalado() {
        this.scaledIngredients = new ArrayList<>();
        this.recommendations = new ArrayList<>();
    }

    public BanqueteEscalado(int banqueteId, int originalPeople, int newPeople, double scaleFactor) {
        this();
        this.banqueteId = banqueteId;
        this.originalPeople = originalPeople;
        this.newPeople = newPeople;
        this.scaleFactor = scaleFactor;
    }

    // ✅ MÉTODO ESTÁTICO PARA CREAR DESDE JSON (corregido sin try-catch innecesario)
    public static BanqueteEscalado fromJson(JSONObject json) {
        BanqueteEscalado banquete = new BanqueteEscalado();

        banquete.setBanqueteId(json.optInt("banqueteId", 0));
        banquete.setOriginalPeople(json.optInt("originalPeople", 0));
        banquete.setNewPeople(json.optInt("newPeople", 0));
        banquete.setScaleFactor(json.optDouble("scaleFactor", 1.0));
        banquete.setAiProcessed(json.optBoolean("aiProcessed", false));
        banquete.setAiVersion(json.optString("aiVersion", ""));
        banquete.setProcessingTimeMs(json.optLong("processingTimeMs", 0));
        banquete.setAdjustedPreparation(json.optString("adjustedPreparation", ""));

        // Ingredientes escalados
        JSONArray ingredientsArray = json.optJSONArray("scaledIngredients");
        List<IngredienteEscalado> ingredientes = new ArrayList<>();
        if (ingredientsArray != null) {
            for (int i = 0; i < ingredientsArray.length(); i++) {
                JSONObject ingJson = ingredientsArray.optJSONObject(i);
                IngredienteEscalado ingrediente = IngredienteEscalado.fromJson(ingJson);
                if (ingrediente != null) {
                    ingredientes.add(ingrediente);
                }
            }
        }
        banquete.setScaledIngredients(ingredientes);

        // Recomendaciones
        JSONArray recommendationsArray = json.optJSONArray("recommendations");
        List<String> recomendaciones = new ArrayList<>();
        if (recommendationsArray != null) {
            for (int i = 0; i < recommendationsArray.length(); i++) {
                recomendaciones.add(recommendationsArray.optString(i));
            }
        }
        banquete.setRecommendations(recomendaciones);

        return banquete;
    }

    // ✅ GETTERS Y SETTERS
    public int getBanqueteId() { return banqueteId; }
    public void setBanqueteId(int banqueteId) { this.banqueteId = banqueteId; }

    public int getOriginalPeople() { return originalPeople; }
    public void setOriginalPeople(int originalPeople) { this.originalPeople = originalPeople; }

    public int getNewPeople() { return newPeople; }
    public void setNewPeople(int newPeople) { this.newPeople = newPeople; }

    public double getScaleFactor() { return scaleFactor; }
    public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }

    public List<IngredienteEscalado> getScaledIngredients() { return scaledIngredients; }
    public void setScaledIngredients(List<IngredienteEscalado> scaledIngredients) {
        this.scaledIngredients = scaledIngredients != null ? scaledIngredients : new ArrayList<>();
    }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations != null ? recommendations : new ArrayList<>();
    }

    public String getAdjustedPreparation() { return adjustedPreparation; }
    public void setAdjustedPreparation(String adjustedPreparation) { this.adjustedPreparation = adjustedPreparation; }

    public boolean isAiProcessed() { return aiProcessed; }
    public void setAiProcessed(boolean aiProcessed) { this.aiProcessed = aiProcessed; }

    public String getAiVersion() { return aiVersion; }
    public void setAiVersion(String aiVersion) { this.aiVersion = aiVersion; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    // ✅ MÉTODOS DE UTILIDAD
    public boolean hasScaledIngredients() {
        return scaledIngredients != null && !scaledIngredients.isEmpty();
    }

    public boolean hasRecommendations() {
        return recommendations != null && !recommendations.isEmpty();
    }

    public String getScaleDescription() {
        if (scaleFactor > 1.0) {
            return "Incremento de " + String.format("%.1f", scaleFactor) + "x";
        } else if (scaleFactor < 1.0) {
            return "Reducción de " + String.format("%.1f", 1.0 / scaleFactor) + "x";
        } else {
            return "Sin cambios";
        }
    }

    @Override
    public String toString() {
        return "BanqueteEscalado{" +
                "banqueteId=" + banqueteId +
                ", originalPeople=" + originalPeople +
                ", newPeople=" + newPeople +
                ", scaleFactor=" + scaleFactor +
                ", ingredientsCount=" + (scaledIngredients != null ? scaledIngredients.size() : 0) +
                ", recommendationsCount=" + (recommendations != null ? recommendations.size() : 0) +
                ", aiProcessed=" + aiProcessed +
                '}';
    }


// ✅ CLASE INTERNA: IngredienteEscalado
    public static class IngredienteEscalado implements Serializable {
        private int id;
        private String name;
        private String originalQuantity;
        private String scaledQuantity;
        private String unit;
        private String category;
        private String image;
        private String scalingNotes;
        private NutritionInfo nutrition;
        private double scaleFactor;
        private double adjustmentFactor = 1.0;
        private boolean aiProcessed = false;
        private boolean fallback = false;
        private String aiReasoning;

        public IngredienteEscalado() {}

        public static IngredienteEscalado fromJson(JSONObject json) {
            try {
                String name = json.optString("name", null);
                if (name == null || name.isEmpty()) return null;

                IngredienteEscalado ingrediente = new IngredienteEscalado();
                ingrediente.setId(json.optInt("id", 0));
                ingrediente.setName(name);
                ingrediente.setOriginalQuantity(json.optString("originalQuantity", ""));
                ingrediente.setScaledQuantity(json.optString("scaledQuantity", ""));
                ingrediente.setUnit(json.optString("unit", ""));
                ingrediente.setCategory(json.optString("category", ""));
                ingrediente.setImage(json.optString("image", ""));
                ingrediente.setScalingNotes(json.optString("scalingNotes", ""));
                ingrediente.setScaleFactor(json.optDouble("scaleFactor", 1.0));
                ingrediente.setAdjustmentFactor(json.optDouble("adjustmentFactor", 1.0));
                ingrediente.setAiProcessed(json.optBoolean("aiProcessed", false));
                ingrediente.setFallback(json.optBoolean("fallback", false));
                ingrediente.setAiReasoning(json.optString("aiReasoning", ""));

                JSONObject nutritionJson = json.optJSONObject("nutrition");
                if (nutritionJson != null) {
                    NutritionInfo nutricion = NutritionInfo.fromJson(nutritionJson);
                    ingrediente.setNutrition(nutricion);
                }

                return ingrediente;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // GETTERS Y SETTERS
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getOriginalQuantity() { return originalQuantity; }
        public void setOriginalQuantity(String originalQuantity) { this.originalQuantity = originalQuantity; }

        public String getScaledQuantity() { return scaledQuantity; }
        public void setScaledQuantity(String scaledQuantity) { this.scaledQuantity = scaledQuantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }

        public String getScalingNotes() { return scalingNotes; }
        public void setScalingNotes(String scalingNotes) { this.scalingNotes = scalingNotes; }

        public NutritionInfo getNutrition() { return nutrition; }
        public void setNutrition(NutritionInfo nutrition) { this.nutrition = nutrition; }

        public double getScaleFactor() { return scaleFactor; }
        public void setScaleFactor(double scaleFactor) { this.scaleFactor = scaleFactor; }

        public double getAdjustmentFactor() { return adjustmentFactor; }
        public void setAdjustmentFactor(double adjustmentFactor) { this.adjustmentFactor = adjustmentFactor; }

        public boolean isAiProcessed() { return aiProcessed; }
        public void setAiProcessed(boolean aiProcessed) { this.aiProcessed = aiProcessed; }

        public boolean isFallback() { return fallback; }
        public void setFallback(boolean fallback) { this.fallback = fallback; }

        public String getAiReasoning() { return aiReasoning; }
        public void setAiReasoning(String aiReasoning) { this.aiReasoning = aiReasoning; }

        public boolean hasImage() { return image != null && !image.isEmpty(); }
        public boolean hasScalingNotes() { return scalingNotes != null && !scalingNotes.isEmpty(); }

        public String getQuantityComparison() {
            return originalQuantity + " → " + scaledQuantity;
        }

        public String getFactorFormateado() {
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(scaleFactor) + "x";
        }

        public String getAjusteFormateado() {
            DecimalFormat df = new DecimalFormat("#.#");
            if (adjustmentFactor > 1.0) {
                return "+" + df.format((adjustmentFactor - 1.0) * 100) + "%";
            } else if (adjustmentFactor < 1.0) {
                return "-" + df.format((1.0 - adjustmentFactor) * 100) + "%";
            }
            return "0%";
        }
    }

    // ✅ CLASE INTERNA: NutritionInfo
    public static class NutritionInfo implements Serializable {
        private double calories;
        private double protein;
        private double carbs;
        private double fats;
        private double sugar;
        private double fiber;
        private double sodium;

        public NutritionInfo() {}

        public static NutritionInfo fromJson(JSONObject json) {
            try {
                NutritionInfo nutricion = new NutritionInfo();
                nutricion.setCalories(json.optDouble("calories", 0.0));
                nutricion.setProtein(json.optDouble("protein", 0.0));
                nutricion.setCarbs(json.optDouble("carbs", 0.0));
                nutricion.setFats(json.optDouble("fats", 0.0));
                nutricion.setSugar(json.optDouble("sugar", 0.0));
                nutricion.setFiber(json.optDouble("fiber", 0.0));
                nutricion.setSodium(json.optDouble("sodium", 0.0));
                return nutricion;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public double getCalories() { return calories; }
        public void setCalories(double calories) { this.calories = calories; }

        public double getProtein() { return protein; }
        public void setProtein(double protein) { this.protein = protein; }

        public double getCarbs() { return carbs; }
        public void setCarbs(double carbs) { this.carbs = carbs; }

        public double getFats() { return fats; }
        public void setFats(double fats) { this.fats = fats; }

        public double getSugar() { return sugar; }
        public void setSugar(double sugar) { this.sugar = sugar; }

        public double getFiber() { return fiber; }
        public void setFiber(double fiber) { this.fiber = fiber; }

        public double getSodium() { return sodium; }
        public void setSodium(double sodium) { this.sodium = sodium; }

        public boolean tieneInformacion() {
            return calories > 0 || protein > 0 || carbs > 0 || fats > 0;
        }

        public String getCaloriesFormateadas() {
            return String.format("%.0f kcal", calories);
        }

        public String getProteinFormateada() {
            return String.format("%.1fg", protein);
        }

        public String getCarbsFormateados() {
            return String.format("%.1fg", carbs);
        }

        public String getFatsFormateadas() {
            return String.format("%.1fg", fats);
        }
    }
}
