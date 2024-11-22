package com.example.foodisea.activity.cliente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.RestauranteRepository;
import com.example.foodisea.repository.UsuarioRepository;
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
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ClienteTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ClienteTrackingActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Repositories
    private PedidoRepository pedidoRepository;
    private RestauranteRepository restauranteRepository;
    private UsuarioRepository usuarioRepository;

    // Firebase Listeners
    private ListenerRegistration pedidoListener;
    private ListenerRegistration repartidorListener;

    // UI Elements
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LinearLayout deliveryInfoContainer;
    private Button btnOrderDetails;
    private GoogleMap mMap;
    private TextView orderStatus1, orderStatus2, orderStatus3, orderStatus4;
    private Button btnChat;
    private TextView tvRestaurant, tvOrderTime;
    private ImageView restaurantLogo;
    private TextView deliveryPersonName;

    // Data
    private String pedidoId;
    private Pedido currentPedido;
    private Restaurante restaurante;
    private Repartidor repartidor;
    private Marker repartidorMarker;
    private LatLng restauranteLocation;
    private LatLng clienteLocation;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_tracking);

        // Inicializar repositories y utilidades
        pedidoRepository = new PedidoRepository(this);
        restauranteRepository = new RestauranteRepository();
        usuarioRepository = new UsuarioRepository();
        geocoder = new Geocoder(this, Locale.getDefault());

        // Obtener pedidoId del intent
        pedidoId = getIntent().getStringExtra("pedido_id");
        pedidoId = "PED001";
        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupBottomSheet();
        initializeMap();
        setupClickListeners();
        startListeningToUpdates();
    }

    private void initializeViews() {
        // Bottom Sheet y contenedores
        View bottomSheet = findViewById(R.id.bottom_sheet);
        deliveryInfoContainer = findViewById(R.id.delivery_info_container);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Botones y controles interactivos
        btnOrderDetails = findViewById(R.id.btn_order_details);
        btnChat = findViewById(R.id.btnChat);

        // TextViews para estados
        orderStatus1 = findViewById(R.id.order_status_1);
        orderStatus2 = findViewById(R.id.order_status_2);
        orderStatus3 = findViewById(R.id.order_status_3);
        orderStatus4 = findViewById(R.id.order_status_4);

        // Información del restaurante y pedido
        tvRestaurant = findViewById(R.id.tvRestaurant);
        tvOrderTime = findViewById(R.id.tvOrderTime);
        restaurantLogo = findViewById(R.id.restaurant_logo);
        deliveryPersonName = findViewById(R.id.delivery_person_name);
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));

        // Configurar el estado inicial y comportamiento
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);

        // Configurar el callback
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                TextView orderStatus = findViewById(R.id.order_status);
                LinearLayout deliveryInfo = findViewById(R.id.delivery_info_container);

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        orderStatus.setVisibility(View.GONE);
                        deliveryInfo.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        orderStatus.setVisibility(View.VISIBLE);
                        deliveryInfo.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Opcional: Añadir animaciones durante el deslizamiento
                deliveryInfoContainer.setAlpha(slideOffset);
            }
        });

        // Ajustar la altura del peek
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));
    }

    private void setupClickListeners() {
        btnOrderDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteCompraDetailsActivity.class);
            intent.putExtra("pedido_id", pedidoId);
            startActivity(intent);
        });

        btnChat.setOnClickListener(v -> {
            if (repartidor != null) {
                Intent intent = new Intent(this, ClientePerfilActivity.class);
                intent.putExtra("repartidor_id", repartidor.getId());
                intent.putExtra("pedido_id", pedidoId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aún no hay repartidor asignado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListeningToUpdates() {
        // Primero obtener todos los detalles iniciales
        pedidoRepository.getPedidoConDetalles(pedidoId)
                .addOnSuccessListener(detalles -> {
                    currentPedido = detalles.getPedido();
                    restaurante = detalles.getRestaurante();
                    repartidor = detalles.getRepartidor();

                    // Actualizar UI con los datos iniciales
                    updatePedidoInfo();
                    updateRestauranteInfo();
                    if (detalles.tieneRepartidor()) {
                        updateRepartidorInfo();
                    }

                    // Iniciar escucha de cambios
                    setupListeners();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener detalles del pedido", e);
                    Toast.makeText(this, "Error al cargar el pedido", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void setupListeners() {
        // Escuchar cambios en el pedido
        pedidoListener = pedidoRepository.listenToPedido(pedidoId, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Pedido updatedPedido = snapshot.toObject(Pedido.class);
                if (updatedPedido != null) {
                    updatedPedido.setId(snapshot.getId());

                    // Si no había repartidor y ahora sí, actualizar UI y empezar a escuchar al repartidor
                    if (currentPedido.getRepartidorId() == null &&
                            updatedPedido.getRepartidorId() != null) {
                        setupRepartidorListener(updatedPedido.getRepartidorId());
                    }

                    currentPedido = updatedPedido;
                    updatePedidoInfo();
                }
            }
        });
    }

    private void setupRepartidorListener(String repartidorId) {
        usuarioRepository.getUserById(repartidorId)
                .addOnSuccessListener(usuario -> {
                    if (usuario instanceof Repartidor) {
                        repartidor = (Repartidor) usuario;
                        updateRepartidorInfo();

                        repartidorListener = pedidoRepository.listenToRepartidor(repartidorId,
                                (snapshot, e) -> {
                                    if (e != null) {
                                        Log.w(TAG, "Repartidor listen failed.", e);
                                        return;
                                    }

                                    if (snapshot != null && snapshot.exists()) {
                                        Repartidor updatedRepartidor = snapshot.toObject(Repartidor.class);
                                        if (updatedRepartidor != null) {
                                            repartidor = updatedRepartidor;
                                            updateRepartidorInfo();
                                            updateDeliveryRoute();
                                        }
                                    }
                                });
                    }
                });
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
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

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void setupMap() {
        if (mMap == null) return;

        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if (currentPedido != null) {
                updateMapLocations();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error setting up map", e);
        }
    }

    private void updateMapLocations() {
        try {
            Log.d(TAG, "Actualizando ubicaciones en el mapa");

            if (restaurante != null && restaurante.getDireccion() != null) {
                Log.d(TAG, "Geocoding dirección restaurante: " + restaurante.getDireccion());
                List<Address> addresses = geocoder.getFromLocationName(
                        restaurante.getDireccion(), 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    restauranteLocation = new LatLng(address.getLatitude(), address.getLongitude());
                    Log.d(TAG, "Ubicación restaurante: " + restauranteLocation.toString());
                }
            }

            if (currentPedido != null && currentPedido.getDireccionEntrega() != null) {
                Log.d(TAG, "Geocoding dirección entrega: " + currentPedido.getDireccionEntrega());
                List<Address> addresses = geocoder.getFromLocationName(
                        currentPedido.getDireccionEntrega(), 1);
                if (!addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    clienteLocation = new LatLng(address.getLatitude(), address.getLongitude());
                    Log.d(TAG, "Ubicación entrega: " + clienteLocation.toString());
                }
            }

            updateDeliveryRoute();
        } catch (IOException e) {
            Log.e(TAG, "Error geocoding addresses", e);
        }
    }

    private void updateDeliveryRoute() {
        if (mMap == null) return;

        // Limpiar mapa anterior
        mMap.clear();

        try {
            // Primero agregar los marcadores que tengamos disponibles
            if (restauranteLocation != null) {
                addMarkerToMap(restauranteLocation,
                        restaurante != null ? restaurante.getNombre() : "Restaurante",
                        R.drawable.ic_restaurant);
            }

            if (repartidor != null) {
                LatLng repartidorLatLng = new LatLng(repartidor.getLatitud(), repartidor.getLongitud());
                addMarkerToMap(repartidorLatLng,
                        "Repartidor: " + repartidor.getNombres(),
                        R.drawable.ic_delivery_marker);
            }

            if (clienteLocation != null) {
                addMarkerToMap(clienteLocation, "Punto de entrega", R.drawable.ic_location_flag);
            }

            // Dibujar la ruta dependiendo del estado del pedido
            if (currentPedido != null) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .width(10)
                        .color(ContextCompat.getColor(this, R.color.btn_medium));

                switch (currentPedido.getEstado()) {
                    case "En preparación":
                        // Solo mostrar restaurante y cliente
                        if (restauranteLocation != null && clienteLocation != null) {
                            polylineOptions.add(restauranteLocation).add(clienteLocation);
                            mMap.addPolyline(polylineOptions);
                        }
                        break;

                    case "En camino":
                        // Mostrar ruta completa con repartidor
                        if (restauranteLocation != null && repartidor != null && clienteLocation != null) {
                            LatLng repartidorLatLng = new LatLng(repartidor.getLatitud(), repartidor.getLongitud());
                            polylineOptions.add(restauranteLocation)
                                    .add(repartidorLatLng)
                                    .add(clienteLocation);
                            mMap.addPolyline(polylineOptions);
                        }
                        break;
                }
            }

            // Ajustar la cámara para mostrar todos los puntos
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            if (restauranteLocation != null) {
                builder.include(restauranteLocation);
                hasPoints = true;
            }
            if (repartidor != null) {
                LatLng repartidorLatLng = new LatLng(repartidor.getLatitud(), repartidor.getLongitud());
                builder.include(repartidorLatLng);
                hasPoints = true;
            }
            if (clienteLocation != null) {
                builder.include(clienteLocation);
                hasPoints = true;
            }

            if (hasPoints) {
                try {
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                } catch (Exception e) {
                    Log.e(TAG, "Error adjusting camera", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating delivery route", e);
        }
    }

    private void addMarkerToMap(LatLng position, String title, int iconResource) {
        if (position == null || mMap == null) return;

        BitmapDescriptor icon = getBitmapDescriptor(iconResource);
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(title)
                .icon(icon));
    }

    private BitmapDescriptor getBitmapDescriptor(int vectorResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResourceId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }

        vectorDrawable.setBounds(0, 0,
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void updatePedidoInfo() {
        if (currentPedido == null) return;

        // Actualizar estado del pedido
        updateOrderStatus(getEstadoIndex(currentPedido.getEstado()));

        // Actualizar hora del pedido
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        String fechaFormateada = sdf.format(currentPedido.getFechaPedido());
        tvOrderTime.setText(fechaFormateada);
    }

    private void updateRestauranteInfo() {
        if (restaurante == null) return;

        tvRestaurant.setText(restaurante.getNombre());
        if (restaurante.getImagenes() != null && !restaurante.getImagenes().isEmpty()) {
            Glide.with(this)
                    .load(restaurante.getImagenes().get(0))
                    .placeholder(R.drawable.restaurant_placeholder)
                    .error(R.drawable.restaurant_placeholder)
                    .into(restaurantLogo);
        }

        updateMapLocations();
    }

    private void updateRepartidorInfo() {
        if (repartidor == null) return;

        String nombreCompleto = repartidor.getNombres() + " " + repartidor.getApellidos();
        deliveryPersonName.setText(nombreCompleto);

        if (repartidor.getFoto() != null && !repartidor.getFoto().isEmpty()) {
            // Implementar carga de foto con Glide si se necesita
        }

        // Si tenemos la ubicación del repartidor, actualizar el mapa
        updateDeliveryRoute();
    }

    private void updateOrderStatus(int status) {
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_500);
        int activeColor = ContextCompat.getColor(this, R.color.success);

        // Resetear todos los estados
        orderStatus1.setTextColor(inactiveColor);
        orderStatus2.setTextColor(inactiveColor);
        orderStatus3.setTextColor(inactiveColor);
        orderStatus4.setTextColor(inactiveColor);

        switch (status) {
            case 4:
                orderStatus4.setTextColor(activeColor);
                orderStatus3.setTextColor(activeColor);
                orderStatus2.setTextColor(activeColor);
                orderStatus1.setTextColor(activeColor);
                break;
            case 3:
                orderStatus3.setTextColor(activeColor);
                orderStatus2.setTextColor(activeColor);
                orderStatus1.setTextColor(activeColor);
                break;
            case 2:
                orderStatus2.setTextColor(activeColor);
                orderStatus1.setTextColor(activeColor);
                break;
            case 1:
                orderStatus1.setTextColor(activeColor);
                break;
        }
    }

    private int getEstadoIndex(String estado) {
        switch (estado) {
            case "Recibido": return 1;
            case "En preparación": return 2;
            case "En camino": return 3;
            case "Entregado": return 4;
            default: return 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Se requieren permisos de ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pedidoListener != null) {
            pedidoListener.remove();
        }
        if (repartidorListener != null) {
            repartidorListener.remove();
        }
    }
}