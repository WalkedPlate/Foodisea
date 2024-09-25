package com.example.foodisea.activity.repartidor;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class RepartidorLlamadaActivity extends AppCompatActivity {

    private BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repartidor_llamada);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set the initial state
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Set up callback
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("BottomSheet", "State Collapsed");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d("BottomSheet", "State Expanded");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d("BottomSheet", "State Dragging");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d("BottomSheet", "State Settling");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d("BottomSheet", "State Hidden");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheet", "Sliding Offset: " + slideOffset);
                // You can animate views here based on slideOffset
            }
        });

        //Botones
        setupButtonListeners();

    }

    private void setupButtonListeners() {
        ImageButton muteButton = findViewById(R.id.muteButton);
        ImageButton endCallButton = findViewById(R.id.endCallButton);
        ImageButton speakerButton = findViewById(R.id.speakerButton);

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle mute functionality
            }
        });

        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle end call functionality
            }
        });

        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle speaker functionality
            }
        });
    }



}