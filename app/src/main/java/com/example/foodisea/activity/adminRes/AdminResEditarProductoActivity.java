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

import com.example.foodisea.R;
import com.example.foodisea.adapter.adminRes.ImagePreviewAdapter;
import com.example.foodisea.databinding.ActivityAdminResEditarProductoBinding;
import com.example.foodisea.model.Producto;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminResEditarProductoActivity extends AppCompatActivity {

    private ActivityAdminResEditarProductoBinding binding;
    private ProductoRepository productoRepository;
    private List<Uri> imagenUris = new ArrayList<>();
    private List<String> imagenesActuales = new ArrayList<>();
    private static final int PICK_IMAGES_REQUEST = 1;
    private ImagePreviewAdapter imagePreviewAdapter;
    private String productoId;
    private Producto productoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResEditarProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener ID del producto de los extras
        productoId = getIntent().getStringExtra("PRODUCTO_ID");
        if (productoId == null) {
            Toast.makeText(this, "Error: ID de producto no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupRecyclerView();
        setupListeners();
        cargarDatosProducto();
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
    }

    private void setupRecyclerView() {
        imagePreviewAdapter = new ImagePreviewAdapter(
                imagenUris,
                imagenesActuales,
                new ImagePreviewAdapter.OnImageDeleteListener() {
                    @Override
                    public void onLocalImageDelete(Uri uri) {
                        imagenUris.remove(uri);
                        imagePreviewAdapter.notifyDataSetChanged();
                        updateImageCountText();
                    }

                    @Override
                    public void onExistingImageDelete(String url) {
                        imagenesActuales.remove(url);
                        imagePreviewAdapter.notifyDataSetChanged();
                        updateImageCountText();
                    }
                }
        );

        binding.rvImagePreview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvImagePreview.setAdapter(imagePreviewAdapter);
    }

    private void cargarDatosProducto() {
        mostrarProgress(true);
        binding.tvProgressStatus.setText("Cargando datos del producto...");

        productoRepository.getProductoById(productoId)
                .addOnSuccessListener(producto -> {
                    productoActual = producto;
                    binding.etNombre.setText(producto.getNombre());
                    binding.etDescripcion.setText(producto.getDescripcion());
                    binding.etPrecio.setText(String.valueOf(producto.getPrecio()));
                    binding.spinnerCategoria.setText(producto.getCategoria(), false);

                    // Cargar imágenes existentes
                    imagenesActuales.addAll(producto.getImagenes());
                    imagePreviewAdapter.notifyDataSetChanged();
                    updateImageCountText();

                    mostrarProgress(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar producto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
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

        binding.btnEditarProducto.setOnClickListener(v -> {
            if (validarFormulario()) {
                mostrarDialogoConfirmacion();
            }
        });

        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void mostrarDialogoConfirmacion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar cambios")
                .setMessage("¿Está seguro de guardar los cambios en este producto?")
                .setPositiveButton("Guardar", (dialog, which) -> actualizarProducto())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean validarFormulario() {
        int totalImagenes = imagenUris.size() + imagenesActuales.size();
        if (totalImagenes < 2) {
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

    private void actualizarProducto() {
        if (!validarFormulario()) return;

        mostrarProgress(true);
        binding.tvProgressStatus.setText("Preparando actualización...");

        String nombre = binding.etNombre.getText().toString().trim();
        String descripcion = binding.etDescripcion.getText().toString().trim();
        double precio = Double.parseDouble(binding.etPrecio.getText().toString());
        String categoria = binding.spinnerCategoria.getText().toString();

        // Mantener las URLs de las imágenes existentes y agregar las nuevas
        List<String> nuevasImagenes = new ArrayList<>(imagenesActuales);

        // Si hay nuevas imágenes para subir
        if (!imagenUris.isEmpty()) {
            ProductoRepository.UploadProgressListener progressListener = new ProductoRepository.UploadProgressListener() {
                @Override
                public void onProgress(int imageIndex, int totalImages, double progress) {
                    int totalProgress = (int) ((imageIndex - 1) * 100 + progress) / totalImages;
                    actualizarProgreso(
                            "Subiendo imagen " + imageIndex + " de " + totalImages,
                            totalProgress
                    );
                }

                @Override
                public void onImageUploaded(int completedUploads, int totalUploads, String imageUrl) {
                    nuevasImagenes.add(imageUrl);
                    if (completedUploads == totalUploads) {
                        actualizarProgreso("Guardando cambios del producto...", 95);
                        // Actualizar el producto con todas las imágenes
                        actualizarDatosProducto(nombre, descripcion, precio, categoria, nuevasImagenes);
                    }
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(AdminResEditarProductoActivity.this, message, Toast.LENGTH_LONG).show();
                    mostrarProgress(false);
                }
            };

            // Subir las nuevas imágenes
            productoRepository.subirImagenesProducto(productoId, imagenUris, progressListener);
        } else {
            // Si no hay nuevas imágenes, actualizar solo los datos
            actualizarDatosProducto(nombre, descripcion, precio, categoria, nuevasImagenes);
        }
    }

    private void actualizarDatosProducto(String nombre, String descripcion, double precio,
                                         String categoria, List<String> imagenes) {
        Producto productoActualizado = new Producto();
        productoActualizado.setId(productoId);
        productoActualizado.setNombre(nombre);
        productoActualizado.setDescripcion(descripcion);
        productoActualizado.setPrecio(precio);
        productoActualizado.setCategoria(categoria);
        productoActualizado.setImagenes(imagenes);
        productoActualizado.setRestauranteId(productoActual.getRestauranteId());
        productoActualizado.setOutOfStock(productoActual.isOutOfStock());

        productoRepository.actualizarProducto(productoActualizado)
                .addOnSuccessListener(aVoid -> {
                    actualizarProgreso("¡Producto actualizado exitosamente!", 100);
                    Toast.makeText(this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("PRODUCTO_ID", productoId);
                    setResult(RESULT_OK, resultIntent);
                    new Handler().postDelayed(this::finish, 1000);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar producto: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    mostrarProgress(false);
                });
    }

    private void mostrarProgress(boolean mostrar) {
        binding.layoutProgress.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.btnEditarProducto.setEnabled(!mostrar);
        if (!mostrar) {
            binding.progressBar.setProgress(0);
            binding.tvProgressPercentage.setText("0%");
            binding.tvProgressStatus.setText("Actualizando producto...");
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
        int total = imagenUris.size() + imagenesActuales.size();
        binding.tvImageCount.setText("Imágenes seleccionadas: " + total);
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