package com.example.foodisea.activity.repartidor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityRepartidorDeliveryMapBinding;
import com.example.foodisea.helper.DirectionsHelper;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.service.RepartidorLocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RepartidorDeliveryMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityRepartidorDeliveryMapBinding binding;
    private static final String TAG = "RepartidorDeliveryMap";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Repositorios
    private PedidoRepository pedidoRepository;
    FirebaseFirestore db;

    //Sesión
    private SessionManager sessionManager;
    private Repartidor repartidorActual;

    // Map related
    private GoogleMap mMap;
    private DirectionsHelper directionsHelper;
    private Marker restauranteMarker;
    private Marker clienteMarker;
    private LatLng restauranteLocation;
    private LatLng clienteLocation;
    private Marker repartidorMarker;
    private ListenerRegistration repartidorLocationListener;

    // Bottom Sheet
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Data
    private String pedidoId;
    private String chatId;
    private Pedido currentPedido;
    private Restaurante restaurante;
    private ListenerRegistration pedidoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepartidorDeliveryMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        repartidorActual = sessionManager.getRepartidorActual();

        // Inicializar repositorios y helpers
        pedidoRepository = new PedidoRepository(this);
        directionsHelper = new DirectionsHelper(this);
        db = FirebaseFirestore.getInstance();

        // Obtener pedidoId del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Obtener chatId del intent
        chatId = getIntent().getStringExtra("chatId");
        if (chatId == null) {
            Toast.makeText(this, "Error: No se encontró el chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        initializeMap();
        setupClickListeners();
        setupPedidoListener();
        setupRepartidorLocationListener();
    }

    private void initializeViews() {
        // Configurar Bottom Sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(200);
    }

    private void setupClickListeners() {
        // Botón de cierre
        binding.btnClose.setOnClickListener(v -> finish());

        // Botón de chat
        binding.chatButton.setOnClickListener(v -> {
            if (currentPedido != null) {
                Intent intent = new Intent(this, RepartidorChatActivity.class);
                intent.putExtra("clienteId", currentPedido.getClienteId());
                intent.putExtra("pedidoId", pedidoId);
                intent.putExtra("chatId",chatId);
                startActivity(intent);
            }
        });

        // Botón de acción principal
        binding.actionButton.setOnClickListener(v -> handleActionButtonClick());
    }

    private void setupPedidoListener() {
        pedidoListener = pedidoRepository.listenToPedido(pedidoId, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Pedido updatedPedido = snapshot.toObject(Pedido.class);
                if (updatedPedido != null) {
                    updatedPedido.setId(snapshot.getId());

                    boolean estadoCambio = currentPedido == null ||
                            !currentPedido.getEstado().equals(updatedPedido.getEstado());

                    currentPedido = updatedPedido;
                    updateUI();

                    if (estadoCambio) {
                        updateMapRoute();
                    }
                }
            }
        });

        // Cargar datos iniciales del pedido
        loadPedidoData();
    }

    private void loadPedidoData() {
        pedidoRepository.getPedidoConDetalles(pedidoId)
                .addOnSuccessListener(detalles -> {
                    currentPedido = detalles.getPedido();
                    restaurante = detalles.getRestaurante();

                    // Cargar información del cliente
                    if (currentPedido != null) {
                        db.collection("usuarios")
                                .document(currentPedido.getClienteId())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    Cliente cliente = documentSnapshot.toObject(Cliente.class);
                                    if (cliente != null) {
                                        // Actualizar UI del cliente
                                        binding.customerName.setText(cliente.getNombres() + " " + cliente.getApellidos());

                                        // Cargar foto del cliente si existe
                                        if (!TextUtils.isEmpty(cliente.getFoto())) {
                                            Glide.with(this)
                                                    .load(cliente.getFoto())
                                                    .placeholder(R.drawable.ic_profile)
                                                    .error(R.drawable.ic_profile)
                                                    .into(binding.customerAvatar);
                                        }
                                    }
                                });
                    }

                    // Actualizar locations
                    if (restaurante != null) {
                        restauranteLocation = new LatLng(
                                restaurante.getLatitud(),
                                restaurante.getLongitud()
                        );
                    }

                    if (currentPedido != null) {
                        clienteLocation = new LatLng(
                                currentPedido.getLatitudEntrega(),
                                currentPedido.getLongitudEntrega()
                        );
                    }

                    updateUI();
                    if (mMap != null) {
                        updateMapRoute();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading pedido data", e);
                    Toast.makeText(this, "Error al cargar los datos del pedido", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateUI() {
        if (currentPedido == null || restaurante == null) return;

        // Actualizar estado en el título y badge
        String estadoText = getEstadoDisplayText(currentPedido.getEstado());
        binding.estadoOrden.setText(estadoText);
        binding.pedidoEstadoBadge.setText(estadoText);

        // Actualizar información del restaurante
        binding.restaurantName.setText(restaurante.getNombre());
        binding.restaurantAddress.setText(restaurante.getDireccion());

        // Actualizar información del pedido
        String pedidoId = currentPedido.getId();
        String displayId = pedidoId.length() >= 5
                ? pedidoId.substring(0, 5).toUpperCase()
                : pedidoId.toUpperCase();
        binding.orderNumber.setText(String.format("Orden #%s", displayId));
        binding.orderTime.setText("Pedido realizado: " + formatDate(currentPedido.getFechaPedido()));
        binding.deliveryAddress.setText(currentPedido.getDireccionEntrega());

        // Actualizar botón según estado
        updateActionButton(currentPedido.getEstado());
    }

    private void updateActionButton(String estado) {
        switch (estado) {
            case Pedido.ESTADO_RECOGIENDO:
                binding.actionButton.setText("RECOGÍ EL PEDIDO");
                binding.actionButton.setEnabled(true);
                break;
            case Pedido.ESTADO_EN_CAMINO:
                binding.actionButton.setText("CONFIRMAR ENTREGA DEL PEDIDO");
                binding.actionButton.setEnabled(true);
                break;
            default:
                binding.actionButton.setEnabled(false);
                break;
        }
    }

    private void handleActionButtonClick() {
        if (currentPedido == null) return;

        switch (currentPedido.getEstado()) {
            case Pedido.ESTADO_RECOGIENDO:
                startLocationService();
                setupRepartidorLocationListener(); // Añadir esto

                pedidoRepository.actualizarEstadoPedido(pedidoId, Pedido.ESTADO_EN_CAMINO)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Pedido en camino", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
                        });
                break;

            case Pedido.ESTADO_EN_CAMINO:
                stopLocationService();
                if (repartidorLocationListener != null) {
                    repartidorLocationListener.remove();
                }

                Intent intent = new Intent(this, RepartidorComprobanteQrActivity.class);
                intent.putExtra("pedidoId", pedidoId);
                startActivity(intent);
                break;
        }
    }

    private void setupRepartidorLocationListener() {
        if (repartidorLocationListener != null) {
            repartidorLocationListener.remove();
        }

        if (repartidorActual != null) {
            repartidorLocationListener = db.collection("usuarios")
                    .document(repartidorActual.getId())
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Repartidor repartidorUpdated = snapshot.toObject(Repartidor.class);
                            if (repartidorUpdated != null &&
                                    repartidorUpdated.getLatitud() != null &&
                                    repartidorUpdated.getLongitud() != null) {

                                updateRepartidorLocation(
                                        new LatLng(repartidorUpdated.getLatitud(), repartidorUpdated.getLongitud())
                                );
                            } else {
                                Log.e(TAG, "Datos de ubicación del repartidor incompletos");
                            }
                        }
                    });
        }
    }

    private void updateRepartidorLocation(LatLng newLocation) {
        if (mMap == null) return;

        // Validar la nueva ubicación
        if (newLocation == null) {
            Log.e(TAG, "Nueva ubicación del repartidor es null");
            return;
        }

        // Limpiar el mapa
        mMap.clear();

        // Validar y dibujar marcadores fijos
        if (restauranteLocation != null) {
            restauranteMarker = mMap.addMarker(new MarkerOptions()
                    .position(restauranteLocation)
                    .title(restaurante != null ? restaurante.getNombre() : "Restaurante")
                    .icon(getBitmapDescriptor(R.drawable.ic_restaurant)));
        }

        if (clienteLocation != null) {
            clienteMarker = mMap.addMarker(new MarkerOptions()
                    .position(clienteLocation)
                    .title("Punto de entrega")
                    .icon(getBitmapDescriptor(R.drawable.ic_location)));
        }

        // Actualizar marcador del repartidor
        repartidorMarker = mMap.addMarker(new MarkerOptions()
                .position(newLocation)
                .title("Mi ubicación")
                .icon(getBitmapDescriptor(R.drawable.ic_delivery_bike)));

        // Dibujar ruta solo si tenemos todas las ubicaciones necesarias
        if (currentPedido != null) {
            switch (currentPedido.getEstado()) {
                case Pedido.ESTADO_RECOGIENDO:
                    if (restauranteLocation != null) {
                        drawRouteToRestaurant(newLocation);
                    }
                    break;
                case Pedido.ESTADO_EN_CAMINO:
                    if (clienteLocation != null) {
                        drawRouteToClient(newLocation);
                    }
                    break;
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (checkLocationPermission()) {
            setupMap();
        } else {
            requestLocationPermission();
        }
    }

    private void setupMap() {
        if (mMap == null) return;

        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if (restauranteLocation != null && clienteLocation != null) {
                updateMapRoute();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error setting up map", e);
        }
    }

    private void updateMapRoute() {
        if (mMap == null || !checkLocationPermission()) return;

        // Limpiar TODO el mapa
        mMap.clear();

        // Validar ubicaciones necesarias
        if (restauranteLocation == null) {
            Log.e(TAG, "Ubicación del restaurante es null");
            return;
        }

        if (clienteLocation == null) {
            Log.e(TAG, "Ubicación del cliente es null");
            return;
        }

        // Añadir marcadores
        restauranteMarker = mMap.addMarker(new MarkerOptions()
                .position(restauranteLocation)
                .title(restaurante != null ? restaurante.getNombre() : "Restaurante")
                .icon(getBitmapDescriptor(R.drawable.ic_restaurant)));

        clienteMarker = mMap.addMarker(new MarkerOptions()
                .position(clienteLocation)
                .title("Punto de entrega")
                .icon(getBitmapDescriptor(R.drawable.ic_location)));

        // Solo usar la ubicación del repartidor desde Firestore
        if (repartidorActual != null) {
            LatLng repartidorLocation = new LatLng(repartidorActual.getLatitud(), repartidorActual.getLongitud());

            // Dibujar ruta según el estado
            if (currentPedido != null) {
                switch (currentPedido.getEstado()) {
                    case Pedido.ESTADO_RECOGIENDO:
                        drawRouteToRestaurant(repartidorLocation);
                        break;
                    case Pedido.ESTADO_EN_CAMINO:
                        drawRouteToClient(repartidorLocation);
                        break;
                }
            }
        }
    }

    private void drawRouteToRestaurant(LatLng currentLocation) {
        if (currentLocation == null || restauranteLocation == null) {
            Log.e(TAG, "No se puede dibujar la ruta: ubicación actual o restaurante es null");
            return;
        }

        directionsHelper.getRoute(currentLocation, restauranteLocation, new DirectionsHelper.DirectionsCallback() {
            @Override
            public void onDirectionsSuccess(List<LatLng> points, String estimatedTime) {
                if (!points.isEmpty()) {
                    drawPolyline(points, R.color.primary);
                    binding.estimatedTime.setText("Llegando al restaurante en " + estimatedTime);
                    adjustMapCamera(points);
                }
            }

            @Override
            public void onDirectionsFailure(String error) {
                Log.e(TAG, "Error getting directions: " + error);
                Toast.makeText(RepartidorDeliveryMapActivity.this,
                        "No se pudo obtener la ruta al restaurante", Toast.LENGTH_SHORT).show();
                adjustMapCameraToMarkers();
            }
        });
    }

    private void drawRouteToClient(LatLng currentLocation) {
        if (currentLocation == null || clienteLocation == null) {
            Log.e(TAG, "No se puede dibujar la ruta: ubicación actual o cliente es null");
            return;
        }

        directionsHelper.getRoute(currentLocation, clienteLocation, new DirectionsHelper.DirectionsCallback() {
            @Override
            public void onDirectionsSuccess(List<LatLng> points, String estimatedTime) {
                if (!points.isEmpty()) {
                    drawPolyline(points, R.color.primary_light);
                    binding.estimatedTime.setText("Llegando al cliente en " + estimatedTime);
                    adjustMapCamera(points);
                }
            }

            @Override
            public void onDirectionsFailure(String error) {
                Log.e(TAG, "Error getting directions: " + error);
                Toast.makeText(RepartidorDeliveryMapActivity.this,
                        "No se pudo obtener la ruta al cliente", Toast.LENGTH_SHORT).show();
                adjustMapCameraToMarkers();
            }
        });
    }

    private void adjustMapCameraToMarkers() {
        if (mMap == null) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            if (restauranteLocation != null) {
                builder.include(restauranteLocation);
                hasPoints = true;
            }
            if (clienteLocation != null) {
                builder.include(clienteLocation);
                hasPoints = true;
            }

            if (hasPoints) {
                int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adjusting camera to markers", e);
        }
    }

    private void drawPolyline(List<LatLng> points, int colorRes) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(ContextCompat.getColor(this, colorRes));
        mMap.addPolyline(polylineOptions);
    }

    private void adjustMapCamera(List<LatLng> points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }

        int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    private BitmapDescriptor getBitmapDescriptor(int vectorResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResourceId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String getEstadoDisplayText(String estado) {
        switch (estado) {
            case Pedido.ESTADO_RECOGIENDO:
                return "Recogiendo pedido";
            case Pedido.ESTADO_EN_CAMINO:
                return "En camino al cliente";
            case Pedido.ESTADO_ENTREGADO:
                return "Pedido entregado";
            default:
                return estado;
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());
        return sdf.format(date);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Se requieren permisos de ubicación para mostrar la ruta",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // Actualización constante de posición
    private void startLocationService() {
        // Detener cualquier instancia previa del servicio
        stopLocationService();

        Intent serviceIntent = new Intent(this, RepartidorLocationService.class);
        serviceIntent.putExtra("repartidor_id", repartidorActual.getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopLocationService() {
        stopService(new Intent(this, RepartidorLocationService.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pedidoListener != null) {
            pedidoListener.remove();
        }
        if (repartidorLocationListener != null) {
            repartidorLocationListener.remove();
        }
        binding = null;
    }
}