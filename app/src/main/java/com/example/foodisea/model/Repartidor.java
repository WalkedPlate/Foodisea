package com.example.foodisea.model;

public class Repartidor extends Usuario {
    private Double latitud;
    private Double longitud;
    private String disposicion;  // "Disponible", "Ocupado"

    public Repartidor() {
    }

    // Constructor que llama a la clase Usuario
    public Repartidor(String id, String nombres, String apellidos, String correo, String telefono, String direccion, String tipoDocumentoId,
                      String documentoId, String fechaNacimiento, String foto, String estadoUsuario, String tipoUsuario,
                      double latitud, double longitud, String disposicion) {
        super(id, nombres, apellidos, correo, telefono, direccion, tipoDocumentoId, documentoId, fechaNacimiento, foto, estadoUsuario, tipoUsuario);
        this.latitud = latitud;
        this.longitud = longitud;
        this.disposicion = disposicion;
    }

    public void recogerPedido(String pedidoId) {
        // Lógica para recoger el pedido
    }

    public void entregarPedido(String pedidoId) {
        // Lógica para entregar el pedido
    }

    //Constructor, getter y setter


    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDisposicion() {
        return disposicion;
    }

    public void setDisposicion(String disposicion) {
        this.disposicion = disposicion;
    }
}

