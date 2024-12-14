package com.example.foodisea.firestore;




import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Carrito;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Reporte;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Superadmin;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreInitializer {
    private final FirebaseFirestore db;

    public FirestoreInitializer() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void initializeDatabase() {
        // Crear usuarios base
        initializeSuperAdmin();
        initializeCliente();
        initializeRepartidor();
        initializeAdministradorRestaurante();

        // Crear otros documentos
        initializeRestaurante();
        initializeProducto();
        initializeCarrito();
        initializePedido();
        initializePago();
        initializeReporte();
    }

    private void initializeSuperAdmin() {
        Superadmin superadmin = new Superadmin(
                "SA001",
                "Juan",
                "Pérez",
                "admin@foodisea.com",
                "987654321",
                "Av. Principal 123",
                "DNI",
                "12345678",
                "1990-01-01",
                "superadmin_photo.jpg",
                "Activo",
                "Superadmin"
        );

        db.collection("usuarios").document(superadmin.getId()).set(superadmin);
    }

    private void initializeCliente() {
        Cliente cliente = new Cliente(
                "CL001",
                "María",
                "García",
                "maria@email.com",
                "999888777",
                "Calle Las Flores 456",
                "DNI",
                "87654321",
                "1995-05-15",
                "cliente_photo.jpg",
                "Activo",
                "Cliente"
        );

        db.collection("usuarios").document(cliente.getId()).set(cliente);
    }

    private void initializeRepartidor() {
        Repartidor repartidor = new Repartidor(
                "RP001",
                "Carlos",
                "López",
                "carlos@delivery.com",
                "966555444",
                "Av. Los Pinos 789",
                "DNI",
                "45678912",
                "1993-08-20",
                "repartidor_photo.jpg",
                "Activo",
                "Repartidor",
                -12.0464,
                -77.0428,
                "Disponible"
        );

        db.collection("usuarios").document(repartidor.getId()).set(repartidor);
    }

    private void initializeAdministradorRestaurante() {
        AdministradorRestaurante adminRest = new AdministradorRestaurante(
                "AR001",
                "Luis",
                "Rodríguez",
                "luis@restaurant.com",
                "955444333",
                "Jr. Las Palmeras 321",
                "DNI",
                "78912345",
                "1988-12-10",
                "admin_rest_photo.jpg",
                "Activo",
                "AdministradorRestaurante",
                "REST001"
        );

        db.collection("usuarios").document(adminRest.getId()).set(adminRest);
    }

    private void initializeRestaurante() {
        List<String> categorias = Arrays.asList("Burger", "Fast Food", "Drinks");
        List<String> imagenes = Arrays.asList("restaurant_1.jpg", "restaurant_2.jpg");

        Restaurante restaurante = new Restaurante(
                "Burger House",
                "Av. La Marina 500",
                "01-4445556",
                categorias,
                4.5,
                imagenes,
                "AR001",
                "El mejor lugar para disfrutar de las hamburguesas más deliciosas"
        );

        db.collection("restaurantes").document("REST001").set(restaurante);
    }

    private void initializeProducto() {
        List<String> imagenes = Arrays.asList("burger_1.jpg", "burger_2.jpg");

        Producto producto = new Producto(
                "PROD001",
                "Classic Burger",
                "Hamburguesa clásica con queso, lechuga y tomate",
                15.90,
                imagenes,
                "Producto",
                false
        );

        db.collection("productos").document(producto.getId()).set(producto);
    }

    private void initializeCarrito() {
        List<ProductoCantidad> productos = new ArrayList<>();
        productos.add(new ProductoCantidad("PROD001", 2));

        Carrito carrito = new Carrito(
                "CART001",
                "CL001",
                "REST001",
                productos
        );

        db.collection("carritos").document(carrito.getId()).set(carrito);
    }

    private void initializePedido() {
        List<ProductoCantidad> productos = new ArrayList<>();
        productos.add(new ProductoCantidad("PROD001", 2));

        Pedido pedido = new Pedido(
                "PED001",
                "CL001",
                "REST001",
                productos,
                "RP001",
                "Recibido",
                new Date(),
                "Calle Las Flores 456",
                "QR001",
                "PAG001"
        );

        db.collection("pedidos").document(pedido.getId()).set(pedido);
    }

    private void initializePago() {
        Pago pago = new Pago(
                "PAG001",
                "PED001",
                31.80,
                "Tarjeta",
                "Completado",
                new Date()
        );

        db.collection("pagos").document(pago.getId()).set(pago);
    }


    private void initializeReporte() {
        Map<String, Integer> ventasPorProducto = new HashMap<>();
        ventasPorProducto.put("PROD001", 2);

        Date fechaInicio = new Date();
        Date fechaFin = new Date(fechaInicio.getTime() + 86400000); // Un día después

        Reporte reporte = new Reporte();
        reporte.setId("REP001");
        reporte.setRestauranteId("REST001");
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setVentasPorProducto(ventasPorProducto);
        reporte.setTotalVentas(31.80);

        db.collection("reportes").document(reporte.getId()).set(reporte);
    }
}
