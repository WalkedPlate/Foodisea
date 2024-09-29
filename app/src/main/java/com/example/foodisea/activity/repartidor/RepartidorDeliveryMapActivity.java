package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityRepartidorDeliveryMapBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class RepartidorDeliveryMapActivity extends AppCompatActivity {

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ActivityRepartidorDeliveryMapBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Usar ViewBinding en lugar de setContentView(R.layout.activity_repartidor_delivery_map)
        binding = ActivityRepartidorDeliveryMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar los insets de la ventana para adaptarse a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener la vista del BottomSheet desde el binding
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

        // Configurar el comportamiento del BottomSheet
        bottomSheetBehavior.setPeekHeight(200); // Altura visible en estado colapsado
        bottomSheetBehavior.setHideable(false); // No permitir ocultar completamente el BottomSheet

        // Ejemplo: Cambiar el estado del BottomSheet cuando se haga click en un botón
        binding.pickupButton.setOnClickListener(v -> {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Expandir el BottomSheet
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Colapsar el BottomSheet
            }
        });

        // Configurar los botones de chat y llamada
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        Intent intent = getIntent();
        String customerName = intent.getStringExtra("customerName");

        // Obtener botones desde el binding
        binding.chatButton.setOnClickListener(v -> {
            Intent intentToChatView = new Intent(RepartidorDeliveryMapActivity.this, RepartidorChatActivity.class);
            intentToChatView.putExtra("customerName", customerName);
            startActivity(intentToChatView);
        });

        // Botón llamada
        binding.phoneButton.setOnClickListener(v -> {
            Intent intentToCallView = new Intent(RepartidorDeliveryMapActivity.this, RepartidorLlamadaActivity.class);
            intentToCallView.putExtra("customerName", customerName);
            startActivity(intentToCallView);
        });

        // Botón cerrar
        binding.btnClose.setOnClickListener(v -> {
            finish();
        });

    }
}