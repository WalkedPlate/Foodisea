package com.example.foodisea.service;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryLocationTracker {
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 segundos
    private static final int FASTEST_UPDATE_INTERVAL = 5000;   // 5 segundos
    private static final String DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationCallback locationCallback;
    private final FirebaseFirestore db;
    private String currentPedidoId;
    private String repartidorId;
    private GoogleMap mMap;
    private PolylineOptions currentRoute;
    private List<LatLng> routePoints;

    public DeliveryLocationTracker(Context context, GoogleMap map) {
        this.context = context;
        this.mMap = map;
        this.db = FirebaseFirestore.getInstance();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.routePoints = new ArrayList<>();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();

                //updateRepartidorLocation(location);
                //updateRouteProgress(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        };
    }

    /*

    public void startTracking(String pedidoId, String repartidorId) {
        this.currentPedidoId = pedidoId;
        this.repartidorId = repartidorId;

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL);

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("LocationTracker", "Error requesting location updates", e);
        }
    }

    private void updateRepartidorLocation(Location location) {
        if (repartidorId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("latitud", location.getLatitude());
        updates.put("longitud", location.getLongitude());

        db.collection("usuarios")
                .document(repartidorId)
                .update(updates)
                .addOnFailureListener(e ->
                        Log.e("LocationTracker", "Error updating repartidor location", e));
    }

    private void updateRouteProgress(LatLng currentLocation) {
        if (routePoints.isEmpty()) return;

        // Encontrar el punto más cercano en la ruta
        int nearestPointIndex = findNearestPointInRoute(currentLocation);

        // Actualizar la ruta dibujada
        List<LatLng> remainingPoints = routePoints.subList(nearestPointIndex, routePoints.size());

        PolylineOptions updatedRoute = new PolylineOptions()
                .addAll(remainingPoints)
                .width(10)
                .color(ContextCompat.getColor(context, R.color.route_active));

        mMap.clear();
        mMap.addPolyline(updatedRoute);
    }

    private int findNearestPointInRoute(LatLng currentLocation) {
        int nearestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < routePoints.size(); i++) {
            LatLng point = routePoints.get(i);
            double distance = SphericalUtil.computeDistanceBetween(currentLocation, point);

            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    public void stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

     */
}
