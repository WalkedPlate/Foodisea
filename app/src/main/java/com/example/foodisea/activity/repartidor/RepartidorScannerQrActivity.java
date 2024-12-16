package com.example.foodisea.activity.repartidor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityRepartidorScannerQrBinding;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.VerificacionEntrega;
import com.example.foodisea.repository.CodigoQRRepository;
import com.example.foodisea.repository.VerificacionEntregaRepository;
import com.example.foodisea.service.QRCodeAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.firestore.DocumentSnapshot;

public class RepartidorScannerQrActivity extends AppCompatActivity {
    private ActivityRepartidorScannerQrBinding binding;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private String pedidoId;
    private VerificacionEntregaRepository verificacionRepository;
    private CodigoQRRepository qrRepository;
    private boolean isScanning = true;
    private long lastScanTimestamp = 0;
    private static final long SCAN_COOLDOWN_MS = 2000; // 2 segundos entre intentos de verificación
    private long lastVerificationTimestamp = 0;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            startCamera();
                        } else {
                            Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepartidorScannerQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null) {
            finish();
            return;
        }

        verificacionRepository = new VerificacionEntregaRepository();
        qrRepository = new CodigoQRRepository();

        previewView = binding.viewFinder;
        binding.btnBack.setOnClickListener(v -> finish());

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraPreview();
            } catch (Exception e) {
                Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview() {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                new QRCodeAnalyzer(result -> {
                    if (isScanning && result != null) {
                        isScanning = false;
                        verificarQREntrega(result);
                    }
                }));

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarQREntrega(String codigoQR) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTimestamp < SCAN_COOLDOWN_MS) {
            Log.d("QRVerification", "Ignorando por cooldown");
            return;
        }
        lastScanTimestamp = currentTime;

        Log.d("QRVerification", "=== INICIO VERIFICACIÓN ===");
        Log.d("QRVerification", "Código QR detectado: " + codigoQR);
        Log.d("QRVerification", "PedidoId actual: " + pedidoId);

        // Quitamos la verificación inicial de isScanning
        isScanning = false;  // Lo ponemos en false al inicio de la verificación

        verificacionRepository.obtenerPorPedidoId(pedidoId)
                .addOnSuccessListener(document -> {
                    if (document != null) {
                        String verificacionId = document.getId();
                        Log.d("QRVerification", "VerificacionId encontrada: " + verificacionId);

                        qrRepository.validarCodigoQR(codigoQR, verificacionId, "ENTREGA")
                                .addOnSuccessListener(isValid -> {
                                    if (isValid) {
                                        Log.d("QRVerification", "QR válido, confirmando entrega...");
                                        verificacionRepository.confirmarEntrega(verificacionId)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("QRVerification", "Entrega confirmada exitosamente");
                                                    verificacionRepository.verificarConfirmacionesCompletas(verificacionId)
                                                            .addOnSuccessListener(confirmacionesCompletas -> {
                                                                runOnUiThread(() -> {
                                                                    vibrar();
                                                                    if (confirmacionesCompletas) {
                                                                        Toast.makeText(this, "¡Pedido completado exitosamente!",
                                                                                Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(this, RepartidorConfirmacionEntregaActivity.class);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        Toast.makeText(this, "Entrega confirmada, esperando confirmación de pago",
                                                                                Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    finish();
                                                                });
                                                            });
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("QRVerification", "Error al confirmar entrega: " + e.getMessage());
                                                    isScanning = true;  // Reactivamos el scanning si hay error
                                                    runOnUiThread(() ->
                                                            Toast.makeText(this, "Error al confirmar la entrega",
                                                                    Toast.LENGTH_SHORT).show()
                                                    );
                                                });
                                    } else {
                                        Log.d("QRVerification", "QR inválido");
                                        isScanning = true;  // Reactivamos el scanning si el QR es inválido
                                        runOnUiThread(() ->
                                                Toast.makeText(this, "Código QR inválido",
                                                        Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("QRVerification", "Error en validación: " + e.getMessage());
                                    isScanning = true;  // Reactivamos el scanning si hay error
                                    runOnUiThread(() ->
                                            Toast.makeText(this, "Error al validar el código QR",
                                                    Toast.LENGTH_SHORT).show()
                                    );
                                });
                    } else {
                        Log.e("QRVerification", "No se encontró verificación");
                        isScanning = true;  // Reactivamos el scanning si no hay verificación
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QRVerification", "Error al obtener verificación: " + e.getMessage());
                    isScanning = true;  // Reactivamos el scanning si hay error
                });
    }
    private void vibrar() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(200);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScanning = true;  // Aseguramos que el scanning esté activo cuando la actividad se reanuda
    }
}