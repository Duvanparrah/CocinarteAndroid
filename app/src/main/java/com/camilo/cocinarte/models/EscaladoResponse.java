package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EscaladoResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private EscaladoData data;

    // Constructores
    public EscaladoResponse() {}

    public EscaladoResponse(boolean success, String message, EscaladoData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public EscaladoData getData() {
        return data;
    }

    public void setData(EscaladoData data) {
        this.data = data;
    }

    // Clase interna EscaladoData
    public static class EscaladoData {

        @SerializedName("banquete_id")
        private int banqueteId;

        @SerializedName("original_portions")
        private int originalPortions;

        @SerializedName("new_portions")
        private int newPortions;

        @SerializedName("scale_factor")
        private double scaleFactor;

        @SerializedName("scaled_ingredients")
        private List<IngredienteEscalado> scaledIngredients;

        @SerializedName("ai_processed")
        private boolean aiProcessed;

        @SerializedName("ai_version")
        private String aiVersion;

        @SerializedName("recommendations")
        private List<String> recommendations;

        @SerializedName("processing_time_ms")
        private long processingTimeMs;

        // Constructores
        public EscaladoData() {}

        // Getters y Setters
        public int getBanqueteId() {
            return banqueteId;
        }

        public void setBanqueteId(int banqueteId) {
            this.banqueteId = banqueteId;
        }

        public int getOriginalPortions() {
            return originalPortions;
        }

        public void setOriginalPortions(int originalPortions) {
            this.originalPortions = originalPortions;
        }

        public int getNewPortions() {
            return newPortions;
        }

        public void setNewPortions(int newPortions) {
            this.newPortions = newPortions;
        }

        public double getScaleFactor() {
            return scaleFactor;
        }

        public void setScaleFactor(double scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        public List<IngredienteEscalado> getScaledIngredients() {
            return scaledIngredients;
        }

        public void setScaledIngredients(List<IngredienteEscalado> scaledIngredients) {
            this.scaledIngredients = scaledIngredients;
        }

        public boolean isAiProcessed() {
            return aiProcessed;
        }

        public void setAiProcessed(boolean aiProcessed) {
            this.aiProcessed = aiProcessed;
        }

        public String getAiVersion() {
            return aiVersion;
        }

        public void setAiVersion(String aiVersion) {
            this.aiVersion = aiVersion;
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public void setProcessingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }
    }

    // Clase interna IngredienteEscalado
    public static class IngredienteEscalado {

        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("original_quantity")
        private String originalQuantity;

        @SerializedName("scaled_quantity")
        private String scaledQuantity;

        @SerializedName("category")
        private String category;

        @SerializedName("nutrition")
        private NutricionEscalada nutrition;

        @SerializedName("scaling_notes")
        private String scalingNotes;

        // Constructores
        public IngredienteEscalado() {}

        // Getters y Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOriginalQuantity() {
            return originalQuantity;
        }

        public void setOriginalQuantity(String originalQuantity) {
            this.originalQuantity = originalQuantity;
        }

        public String getScaledQuantity() {
            return scaledQuantity;
        }

        public void setScaledQuantity(String scaledQuantity) {
            this.scaledQuantity = scaledQuantity;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public NutricionEscalada getNutrition() {
            return nutrition;
        }

        public void setNutrition(NutricionEscalada nutrition) {
            this.nutrition = nutrition;
        }

        public String getScalingNotes() {
            return scalingNotes;
        }

        public void setScalingNotes(String scalingNotes) {
            this.scalingNotes = scalingNotes;
        }
    }

    // Clase interna NutricionEscalada
    public static class NutricionEscalada {

        @SerializedName("calories")
        private double calories;

        @SerializedName("protein")
        private double protein;

        @SerializedName("carbs")
        private double carbs;

        @SerializedName("fats")
        private double fats;

        @SerializedName("sugar")
        private double sugar;

        @SerializedName("fiber")
        private double fiber;

        @SerializedName("sodium")
        private double sodium;

        // Constructores
        public NutricionEscalada() {}

        // Getters y Setters
        public double getCalories() {
            return calories;
        }

        public void setCalories(double calories) {
            this.calories = calories;
        }

        public double getProtein() {
            return protein;
        }

        public void setProtein(double protein) {
            this.protein = protein;
        }

        public double getCarbs() {
            return carbs;
        }

        public void setCarbs(double carbs) {
            this.carbs = carbs;
        }

        public double getFats() {
            return fats;
        }

        public void setFats(double fats) {
            this.fats = fats;
        }

        public double getSugar() {
            return sugar;
        }

        public void setSugar(double sugar) {
            this.sugar = sugar;
        }

        public double getFiber() {
            return fiber;
        }

        public void setFiber(double fiber) {
            this.fiber = fiber;
        }

        public double getSodium() {
            return sodium;
        }

        public void setSodium(double sodium) {
            this.sodium = sodium;
        }
    }
}