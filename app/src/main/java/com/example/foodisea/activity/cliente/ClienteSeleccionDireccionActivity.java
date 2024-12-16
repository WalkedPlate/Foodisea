package com.example.foodisea.activity.cliente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityClienteSeleccionDireccionBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ClienteSeleccionDireccionActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private ActivityClienteSeleccionDireccionBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LoadingDialog loadingDialog;
    private Geocoder geocoder;
    private LatLng selectedLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteSeleccionDireccionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializaciones
        loadingDialog = new LoadingDialog(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupWindowInsets();
        setupViews();
    }

    private void setupWindowInsets() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupViews() {
        // Configurar SearchView
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                buscarDireccion(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Botón confirmar
        binding.btnConfirmar.setOnClickListener(v -> {
            if (selectedLocation == null) {
                Toast.makeText(this, "Por favor selecciona una ubicación",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            confirmarDireccion();
        });

        // Botón volver
        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this::onMapClicked);

        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Verificar permisos y mostrar ubicación actual
        if (checkLocationPermission()) {
            mostrarUbicacionActual();
        } else {
            requestLocationPermission();
        }
    }

    private void onMapClicked(LatLng latLng) {
        selectedLocation = latLng;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        obtenerDireccionDesdeLatLng(latLng);
    }

    private void buscarDireccion(String query) {
        loadingDialog.show("Buscando dirección...");

        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                selectedLocation = location;
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(location));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

                binding.tvDireccionSeleccionada.setText(address.getAddressLine(0));
            } else {
                Toast.makeText(this, "Dirección no encontrada",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al buscar dirección",
                    Toast.LENGTH_SHORT).show();
        }

        loadingDialog.dismiss();
    }

    private void obtenerDireccionDesdeLatLng(LatLng latLng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                String direccion = addresses.get(0).getAddressLine(0);
                binding.tvDireccionSeleccionada.setText(direccion);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error al obtener dirección",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarDireccion() {
        String direccion = binding.tvDireccionSeleccionada.getText().toString();
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una dirección válida",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("direccion", direccion);
        resultIntent.putExtra("latitud", selectedLocation.latitude);
        resultIntent.putExtra("longitud", selectedLocation.longitude);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void mostrarUbicacionActual() {
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    currentLocation, 15));
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mostrarUbicacionActual();
            }
        }
    }
}