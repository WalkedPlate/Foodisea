package com.example.foodisea.notificaciones;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TokenManager.java - Clase auxiliar para manejar tokens
public class TokenManager {
    private static final String PREFS_NAME = "FCMPrefs";
    private static final String TOKEN_KEY = "fcm_token";
    private Context context;
    private FirebaseFirestore db;

    public TokenManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void obtenerYGuardarToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        guardarToken(token);
                    }
                });
    }

    public void guardarToken(String token) {
        // Guardar localmente
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(TOKEN_KEY, token).apply();

        // Guardar en Firestore
        String deviceId = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("timestamp", FieldValue.serverTimestamp());
        tokenData.put("platform", "android");
        tokenData.put("deviceId", deviceId);

        db.collection("fcm_tokens")
                .document(deviceId)
                .set(tokenData)
                .addOnSuccessListener(aVoid -> Log.d("FCM", "Token guardado en Firestore"))
                .addOnFailureListener(e -> Log.e("FCM", "Error guardando token", e));
    }

    public String getStoredToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }
}
