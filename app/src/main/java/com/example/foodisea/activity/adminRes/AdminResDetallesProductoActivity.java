package com.example.foodisea.activity.adminRes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
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

        binding.btnEliminar.setOnClickListener(view -> {
            mostrarAlerta(view);
        });

    }

    public void mostrarAlerta(View view){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("¿Estas seguro de eliminar el producto?");
        alertDialog.setMessage("Si eliminas este producto sera de manera permanente");
        alertDialog.setPositiveButton("ELIMINAR",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent carta = new Intent(AdminResDetallesProductoActivity.this, AdminResCartaActivity.class);
                        startActivity(carta);
                    }
                });
        alertDialog.setNegativeButton("CANCELAR",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertDialog.show();
    }
}