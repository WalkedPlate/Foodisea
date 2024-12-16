package com.example.foodisea.manager;

import com.example.foodisea.model.AppLog;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LogManager {
    private final FirebaseFirestore db;

    public LogManager() {
        db = FirebaseFirestore.getInstance();
    }

    // Método para crear log asíncrono que devuelve un Task
    public Task<DocumentReference> createLog(String userId, String actionType, String details) {
        AppLog log = new AppLog();
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setDetails(details);
        log.setTimestamp(System.currentTimeMillis());
        //log.setUserRole(getCurrentUserRole());

        // Devuelve directamente el Task de Firestore
        return db.collection("logs").add(log);
    }

    // Método para recuperar logs que devuelve un Task
    public Task<List<AppLog>> fetchLogs(Instant startDate, Instant endDate) {
        TaskCompletionSource<List<AppLog>> taskCompletionSource = new TaskCompletionSource<>();

        Query query = db.collection("logs")
                .whereGreaterThanOrEqualTo("timestamp", startDate.toEpochMilli())
                .whereLessThanOrEqualTo("timestamp", endDate.toEpochMilli())
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<AppLog> logs = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    AppLog log = document.toObject(AppLog.class);
                    logs.add(log);
                }
                taskCompletionSource.setResult(logs);
            } else {
                taskCompletionSource.setException(task.getException());
            }
        });

        return taskCompletionSource.getTask();
    }

    // Método para eliminar logs antiguos
    public Task<Void> deleteOldLogs(Instant olderThan) {
        Query oldLogsQuery = db.collection("logs")
                .whereLessThan("timestamp", olderThan.toEpochMilli());

        return oldLogsQuery.get().continueWithTask(task -> {
            if (task.isSuccessful()) {
                WriteBatch batch = db.batch();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    batch.delete(document.getReference());
                }
                return batch.commit();
            } else {
                return Tasks.forException(task.getException());
            }
        });
    }
}