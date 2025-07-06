package com.camilo.cocinarte.models;

import com.google.gson.annotations.SerializedName;

/**
 * ✅ CLASE GENÉRICA PARA RESPUESTAS DE LA API
 * Envuelve las respuestas del servidor con metadatos de estado
 */
public class ApiResponse<T> {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private T data;

    @SerializedName("message")
    private String message;

    @SerializedName("error")
    private String error;

    @SerializedName("total")
    private Integer total;

    @SerializedName("page")
    private Integer page;

    @SerializedName("limit")
    private Integer limit;

    @SerializedName("timestamp")
    private String timestamp;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================

    public ApiResponse() {}

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // ==========================================
    // MÉTODOS DE UTILIDAD
    // ==========================================

    /**
     * ✅ VERIFICAR SI LA RESPUESTA ES EXITOSA Y TIENE DATOS
     */
    public boolean hasData() {
        return success && data != null;
    }

    /**
     * ✅ OBTENER MENSAJE DE ERROR O GENERAL
     */
    public String getDisplayMessage() {
        if (!success && error != null && !error.trim().isEmpty()) {
            return error;
        }
        return message != null ? message : "";
    }

    /**
     * ✅ VERIFICAR SI ES UNA RESPUESTA PAGINADA
     */
    public boolean isPaginated() {
        return page != null && limit != null;
    }

    /**
     * ✅ OBTENER INFORMACIÓN DE PAGINACIÓN
     */
    public PaginationInfo getPaginationInfo() {
        if (!isPaginated()) {
            return null;
        }

        return new PaginationInfo(page, limit, total);
    }

    // ==========================================
    // MÉTODOS ESTÁTICOS PARA CREAR RESPUESTAS
    // ==========================================

    /**
     * ✅ CREAR RESPUESTA EXITOSA CON DATOS
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    /**
     * ✅ CREAR RESPUESTA EXITOSA CON DATOS Y MENSAJE
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    /**
     * ✅ CREAR RESPUESTA DE ERROR
     */
    public static <T> ApiResponse<T> error(String error) {
        ApiResponse<T> response = new ApiResponse<>(false, null, null);
        response.setError(error);
        return response;
    }

    /**
     * ✅ CREAR RESPUESTA DE ERROR CON MENSAJE
     */
    public static <T> ApiResponse<T> error(String error, String message) {
        ApiResponse<T> response = new ApiResponse<>(false, null, message);
        response.setError(error);
        return response;
    }

    // ==========================================
    // CLASE INTERNA PARA INFORMACIÓN DE PAGINACIÓN
    // ==========================================

    public static class PaginationInfo {
        private final int currentPage;
        private final int itemsPerPage;
        private final Integer totalItems;

        public PaginationInfo(int currentPage, int itemsPerPage, Integer totalItems) {
            this.currentPage = currentPage;
            this.itemsPerPage = itemsPerPage;
            this.totalItems = totalItems;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getItemsPerPage() {
            return itemsPerPage;
        }

        public Integer getTotalItems() {
            return totalItems;
        }

        public Integer getTotalPages() {
            if (totalItems == null || itemsPerPage <= 0) {
                return null;
            }
            return (int) Math.ceil((double) totalItems / itemsPerPage);
        }

        public boolean hasNextPage() {
            Integer totalPages = getTotalPages();
            return totalPages != null && currentPage < totalPages;
        }

        public boolean hasPreviousPage() {
            return currentPage > 1;
        }

        @Override
        public String toString() {
            return "PaginationInfo{" +
                    "currentPage=" + currentPage +
                    ", itemsPerPage=" + itemsPerPage +
                    ", totalItems=" + totalItems +
                    ", totalPages=" + getTotalPages() +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", total=" + total +
                ", page=" + page +
                ", limit=" + limit +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}