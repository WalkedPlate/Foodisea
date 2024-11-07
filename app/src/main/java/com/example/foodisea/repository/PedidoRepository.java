package com.example.foodisea.repository;

import android.content.Context;

import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.notification.NotificationHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PedidoRepository {
    private final FirebaseFirestore db;
    private final NotificationHelper notificationHelper;

    public PedidoRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.notificationHelper = new NotificationHelper(context);
    }

    public Task<Void> crearPedido(Pedido pedido) {
        return db.collection("pedidos")
                .document()
                .set(pedido)
                .addOnSuccessListener(aVoid -> {
                    notificationHelper.sendNewOrderNotification(pedido);
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


}
