package com.example.foodisea.dto;

import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;

public class PedidoConDetalles {
    private Pedido pedido;
    private Restaurante restaurante;
    private Repartidor repartidor;

    public PedidoConDetalles(Pedido pedido, Restaurante restaurante, Repartidor repartidor) {
        this.pedido = pedido;
        this.restaurante = restaurante;
        this.repartidor = repartidor;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public Repartidor getRepartidor() {
        return repartidor;
    }

    public boolean tieneRepartidor() {
        return repartidor != null;
    }
}
