package com.example.foodisea.repository;

import android.content.Context;

import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.notification.NotificationHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PedidoRepository {
    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;
    private static final double RADIO_BUSQUEDA_KM = 5.0; // Radio de búsqueda para repartidores

    public PedidoRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.notificationHelper = new NotificationHelper(context);
    }

    public Task<DocumentReference> crearPedido(Pedido pedido) {
        // Ahora retornamos el DocumentReference para poder obtener el ID del pedido creado
        return db.collection("pedidos")
                .add(pedido)
                .addOnSuccessListener(documentReference -> {
                    pedido.setId(documentReference.getId());
                    // Buscar repartidores cercanos
                    buscarRepartidoresCercanos(pedido);
                    // Enviar notificación al restaurante
                    notificationHelper.sendNewOrderNotification(pedido);
                });
    }

    private void buscarRepartidoresCercanos(Pedido pedido) {
        // Solo buscar repartidores si tenemos las coordenadas de entrega
        if (pedido.getLatitudEntrega() == null || pedido.getLongitudEntrega() == null) {
            return;
        }

        // Consulta para encontrar repartidores disponibles
        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Repartidor")
                .whereEqualTo("estado", "Disponible")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Repartidor> repartidoresCercanos = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Repartidor repartidor = document.toObject(Repartidor.class);
                        if (repartidor != null) {
                            // Calcular distancia entre repartidor y punto de entrega
                            double distancia = calcularDistancia(
                                    repartidor.getLatitud(),
                                    repartidor.getLongitud(),
                                    pedido.getLatitudEntrega(),
                                    pedido.getLongitudEntrega()
                            );

                            if (distancia <= RADIO_BUSQUEDA_KM) {
                                repartidoresCercanos.add(repartidor);
                            }
                        }
                    }

                    // Notificar a repartidores cercanos

                    /*
                    for (Repartidor repartidor : repartidoresCercanos) {
                        notificationHelper.sendNewOrderToDeliveryNotification(
                                repartidor.getId(), pedido);
                    }

                     */
                });
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        // Radio de la Tierra en kilómetros
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distancia en kilómetros
    }

    // Obtener pedidos con ubicación cercana para repartidor
    public Task<List<Pedido>> getPedidosCercanosParaRepartidor(double latitudRepartidor,
                                                               double longitudRepartidor) {
        return db.collection("pedidos")
                .whereEqualTo("estado", "Recibido")
                .whereEqualTo("repartidorId", null)
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidosCercanos = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Pedido pedido = doc.toObject(Pedido.class);
                            if (pedido != null && pedido.getLatitudEntrega() != null
                                    && pedido.getLongitudEntrega() != null) {
                                // Calcular distancia
                                double distancia = calcularDistancia(
                                        latitudRepartidor,
                                        longitudRepartidor,
                                        pedido.getLatitudEntrega(),
                                        pedido.getLongitudEntrega()
                                );

                                if (distancia <= RADIO_BUSQUEDA_KM) {
                                    pedido.setId(doc.getId());
                                    pedidosCercanos.add(pedido);
                                }
                            }
                        }
                    }
                    return pedidosCercanos;
                });
    }

    // Método para obtener la ruta óptima para un repartidor
    public Task<List<Pedido>> getRutaOptima(String repartidorId, LatLng ubicacionActual) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Arrays.asList("En camino", "Recibido"))
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            Pedido pedido = doc.toObject(Pedido.class);
                            if (pedido != null) {
                                pedido.setId(doc.getId());
                                pedidos.add(pedido);
                            }
                        }
                        // Ordenar pedidos por distancia desde la ubicación actual
                        Collections.sort(pedidos, (p1, p2) -> {
                            double d1 = calcularDistancia(
                                    ubicacionActual.latitude,
                                    ubicacionActual.longitude,
                                    p1.getLatitudEntrega(),
                                    p1.getLongitudEntrega()
                            );
                            double d2 = calcularDistancia(
                                    ubicacionActual.latitude,
                                    ubicacionActual.longitude,
                                    p2.getLatitudEntrega(),
                                    p2.getLongitudEntrega()
                            );
                            return Double.compare(d1, d2);
                        });
                    }
                    return pedidos;
                });
    }





    // Actualizar estado del pedido
    public Task<Void> actualizarEstadoPedido(String pedidoId, String nuevoEstado) {
        return db.collection("pedidos")
                .document(pedidoId)
                .update("estado", nuevoEstado);
    }

    // Obtener pedidos activos de un repartidor
    public Task<List<Pedido>> getPedidosActivosRepartidor(String repartidorId) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Arrays.asList("En camino", "Recibido"))
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        pedidos.add(doc.toObject(Pedido.class));
                    }
                    return pedidos;
                });
    }

    // Obtener pedidos activos de un restaurante
    public Task<List<PedidoConCliente>> getPedidosActivosRestaurante(String restauranteId) {
        return db.collection("pedidos")
                .whereEqualTo("restauranteId", restauranteId)
                .whereIn("estado", Arrays.asList("En camino", "Recibido"))
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        pedido.setId(doc.getId());
                        pedidos.add(pedido);
                    }

                    // Crear una lista de Tasks para obtener los clientes
                    List<Task<DocumentSnapshot>> clienteTasks = new ArrayList<>();

                    // Obtener todos los documentos de clientes
                    for (Pedido pedido : pedidos) {
                        Task<DocumentSnapshot> clienteTask = db.collection("usuarios")
                                .document(pedido.getClienteId())
                                .get();
                        clienteTasks.add(clienteTask);
                    }

                    // Esperar a que todas las consultas de clientes se completen
                    return Tasks.whenAllComplete(clienteTasks)
                            .continueWith(allClientsTask -> {
                                List<PedidoConCliente> resultado = new ArrayList<>();

                                for (int i = 0; i < pedidos.size(); i++) {
                                    Task<DocumentSnapshot> clienteTask = clienteTasks.get(i);
                                    if (clienteTask.isSuccessful()) {
                                        DocumentSnapshot clienteDoc = clienteTask.getResult();
                                        Cliente cliente = clienteDoc.toObject(Cliente.class);
                                        resultado.add(new PedidoConCliente(pedidos.get(i), cliente));
                                    }
                                }

                                return resultado;
                            });
                });
    }

    // Obtener pedidos por cliente
    public Task<List<Pedido>> getPedidosPorCliente(String clienteId) {
        return db.collection("pedidos")
                .whereEqualTo("clienteId", clienteId)
                .orderBy("fechaPedido", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        pedidos.add(doc.toObject(Pedido.class));
                    }
                    return pedidos;
                });
    }




    /**
     * Configura un listener para escuchar cambios en un pedido específico
     * @param pedidoId ID del pedido a escuchar
     * @param listener Listener para manejar los cambios
     * @return ListenerRegistration que puede usarse para detener la escucha
     */
    public ListenerRegistration listenToPedido(String pedidoId, EventListener<DocumentSnapshot> listener) {
        return db.collection("pedidos")
                .document(pedidoId)
                .addSnapshotListener(listener);
    }

    /**
     * Configura un listener para escuchar cambios en la ubicación de un repartidor
     * @param repartidorId ID del repartidor
     * @param listener Listener para manejar los cambios
     * @return ListenerRegistration que puede usarse para detener la escucha
     */
    public ListenerRegistration listenToRepartidor(String repartidorId, EventListener<DocumentSnapshot> listener) {
        return db.collection("usuarios")
                .document(repartidorId)
                .addSnapshotListener(listener);
    }

    /**
     * Obtiene los detalles completos de un pedido
     * @param pedidoId ID del pedido
     * @return Task con el pedido y su información relacionada
     */
    public Task<PedidoConDetalles> getPedidoConDetalles(String pedidoId) {
        return db.collection("pedidos")
                .document(pedidoId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot pedidoDoc = task.getResult();
                    Pedido pedido = pedidoDoc.toObject(Pedido.class);
                    if (pedido == null) {
                        throw new Exception("Pedido no encontrado");
                    }
                    pedido.setId(pedidoDoc.getId());

                    // Obtener datos del restaurante y repartidor en paralelo
                    Task<DocumentSnapshot> restauranteTask = db.collection("restaurantes")
                            .document(pedido.getRestauranteId())
                            .get();

                    Task<DocumentSnapshot> repartidorTask;
                    if (pedido.getRepartidorId() != null) {
                        repartidorTask = db.collection("usuarios")
                                .document(pedido.getRepartidorId())
                                .get();
                    } else {
                        repartidorTask = null;
                    }

                    List<Task<?>> tasks = new ArrayList<>();
                    tasks.add(restauranteTask);
                    if (repartidorTask != null) tasks.add(repartidorTask);

                    return Tasks.whenAllComplete(tasks)
                            .continueWith(allTask -> {
                                Restaurante restaurante = restauranteTask.getResult()
                                        .toObject(Restaurante.class);

                                Repartidor repartidor = null;
                                if (repartidorTask != null && repartidorTask.isSuccessful()) {
                                    repartidor = repartidorTask.getResult()
                                            .toObject(Repartidor.class);
                                }

                                return new PedidoConDetalles(pedido, restaurante, repartidor);
                            });
                });
    }

    // Obtener un pedido específico
    public Task<Pedido> getPedidoById(String pedidoId) {
        return db.collection("pedidos")
                .document(pedidoId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Pedido pedido = task.getResult().toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(task.getResult().getId());
                        }
                        return pedido;
                    }
                    throw task.getException();
                });
    }


}
