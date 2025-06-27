package com.camilo.cocinarte.utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReaccionCache {

    // Mapa que asocia cada ID de receta con sus reacciones (JSONObject con likes y comentarios)
    private static final Map<Integer, JSONObject> cache = new HashMap<>();

    // Guardar reacciones en caché
    public static void guardarReacciones(int recetaId, JSONObject reacciones) {
        cache.put(recetaId, reacciones);
    }

    // Obtener reacciones desde la caché
    public static JSONObject getReacciones(int recetaId) {
        return cache.get(recetaId);
    }

    // Verificar si hay reacciones almacenadas
    public static boolean contieneReacciones(int recetaId) {
        return cache.containsKey(recetaId);
    }

    // Limpiar todas las reacciones de la caché (opcional, si se desea implementar)
    public static void limpiar() {
        cache.clear();
    }

    // Agrega esto en ReaccionCache
    public static void actualizarLike(int recetaId, boolean isLiked, int totalLikes) {
        JSONObject cache = getReacciones(recetaId);
        if (cache != null) {
            try {
                JSONObject likesObj = cache.optJSONObject("likes");
                if (likesObj == null) {
                    likesObj = new JSONObject();
                    cache.put("likes", likesObj);
                }
                likesObj.put("user_liked", isLiked);
                likesObj.put("total", totalLikes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
