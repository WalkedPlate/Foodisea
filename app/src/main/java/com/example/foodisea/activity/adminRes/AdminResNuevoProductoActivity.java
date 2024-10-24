package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.adapter.adminRes.ImagePreviewAdapter;
import com.example.foodisea.databinding.ActivityAdminResNuevoProductoBinding;
import com.example.foodisea.model.Producto;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminResNuevoProductoActivity extends AppCompatActivity {

    private ActivityAdminResNuevoProductoBinding binding;
    private ProductoRepository productoRepository;
    private List<Uri> imagenUris = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 1;
    private ImagePreviewAdapter imagePreviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResNuevoProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();
        setupRecyclerView();
        setupListeners();
    }

    private void setupUI() {
        productoRepository = new ProductoRepository();

        // Configurar AutoCompleteTextView para categorías
        String[] categorias = getResources().getStringArray(R.array.categorias_productos);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        binding.spinnerCategoria.setAdapter(adapter);

        // Establecer la primera categoría por defecto
        if (categorias.length > 0) {
            binding.spinnerCategoria.setText(categorias[0], false);
        }
    }


    private void setupRecyclerView() {
        imagePreviewAdapter = new ImagePreviewAdapter(imagenUris, uri -> {
            // Manejar eliminación de imagen
            imagenUris.remove(uri);
            imagePreviewAdapter.notifyDataSetChanged();
            updateImageCountText();
        });

        binding.rvImagePreview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvImagePreview.setAdapter(imagePreviewAdapter);
    }

    private void setupListeners() {
        binding.btnSelectImages.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(
                    Intent.createChooser(intent, "Seleccionar imágenes"),
                    PICK_IMAGES_REQUEST
            );
        });

        binding.btnCrearProducto.setOnClickListener(v -> {
            if (validarFormulario()) {
                mostrarDialogoConfirmacion();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void mostrarDialogoConfirmacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar creación")
                .setMessage("¿Está seguro de crear este producto?")
                .setPositiveButton("Crear", (dialog, which) -> crearProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean validarFormulario() {
        if (imagenUris.size() < 2) {
            Toast.makeText(this, "Se requieren mínimo 2 imágenes", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.etNombre.getText().toString().trim().isEmpty()) {
            binding.etNombre.setError("El nombre es requerido");
            return false;
        }

        if (binding.etPrecio.getText().toString().trim().isEmpty()) {
            binding.etPrecio.setError("El precio es requerido");
            return false;
        }

        try {
            Double.parseDouble(binding.etPrecio.getText().toString());
        } catch (NumberFormatException e) {
            binding.etPrecio.setError("Ingrese un precio válido");
            return false;
        }

        if (binding.etDescripcion.getText().toString().trim().isEmpty()) {
            binding.etDescripcion.setError("La descripción es requerida");
            return false;
        }

        String categoria = binding.spinnerCategoria.getText().toString();
        if (categoria.isEmpty()) {
            binding.spinnerCategoria.setError("La categoría es requerida");
            return false;
        }

        return true;
    }

    private void crearProducto() {
        if (!validarFormulario()) return;

        mostrarProgress(true);
        binding.tvProgressStatus.setText("Preparando subida de imágenes...");

        String nombre = binding.etNombre.getText().toString().trim();
        String descripcion = binding.etDescripcion.getText().toString().trim();
        double precio = Double.parseDouble(binding.etPrecio.getText().toString());
        String categoria = binding.spinnerCategoria.getText().toString();
        String restauranteId = "ID_DEL_RESTAURANTE"; // Obtener de preferencias o intent

        // Crear lista de tareas de subida
        List<UploadTask> uploadTasks = new ArrayList<>();
        List<String> imageUrls = new ArrayList<>();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("productos")
                .child(UUID.randomUUID().toString());

        // Preparar todas las subidas
        for (int i = 0; i < imagenUris.size(); i++) {
            Uri imageUri = imagenUris.get(i);
            String fileName = "imagen_" + i + ".jpg";
            StorageReference imageRef = storageRef.child(fileName);
            uploadTasks.add(imageRef.putFile(imageUri));
        }

        // Contador para el progreso total
        AtomicInteger completedUploads = new AtomicInteger(0);
        int totalUploads = uploadTasks.size();

        // Iniciar subidas y monitorear progreso
        for (int i = 0; i < uploadTasks.size(); i++) {
            UploadTask uploadTask = uploadTasks.get(i);
            final int imageIndex = i;

            uploadTask.addOnProgressListener(snapshot -> {
                double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                int totalProgress = (int) ((completedUploads.get() * 100 + progress) / totalUploads);
                actualizarProgreso(
                        "Subiendo imagen " + (imageIndex + 1) + " de " + totalUploads,
                        totalProgress
                );
            }).addOnSuccessListener(taskSnapshot -> {
                completedUploads.incrementAndGet();
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());

                    // Si todas las imágenes se subieron, crear el producto
                    if (imageUrls.size() == totalUploads) {
                        actualizarProgreso("Guardando información del producto...", 95);

                        // Crear objeto Producto
                        Producto producto = new Producto();
                        producto.setId(UUID.randomUUID().toString());
                        producto.setNombre(nombre);
                        producto.setDescripcion(descripcion);
                        producto.setPrecio(precio);
                        producto.setCategoria(categoria);
                        producto.setRestauranteId(restauranteId);
                        producto.setImagenes(imageUrls);
                        producto.setOutOfStock(false);

                        // Guardar en Firestore
                        FirebaseFirestore.getInstance()
                                .collection("productos")
                                .document(producto.getId())
                                .set(producto)
                                .addOnSuccessListener(aVoid -> {
                                    actualizarProgreso("¡Producto creado exitosamente!", 100);
                                    Toast.makeText(this, "Producto creado exitosamente", Toast.LENGTH_SHORT).show();
                                    new Handler().postDelayed(this::finish, 1000);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    mostrarProgress(false);
                                });
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                mostrarProgress(false);
            });
        }
    }



    private void mostrarProgress(boolean mostrar) {
        binding.layoutProgress.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnCrearProducto.setEnabled(!mostrar);
        if (!mostrar) {
            binding.progressBar.setProgress(0);
            binding.tvProgressPercentage.setText("0%");
            binding.tvProgressStatus.setText("Subiendo imágenes...");
        }
    }

    private void actualizarProgreso(String status, int progress) {
        runOnUiThread(() -> {
            binding.tvProgressStatus.setText(status);
            binding.progressBar.setProgress(progress);
            binding.tvProgressPercentage.setText(progress + "%");
        });
    }

    private void updateImageCountText() {
        binding.tvImageCount.setText("Imágenes seleccionadas: " + imagenUris.size());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data.getClipData() != null) {
                // Múltiples imágenes seleccionadas
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imagenUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Una sola imagen seleccionada
                imagenUris.add(data.getData());
            }

            imagePreviewAdapter.notifyDataSetChanged();
            updateImageCountText();
        }
    }
}