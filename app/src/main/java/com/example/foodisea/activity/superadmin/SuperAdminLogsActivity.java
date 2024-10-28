package com.example.foodisea.activity.superadmin;

import android.content.Intent;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.List;

public class SuperAdminLogsActivity extends AppCompatActivity {

    ActivitySuperAdminLogsBinding binding;
    private LogsAdapter logsAdapter;
    private List<LogItem> logsList;

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

        // Inicializar RecyclerView
        initRecyclerView();

        // Cargar datos iniciales
        loadInitialData();

        //funcion de botones:
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        binding.btnHome.setOnClickListener(v -> {
            Intent home = new Intent(this, SuperadminMainActivity.class);
            startActivity(home);
        });
    }

    private void initRecyclerView() {
        logsList = new ArrayList<>();
        logsAdapter = new LogsAdapter(this, logsList);

        binding.rvLogs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLogs.setAdapter(logsAdapter);

        // Opcional: Añadir decoración para separar items
        binding.rvLogs.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void loadInitialData() {
        List<LogItem> initialLogs = new ArrayList<>();

        // Eventos de inicio de sesión
        initialLogs.add(new LogItem(
                "Administrador principal ha iniciado sesión",
                "Hace 2 minutos",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Usuario 'gerente_tienda1' ha cerrado sesión",
                "Hace 15 minutos",
                R.drawable.ic_log_notification
        ));

        // Eventos de gestión de usuarios
        initialLogs.add(new LogItem(
                "Nuevo usuario 'empleado123' registrado en el sistema",
                "Hace 30 minutos",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Permisos actualizados para usuario 'supervisor_ventas'",
                "Hace 45 minutos",
                R.drawable.ic_log_notification
        ));

        // Eventos de pedidos
        initialLogs.add(new LogItem(
                "Nuevo pedido #A789 creado - Total: $156.50",
                "Hace 1 hora",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Pedido #A785 marcado como entregado",
                "Hace 1.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de inventario
        initialLogs.add(new LogItem(
                "Stock actualizado: 'Hamburguesa Clásica' - 150 unidades",
                "Hace 2 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Alerta: Producto 'Papas Fritas' bajo stock (10 unidades)",
                "Hace 2.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de sistema
        initialLogs.add(new LogItem(
                "Backup automático del sistema completado",
                "Hace 3 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Actualización de precios completada - 25 productos afectados",
                "Hace 3.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de promociones
        initialLogs.add(new LogItem(
                "Nueva promoción creada: '2x1 en Bebidas' - Activa",
                "Hace 4 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Promoción 'Martes de Hamburguesas' finalizada",
                "Hace 4.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de reportes
        initialLogs.add(new LogItem(
                "Reporte mensual de ventas generado",
                "Hace 5 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Análisis de satisfacción del cliente actualizado",
                "Hace 5.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de configuración
        initialLogs.add(new LogItem(
                "Configuración de impresora actualizada - Local principal",
                "Hace 6 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Nuevo método de pago agregado: 'Yape'",
                "Hace 6.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de mantenimiento
        initialLogs.add(new LogItem(
                "Mantenimiento programado completado",
                "Hace 7 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Limpieza de caché del sistema realizada",
                "Hace 7.5 horas",
                R.drawable.ic_log_notification
        ));

        // Eventos de alertas
        initialLogs.add(new LogItem(
                "Alerta: Intento de acceso no autorizado detectado",
                "Hace 8 horas",
                R.drawable.ic_log_notification
        ));

        initialLogs.add(new LogItem(
                "Advertencia: Tiempo de respuesta del servidor elevado",
                "Hace 8.5 horas",
                R.drawable.ic_log_notification
        ));

        // Actualizar el adapter con los datos
        logsList.clear();
        logsList.addAll(initialLogs);
        logsAdapter.notifyDataSetChanged();
    }
}