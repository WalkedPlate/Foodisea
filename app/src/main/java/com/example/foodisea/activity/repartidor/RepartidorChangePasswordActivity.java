package com.example.foodisea.activity.repartidor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityClienteChangePasswordBinding;
import com.example.foodisea.databinding.ActivityRepartidorChangePasswordBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RepartidorChangePasswordActivity extends AppCompatActivity {

    ActivityRepartidorChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeComponents();
        setupViews();
    }

    private void initializeComponents() {
        binding = ActivityRepartidorChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        EdgeToEdge.enable(this);
        setupWindowInsets();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupViews() {
        setupClickListeners();
        setupInputValidation();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChangePass.setOnClickListener(view -> {
            if (validatePasswords()) {
                String newPassword = binding.etPassword.getText().toString();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    finish();  // Cerrar la actividad
                                } else {
                                    showError("Error al cambiar la contraseña");
                                }
                            });
                } else {
                    showError("No se pudo autenticar al usuario");
                }
            }
        });
    }

    private void setupInputValidation() {

        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        binding.etPassword.addTextChangedListener(passwordWatcher);
        binding.etConfirmPassword.addTextChangedListener(passwordWatcher);
    }

    private boolean validatePasswords() {
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        if (password.isEmpty()) {
            binding.etPasswordLayout.setError(getString(R.string.error_ingresa_password));
            return false;
        } else if (password.length() < 6) {
            binding.etPasswordLayout.setError(getString(R.string.error_password_corto));
            return false;
        } else {
            binding.etPasswordLayout.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPasswordLayout.setError(getString(R.string.error_confirma_password));
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.etConfirmPasswordLayout.setError(getString(R.string.error_passwords_no_coinciden));
            return false;
        } else {
            binding.etConfirmPasswordLayout.setError(null);
        }

        return true;
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}