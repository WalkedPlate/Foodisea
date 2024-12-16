package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.superAdmin.LogsAdapter;
import com.example.foodisea.databinding.ActivitySuperAdminLogsBinding;
import com.example.foodisea.item.LogItem;
import com.example.foodisea.manager.LogManager;
import com.example.foodisea.model.AppLog;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SuperAdminLogsActivity extends AppCompatActivity {

    ActivitySuperAdminLogsBinding binding;
    private LogsAdapter logsAdapter;
    private List<AppLog> logsList;
    private LogManager logManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySuperAdminLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar datos iniciales
        loadInitialData();


        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });
    }

    private void initRecyclerView() {
        //logsList = new ArrayList<>();
        logsAdapter = new LogsAdapter(this, logsList);

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(logsAdapter);

        // Opcional: Añadir decoración para separar items
        binding.rvLogs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadInitialData() {

        logManager = new LogManager();

        // Recuperar logs
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(Duration.ofDays(240));

        logManager.fetchLogs(startDate, endDate)
                .addOnSuccessListener(logs -> {
                    // Manejar los logs recuperados
                    logsList = logs;

                    // Inicializar RecyclerView
                    initRecyclerView();

                    // Notificar al adaptador que los datos han cambiado
                    logsAdapter.updateLogs(logsList);
                })
                .addOnFailureListener(e -> {
                    // Manejar errores
                    Toast.makeText(this, "Error al recuperar logs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}