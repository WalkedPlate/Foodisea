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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Botón para Superadmin
        Button btnRole1 = findViewById(R.id.btnRole1);
        btnRole1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SuperadminMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Cliente
        Button btnRole2 = findViewById(R.id.btnRole2);
        btnRole2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ClienteMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Repartidor
        Button btnRole3 = findViewById(R.id.btnRole3);
        btnRole3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RepartidorMainActivity.class);
                startActivity(intent);
            }
        });

        // Botón para Administrador de Restaurante
        Button btnRole4 = findViewById(R.id.btnRole4);
        btnRole4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AdminResHomeActivity.class);
                startActivity(intent);
            }
        });



    }
}