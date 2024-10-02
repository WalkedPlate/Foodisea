package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodisea.databinding.ActivityAdminResDetallesProductoBinding;

public class AdminResDetallesProductoActivity extends AppCompatActivity {
    ActivityAdminResDetallesProductoBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminResDetallesProductoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        binding.btnBack.setOnClickListener(view -> {
            Intent carta = new Intent(this, AdminResCartaActivity.class);
            startActivity(carta);
        });

    }
}