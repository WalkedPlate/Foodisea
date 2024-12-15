package com.example.foodisea.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.foodisea.MainActivity;
import com.example.foodisea.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RepartidorLocationService extends Service {
    private static final String TAG = "RepartidorLocationService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isTracking = false;
    private String repartidorId;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        createNotificationChannel();
        setupLocationCallback();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        repartidorId = intent.getStringExtra("repartidor_id");
        if (repartidorId == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        startLocationTracking();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Servicio de ubicación",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Servicio de ubicación para repartidores");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Servicio de ubicación activo")
                .setContentText("Compartiendo tu ubicación")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocationInFirestore(location);
                }
            }
        };
    }

    private void startLocationTracking() {
        if (isTracking) return;

        LocationRequest locationRequest = new LocationRequest.Builder(10000) // 10 segundos
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateIntervalMillis(5000) // 5 segundos mínimo
                .build();

        try {
            startForeground(NOTIFICATION_ID, createNotification());

            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                );
                isTracking = true;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error starting location updates", e);
            stopSelf();
        }
    }

    private void updateLocationInFirestore(Location location) {
        if (repartidorId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("latitud", location.getLatitude());
        updates.put("longitud", location.getLongitude());
        updates.put("ultimaActualizacion", new Date());

        db.collection("usuarios")
                .document(repartidorId)
                .update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Error updating location", e));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        isTracking = false;
    }
}