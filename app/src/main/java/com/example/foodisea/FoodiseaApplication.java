package com.example.foodisea;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;


import androidx.multidex.MultiDex;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.FirebaseApp;

public class FoodiseaApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Inicializar Firebase si aún no está inicializado
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }

        // Verificar y configurar Google Play Services
        int resultCode = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            // Google Play Services está disponible
            configureGoogleServices();
        }
    }

    private void configureGoogleServices() {
        try {
            // Configurar los servicios de Google
            com.google.android.gms.security.ProviderInstaller.installIfNeeded(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

