package com.example.foodisea.helper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class DirectionsHelper {
    private static final String BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private final Context context;
    private final RequestQueue requestQueue;
    private String apiKey;

    public interface DirectionsCallback {
        void onDirectionsSuccess(List<LatLng> points, String estimatedTime);
        void onDirectionsFailure(String error);
    }

    public DirectionsHelper(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);

        // Obtener API key del Manifest
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            apiKey = bundle.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("DirectionsHelper", "Failed to load API key from manifest", e);
        }
    }

    public void getRoute(LatLng origin, LatLng destination, DirectionsCallback callback) {
        String url = Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter("origin", origin.latitude + "," + origin.longitude)
                .appendQueryParameter("destination", destination.latitude + "," + destination.longitude)
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", apiKey)
                .build().toString();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Procesar la respuesta
                        JSONArray routes = response.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);

                            // Obtener el tiempo estimado
                            String duration = route.getJSONArray("legs")
                                    .getJSONObject(0)
                                    .getJSONObject("duration")
                                    .getString("text");

                            // Decodificar la polyline
                            String encodedPolyline = route.getJSONObject("overview_polyline")
                                    .getString("points");
                            List<LatLng> points = decodePoly(encodedPolyline);

                            callback.onDirectionsSuccess(points, duration);
                        } else {
                            callback.onDirectionsFailure("No se encontró una ruta");
                        }
                    } catch (JSONException e) {
                        callback.onDirectionsFailure("Error procesando la respuesta: " + e.getMessage());
                    }
                },
                error -> callback.onDirectionsFailure("Error de red: " + error.getMessage()));

        requestQueue.add(request);
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng(lat / 1E5, lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}