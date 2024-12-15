package com.example.foodisea.model;

import com.example.foodisea.repository.UsuarioRepository;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Pedido {
    @Exclude
    private String id;

    private String clienteId;  // Referencia al cliente que hizo el pedido (ID)
    private String restauranteId;  // ID del restaurante donde se hizo el pedido
    private List<ProductoCantidad> productos;  // Lista de productos y sus cantidades solicitadas
    private String repartidorId;  // Referencia al repartidor asignado (ID)
    private String estado;  // "Recibido", "En preparación", "En camino", "Recogiendo pedido", "Entregado"
    private Date fechaPedido;
    private String direccionEntrega;
    private Double latitudEntrega;
    private Double longitudEntrega;
    private String verificacionEntregaId;  // Reemplaza codigoQrId y pagoId
    private String estadoVerificacion;     // "PENDIENTE", "ENTREGA_CONFIRMADA", "PAGO_CONFIRMADO", "COMPLETADO"
    private Double montoTotal;


    // Estados del pedido
    public static final String ESTADO_RECIBIDO = "Recibido";
    public static final String ESTADO_EN_PREPARACION = "En preparación";
    public static final String ESTADO_RECOGIENDO = "Recogiendo pedido";  // Nuevo estado
    public static final String ESTADO_EN_CAMINO = "En camino";
    public static final String ESTADO_ENTREGADO = "Entregado";

    //Constructor, getter y setter

    // Constructor


    public Pedido(String id, String clienteId, String restauranteId, List<ProductoCantidad> productos, String repartidorId, String estado, Date fechaPedido, String direccionEntrega, Double latitudEntrega, Double longitudEntrega, String verificacionEntregaId, String estadoVerificacion, Double montoTotal) {
        this.id = id;
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.productos = productos;
        this.repartidorId = repartidorId;
        this.estado = estado;
        this.fechaPedido = fechaPedido;
        this.direccionEntrega = direccionEntrega;
        this.latitudEntrega = latitudEntrega;
        this.longitudEntrega = longitudEntrega;
        this.verificacionEntregaId = verificacionEntregaId;
        this.estadoVerificacion = estadoVerificacion;
        this.montoTotal = montoTotal;
    }

    public Pedido() {
    }

    public void iniciarVerificacion() {
        if (!"En camino".equals(this.estado)) {
            throw new IllegalStateException("Solo se puede iniciar verificación cuando el pedido está en camino");
        }
        this.estadoVerificacion = "PENDIENTE";
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


    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
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

    public Double getLongitudEntrega() {
        return longitudEntrega;
    }

    public void setLongitudEntrega(Double longitudEntrega) {
        this.longitudEntrega = longitudEntrega;
    }

    public String getVerificacionEntregaId() {
        return verificacionEntregaId;
    }

    public void setVerificacionEntregaId(String verificacionEntregaId) {
        this.verificacionEntregaId = verificacionEntregaId;
    }

    public String getEstadoVerificacion() {
        return estadoVerificacion;
    }

    public void setEstadoVerificacion(String estadoVerificacion) {
        this.estadoVerificacion = estadoVerificacion;
    }

    public Double getLatitudEntrega() {
        return latitudEntrega;
    }

    public void setLatitudEntrega(Double latitudEntrega) {
        this.latitudEntrega = latitudEntrega;
    }


    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }
}
