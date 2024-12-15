package com.example.foodisea.activity.cliente;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.foodisea.databinding.ActivityClienteTrackingBinding;
import com.example.foodisea.helper.DirectionsHelper;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;
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

    private ActivityClienteTrackingBinding binding;

    private static final String TAG = "ClienteTrackingActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Repositories
    private PedidoRepository pedidoRepository;
    private RestauranteRepository restauranteRepository;
    private UsuarioRepository usuarioRepository;

    // Firebase Listeners
    private ListenerRegistration pedidoListener;
    private ListenerRegistration repartidorListener;

    // Map related
    private GoogleMap mMap;
    private DirectionsHelper directionsHelper;
    private Marker repartidorMarker;
    private LatLng restauranteLocation;
    private LatLng clienteLocation;

    // Bottom Sheet UI
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Data
    private String pedidoId;
    private Pedido currentPedido;
    private Restaurante restaurante;
    private Repartidor repartidor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar repositories y utilidades
        pedidoRepository = new PedidoRepository(this);
        restauranteRepository = new RestauranteRepository();
        usuarioRepository = new UsuarioRepository();

        // Inicializar DirectionsHelper
        directionsHelper = new DirectionsHelper(this);

        // Obtener pedidoId del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        initializeMap();
        setupClickListeners();
        startListeningToUpdates();
    }

    private void initializeViews() {
        // Bottom Sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        setupBottomSheet();
    }


    private void setupBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                TransitionManager.beginDelayedTransition((ViewGroup) bottomSheet);

                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        showExpandedContent(true);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        showExpandedContent(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                binding.deliveryProgress.setAlpha(slideOffset);
            }
        });
    }

    private void setupClickListeners() {
        binding.btnOrderDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteCompraDetailsActivity.class);
            intent.putExtra("pedidoId", pedidoId);
            startActivity(intent);
        });

        binding.btnChat.setOnClickListener(v -> {
            if (repartidor != null) {
                Intent intent = new Intent(this, ClientePerfilActivity.class);
                intent.putExtra("repartidor_id", repartidor.getId());
                intent.putExtra("pedido_id", pedidoId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Aún no hay repartidor asignado", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCall.setOnClickListener(v -> {
            if (repartidor != null && repartidor.getTelefono() != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + repartidor.getTelefono()));
                startActivity(intent);
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

                    // Verificar si se asignó un repartidor nuevo
                    boolean repartidorCambio = (currentPedido.getRepartidorId() == null &&
                            updatedPedido.getRepartidorId() != null) ||
                            (currentPedido.getRepartidorId() != null &&
                                    !currentPedido.getRepartidorId().equals(updatedPedido.getRepartidorId()));

                    // Actualizar coordenadas de entrega
                    clienteLocation = new LatLng(
                            updatedPedido.getLatitudEntrega(),
                            updatedPedido.getLongitudEntrega()
                    );

                    boolean estadoCambio = currentPedido == null ||
                            !currentPedido.getEstado().equals(updatedPedido.getEstado());

                    currentPedido = updatedPedido;
                    updatePedidoInfo();

                    // Si hay un nuevo repartidor, configurar su listener
                    if (repartidorCambio && currentPedido.getRepartidorId() != null) {
                        setupRepartidorListener(currentPedido.getRepartidorId());
                    }

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
            Log.d(TAG, "Configurando listener para repartidor: " + repartidorId);
            repartidorListener = pedidoRepository.listenToRepartidor(repartidorId,
                    (snapshot, e) -> {
                        if (e != null) {
                            Log.w(TAG, "Repartidor listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Repartidor updatedRepartidor = snapshot.toObject(Repartidor.class);
                            if (updatedRepartidor != null) {
                                updatedRepartidor.setId(snapshot.getId()); // Asegúrate de setear el ID
                                repartidor = updatedRepartidor;
                                runOnUiThread(() -> {
                                    updateRepartidorInfo();
                                    updateDeliveryRoute();
                                });
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

    private void updateDeliveryRoute() {
        if (mMap == null || !checkLocationPermission()) return;

        mMap.clear();

        // Siempre añadimos los marcadores de restaurante y punto de entrega
        addMarkerToMap(restauranteLocation, restaurante.getNombre(), R.drawable.ic_restaurant);
        addMarkerToMap(clienteLocation, "Punto de entrega", R.drawable.ic_location_flag);

        // Si hay repartidor, manejamos su marcador y rutas
        if (repartidor != null) {
            LatLng repartidorLatLng = new LatLng(repartidor.getLatitud(), repartidor.getLongitud());

            // Actualizamos el marcador del repartidor
            updateRepartidorMarker(repartidorLatLng, repartidor.getNombres());

            switch (currentPedido.getEstado()) {
                case Pedido.ESTADO_RECOGIENDO:
                    // Ruta del repartidor al restaurante
                    directionsHelper.getRoute(repartidorLatLng, restauranteLocation,
                            new DirectionsHelper.DirectionsCallback() {
                                @Override
                                public void onDirectionsSuccess(List<LatLng> points, String estimatedTime) {
                                    drawPolyline(points, R.color.route_active);
                                    binding.estimatedTime.setText("Llegando al restaurante en " + estimatedTime);
                                    includeLatLngsInCameraBounds(points);
                                }

                                @Override
                                public void onDirectionsFailure(String error) {
                                    Log.e(TAG, "Error getting directions: " + error);
                                }
                            });

                    // Ruta punteada del restaurante al destino
                    drawDottedRoute(restauranteLocation, clienteLocation,
                            ContextCompat.getColor(this, R.color.gray_300));
                    break;

                case Pedido.ESTADO_EN_CAMINO:
                    // Solo dibujamos la ruta activa al punto de entrega
                    directionsHelper.getRoute(repartidorLatLng, clienteLocation,
                            new DirectionsHelper.DirectionsCallback() {
                                @Override
                                public void onDirectionsSuccess(List<LatLng> points, String estimatedTime) {
                                    drawPolyline(points, R.color.route_active);
                                    binding.estimatedTime.setText("Llegando en " + estimatedTime);
                                    includeLatLngsInCameraBounds(points);
                                }

                                @Override
                                public void onDirectionsFailure(String error) {
                                    Log.e(TAG, "Error getting directions: " + error);
                                }
                            });
                    break;
            }
        }
        else {
            // Si estamos en estado inicial, solo ajustamos la cámara
            switch (currentPedido.getEstado()) {
                case Pedido.ESTADO_RECIBIDO:
                case Pedido.ESTADO_EN_PREPARACION:
                    adjustMapCamera();
                    break;
            }
        }
        // En los estados Recibido y En preparación no dibujamos ninguna línea
        adjustMapCamera(); // Ajustar la cámara para mostrar los marcadores
    }
    private void adjustMapCamera() {
        if (mMap == null) return;

        try {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // Siempre incluir restaurante y punto de entrega
            builder.include(restauranteLocation);
            builder.include(clienteLocation);

            // Si hay repartidor, incluir su ubicación
            if (repartidor != null) {
                builder.include(new LatLng(repartidor.getLatitud(), repartidor.getLongitud()));
            }

            // Construir los límites
            LatLngBounds bounds = builder.build();

            // Obtener el ancho y alto de la pantalla
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            // Calcular el padding (20% del ancho de pantalla)
            int padding = (int) (width * 0.20);

            // Crear el update de cámara
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

            // Animar la cámara
            mMap.animateCamera(cu);
        } catch (Exception e) {
            Log.e(TAG, "Error adjusting camera", e);

            // Si falla, intentar un zoom out para mostrar toda el área
            if (restauranteLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(restauranteLocation, 13f));
            }
        }
    }


    private void drawPolyline(List<LatLng> points, int colorResId, float width) {
        if (mMap == null || points == null || points.isEmpty()) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .width(width)
                .color(ContextCompat.getColor(this, colorResId));

        mMap.addPolyline(polylineOptions);
    }

    // Sobrecarga del método para usar el ancho por defecto
    private void drawPolyline(List<LatLng> points, int colorResId) {
        drawPolyline(points, colorResId, 10f);
    }

    private void includeLatLngsInCameraBounds(List<LatLng> points) {
        if (points.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }

        // Incluir también la ubicación del restaurante si no está en la ruta
        builder.include(restauranteLocation);

        // Aplicar el padding y animar la cámara
        int padding = getResources().getDimensionPixelSize(R.dimen.map_padding);
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
        mMap.animateCamera(cu);
    }


    // Método para actualizar el marcador del repartidor con icono de moto
    private void updateRepartidorMarker(LatLng position, String nombre) {
        if (mMap == null) return;

        try {
            if (repartidorMarker == null) {
                BitmapDescriptor icon = getBitmapDescriptor(R.drawable.ic_delivery_bike);
                repartidorMarker = mMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title("Repartidor: " + nombre)
                        .icon(icon)
                        .flat(true)
                        .anchor(0.5f, 0.5f));
            } else if (!repartidorMarker.getPosition().equals(position)) {
                animateMarker(repartidorMarker, position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating delivery marker", e);
        }
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

        String estado = currentPedido.getEstado();
        updateOrderStatus(getEstadoIndex(estado));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        binding.tvOrderTime.setText(sdf.format(currentPedido.getFechaPedido()));

        binding.estimatedTime.setVisibility(estado.equals("En camino") ? View.VISIBLE : View.GONE);
    }

    private int getProgressForState(String estado) {
        switch (estado) {
            case Pedido.ESTADO_RECIBIDO: return 20;        // 20%
            case Pedido.ESTADO_EN_PREPARACION: return 40;  // 40%
            case Pedido.ESTADO_RECOGIENDO: return 60;      // 60%
            case Pedido.ESTADO_EN_CAMINO: return 80;       // 80%
            case Pedido.ESTADO_ENTREGADO: return 100;      // 100%
            default: return 0;
        }
    }

    private void updateRestauranteInfo() {
        if (restaurante == null) return;

        binding.tvRestaurant.setText(restaurante.getNombre());
        if (restaurante.getImagenes() != null && !restaurante.getImagenes().isEmpty()) {
            Glide.with(this)
                    .load(restaurante.getImagenes().get(0))
                    .placeholder(R.drawable.restaurant_placeholder)
                    .error(R.drawable.restaurant_placeholder)
                    .into(binding.restaurantLogo);
        }

        updateMapLocations();
    }

    private void updateRepartidorInfo() {
        if (repartidor == null) return;

        String nombreCompleto = repartidor.getNombres() + " " + repartidor.getApellidos();
        binding.deliveryPersonName.setText(nombreCompleto);

        if (repartidor.getFoto() != null && !repartidor.getFoto().isEmpty()) {
            Glide.with(this)
                    .load(repartidor.getFoto())
                    .placeholder(R.drawable.rounded_person_24)
                    .error(R.drawable.rounded_person_24)
                    .into(binding.deliveryPersonPhoto);
        }

        binding.deliveryPersonInfo.setVisibility(View.VISIBLE);
        updateDeliveryRoute();
    }


    private void updateOrderStatus(int status) {
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_500);
        int activeColor = ContextCompat.getColor(this, R.color.success);

        // Resetear todos los estados
        binding.orderStatus1.setTextColor(inactiveColor);
        binding.orderStatus2.setTextColor(inactiveColor);
        binding.orderStatus3.setTextColor(inactiveColor);
        binding.orderStatus4.setTextColor(inactiveColor);
        binding.orderStatus5.setTextColor(inactiveColor);

        // Activar estados según el progreso
        switch (status) {
            case 5: // Entregado
                binding.orderStatus5.setTextColor(activeColor);
            case 4: // En camino
                binding.orderStatus4.setTextColor(activeColor);
            case 3: // Recogiendo
                binding.orderStatus3.setTextColor(activeColor);
            case 2: // En preparación
                binding.orderStatus2.setTextColor(activeColor);
            case 1: // Recibido
                binding.orderStatus1.setTextColor(activeColor);
                break;
        }

        // Actualizar el badge y progress
        updateBadgeAndProgress(status);
    }

    private void updateBadgeAndProgress(int status) {
        int badgeColor = getBadgeColor(status);
        Drawable background = binding.orderStatusBadge.getBackground();
        if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(ContextCompat.getColor(this, badgeColor));
        }

        binding.deliveryProgress.setProgress(getProgressForState(currentPedido.getEstado()));
        binding.orderStatusBadge.setText(getEstadoTexto(currentPedido.getEstado()));
    }


    private int getBadgeColor(int status) {
        switch (status) {
            case 1: return R.color.warning;
            case 2: return R.color.warning;
            case 3: // Recogiendo pedido
            case 4: // En camino
                return R.color.btn_medium;
            case 5: return R.color.success;
            default: return R.color.gray_500;
        }
    }

    private String getEstadoTexto(String estado) {
        switch(estado) {
            case Pedido.ESTADO_RECIBIDO:
                return "Tu pedido ha sido recibido";
            case Pedido.ESTADO_EN_PREPARACION:
                return "Preparando tu pedido";
            case Pedido.ESTADO_RECOGIENDO:
                return "Repartidor en camino al restaurante";
            case Pedido.ESTADO_EN_CAMINO:
                return "Tu pedido está en camino";
            case Pedido.ESTADO_ENTREGADO:
                return "Pedido entregado";
            default:
                return "Estado desconocido";
        }
    }


    private int getEstadoIndex(String estado) {
        switch (estado) {
            case Pedido.ESTADO_RECIBIDO: return 1;
            case Pedido.ESTADO_EN_PREPARACION: return 2;
            case Pedido.ESTADO_RECOGIENDO: return 3;
            case Pedido.ESTADO_EN_CAMINO: return 4;
            case Pedido.ESTADO_ENTREGADO: return 5;
            default: return 0;
        }
    }

    private void showExpandedContent(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        binding.deliveryProgress.setVisibility(visibility);
        binding.statusStepsContainer.setVisibility(visibility);

        // Si hay repartidor, mostrar/ocultar su información
        if (repartidor != null) {
            binding.deliveryPersonInfo.setVisibility(visibility);
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
        binding = null;
    }
}