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
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class RepartidorDeliveryMapActivity extends AppCompatActivity {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repartidor_delivery_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Obtener la vista del BottomSheet desde el XML
        View bottomSheet = findViewById(R.id.bottomSheet);

        // Instanciar el BottomSheetBehavior desde la vista del BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Configurar el comportamiento del BottomSheet
        bottomSheetBehavior.setPeekHeight(200); // Altura visible en estado colapsado
        bottomSheetBehavior.setHideable(false); // No permitir ocultar completamente el BottomSheet

        // Ejemplo: Cambiar el estado del BottomSheet cuando se haga click en un botón
        TextView pickupButton = findViewById(R.id.pickupButton);
        pickupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED); // Expandir el BottomSheet
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Colapsar el BottomSheet
                }
            }
        });

        //Botones:

        setupButtonListeners();


    }


    private void setupButtonListeners() {
        Intent intent = getIntent();
        String customerName = intent.getStringExtra("customerName");


        ImageButton chatButton = findViewById(R.id.chatButton);
        ImageButton phoneButton = findViewById(R.id.phoneButton);


        chatButton.setOnClickListener(v -> {
            Intent intentToCallView = new Intent(RepartidorDeliveryMapActivity.this, RepartidorChatActivity.class);
            intentToCallView.putExtra("customerName",customerName);
            startActivity(intentToCallView);

        });

        phoneButton.setOnClickListener(v -> {
            Intent intentToChatView = new Intent(RepartidorDeliveryMapActivity.this, RepartidorLlamadaActivity.class);
            intentToChatView.putExtra("customerName",customerName);
            startActivity(intentToChatView);
        });


    }



}