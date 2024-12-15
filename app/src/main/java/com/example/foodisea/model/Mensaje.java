package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Mensaje {
    @Exclude
    private String id; // ID generado automáticamente por Firestore

    private String chatId;      // ID del chat al que pertenece el mensaje
    private String emisorId;    // ID del usuario que envió el mensaje
    private String texto;       // Contenido del mensaje
    private Long timestamp;     // Marca de tiempo del mensaje
    private String tipo;        // Tipo de mensaje ("texto", "imagen", etc.)

    // Constructor vacío requerido por Firestore
    public Mensaje() {}

    // Getters y setters
    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getEmisorId() {
        return emisorId;
    }

    public void setEmisorId(String emisorId) {
        this.emisorId = emisorId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}