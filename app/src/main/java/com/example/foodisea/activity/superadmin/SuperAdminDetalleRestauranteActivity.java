package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivitySuperAdminDetalleRestauranteBinding;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SuperAdminDetalleRestauranteActivity extends AppCompatActivity {

    ActivitySuperAdminDetalleRestauranteBinding binding;
    UsuarioRepository usuarioRepository;
    List<AdministradorRestaurante> listaAdministradores = new ArrayList<>();
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();


        Intent intent = getIntent();
        String restauranteId = intent.getStringExtra("restauranteId");
        String nombreRestaurante = intent.getStringExtra("name");
        String descripcion = intent.getStringExtra("descripcion");
        String administradorId = intent.getStringExtra("administradorId");
        String direccion = intent.getStringExtra("direccion");
        String telefono = intent.getStringExtra("telefono");
        String imagen = intent.getStringExtra("image");

        setupListeners(restauranteId);

        usuarioRepository = new UsuarioRepository();
        cargarListaAdministradores(administradorId);



        binding.nombreRestaurant.setText(nombreRestaurante);
        binding.descripcionRestaurant.setText(descripcion);
        binding.tvRestaurantAddress.setText(direccion);
        binding.tvRestaurantTelef.setText(telefono);

        // Cargar imagen desde Firebase Storage
        if (imagen != null && !imagen.isEmpty()) {
            String imageRef = imagen;
            if (imageRef.startsWith("https://")) {
                loadImageWithGlide(imageRef, binding.ivRestaurantImage);
            } else {
                StorageReference storageRef = storage.getReference().child(imageRef);
                storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> loadImageWithGlide(uri.toString(), binding.ivRestaurantImage))
                        .addOnFailureListener(e -> {
                            binding.ivRestaurantImage.setImageResource(R.drawable.placeholder_image);
                        });
            }
        } else {
            binding.ivRestaurantImage.setImageResource(R.drawable.placeholder_image);
        }
    }

    /**
     * Inicializa los componentes principales de la actividad
     */
    private void initializeComponents() {
        binding = ActivitySuperAdminDetalleRestauranteBinding.inflate(getLayoutInflater());
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
    private void setupListeners(String restauranteId) {
        binding.btnBack.setOnClickListener(v -> finish());
//        binding.btnEdit.setOnClickListener(v-> {
//            Intent editRest = new Intent(this, SuperAdminEditRestauranteActivity.class);
//            startActivity(editRest);
//        });
        binding.btnReporte.setOnClickListener(v -> {
            Intent reporteRest = new Intent(this, SuperAdminRestaurantesReportesActivity.class);
            reporteRest.putExtra("restauranteId",restauranteId);
            startActivity(reporteRest);
        });
    }

    private void cargarListaAdministradores(String administradorId){
        usuarioRepository.getAdministradoresRestaurantes()
                .addOnSuccessListener(administradores->{
                    listaAdministradores = administradores;
                    AdministradorRestaurante administradorRestaurante =obtenerAdministrador(administradorId);
                    if (administradorRestaurante != null) {
                        binding.nombreAdministrador.setText(administradorRestaurante.getNombres() + " " + administradorRestaurante.getApellidos());
                    } else {
                        Toast.makeText(this, "Administrador no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar administradores: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private AdministradorRestaurante obtenerAdministrador(String idAdministrador){
        for (AdministradorRestaurante administradorRestaurante :listaAdministradores){
            if(Objects.equals(administradorRestaurante.getId(), idAdministrador)){
                return administradorRestaurante;
            }
        }
        return null;
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }
}