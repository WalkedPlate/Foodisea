package com.example.foodisea.dto;

import com.example.foodisea.model.Pedido;

public class PedidoConDistancia {
    private Pedido pedido;
    private double distanciaTotal;

    public PedidoConDistancia(Pedido pedido, double distanciaTotal) {
        this.pedido = pedido;
        this.distanciaTotal = distanciaTotal;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public void setDistanciaTotal(double distanciaTotal) {
        this.distanciaTotal = distanciaTotal;
    }
}
