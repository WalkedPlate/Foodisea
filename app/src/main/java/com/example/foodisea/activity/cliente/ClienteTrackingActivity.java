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
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_tracking);

        // Inicializar repositories y utilidades
        pedidoRepository = new PedidoRepository(this);
        restauranteRepository = new RestauranteRepository();
        usuarioRepository = new UsuarioRepository();

        // Obtener pedidoId del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
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
        pedidoRepository.getPedidoConDetalles(pedidoId)
                .addOnSuccessListener(detalles -> {
                    currentPedido = detalles.getPedido();
                    restaurante = detalles.getRestaurante();
                    repartidor = detalles.getRepartidor();

                    // Asignar las coordenadas inmediatamente
                    restauranteLocation = new LatLng(
                            restaurante.getLatitud(),
                            restaurante.getLongitud()
                    );

                    clienteLocation = new LatLng(
                            currentPedido.getLatitudEntrega(),
                            currentPedido.getLongitudEntrega()
                    );

                    // Actualizar UI con los datos iniciales
                    updatePedidoInfo();
                    updateRestauranteInfo();
                    if (detalles.tieneRepartidor()) {
                        updateRepartidorInfo();
                    }

                    // Iniciar escucha de cambios
                    setupListeners();

                    // Actualizar mapa si ya está listo
                    if (mMap != null) {
                        updateDeliveryRoute();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener detalles del pedido", e);
                    Toast.makeText(this, "Error al cargar el pedido",
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }



    private void setupListeners() {
        pedidoListener = pedidoRepository.listenToPedido(pedidoId, (snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Pedido updatedPedido = snapshot.toObject(Pedido.class);
                if (updatedPedido != null) {
                    updatedPedido.setId(snapshot.getId());

                    // Actualizar coordenadas de entrega
                    clienteLocation = new LatLng(
                            updatedPedido.getLatitudEntrega(),
                            updatedPedido.getLongitudEntrega()
                    );

                    boolean estadoCambio = currentPedido == null ||
                            !currentPedido.getEstado().equals(updatedPedido.getEstado());

                    currentPedido = updatedPedido;
                    updatePedidoInfo();

                    if (estadoCambio) {
                        updateDeliveryRoute();
                    }
                }
            }
        });
    }

    private void setupRepartidorListener(String repartidorId) {
        // Remover listener anterior si existe
        if (repartidorListener != null) {
            repartidorListener.remove();
        }

        if (repartidorId != null) {
            repartidorListener = pedidoRepository.listenToRepartidor(repartidorId,
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot,
                                            @Nullable FirebaseFirestoreException e) {
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
                        }
                    });
        }
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

            // Actualizar el mapa si ya tenemos las coordenadas
            if (restauranteLocation != null && clienteLocation != null) {
                updateDeliveryRoute();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error setting up map", e);
        }
    }

    private void updateMapLocations() {
        // Verificar que tenemos todos los datos necesarios
        if (restaurante == null || currentPedido == null) {
            Log.e(TAG, "Datos incompletos para actualizar ubicaciones");
            return;
        }

        // Actualizar ubicaciones
        restauranteLocation = new LatLng(
                restaurante.getLatitud(),
                restaurante.getLongitud()
        );

        clienteLocation = new LatLng(
                currentPedido.getLatitudEntrega(),
                currentPedido.getLongitudEntrega()
        );

        // Log para debug
        Log.d(TAG, "Ubicación restaurante: " + restauranteLocation.toString());
        Log.d(TAG, "Ubicación entrega: " + clienteLocation.toString());

        updateDeliveryRoute();
    }

    private void procederConActualizacion() {
        // Solo actualizar la ruta cuando tengamos ambas ubicaciones
        if (restauranteLocation != null && clienteLocation != null) {
            updateDeliveryRoute();
        }
    }


    private void updateDeliveryRoute() {
        if (mMap == null) return;
        mMap.clear();

        try {
            // Agregar marcadores de origen y destino
            addMarkerToMap(restauranteLocation,
                    restaurante.getNombre(),
                    R.drawable.ic_restaurant);

            addMarkerToMap(clienteLocation,
                    "Punto de entrega",
                    R.drawable.ic_location_flag);

            // Dibujar rutas según el estado del pedido
            switch (currentPedido.getEstado()) {
                case "Recibido":
                    // Ruta punteada entre restaurante y destino
                    drawDottedRoute(restauranteLocation, clienteLocation,
                            ContextCompat.getColor(this, R.color.gray_500));
                    break;

                case "En preparación":
                    // Ruta sólida del restaurante al destino
                    drawStaticRoute(restauranteLocation, clienteLocation,
                            ContextCompat.getColor(this, R.color.warning));
                    break;

                case "En camino":
                    if (repartidor != null) {
                        LatLng repartidorLatLng = new LatLng(
                                repartidor.getLatitud(),
                                repartidor.getLongitud()
                        );

                        // Actualizar marcador del repartidor
                        updateRepartidorMarker(repartidorLatLng,
                                repartidor.getNombres() + " " + repartidor.getApellidos());

                        // Ruta completada en verde (restaurante a repartidor)
                        drawStaticRoute(restauranteLocation, repartidorLatLng,
                                ContextCompat.getColor(this, R.color.success));

                        // Ruta activa en azul (repartidor a destino)
                        drawStaticRoute(repartidorLatLng, clienteLocation,
                                ContextCompat.getColor(this, R.color.btn_medium));
                    }
                    break;

                case "Entregado":
                    // Ruta completa en verde
                    drawStaticRoute(restauranteLocation, clienteLocation,
                            ContextCompat.getColor(this, R.color.success));
                    break;
            }

            // Ajustar cámara para mostrar toda la ruta
            adjustMapCamera();

        } catch (Exception e) {
            Log.e(TAG, "Error updating delivery route", e);
        }
    }

    private void updateRepartidorMarker(LatLng position, String nombre) {
        if (mMap == null) return;

        try {
            if (repartidorMarker == null) {
                BitmapDescriptor icon = getBitmapDescriptor(R.drawable.ic_delivery_marker);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title("Repartidor: " + nombre)
                        .icon(icon)
                        .flat(true)
                        .anchor(0.5f, 0.5f);

                repartidorMarker = mMap.addMarker(markerOptions);
            } else {
                // Solo animar si la posición cambió
                LatLng currentPos = repartidorMarker.getPosition();
                if (currentPos.latitude != position.latitude ||
                        currentPos.longitude != position.longitude) {
                    animateMarker(repartidorMarker, position);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating delivery marker", e);
        }
    }

    // Métodos auxiliares para las rutas
    private void drawCompletedRoute(LatLng start, LatLng end) {
        PolylineOptions options = new PolylineOptions()
                .add(start, end)
                .width(10)
                .color(ContextCompat.getColor(this, R.color.success));
        mMap.addPolyline(options);
    }

    private void drawActiveRoute(LatLng start, LatLng end) {
        PolylineOptions options = new PolylineOptions()
                .add(start, end)
                .width(10)
                .color(ContextCompat.getColor(this, R.color.btn_medium));
        mMap.addPolyline(options);
    }

    private void drawDottedRoute(LatLng start, LatLng end, int color) {
        PolylineOptions options = new PolylineOptions()
                .add(start, end)
                .width(10)
                .color(color)
                .pattern(Arrays.asList(
                        new Dot(), new Gap(20)
                ));
        mMap.addPolyline(options);
    }

    // Método para dibujar una ruta estática
    private void drawStaticRoute(LatLng origin, LatLng destination, int color) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origin, destination)
                .width(10)
                .color(color);
        mMap.addPolyline(polylineOptions);
    }

    // Método para ajustar la cámara del mapa
    private void adjustMapCamera() {
        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder()
                    .include(restauranteLocation)
                    .include(clienteLocation);

            if (repartidor != null) {
                builder.include(new LatLng(
                        repartidor.getLatitud(),
                        repartidor.getLongitud()
                ));
            }

            LatLngBounds bounds = builder.build();
            int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        } catch (Exception e) {
            Log.e(TAG, "Error adjusting camera", e);
        }
    }

    // Método para animar el movimiento del marcador
    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 3000; // Duración de la animación en ms

        final LatLng startPosition = marker.getPosition();
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lat = t * toPosition.latitude + (1 - t) * startPosition.latitude;
                double lng = t * toPosition.longitude + (1 - t) * startPosition.longitude;
                marker.setPosition(new LatLng(lat, lng));

                // Calcular la rotación del marcador
                if (t < 1.0) {
                    handler.postDelayed(this, 16); // 60fps
                }
            }
        });
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
        try {
            Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResourceId);
            if (vectorDrawable == null) {
                return BitmapDescriptorFactory.defaultMarker();
            }

            int width = vectorDrawable.getIntrinsicWidth();
            int height = vectorDrawable.getIntrinsicHeight();
            // Si el drawable no tiene tamaño intrínseco, usar valores por defecto
            if (width <= 0) width = 48;
            if (height <= 0) height = 48;

            vectorDrawable.setBounds(0, 0, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);

            // Asegurarse de que el bitmap no sea nulo
            if (bitmap == null) {
                return BitmapDescriptorFactory.defaultMarker();
            }

            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error creating marker icon", e);
            return BitmapDescriptorFactory.defaultMarker();
        }
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