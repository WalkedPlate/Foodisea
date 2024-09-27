package com.example.foodisea;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.cliente.ClienteMainActivity;
import com.example.foodisea.activity.cliente.ClienteRestauranteActivity;
import com.example.foodisea.activity.repartidor.RepartidorMainActivity;
import com.example.foodisea.activity.superadmin.SuperadminMainActivity;
import com.example.foodisea.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Inicializando ViewBinding
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        // Botón para Superadmin
        binding.btnRole1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SuperadminMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Cliente
        binding.btnRole2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClienteMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Repartidor
        binding.btnRole3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RepartidorMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Administrador de Restaurante
        binding.btnRole4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdminResHomeActivity.class);
                startActivity(intent);
            }
        });



    }
}