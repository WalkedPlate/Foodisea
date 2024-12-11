package com.example.foodisea.activity.login;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.databinding.ActivityRegisterBinding;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

/**
 * Activity que maneja el registro de nuevos usuarios tipo Cliente.
 * Permite el registro con datos personales y foto de perfil opcional.
 */
public class RegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityRegisterBinding binding;
    private UsuarioRepository usuarioRepository;
    private LoadingDialog loadingDialog;
    private String fechaNacimiento = "";
    private String tipoUsuario;

    // Variables para manejo de fotos
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private Uri imageUri;
    private boolean hasSelectedImage = false;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    // Variables para manejo de ubicaciones
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentLocationMarker;
    private static final LatLng DEFAULT_LOCATION = new LatLng(-12.046374, -77.042793); // Lima
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;
    private Geocoder geocoder;
    private static final int DEBOUNCE_TIME = 800; // milisegundos
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private String lastSearchText = "";
    private boolean isUpdatingFromMap = false;


    // Launchers para resultados de cámara y galería
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    handleImageSelection(imageUri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    handleImageSelection(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tipoUsuario = getIntent().getStringExtra(SelectRolActivity.EXTRA_TIPO_USUARIO);
        if (tipoUsuario == null) {
            finish();
            return;
        }
        initializeComponents();
        setupViews();
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);
        EdgeToEdge.enable(this);
        setupWindowInsets();

        // Inicializar componentes de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        // Inicializar mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Configura los insets de la ventana para una correcta visualización
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura las vistas y sus comportamientos
     */
    private void setupViews() {
        setupClickListeners();
        setupInputValidation();
        setupDatePicker();
        setupPhotoSelection();
        setupLocationViews();
    }

    /**
     * Configura las vistas del mapa
     */
    private void setupLocationViews() {
        // Configurar el botón de mi ubicación
        binding.btnMyLocation.setOnClickListener(v -> getCurrentLocation());

        // Botón para ajustar el mapa al marcador
        binding.btnAdjustMap.setOnClickListener(v -> {
            if (currentLocationMarker != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        currentLocationMarker.getPosition(), DEFAULT_ZOOM));
            }
        });

        // Mejorar la búsqueda con debounce
        binding.etDireccion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingFromMap) return; // Evitar bucle infinito

                String text = s.toString().trim();
                if (text.isEmpty()) {
                    // Si se borra el texto, limpiar el marcador
                    if (currentLocationMarker != null) {
                        currentLocationMarker.remove();
                        currentLocationMarker = null;
                    }
                    binding.txtLatitud.setText("");
                    binding.txtLongitud.setText("");
                    return;
                }

                // Cancelar búsqueda anterior si existe
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Programar nueva búsqueda
                if (!text.equals(lastSearchText)) {
                    searchRunnable = () -> searchAddress(text);
                    searchHandler.postDelayed(searchRunnable, DEBOUNCE_TIME);
                    lastSearchText = text;
                }
            }
        });

        // Agregar acción de búsqueda al teclado
        binding.etDireccion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String address = binding.etDireccion.getText().toString().trim();
                if (!address.isEmpty()) {
                    searchAddress(address);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });
    }

    // Método para ocultar el teclado
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void updateLocationOnMap(LatLng location) {
        if (location == null) return;

        try {
            // Actualizar marcador
            if (currentLocationMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(location)
                        .title("Ubicación seleccionada")
                        .draggable(true); // Permitir arrastrar el marcador
                currentLocationMarker = mMap.addMarker(markerOptions);
            } else {
                currentLocationMarker.setPosition(location);
            }

            // Actualizar campos
            binding.txtLatitud.setText(String.format(Locale.US, "%.6f", location.latitude));
            binding.txtLongitud.setText(String.format(Locale.US, "%.6f", location.longitude));

            // Obtener y actualizar dirección
            getAddressFromLocation(location);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al actualizar la ubicación");
        }
    }

    private void getAddressFromLocation(LatLng location) {
        try {
            if (Geocoder.isPresent()) {
                isUpdatingFromMap = true; // Evitar bucle infinito
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(location.latitude, location.longitude, 1, addresses -> {
                        runOnUiThread(() -> {
                            if (!addresses.isEmpty()) {
                                String addressText = getFormattedAddress(addresses.get(0));
                                binding.etDireccion.setText(addressText);
                            }
                            isUpdatingFromMap = false;
                        });
                    });
                } else {
                    List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                    if (!addresses.isEmpty()) {
                        String addressText = getFormattedAddress(addresses.get(0));
                        binding.etDireccion.setText(addressText);
                    }
                    isUpdatingFromMap = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            isUpdatingFromMap = false;
        }
    }

    private String getFormattedAddress(Address address) {
        List<String> addressParts = new ArrayList<>();

        // Priorizar el nombre de la vía
        if (address.getThoroughfare() != null) {
            String thoroughfare = address.getThoroughfare();
            if (address.getSubThoroughfare() != null) {
                thoroughfare += " " + address.getSubThoroughfare();
            }
            addressParts.add(thoroughfare);
        }

        // Agregar distrito
        if (address.getSubLocality() != null) {
            addressParts.add(address.getSubLocality());
        } else if (address.getLocality() != null) {
            addressParts.add(address.getLocality());
        }

        // Agregar provincia si es diferente al distrito
        if (address.getLocality() != null && !address.getLocality().equals(address.getSubLocality())) {
            addressParts.add(address.getLocality());
        }

        return TextUtils.join(", ", addressParts);
    }

    private void searchAddress(String address) {
        if (TextUtils.isEmpty(address)) return;

        loadingDialog.show("Buscando ubicación...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(address, 5, new Geocoder.GeocodeListener() {
                @Override
                public void onGeocode(@NonNull List<Address> addresses) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        if (!addresses.isEmpty()) {
                            Address location = addresses.get(0);
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            updateLocationOnMap(latLng);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        } else {
                            showError("No se encontró la dirección");
                        }
                    });
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    runOnUiThread(() -> {
                        loadingDialog.dismiss();
                        showError("Error al buscar la dirección: " + errorMessage);
                    });
                }
            });
        }
    }

    private void getCurrentLocation() {
        if (!checkLocationPermission()) {
            requestLocationPermission();
            return;
        }

        try {
            Task<Location> locationResult = fusedLocationClient.getLastLocation();
            locationResult.addOnCompleteListener(this, task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    updateLocationOnMap(latLng);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                } else {
                    showError("No se pudo obtener tu ubicación actual");
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            showError("Error al obtener la ubicación");
        }
    }

    /**
     * Verifica si se tienen los permisos de ubicación necesarios
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Solicita los permisos de ubicación necesarios
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // El usuario ya ha rechazado el permiso antes, mostrar explicación
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Permiso de ubicación necesario")
                    .setMessage("Se necesita acceso a tu ubicación para poder seleccionar correctamente tu domicilio en el mapa.")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        // Solicitar permiso
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            // Primera vez que se solicita el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean validateLocation() {
        if (binding.etDireccion.getText().toString().trim().isEmpty()) {
            binding.etDireccionLayout.setError("Ingresa una dirección");
            return false;
        }

        if (currentLocationMarker == null) {
            showError("Por favor, selecciona una ubicación en el mapa");
            return false;
        }

        binding.etDireccionLayout.setError(null);
        return true;
    }


    /**
     * Configura los listeners para los botones y campos clickeables
     */
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegistrar.setOnClickListener(v -> attemptRegister());
        binding.etFechaNacimiento.setOnClickListener(v -> showDatePicker());
        binding.tvTipoDoc.setOnItemClickListener((parent, view, position, id) -> {
            showCorrespondingField(getSelectedDocumentType());
        });
    }

    /**
     * Configura el campo de fecha de nacimiento
     */
    private void setupDatePicker() {
        binding.etFechaNacimiento.setFocusable(false);
        binding.etFechaNacimiento.setClickable(true);
    }

    /**
     * Configura los listeners para la selección de foto de perfil
     */
    private void setupPhotoSelection() {
        binding.imgProfile.setOnClickListener(v -> showImageSelectionDialog());
        binding.iconCamera.setOnClickListener(v -> showImageSelectionDialog());
    }

    /**
     * Configura la validación en tiempo real de todos los campos
     */
    private void setupInputValidation() {
        setupTextWatcher(binding.etNombres, this::validateNombres);
        setupTextWatcher(binding.etApellidos, this::validateApellidos);
        setupTextWatcher(binding.etCorreo, this::validateEmail);
        setupTextWatcher(binding.etTelefono, this::validateTelefono);
        setupTextWatcher(binding.etDni, this::validateDni);

        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        binding.etPassword.addTextChangedListener(passwordWatcher);
        binding.etConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    /**
     * Configura un TextWatcher para un campo de texto
     */
    private void setupTextWatcher(TextInputEditText editText, Function<String, Boolean> validator) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validator.apply(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Muestra el diálogo para seleccionar la fuente de la imagen
     */
    private void showImageSelectionDialog() {
        String[] options = {"Tomar foto", "Elegir de galería", "Cancelar"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar foto de perfil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            dispatchTakePictureIntent();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    /**
     * Inicia la captura de foto con la cámara
     */
    private void dispatchTakePictureIntent() {
        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Perfil_" + UUID.randomUUID().toString());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Foto de perfil");

            imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraLauncher.launch(takePictureIntent);
            }
        } catch (Exception e) {
            showError("Error al abrir la cámara: " + e.getMessage());
        }
    }

    /**
     * Abre la galería para seleccionar una imagen
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        galleryLauncher.launch(Intent.createChooser(intent, "Seleccionar imagen"));
    }

    /**
     * Procesa y muestra la imagen seleccionada
     */
    private void handleImageSelection(Uri uri) {
        if (uri != null) {
            imageUri = uri;
            hasSelectedImage = true;
            Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(binding.imgProfile);
        }
    }

    /**
     * Muestra el selector de fecha configurado para usuarios mayores de 18 años
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    fechaNacimiento = sdf.format(calendar.getTime());
                    binding.etFechaNacimiento.setText(fechaNacimiento);
                    validateFechaNacimiento();
                },
                year, month, day);

        calendar.add(Calendar.YEAR, -18);
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }

    /**
     * Realiza el intento de registro validando y recopilando todos los datos
     */
    private void attemptRegister() {
        if (!validateFields()) {
            return;
        }

        loadingDialog.show(getString(R.string.mensaje_registrando));

        // Preguntar si desea continuar sin foto solo si no ha seleccionado una
        if (!hasSelectedImage) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Foto de perfil")
                    .setMessage("¿Deseas continuar el registro sin foto de perfil? Podrás agregarla más tarde.")
                    .setPositiveButton("Continuar sin foto", (dialog, which) -> {
                        proceedWithRegistration(null);
                    })
                    .setNegativeButton("Seleccionar foto", (dialog, which) -> {
                        loadingDialog.dismiss();
                        showImageSelectionDialog();
                    })
                    .show();
        } else {
            proceedWithRegistration(imageUri);
        }
    }

    /**
     * Procede con el registro del usuario
     * @param photoUri Uri de la foto seleccionada, null si no hay foto
     */
    private void proceedWithRegistration(Uri photoUri) {
        Usuario nuevoUsuario = createUserObject();
        String password = binding.etPassword.getText().toString();

        // Registrar usuario con o sin foto
        usuarioRepository.registerUser(
                nuevoUsuario.getCorreo(),
                password,
                nuevoUsuario,
                photoUri  // será null si no hay foto
        ).addOnSuccessListener(usuario -> {
            loadingDialog.dismiss();
            showSuccess(getString(R.string.mensaje_registro_exitoso));
            finish();
            startActivity(new Intent(this, ConfirmRegisterActivity.class));
        }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            handleRegistrationError(e);
        });
    }

    /**
     * Crea y configura el objeto Usuario con todos sus datos
     */
    private Usuario createUserObject() {
        Usuario nuevoUsuario;
        if (tipoUsuario.equals(SelectRolActivity.TIPO_REPARTIDOR)) {
            Repartidor repartidor = new Repartidor();
            repartidor.setEstado("Activo");
            repartidor.setDisposicion("Disponible");
            repartidor.setLatitud(0.0);
            repartidor.setLongitud(0.0);
            nuevoUsuario = repartidor;
        } else {
            nuevoUsuario = new Cliente();
        }

        nuevoUsuario.setNombres(binding.etNombres.getText().toString().trim());
        nuevoUsuario.setApellidos(binding.etApellidos.getText().toString().trim());
        nuevoUsuario.setCorreo(binding.etCorreo.getText().toString().trim());
        nuevoUsuario.setTelefono(binding.etTelefono.getText().toString().trim());
        nuevoUsuario.setDireccion(binding.etDireccion.getText().toString().trim());
        nuevoUsuario.setDocumentoId(binding.etDni.getText().toString().trim());
        nuevoUsuario.setFechaNacimiento(fechaNacimiento);
        nuevoUsuario.setEstado("Activo");
        nuevoUsuario.setTipoUsuario(tipoUsuario);

        // Agregar coordenadas
        if (currentLocationMarker != null) {
            LatLng position = currentLocationMarker.getPosition();
            nuevoUsuario.setLatitudDireccion(position.latitude);
            nuevoUsuario.setLongitudDireccion(position.longitude);
        }

        return nuevoUsuario;
    }

    /**
     * Valida todos los campos del formulario
     */
    private boolean validateFields() {
        boolean isValid = true;

        isValid &= validateNombres(binding.etNombres.getText().toString());
        isValid &= validateApellidos(binding.etApellidos.getText().toString());
        isValid &= validateEmail(binding.etCorreo.getText().toString());
        isValid &= validateTelefono(binding.etTelefono.getText().toString());
        isValid &= validateDni(binding.etDni.getText().toString());
        isValid &= validateDireccion(binding.etDireccion.getText().toString());
        isValid &= validateFechaNacimiento();
        isValid &= validatePasswords();
        isValid &= validateLocation();

        return isValid;
    }

    // Los métodos de validación existentes se mantienen igual...
    private boolean validateFechaNacimiento() {
        if (fechaNacimiento.isEmpty()) {
            binding.etFechaNacimientoLayout.setError(getString(R.string.error_seleccionar_fecha));
            return false;
        }
        binding.etFechaNacimientoLayout.setError(null);
        return true;
    }

    private boolean validateNombres(String nombres) {
        if (nombres.trim().isEmpty()) {
            binding.etNombresLayout.setError(getString(R.string.error_ingresa_nombres));
            return false;
        } else if (nombres.length() < 2) {
            binding.etNombresLayout.setError(getString(R.string.error_nombre_corto));
            return false;
        } else if (!nombres.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            binding.etNombresLayout.setError(getString(R.string.error_solo_letras));
            return false;
        }
        binding.etNombresLayout.setError(null);
        return true;
    }

    private boolean validateApellidos(String apellidos) {
        if (apellidos.trim().isEmpty()) {
            binding.etApellidosLayout.setError(getString(R.string.error_ingresa_apellidos));
            return false;
        } else if (apellidos.length() < 2) {
            binding.etApellidosLayout.setError(getString(R.string.error_apellido_corto));
            return false;
        } else if (!apellidos.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            binding.etApellidosLayout.setError(getString(R.string.error_solo_letras));
            return false;
        }
        binding.etApellidosLayout.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.trim().isEmpty()) {
            binding.etCorreoLayout.setError(getString(R.string.error_ingresa_email));
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etCorreoLayout.setError(getString(R.string.error_email_invalido));
            return false;
        }
        binding.etCorreoLayout.setError(null);
        return true;
    }

    private boolean validateTelefono(String telefono) {
        if (telefono.trim().isEmpty()) {
            binding.etTelefonoLayout.setError(getString(R.string.error_ingresa_telefono));
            return false;
        } else if (telefono.length() != 9) {
            binding.etTelefonoLayout.setError(getString(R.string.error_telefono_formato));
            return false;
        } else if (!telefono.matches("^9\\d{8}$")) {
            binding.etTelefonoLayout.setError(getString(R.string.error_telefono_formato));
            return false;
        }
        binding.etTelefonoLayout.setError(null);
        return true;
    }

    private boolean validateDni(String dni) {
        if (dni.trim().isEmpty()) {
            binding.etDniLayout.setError(getString(R.string.error_ingresa_dni));
            return false;
        } else if (dni.length() != 8) {
            binding.etDniLayout.setError(getString(R.string.error_dni_formato));
            return false;
        } else if (!dni.matches("\\d{8}")) {
            binding.etDniLayout.setError(getString(R.string.error_dni_solo_numeros));
            return false;
        }
        binding.etDniLayout.setError(null);
        return true;
    }

    private boolean validateDireccion(String direccion) {
        if (direccion.trim().isEmpty()) {
            binding.etDireccionLayout.setError(getString(R.string.error_ingresa_direccion));
            return false;
        }
        binding.etDireccionLayout.setError(null);
        return true;
    }

    private boolean validatePasswords() {
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        if (password.isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.error_ingresa_password));
            return false;
        } else if (password.length() < 6) {
            binding.etPasswordLayout.setError(getString(R.string.error_password_corto));
            return false;
        } else {
            binding.etPasswordLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPasswordLayout.setError(getString(R.string.error_confirma_password));
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.etConfirmPasswordLayout.setError(getString(R.string.error_passwords_no_coinciden));
            return false;
        } else {
            binding.etConfirmPasswordLayout.setError(null);
        }

        return true;
    }

    /**
     * Maneja los diferentes tipos de errores que pueden ocurrir durante el registro
     */
    private void handleRegistrationError(Exception e) {
        String errorMessage;
        if (e instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = getString(R.string.error_password_debil);
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = getString(R.string.error_email_invalido);
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            errorMessage = getString(R.string.error_email_existente);
        } else if (e instanceof FirebaseNetworkException) {
            errorMessage = getString(R.string.error_conexion);
        } else {
            errorMessage = getString(R.string.error_generico_registro);
        }
        showError(errorMessage);
    }

    /**
     * Muestra un diálogo de error
     */
    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.error_titulo))
                .setMessage(message)
                .setPositiveButton(getString(R.string.aceptar), null)
                .show();
    }

    /**
     * Muestra un diálogo de éxito
     */
    private void showSuccess(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.exito_titulo))
                .setMessage(message)
                .setPositiveButton(getString(R.string.aceptar), null)
                .show();
    }

    /**
     * Maneja permisos de la cámara
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMapSettings();
                getCurrentLocation();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, intentar abrir la cámara de nuevo
                dispatchTakePictureIntent();
            } else {
                // Permiso denegado
                if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // Usuario marcó "No volver a preguntar"
                    showSettingsDialog();
                } else {
                    showError("Error en el permiso de cámara");
                }
            }
        }
    }

    private void showSettingsDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permiso de Cámara Necesario")
                .setMessage("Se necesita acceso a la cámara para tomar la foto de perfil. " +
                        "Por favor, habilita el permiso en la configuración.")
                .setPositiveButton("Configuración", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMapSettings();
    }

    private void setupMapSettings() {
        if (mMap == null) return;

        try {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setScrollGesturesEnabled(true);

            // Click listener para el mapa
            mMap.setOnMapClickListener(this::updateLocationOnMap);

            // Agregar listener para cuando se arrastra el marcador
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {}

                @Override
                public void onMarkerDrag(Marker marker) {}

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    updateLocationOnMap(marker.getPosition());
                }
            });

            // Comprobar permisos
            if (checkLocationPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }

            // Centrar en Lima
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12f));

        } catch (SecurityException e) {
            e.printStackTrace();
            showError("Error al configurar el mapa");
        }
    }

    /**
     * Tipo de documento de identificación
     */

    private String getSelectedDocumentType() {
        return binding.tvTipoDoc.getText().toString();
    }

    private void showCorrespondingField(String documentType) {
        hideAllFields();
        if (documentType.equals("DNI")) {
            binding.etDniLayout.setVisibility(View.VISIBLE);
        } else if (documentType.equals("Pasaporte")) {
            binding.etPassportLayout.setVisibility(View.VISIBLE);
        } else if (documentType.equals("Carnet de extranjería")) {
            binding.etExtranjeriaLayout.setVisibility(View.VISIBLE);
        }
    }

    // Oculta todos los campos de entrada
    private void hideAllFields() {
        binding.etDniLayout.setVisibility(View.GONE);
        binding.etPassportLayout.setVisibility(View.GONE);
        binding.etExtranjeriaLayout.setVisibility(View.GONE);
    }
}