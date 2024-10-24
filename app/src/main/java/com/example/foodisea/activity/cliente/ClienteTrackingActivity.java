package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class ClienteTrackingActivity extends AppCompatActivity {


    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LinearLayout deliveryInfoContainer;
    private Button btnOrderDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_tracking);

        // Referenciar el Bottom Sheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        TextView orderStatus = findViewById(R.id.order_status);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Referenciar otros elementos del Bottom Sheet
        deliveryInfoContainer = findViewById(R.id.delivery_info_container);
        btnOrderDetails = findViewById(R.id.btn_order_details);

        // Configurar el BottomSheetBehavior
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // Mostrar información del repartidor cuando se expande
                    deliveryInfoContainer.setVisibility(View.VISIBLE);
                    orderStatus.setVisibility(View.GONE);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // Ocultar información del repartidor cuando se colapsa
                    deliveryInfoContainer.setVisibility(View.GONE);
                    orderStatus.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Aquí puedes agregar animaciones o efectos mientras se desliza
            }
        });

        // Configurar el botón de detalles de compra
        btnOrderDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteCompraDetailsActivity.class);
            startActivity(intent);
        });

        // Inicialmente configurar el Bottom Sheet como colapsado
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    }
