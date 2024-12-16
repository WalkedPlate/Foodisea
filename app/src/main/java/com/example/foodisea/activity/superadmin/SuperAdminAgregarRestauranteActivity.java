package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminAgregarRestauranteBinding;
import com.example.foodisea.databinding.ActivitySuperAdminSolicitudesRepartidorBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.repository.RestauranteRepository;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class SuperAdminAgregarRestauranteActivity extends AppCompatActivity {
    ActivitySuperAdminAgregarRestauranteBinding binding;
    RestauranteRepository restauranteRepository;
    UsuarioRepository usuarioRepository;
    List<AdministradorRestaurante> listaAdministradoresLibres = new ArrayList<>();

    private String idAdministradorSeleccionado = null;
    private AutoCompleteTextView spinnerAdministrador;
    private static final int PICK_IMAGES_REQUEST = 1;
    private List<String> listaUrlsImagenes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupListeners();

        restauranteRepository = new RestauranteRepository();

        // Configura el botón de guardar restaurante
        binding.btnCrearRestaurante.setOnClickListener(v -> {
            // Deshabilitar el botón inmediatamente para prevenir múltiples clicks
            binding.btnCrearRestaurante.setEnabled(false);

            String nombre = binding.etNombreRes.getText().toString();
            String descripcion = binding.etDescription.getText().toString();
            String direccion = binding.etDireccion.getText().toString();
            String telefono = binding.etTelefono.getText().toString();



            if (!nombre.isEmpty() && !telefono.isEmpty() && !descripcion.isEmpty() && !direccion.isEmpty() && idAdministradorSeleccionado != null) {
                Restaurante restaurante = new Restaurante(
                        nombre,
                        direccion,
                        telefono,
                        new ArrayList<>(), // Establecer estado inicial
                        5.0,
                        listaUrlsImagenes,
                        idAdministradorSeleccionado,
                        descripcion
                );

                List<String> listaCategoria = new ArrayList<>();
                listaCategoria.add("Categoria 1");
                listaCategoria.add("Categoria 2");
                restaurante.setCategorias(listaCategoria);

                restauranteRepository.createRestaurante(restaurante)
                        .addOnSuccessListener(documentReference -> {
                            String restauranteId = documentReference.getId();
                            usuarioRepository.asignarRestauranteAAdministrador(idAdministradorSeleccionado, restauranteId)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirestoreSuccess", "Restaurante y administrador actualizados exitosamente");
                                        Toast.makeText(this, "Restaurante guardado exitosamente", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreError", "Error al actualizar el administrador", e);
                                        Toast.makeText(this, "Error al actualizar el administrador", Toast.LENGTH_SHORT).show();
                                        // Reactivar el botón en caso de error
                                        binding.btnCrearRestaurante.setEnabled(true);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirestoreError", "Error al agregar restaurante", e);
                            Toast.makeText(this, "Error al guardar el restaurante", Toast.LENGTH_SHORT).show();
                            // Reactivar el botón en caso de error
                            binding.btnCrearRestaurante.setEnabled(true);
                        });
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos y selecciona un administrador", Toast.LENGTH_SHORT).show();
                // Reactivar el botón si la validación falla
                binding.btnCrearRestaurante.setEnabled(true);
            }
        });




        spinnerAdministrador = findViewById(R.id.spinnerAdministrador);

        usuarioRepository = new UsuarioRepository();

        // Cargar los administradores desde Firestore
        cargarAdministradores();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Muestra la imagen seleccionada
                binding.imgProfile.setImageURI(selectedImageUri);
                binding.btnEliminarImagen.setVisibility(View.VISIBLE);

                // Subir imagen a Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child("restaurantes/" + binding.etNombreRes.getText()+"/imagen_"+System.currentTimeMillis());
                storageRef.putFile(selectedImageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            // Obtener el URL de descarga
                            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String urlImagen = uri.toString();
                                listaUrlsImagenes.add(urlImagen); // Añadir URL a la lista
                                Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show();
                                Log.e("FirebaseError", "Error al obtener URL", e);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                            Log.e("FirebaseError", "Error al subir imagen", e);
                        });
            }
        }
    }


    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySuperAdminAgregarRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    /**
     * Configura los insets de la ventana
     */
    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnImagen.setOnClickListener(v -> {
            // Abre el selector de imágenes
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 100);
        });
        // Eliminar la imagen
        binding.btnEliminarImagen.setOnClickListener(v -> {
            binding.imgProfile.setImageDrawable(null); // Eliminar imagen
            binding.btnEliminarImagen.setVisibility(View.GONE); // Esconde el botón de eliminar
        });

    }


    private void cargarAdministradores(){
        usuarioRepository.getAdministradoresLibres()
                .addOnSuccessListener(listaAdministradores -> {
                    listaAdministradoresLibres = listaAdministradores;
                    configurarSpinner(); // Configurar el spinner después de cargar los datos
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error al cargar administradores", e);
                });
    }


    private void configurarSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                obtenerNombresDeAdministradores(listaAdministradoresLibres) // Extrae los nombres
        );
        spinnerAdministrador.setAdapter(adapter);

        // Configurar acción al seleccionar un elemento
        spinnerAdministrador.setOnItemClickListener((parent, view, position, id) -> {
            // Obtener el administrador seleccionado
            AdministradorRestaurante seleccionado = listaAdministradoresLibres.get(position);

            // Guardar el ID del administrador seleccionado
            idAdministradorSeleccionado = seleccionado.getId();

            // Mostrar mensaje para depuración
            Toast.makeText(this, "Seleccionaste: " + seleccionado.getNombres() + " (ID: " + idAdministradorSeleccionado + ")", Toast.LENGTH_SHORT).show();
        });
    }


    private List<String> obtenerNombresDeAdministradores(List<AdministradorRestaurante> administradores) {
        List<String> nombres = new ArrayList<>();
        for (AdministradorRestaurante administrador : administradores) {
            nombres.add(administrador.getNombres() +" "+ administrador.getApellidos()); // Extrae el nombre
        }
        return nombres;
    }


}
