package com.example.foodisea.repository;

import android.content.Context;
import android.util.Log;

import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.dto.PedidoConDetalles;
import com.example.foodisea.dto.PedidoConDistancia;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.VerificacionEntrega;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PedidoRepository {
    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;
    private static final double RADIO_BUSQUEDA_KM = 100.0; // Radio de búsqueda para repartidores

    // Mapa estático para almacenar las distancias
    private static final Map<String, Double> distanciasCalculadas = new HashMap<>();

    public PedidoRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.notificationHelper = new NotificationHelper(context);
    }


    public Task<Void> crearPedidoConVerificacion(Pedido pedido) {
        // Primero creamos el pedido
        return db.collection("pedidos")
                .add(pedido)
                .continueWithTask(task -> {
                    String pedidoId = task.getResult().getId();
                    pedido.setId(pedidoId);

                    // Crear la verificación de entrega
                    VerificacionEntrega verificacion = new VerificacionEntrega(pedidoId);
                    return db.collection("verificaciones_entrega")
                            .add(verificacion);
                })
                .continueWithTask(task -> {
                    String verificacionId = task.getResult().getId();

                    // Crear QR de entrega
                    CodigoQR qrEntrega = new CodigoQR(verificacionId, "ENTREGA");
                    return db.collection("codigosQR")
                            .add(qrEntrega)
                            .continueWithTask(qrTask -> {
                                String qrEntregaId = qrTask.getResult().getId();

                                // Crear QR de pago
                                CodigoQR qrPago = new CodigoQR(verificacionId, "PAGO");
                                return db.collection("codigosQR")
                                        .add(qrPago)
                                        .continueWithTask(qrPagoTask -> {
                                            String qrPagoId = qrPagoTask.getResult().getId();

                                            // Actualizar la verificación con los IDs de QR
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("qrEntregaId", qrEntregaId);
                                            updates.put("qrPagoId", qrPagoId);
                                            return db.collection("verificaciones_entrega")
                                                    .document(verificacionId)
                                                    .update(updates);
                                        });
                            });
                });
    }

    public Task<Void> actualizarEstadoPedido(String pedidoId, String nuevoEstado) {
        return db.collection("pedidos")
                .document(pedidoId)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    DocumentSnapshot document = task.getResult();
                    Pedido pedido = document.toObject(Pedido.class);
                    String estadoAnterior = pedido != null ? pedido.getEstado() : "";

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("estado", nuevoEstado);

                    return db.collection("pedidos")
                            .document(pedidoId)
                            .update(updates)
                            .continueWithTask(updateTask -> {
                                if (updateTask.isSuccessful() && pedido != null) {
                                    pedido.setId(pedidoId);
                                    pedido.setEstado(nuevoEstado);
                                    // Enviar notificación del cambio de estado
                                    notificationHelper.enviarNotificacionCambioEstadoPedido(
                                            pedido,
                                            estadoAnterior
                                    );
                                }
                                return updateTask;
                            });
                });
    }


    private void buscarRepartidoresCercanos(Pedido pedido) {
        if (pedido.getLatitudEntrega() == null || pedido.getLongitudEntrega() == null) {
            return;
        }

        db.collection("usuarios")
                .whereEqualTo("tipoUsuario", "Repartidor")
                .whereEqualTo("estado", "Disponible")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Repartidor repartidor = document.toObject(Repartidor.class);
                        if (repartidor != null) {
                            double distancia = calcularDistancia(
                                    repartidor.getLatitud(),
                                    repartidor.getLongitud(),
                                    pedido.getLatitudEntrega(),
                                    pedido.getLongitudEntrega()
                            );

                            if (distancia <= RADIO_BUSQUEDA_KM) {
                                // Notificar al repartidor sobre el nuevo pedido
                                notificationHelper.enviarNotificacionNuevoPedidoRepartidor(
                                        repartidor.getId(),
                                        pedido
                                );
                            }
                        }
                    }
                });
    }

    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
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
        // Limpiar distancias anteriores
        distanciasCalculadas.clear();

        return db.collection("pedidos")
                .whereEqualTo("estado", "En preparación")
                .whereEqualTo("repartidorId", null)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    List<DocumentSnapshot> pedidosDocs = task.getResult().getDocuments();
                    List<Task<PedidoConDistancia>> pedidosTasks = new ArrayList<>();

                    for (DocumentSnapshot doc : pedidosDocs) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(doc.getId());

                            // Obtener datos del restaurante para calcular distancia total
                            Task<PedidoConDistancia> pedidoTask = db.collection("restaurantes")
                                    .document(pedido.getRestauranteId())
                                    .get()
                                    .continueWith(restauranteTask -> {
                                        if (!restauranteTask.isSuccessful()) {
                                            throw restauranteTask.getException();
                                        }

                                        Restaurante restaurante = restauranteTask.getResult().toObject(Restaurante.class);
                                        if (restaurante == null || restaurante.getLatitud() == null ||
                                                restaurante.getLongitud() == null ||
                                                pedido.getLatitudEntrega() == null ||
                                                pedido.getLongitudEntrega() == null) {
                                            return null;
                                        }

                                        // Calcular distancia repartidor -> restaurante
                                        double distanciaARestaurante = calcularDistancia(
                                                latitudRepartidor,
                                                longitudRepartidor,
                                                restaurante.getLatitud(),
                                                restaurante.getLongitud()
                                        );

                                        // Calcular distancia restaurante -> cliente
                                        double distanciaACliente = calcularDistancia(
                                                restaurante.getLatitud(),
                                                restaurante.getLongitud(),
                                                pedido.getLatitudEntrega(),
                                                pedido.getLongitudEntrega()
                                        );

                                        // Distancia total del recorrido
                                        double distanciaTotal = distanciaARestaurante + distanciaACliente;

                                        return new PedidoConDistancia(pedido, distanciaTotal);
                                    });

                            pedidosTasks.add(pedidoTask);
                        }
                    }

                    return Tasks.whenAllSuccess(pedidosTasks)
                            .continueWith(allTask -> {
                                List<Pedido> pedidosCercanos = new ArrayList<>();
                                List<Object> rawResults = allTask.getResult();

                                // Ordenar los resultados por distancia antes de filtrar
                                List<PedidoConDistancia> pedidosConDistancia = rawResults.stream()
                                        .filter(obj -> obj instanceof PedidoConDistancia)
                                        .map(obj -> (PedidoConDistancia) obj)
                                        .filter(pcd -> pcd != null && pcd.getDistanciaTotal() <= RADIO_BUSQUEDA_KM)
                                        .sorted(Comparator.comparingDouble(PedidoConDistancia::getDistanciaTotal))
                                        .collect(Collectors.toList());

                                // Procesar los pedidos ordenados
                                for (PedidoConDistancia pedidoConDist : pedidosConDistancia) {
                                    distanciasCalculadas.put(
                                            pedidoConDist.getPedido().getId(),
                                            pedidoConDist.getDistanciaTotal()
                                    );
                                    pedidosCercanos.add(pedidoConDist.getPedido());
                                }

                                return pedidosCercanos;
                            });
                });
    }

    public double getDistanciaCalculada(String pedidoId) {
        return distanciasCalculadas.getOrDefault(pedidoId, 0.0);
    }


    // Obtener pedidos activos de un repartidor
    public Task<List<Pedido>> getPedidosActivosRepartidor(String repartidorId) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Arrays.asList("En camino", "Recogiendo pedido"))
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(doc.getId());
                            pedidos.add(pedido);
                        }
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


    public Task<Void> asignarRepartidorAPedido(String pedidoId, String repartidorId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", "Recogiendo pedido");
        updates.put("repartidorId", repartidorId);

        return db.collection("pedidos")
                .document(pedidoId)
                .update(updates)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return db.collection("pedidos")
                                .document(pedidoId)
                                .get()
                                .continueWithTask(getTask -> {
                                    if (getTask.isSuccessful()) {
                                        Pedido pedido = getTask.getResult().toObject(Pedido.class);
                                        if (pedido != null) {
                                            pedido.setId(pedidoId);
                                            // Notificar al cliente sobre la asignación del repartidor
                                            notificationHelper.enviarNotificacionAsignacionRepartidor(
                                                    pedido,
                                                    repartidorId
                                            );
                                        }
                                    }
                                    return task;
                                });
                    }
                    return task;
                });
    }

    public Task<Void> actualizarDisposicionRepartidor(String repartidorId, String disposicion) {
        return db.collection("usuarios")
                .document(repartidorId)
                .update("disposicion", disposicion);
    }

    // Obtener pedidos por cliente
    public Task<List<Pedido>> getPedidosPorCliente(String clienteId) {
        return db.collection("pedidos")
                .whereEqualTo("clienteId", clienteId)
                .get()
                .continueWith(task -> {
                    List<Pedido> pedidos = new ArrayList<>();
                    for (DocumentSnapshot doc : task.getResult()) {
                        Pedido pedido = doc.toObject(Pedido.class);
                        if (pedido != null) {
                            pedido.setId(doc.getId());
                            pedidos.add(pedido);
                        }
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

    // Método para obtener pedidos asignados al repartidor
    public Task<List<Pedido>> getPedidosAsignadosRepartidor(String repartidorId) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Arrays.asList("En camino", "Entregado"))
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
                    }
                    return pedidos;
                });
    }

    // Método para obtener pedidos asignados al repartidor
    public Task<List<Pedido>> getPedidosEntregadosRepartidor(String repartidorId) {
        return db.collection("pedidos")
                .whereEqualTo("repartidorId", repartidorId)
                .whereIn("estado", Collections.singletonList("Entregado"))
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
                    }
                    return pedidos;
                });
    }

}
