package com.example.foodisea.activity.cliente;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.activity.login.LoginActivity;
import com.example.foodisea.adapter.cliente.RestauranteAdapter;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityClienteMainBinding;
import com.example.foodisea.dialog.LoadingDialog;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.RestauranteRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity principal para usuarios de tipo Cliente.
 * Muestra la lista de restaurantes y permite acceder a las funciones principales.
 */
public class ClienteMainActivity extends AppCompatActivity {
    private ActivityClienteMainBinding binding;
    private SessionManager sessionManager;
    private Cliente clienteActual;
    private LinearLayout emptyStateLayout;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeComponents();
        validateSession();
    }

    /**
     * Verifica la existencia de una sesión válida
     */
    private void validateSession() {
        //loadingDialog.show("Verificando sesión...");

        sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
            @Override
            public void onSessionValid(Usuario usuario) {
                clienteActual = sessionManager.getClienteActual();
                //loadingDialog.dismiss();

                initializeUI();
            }

            @Override
            public void onSessionError() {
                handleSessionError();
            }
        });
    }


    /**
     * Inicializa los componentes principales
     */
    private void initializeComponents() {
        binding = ActivityClienteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar EdgeToEdge
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = SessionManager.getInstance(this);
        loadingDialog = new LoadingDialog(this);
    }

    /**
     * Inicializa la UI una vez que la sesión está validada
     */
    private void initializeUI() {
        setupUI();
        setupClickListeners();
        loadRestaurants();
    }

    /**
     * Configura la UI con los datos del usuario
     */
    private void setupUI() {
        try {
            String welcomeMessage = String.format("¡Hola %s, qué te gustaría comer hoy?",
                    clienteActual.getNombres().split(" ")[0]);
            binding.tvWelcome.setText(welcomeMessage);
        } catch (Exception e) {
            // En caso de algún error con el nombre, usar mensaje genérico
            binding.tvWelcome.setText("¡Hola, qué te gustaría comer hoy?");
            Log.e("ClienteMainActivity", "Error al configurar mensaje de bienvenida", e);
        }
    }

    /**
     * Configura los listeners de los botones
     */
    private void setupClickListeners() {
        binding.btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClientePerfilActivity.class);
            startActivity(intent);
        });

        binding.btnNotifications.setOnClickListener(v -> {
            Intent intent = new Intent(this, ClienteNotificacionesActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Obtiene y muestra la lista de restaurantes desde Firestore
     */
    private void loadRestaurants() {
        RestauranteRepository restauranteRepository = new RestauranteRepository();
        LoadingDialog loadingDialog = new LoadingDialog(this);

        loadingDialog.show("Cargando restaurantes...");

        restauranteRepository.getRestaurantesActivos()
                .addOnSuccessListener(restaurantes -> {
                    loadingDialog.dismiss();

                    if (restaurantes.isEmpty()) {
                        showEmptyState();
                    } else {
                        showRestaurants(restaurantes);
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    showError("Error al cargar restaurantes: " + e.getMessage());
                });
    }

    /**
     * Muestra los restaurantes en el RecyclerView
     */
    private void showRestaurants(List<Restaurante> restaurantes) {
        binding.rvRestaurants.setVisibility(View.VISIBLE);
        binding.tvRestaurants.setVisibility(View.VISIBLE);

        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }

        RestauranteAdapter adapter = new RestauranteAdapter(this, restaurantes);
        binding.rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRestaurants.setAdapter(adapter);
    }

    /**
     * Muestra un estado vacío cuando no hay restaurantes
     */
    private void showEmptyState() {
        binding.rvRestaurants.setVisibility(View.GONE);

        // Crear layout para estado vacío si no existe
        if (emptyStateLayout == null) {
            emptyStateLayout = new LinearLayout(this);
            emptyStateLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            emptyStateLayout.setOrientation(LinearLayout.VERTICAL);
            emptyStateLayout.setGravity(Gravity.CENTER);
            emptyStateLayout.setPadding(
                    dpToPx(32),
                    dpToPx(32),
                    dpToPx(32),
                    dpToPx(32)
            );

            // Crear ImageView para el ícono
            ImageView emptyIcon = new ImageView(this);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    dpToPx(100),
                    dpToPx(100)
            );
            iconParams.bottomMargin = dpToPx(16);
            emptyIcon.setLayoutParams(iconParams);
            emptyIcon.setImageResource(R.drawable.ic_restaurant);
            emptyIcon.setColorFilter(ContextCompat.getColor(this, R.color.btn_medium));

            // Crear TextView para el mensaje
            TextView emptyText = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.bottomMargin = dpToPx(24);
            emptyText.setLayoutParams(textParams);
            emptyText.setText("No hay restaurantes disponibles en este momento");
            emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            emptyText.setTextColor(ContextCompat.getColor(this, R.color.black));
            emptyText.setTextSize(16);
            emptyText.setTypeface(ResourcesCompat.getFont(this, R.font.sen_font));

            // Crear Button para reintentar
            MaterialButton retryButton = new MaterialButton(this);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            retryButton.setLayoutParams(buttonParams);
            retryButton.setText("Reintentar");
            retryButton.setOnClickListener(v -> loadRestaurants());

            // Agregar vistas al layout vacío
            emptyStateLayout.addView(emptyIcon);
            emptyStateLayout.addView(emptyText);
            emptyStateLayout.addView(retryButton);

            // Agregar el layout al contenedor principal
            ViewGroup parent = (ViewGroup) binding.rvRestaurants.getParent();
            parent.addView(emptyStateLayout);
        }

        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Convierte dp a píxeles
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Muestra un diálogo de error
     */
    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Reintentar", (dialog, which) -> loadRestaurants())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Maneja errores de sesión
     */
    private void handleSessionError() {
        if (!isFinishing()) {
            loadingDialog.dismiss();
            sessionManager.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (emptyStateLayout != null && emptyStateLayout.getParent() != null) {
            ((ViewGroup) emptyStateLayout.getParent()).removeView(emptyStateLayout);
        }
        emptyStateLayout = null;
        binding = null;
    }
}