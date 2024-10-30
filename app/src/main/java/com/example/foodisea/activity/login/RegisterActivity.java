package com.example.foodisea.activity.login;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.databinding.ActivityRegisterBinding;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.function.Function;

/**
 * Activity que maneja el registro de nuevos usuarios tipo Cliente.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UsuarioRepository usuarioRepository;
    private LoadingDialog loadingDialog;
    private String fechaNacimiento = "";
    private String tipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtener el tipo de usuario del intent
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
        // Inicializar ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar repositorio y diálogo de carga
        usuarioRepository = new UsuarioRepository();
        loadingDialog = new LoadingDialog(this);

        // Configurar diseño edge-to-edge
        EdgeToEdge.enable(this);
        setupWindowInsets();
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
    }


    /**
     * Configura los listeners para los botones y campos clickeables
     */
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnRegistrar.setOnClickListener(v -> attemptRegister());
        binding.etFechaNacimiento.setOnClickListener(v -> showDatePicker());
    }

    /**
     * Configura el campo de fecha de nacimiento
     */
    private void setupDatePicker() {
        // Deshabilitar entrada manual de fecha
        binding.etFechaNacimiento.setFocusable(false);
        binding.etFechaNacimiento.setClickable(true);
    }

    /**
     * Configura la validación en tiempo real de todos los campos
     */
    private void setupInputValidation() {
        // Configurar watchers para validación en tiempo real
        setupTextWatcher(binding.etNombres, this::validateNombres);
        setupTextWatcher(binding.etApellidos, this::validateApellidos);
        setupTextWatcher(binding.etCorreo, this::validateEmail);
        setupTextWatcher(binding.etTelefono, this::validateTelefono);
        setupTextWatcher(binding.etDni, this::validateDni);

        // Watcher especial para las contraseñas que valida ambos campos
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
     * Método utilitario para configurar TextWatcher
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
     * Muestra el selector de fecha configurado para usuarios mayores de 18 años
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        // Establecer fecha por defecto 18 años atrás
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Formatear y guardar la fecha seleccionada
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    fechaNacimiento = sdf.format(calendar.getTime());
                    binding.etFechaNacimiento.setText(fechaNacimiento);
                    validateFechaNacimiento();
                },
                year, month, day);

        // Establecer fecha máxima (18 años atrás)
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

        // Crear nuevo usuario según el tipo
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

        // Configurar datos comunes
        nuevoUsuario.setNombres(binding.etNombres.getText().toString().trim());
        nuevoUsuario.setApellidos(binding.etApellidos.getText().toString().trim());
        nuevoUsuario.setCorreo(binding.etCorreo.getText().toString().trim());
        nuevoUsuario.setTelefono(binding.etTelefono.getText().toString().trim());
        nuevoUsuario.setDireccion(binding.etDireccion.getText().toString().trim());
        nuevoUsuario.setDocumentoId(binding.etDni.getText().toString().trim());
        nuevoUsuario.setFechaNacimiento(fechaNacimiento);
        nuevoUsuario.setFoto("");
        nuevoUsuario.setEstado("Activo");
        nuevoUsuario.setTipoUsuario(tipoUsuario);

        String password = binding.etPassword.getText().toString();

        // Intentar registro en Firebase
        usuarioRepository.registerUser(nuevoUsuario.getCorreo(), password, nuevoUsuario)
                .addOnSuccessListener(usuario -> {
                    loadingDialog.dismiss();
                    showSuccess(getString(R.string.mensaje_registro_exitoso));
                    finish();
                    startActivity(new Intent(this, ConfirmRegisterActivity.class));
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    handleRegistrationError(e);
                });
    }

    /**
     * Valida todos los campos del formulario
     * @return true si todos los campos son válidos
     */
    private boolean validateFields() {
        boolean isValid = true;

        // Validar todos los campos y acumular el resultado
        isValid &= validateNombres(binding.etNombres.getText().toString());
        isValid &= validateApellidos(binding.etApellidos.getText().toString());
        isValid &= validateEmail(binding.etCorreo.getText().toString());
        isValid &= validateTelefono(binding.etTelefono.getText().toString());
        isValid &= validateDni(binding.etDni.getText().toString());
        isValid &= validateDireccion(binding.etDireccion.getText().toString());
        isValid &= validateFechaNacimiento();
        isValid &= validatePasswords();

        return isValid;
    }

    /**
     * Valida la fecha de nacimiento
     */
    private boolean validateFechaNacimiento() {
        if (fechaNacimiento.isEmpty()) {
            binding.etFechaNacimientoLayout.setError(getString(R.string.error_seleccionar_fecha));
            return false;
        }
        binding.etFechaNacimientoLayout.setError(null);
        return true;
    }


    /**
     * Valida el formato de los nombres
     */
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

    /**
     * Valida el formato de los apellidos
     */
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

    /**
     * Valida el formato del email
     */
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

    /**
     * Valida el formato del teléfono (9 dígitos comenzando con 9)
     */
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

    /**
     * Valida el formato del DNI (8 dígitos)
     */
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

    /**
     * Valida que la dirección no esté vacía
     */
    private boolean validateDireccion(String direccion) {
        if (direccion.trim().isEmpty()) {
            binding.etDireccionLayout.setError(getString(R.string.error_ingresa_direccion));
            return false;
        }
        binding.etDireccionLayout.setError(null);
        return true;
    }


    /**
     * Valida las contraseñas y su coincidencia
     */
    private boolean validatePasswords() {
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        // Validar contraseña principal
        if (password.isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.error_ingresa_password));
            return false;
        } else if (password.length() < 6) {
            binding.etPasswordLayout.setError(getString(R.string.error_password_corto));
            return false;
        } else {
            binding.etPasswordLayout.setError(null);
        }

        // Validar confirmación de contraseña
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar recursos
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        binding = null;
    }
}