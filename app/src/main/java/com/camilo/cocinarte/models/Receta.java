package com.camilo.cocinarte.models;

import java.io.Serializable;
import java.util.List;

public class Receta implements Serializable {
    private String nombre;
    private String imagenUri;
    private String tiempo;
    private String unidad;
    private String dificultad;
    private List<String> ingredientes;
    private List<String> pasos;
    private int kcal;
    private int proteinas;
    private int carbohidratos;
    private int grasa;



    public Receta(String nombre, String imagenUri, String tiempo, String unidad, String dificultad,
                  List<String> ingredientes, List<String> pasos,
                  int kcal, int proteinas, int carbohidratos, int grasa) {
        this.nombre = nombre;
        this.imagenUri = imagenUri;
        this.tiempo = tiempo;
        this.unidad = unidad;
        this.dificultad = dificultad;
        this.ingredientes = ingredientes;
        this.pasos = pasos;
        this.kcal = kcal;
        this.proteinas = proteinas;
        this.carbohidratos = carbohidratos;
        this.grasa = grasa;
    }

    // Getters (puedes agregar setters si necesitas)
    public String getNombre() {
        return nombre;
    }

    public String getImagenUri() {
        return imagenUri;
    }

    public String getTiempo() {
        return tiempo;
    }

    public String getUnidad() {
        return unidad;
    }
    public String getDificultad() {
        return dificultad;
    }

    public List<String> getIngredientes() {
        return ingredientes;
    }

    public List<String> getPasos() {
        return pasos;
    }

    public int getKcal() {
        return kcal;
    }

    public int getProteinas() {
        return proteinas;
    }

    public int getCarbohidratos() {
        return carbohidratos;
    }

    public int getGrasa() {
        return grasa;
    }
}
