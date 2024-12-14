package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.Random;

@IgnoreExtraProperties
public class CodigoQR {
    @Exclude
    private String id;
    private String verificacionId;  // ID de la verificación asociada
    private String tipo;           // "ENTREGA" o "PAGO"
    private String codigo;         // Código QR generado
    private String estado;         // "GENERADO", "ESCANEADO"
    private Date fechaGeneracion;
    private Date fechaEscaneo;

    public CodigoQR() {
    }


    public CodigoQR(String verificacionId, String tipo) {
        this.verificacionId = verificacionId;
        this.tipo = tipo;
        this.estado = "GENERADO";
        this.fechaGeneracion = new Date();
        this.codigo = generarCodigoUnico();
    }


    private String generarCodigoUnico() {
        // Generamos un código de 12 caracteres que incluye:
        // - Prefijo según tipo (E para entrega, P para pago)
        // - Timestamp actual en hexadecimal (8 caracteres)
        // - 3 caracteres aleatorios
        String prefijo = this.tipo.equals("ENTREGA") ? "E" : "P";

        // Obtener timestamp y convertirlo a hexadecimal
        String timestamp = Long.toHexString(System.currentTimeMillis()).toUpperCase();
        // Nos quedamos con los últimos 8 caracteres
        timestamp = timestamp.substring(Math.max(0, timestamp.length() - 8));

        // Generar 3 caracteres aleatorios (letras y números)
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder random = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 3; i++) {
            random.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return prefijo + timestamp + random.toString();
    }


    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getVerificacionId() {
        return verificacionId;
    }

    public void setVerificacionId(String verificacionId) {
        this.verificacionId = verificacionId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Date getFechaEscaneo() {
        return fechaEscaneo;
    }

    public void setFechaEscaneo(Date fechaEscaneo) {
        this.fechaEscaneo = fechaEscaneo;
    }
}

