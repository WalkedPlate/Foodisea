package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class CodigoQR {
    @Exclude
    private String id;
    private String pedidoId;  // ID del pedido asociado
    private String codigo;
    private String estado;  // "Generado", "Escaneado"
    private Date fechaGeneracion;

    //Constructor, getter y setter

    // Constructor de la clase CodigoQR
    public CodigoQR(String id, String pedidoId, String codigo, String estado, Date fechaGeneracion) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.codigo = codigo;
        this.estado = estado;
        this.fechaGeneracion = fechaGeneracion;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
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
}

