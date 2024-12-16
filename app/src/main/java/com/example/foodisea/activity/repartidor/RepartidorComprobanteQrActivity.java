package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodisea.R;
import com.example.foodisea.databinding.ActivityRepartidorComprobanteQrBinding;
import com.example.foodisea.model.CodigoQR;
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

public class RepartidorComprobanteQrActivity extends AppCompatActivity {
    private ActivityRepartidorComprobanteQrBinding binding;
    private String pedidoId;
    private VerificacionEntregaRepository verificacionRepository;
    private CodigoQRRepository qrRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepartidorComprobanteQrBinding.inflate(getLayoutInflater());
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
        cargarQRRepartidor();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnEscanearEntrega.setOnClickListener(v -> {
            Intent intent = new Intent(this, RepartidorScannerQrActivity.class);
            intent.putExtra("pedidoId", pedidoId);
            startActivity(intent);
        });
    }

    private void cargarQRRepartidor() {
        Log.d("QRDebug", "Cargando QR para pedidoId: " + pedidoId);

        verificacionRepository.obtenerPorPedidoId(pedidoId)
                .addOnSuccessListener(document -> {
                    if (document != null) {
                        String verificacionId = document.getId();
                        // Obtener el QR de PAGO (que el cliente debe escanear)
                        qrRepository.obtenerQRVerificacion(verificacionId, "PAGO")
                                .addOnSuccessListener(qrDoc -> {
                                    if (qrDoc != null) {
                                        CodigoQR codigoQR = qrDoc.toObject(CodigoQR.class);
                                        if (codigoQR != null) {
                                            generarQRBitmap(codigoQR.getCodigo());
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al cargar el QR", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar la verificación", Toast.LENGTH_SHORT).show());
    }

    private void generarQRBitmap(String contenido) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, 512, 512, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            runOnUiThread(() -> {
                binding.imgQrRepartidor.setImageBitmap(bitmap);
                binding.imgQrRepartidor.setVisibility(View.VISIBLE);
            });

        } catch (WriterException e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Error al generar el QR", Toast.LENGTH_SHORT).show()
            );
        }
    }
}