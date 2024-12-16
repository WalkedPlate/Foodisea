package com.example.foodisea.activity.cliente;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.activity.adminRes.AdminResHomeActivity;
import com.example.foodisea.activity.adminRes.AdminResPedidosActivity;
import com.example.foodisea.databinding.ActivityClienteComprobanteQrBinding;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.VerificacionEntrega;
import com.example.foodisea.repository.CodigoQRRepository;
import com.example.foodisea.repository.VerificacionEntregaRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class ClienteComprobanteQrActivity extends AppCompatActivity {
    private ActivityClienteComprobanteQrBinding binding;
    private String pedidoId;
    private VerificacionEntregaRepository verificacionRepository;
    private CodigoQRRepository qrRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteComprobanteQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar repositorios
        verificacionRepository = new VerificacionEntregaRepository(this);
        qrRepository = new CodigoQRRepository();

        // Obtener pedidoId del intent
        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null) {
            Toast.makeText(this, "Error: No se encontró el pedido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        cargarQRCliente();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnQrPago.setOnClickListener(v -> {
            // Iniciar actividad de escaneo
            Intent intent = new Intent(this, ClienteScannerQrActivity.class);
            intent.putExtra("pedidoId", pedidoId);
            startActivity(intent);
        });
    }

    private void cargarQRCliente() {
        // Añadir log para debug
        Log.d("QRDebug", "Iniciando carga de QR para pedidoId: " + pedidoId);

        verificacionRepository.obtenerPorPedidoId(pedidoId)
                .addOnSuccessListener(document -> {
                    if (document != null) {
                        Log.d("QRDebug", "Verificación encontrada");
                        String verificacionId = document.getId(); // Obtener el ID del documento

                        // Obtener el QR de entrega (no de pago, ya que este QR es para que el repartidor lo escanee)
                        qrRepository.obtenerQRVerificacion(verificacionId, "ENTREGA")
                                .addOnSuccessListener(qrDoc -> {
                                    if (qrDoc != null) {
                                        CodigoQR codigoQR = qrDoc.toObject(CodigoQR.class);
                                        if (codigoQR != null) {
                                            Log.d("QRDebug", "Código QR encontrado: " + codigoQR.getCodigo());
                                            generarQRBitmap(codigoQR.getCodigo());
                                        } else {
                                            Log.e("QRDebug", "CodigoQR es null");
                                        }
                                    } else {
                                        Log.e("QRDebug", "No se encontró documento QR");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("QRDebug", "Error al obtener QR: " + e.getMessage());
                                    Toast.makeText(this, "Error al cargar el QR", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("QRDebug", "No se encontró verificación");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("QRDebug", "Error al cargar verificación: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar la verificación", Toast.LENGTH_SHORT).show();
                });
    }

    private void generarQRBitmap(String contenido) {
        try {
            Log.d("QRDebug", "Generando bitmap para contenido: " + contenido);

            // Configuración para el QR
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1); // Margen más pequeño
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Alta corrección de errores

            // Generar QR
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 512, 512, hints);

            // Crear bitmap
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            // Llenar bitmap
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Mostrar en UI thread
            runOnUiThread(() -> {
                binding.imgQrCliente.setImageBitmap(bitmap);
                binding.imgQrCliente.setVisibility(View.VISIBLE);
                Log.d("QRDebug", "QR mostrado en ImageView");
            });

        } catch (WriterException e) {
            Log.e("QRDebug", "Error al generar QR: " + e.getMessage());
            runOnUiThread(() ->
                    Toast.makeText(this, "Error al generar el QR", Toast.LENGTH_SHORT).show()
            );
        }
    }
}