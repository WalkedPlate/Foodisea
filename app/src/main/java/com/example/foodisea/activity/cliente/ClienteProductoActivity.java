package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityClienteProductoBinding;

public class ClienteProductoActivity extends AppCompatActivity {

    ActivityClienteProductoBinding binding;
    private int quantity = 0; // Variable para mantener la cantidad


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout usando View Binding
        binding = ActivityClienteProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Obtener los datos del intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("productName");
        String descripcion = intent.getStringExtra("productDescription");
        double price = intent.getDoubleExtra("productPrice", 0);
        String imageResource = intent.getStringExtra("productImage");

        // Configurar la vista con los datos recibidos
        binding.productDetailName.setText(name);
        binding.productDetailPrice.setText("S/." + price);
        binding.tvDescripcionProduct.setText(descripcion);
        binding.productDetailImage.setImageResource(getResources().getIdentifier(imageResource, "drawable", getPackageName()));


        // funcion de los botones
        binding.btnBack.setOnClickListener(v -> finish());

        // Botón de carrito de compras
        binding.btnCart.setOnClickListener(v -> {
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });


        // Botones de cantidad
        // Configurar el listener para disminuir la cantidad
        binding.btnMinus.setOnClickListener(v -> {
            if (quantity > 0) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });

        // Configurar el listener para aumentar la cantidad
        binding.btnPlus.setOnClickListener(v -> {
            quantity++;
            binding.tvQuantity.setText(String.valueOf(quantity));
        });
    }
}