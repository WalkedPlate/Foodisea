package com.example.foodisea.service;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.function.Consumer;

public class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
    private final BarcodeScanner scanner;
    private final Consumer<String> onQRCodeDetected;
    private long lastAnalysisTimestamp = 0;
    private static final long COOLDOWN_MS = 1000; // 1 segundo entre análisis

    public QRCodeAnalyzer(Consumer<String> onQRCodeDetected) {
        this.scanner = BarcodeScanning.getClient();
        this.onQRCodeDetected = onQRCodeDetected;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - lastAnalysisTimestamp < COOLDOWN_MS) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            Barcode barcode = barcodes.get(0);
                            if (barcode.getRawValue() != null) {
                                Log.d("QRDebug", "QR detectado: " + barcode.getRawValue());
                                lastAnalysisTimestamp = currentTimestamp;
                                onQRCodeDetected.accept(barcode.getRawValue());
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("QRDebug", "Error en el escaneo: " + e.getMessage()))
                    .addOnCompleteListener(task -> imageProxy.close());
        } else {
            imageProxy.close();
        }
    }
}