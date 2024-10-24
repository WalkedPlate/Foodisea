package com.example.foodisea.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Pedido {

    private String id;
    private String clienteId;  // Referencia al cliente que hizo el pedido (ID)
    private String restauranteId;  // ID del restaurante donde se hizo el pedido
    private List<ProductoCantidad> productos;  // Lista de productos y sus cantidades solicitadas
    private String repartidorId;  // Referencia al repartidor asignado (ID)
    private String estado;  // "Recibido", "En preparación", "En camino", "Entregado"
    private Date fechaPedido;
    private String direccionEntrega;
    private String codigoQrId;  // Referencia al Código QR (ID)
    private String pagoId;  // Referencia al Pago (ID)


    //Constructor, getter y setter

    // Constructor
    public Pedido(String id, String clienteId, String restauranteId, List<ProductoCantidad> productos,
                  String repartidorId, String estado, Date fechaPedido, String direccionEntrega,
                  String codigoQrId, String pagoId) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.productos = productos;
        this.repartidorId = repartidorId;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.direccionEntrega = direccionEntrega;
        this.codigoQrId = codigoQrId;
        this.pagoId = pagoId;
    }


    public double getTotal(Map<String, Double> preciosProductos) {
        double total = 0.0;
        if (productos != null) {
            for (ProductoCantidad productoCantidad : productos) {
                Double precio = preciosProductos.get(productoCantidad.getProductoId());
                if (precio != null) {
                    total += precio * productoCantidad.getCantidad();
                }
            }
        }
        return total;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(String restauranteId) {
        this.restauranteId = restauranteId;
    }

    public List<ProductoCantidad> getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoCantidad> productos) {
        this.productos = productos;
    }

    public String getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(String repartidorId) {
        this.repartidorId = repartidorId;
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

    public String getCodigoQrId() {
        return codigoQrId;
    }

    public void setCodigoQrId(String codigoQrId) {
        this.codigoQrId = codigoQrId;
    }

    public String getPagoId() {
        return pagoId;
    }

    public void setPagoId(String pagoId) {
        this.pagoId = pagoId;
    }


}
