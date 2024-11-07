package com.example.foodisea.dto;

import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;

public class PedidoConCliente {
    private Pedido pedido;
    private Cliente cliente;

    public PedidoConCliente(Pedido pedido, Cliente cliente) {
        this.pedido = pedido;
        this.cliente = cliente;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
