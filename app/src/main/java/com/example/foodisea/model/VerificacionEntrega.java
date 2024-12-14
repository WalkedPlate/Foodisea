package com.example.foodisea.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class VerificacionEntrega {
    @Exclude
    private String id;
    private String pedidoId;
    private String qrEntregaId;
    private String qrPagoId;
    private boolean entregaConfirmada;
    private boolean pagoConfirmado;
    private Date fechaGeneracion;
    private Date fechaEntregaConfirmada;
    private Date fechaPagoConfirmado;

    public VerificacionEntrega() {}

    public VerificacionEntrega(String pedidoId) {
        this.pedidoId = pedidoId;
        this.entregaConfirmada = false;
        this.pagoConfirmado = false;
        this.fechaGeneracion = new Date();
    }

    public void asignarCodigosQR(String qrEntregaId, String qrPagoId) {
        this.qrEntregaId = qrEntregaId;
        this.qrPagoId = qrPagoId;
    }

    public boolean isVerificacionCompleta() {
        return entregaConfirmada && pagoConfirmado;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(String pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getQrEntregaId() {
        return qrEntregaId;
    }

    public void setQrEntregaId(String qrEntregaId) {
        this.qrEntregaId = qrEntregaId;
    }

    public String getQrPagoId() {
        return qrPagoId;
    }

    public void setQrPagoId(String qrPagoId) {
        this.qrPagoId = qrPagoId;
    }

    public boolean isEntregaConfirmada() {
        return entregaConfirmada;
    }

    public void setEntregaConfirmada(boolean entregaConfirmada) {
        this.entregaConfirmada = entregaConfirmada;
    }

    public boolean isPagoConfirmado() {
        return pagoConfirmado;
    }

    public void setPagoConfirmado(boolean pagoConfirmado) {
        this.pagoConfirmado = pagoConfirmado;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Date getFechaEntregaConfirmada() {
        return fechaEntregaConfirmada;
    }

    public void setFechaEntregaConfirmada(Date fechaEntregaConfirmada) {
        this.fechaEntregaConfirmada = fechaEntregaConfirmada;
    }

    public Date getFechaPagoConfirmado() {
        return fechaPagoConfirmado;
    }

    public void setFechaPagoConfirmado(Date fechaPagoConfirmado) {
        this.fechaPagoConfirmado = fechaPagoConfirmado;
    }
}
