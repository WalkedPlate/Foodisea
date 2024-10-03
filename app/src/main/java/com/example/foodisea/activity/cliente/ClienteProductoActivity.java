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

public class ClienteProductoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_producto);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView productImage = findViewById(R.id.productDetailImage);
        TextView productName = findViewById(R.id.productDetailName);
        TextView productDescription = findViewById(R.id.tvDescripcionProduct);
        TextView productPrice = findViewById(R.id.productDetailPrice);


        // Obtener los datos del intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("productName");
        String descripcion = intent.getStringExtra("productDescription");
        double price = intent.getDoubleExtra("productPrice", 0);
        String imageResource = intent.getStringExtra("productImage");

        // Configurar la vista con los datos recibidos
        productName.setText(name);
        productPrice.setText("S/." + price);
        productDescription.setText(descripcion);
        productImage.setImageResource(getResources().getIdentifier(imageResource, "drawable", getPackageName()));


        // funcion de los botones
        Button btnBack = findViewById(R.id.btnBack);
        Button btnCart = findViewById(R.id.btnCart);
        TextView tvCartItemCount = findViewById(R.id.tvCartItemCount);

        btnBack.setOnClickListener(v -> {
            // Acción para regresar
            finish(); // Cierra la actividad actual y regresa
        });

        btnCart.setOnClickListener(v -> {
            // Acción para ir al carrito de compras
            Intent carrito = new Intent(this, ClienteCarritoActivity.class);
            startActivity(carrito);
        });
    }
}