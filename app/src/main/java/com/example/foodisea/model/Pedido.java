package com.example.foodisea.model;

import java.util.Date;
import java.util.List;

public class Pedido {

    private String id;
    private Cliente cliente;  // Referencia al objeto Cliente que hizo el pedido
    private String restauranteId;  // ID del Restaurante donde se hizo el pedido
    private List<PlatoCantidad> platos;  // Lista de platos y sus cantidades solicitadas
    private Repartidor repartidor;  // Referencia al repartidor (si ya está asignado)
    private String estado;  // "Recibido", "En preparación", "En camino", "Entregado"
    private Date fechaPedido;
    private String direccionEntrega;
    private CodigoQR qrCode;  // Referencia al objeto CodigoQR
    private Pago pago;  // Referenc


    //Constructor, getter y setter

    // Constructor
    public Pedido(String id, Cliente cliente, String restauranteId, List<PlatoCantidad> platos,
                  Repartidor repartidor, String estado, Date fechaPedido, String direccionEntrega,
                  CodigoQR qrCode, Pago pago) {
        this.id = id;
        this.cliente = cliente;
        this.restauranteId = restauranteId;
        this.platos = platos;
        this.repartidor = repartidor;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.direccionEntrega = direccionEntrega;
        this.qrCode = qrCode;
        this.pago = pago;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(String restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<PlatoCantidad> getPlatos() {
        return platos;
    }

    public void setPlatos(List<PlatoCantidad> platos) {
        this.platos = platos;
    }

    public Repartidor getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(Repartidor repartidor) {
        this.repartidor = repartidor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Date getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(Date fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(String direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
    }

    public CodigoQR getQrCode() {
        return qrCode;
    }

    public void setQrCode(CodigoQR qrCode) {
        this.qrCode = qrCode;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }
}
