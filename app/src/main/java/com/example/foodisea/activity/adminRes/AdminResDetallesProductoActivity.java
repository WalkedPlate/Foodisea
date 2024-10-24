package com.example.foodisea.activity.adminRes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodisea.adapter.adminRes.ImageAdapter;
import com.example.foodisea.databinding.ActivityAdminResDetallesProductoBinding;
import com.example.foodisea.repository.ProductoRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

public class AdminResDetallesProductoActivity extends AppCompatActivity {
    private ActivityAdminResDetallesProductoBinding binding;
    private ProductoRepository productoRepository;
    private String productoId;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResDetallesProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productoRepository = new ProductoRepository();
        productoId = getIntent().getStringExtra("PRODUCTO_ID");

        if (productoId == null) {
            Toast.makeText(this, "Error al cargar el producto", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupViews();
        setupViewPager();
        loadProductData();
        setupButtons();
    }

    private void setupViews() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        imageAdapter = new ImageAdapter();
        binding.viewPagerImages.setAdapter(imageAdapter);

        // Conectar ViewPager2 con TabLayout para los indicadores
        new TabLayoutMediator(
                binding.tabLayoutIndicator,
                binding.viewPagerImages,
                (tab, position) -> {} // No necesitamos títulos para los tabs
        ).attach();

        // Actualizar el contador de imágenes
        binding.viewPagerImages.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Actualizar el contador
                updateImageCounter(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
    }

    private void updateImageCounter(int position) {
        if (imageAdapter != null && imageAdapter.getItemCount() > 0) {
            binding.imageCounter.setText(String.format(Locale.getDefault(), "%d/%d",
                    position + 1, imageAdapter.getItemCount()));
        }
    }

    private void loadProductData() {
        showLoading(true);
        productoRepository.getProductoById(productoId)
                .addOnSuccessListener(producto -> {
                    binding.nombreProducto.setText(producto.getNombre());
                    binding.tvDescripcionProduct.setText(producto.getDescripcion());
                    binding.productDetailPrice.setText(String.format("S/. %.2f", producto.getPrecio()));

                    // Cargar imágenes en el ViewPager
                    if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
                        imageAdapter.setImageUrls(producto.getImagenes());
                    }
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar el producto: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void setupButtons() {
        binding.btnEliminar.setOnClickListener(v -> mostrarAlertaEliminar());
        binding.btnEditar.setOnClickListener(v -> {
            // TODO: Implementar edición
            Toast.makeText(this, "Función de editar en desarrollo", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarAlertaEliminar() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("¿Estás seguro de eliminar el producto?");
        alertDialog.setMessage("Si eliminas este producto será de manera permanente");
        alertDialog.setPositiveButton("ELIMINAR",
                (dialogInterface, i) -> eliminarProducto());
        alertDialog.setNegativeButton("CANCELAR",
                (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void eliminarProducto() {
        showLoading(true);
        productoRepository.eliminarProducto(productoId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Producto eliminado exitosamente",
                            Toast.LENGTH_SHORT).show();
                    // Regresar a la actividad de la carta
                    Intent carta = new Intent(this, AdminResCartaActivity.class);
                    startActivity(carta);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al eliminar el producto: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void showLoading(boolean show) {
        binding.progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnEditar.setEnabled(!show);
        binding.btnEliminar.setEnabled(!show);
    }


}