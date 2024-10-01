package com.example.foodisea.model;

public class Repartidor extends Usuario {
    private double latitud;
    private double longitud;
    private String estado;  // "Disponible", "Ocupado"

    public void recogerPedido(String pedidoId) {
        // Lógica para recoger el pedido
    }

    public void entregarPedido(String pedidoId) {
        // Lógica para entregar el pedido
    }

    //Constructor, getter y setter


    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

