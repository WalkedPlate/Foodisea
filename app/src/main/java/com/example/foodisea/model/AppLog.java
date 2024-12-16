package com.example.foodisea.model;

public class AppLog {
    private String id;
    private String userId; // ID del usuario que realizó la acción
    private String actionType; // Tipo de acción (creación de usuario, pedido, etc.)
    private String details; // Detalles específicos de la acción
    private long timestamp; // Marca de tiempo de la acción
    private String userRole; // Rol del usuario que realizó la acción

    public AppLog() {
    }

    public AppLog(String id, String userId, String actionType, String details, long timestamp, String userRole) {
        this.id = id;
        this.userId = userId;
        this.actionType = actionType;
        this.details = details;
        this.timestamp = timestamp;
        this.userRole = userRole;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
