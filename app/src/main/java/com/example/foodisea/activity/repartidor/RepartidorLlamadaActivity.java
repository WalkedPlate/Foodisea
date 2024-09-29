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
import com.example.foodisea.databinding.ActivityRepartidorLlamadaBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class RepartidorLlamadaActivity extends AppCompatActivity {

    private BottomSheetBehavior<View> bottomSheetBehavior;
    private ActivityRepartidorLlamadaBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Utilizar ViewBinding
        binding = ActivityRepartidorLlamadaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar insets del sistema para la ventana
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar BottomSheet usando ViewBinding
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);

        // Configurar el comportamiento inicial del BottomSheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Configurar callbacks para cambios de estado del BottomSheet
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("BottomSheet", "Estado: Colapsado");
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        Log.d("BottomSheet", "Estado: Expandido");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        Log.d("BottomSheet", "Estado: Arrastrando");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        Log.d("BottomSheet", "Estado: Ajustándose");
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        Log.d("BottomSheet", "Estado: Oculto");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheet", "Desplazamiento: " + slideOffset);
                // Puedes animar vistas aquí basándote en el desplazamiento
            }
        });

        // Configurar los botones
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Utilizar binding para referenciar los botones
        binding.muteButton.setOnClickListener(v -> {
            // Funcionalidad para silenciar
            Log.d("ButtonAction", "Mute button clicked");
        });

        binding.endCallButton.setOnClickListener(v -> {
            // Funcionalidad para finalizar la llamada
            Log.d("ButtonAction", "End call button clicked");
        });

        binding.speakerButton.setOnClickListener(v -> {
            // Funcionalidad para activar el altavoz
            Log.d("ButtonAction", "Speaker button clicked");
        });
    }
}