package com.example.foodisea.manager;

import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.VerificacionEntrega;
import com.example.foodisea.repository.CodigoQRRepository;
import com.example.foodisea.repository.PedidoRepository;
import com.example.foodisea.repository.VerificacionEntregaRepository;
import com.google.android.gms.tasks.Task;

// Clase utilitaria para manejar la creación de verificaciones de pedidos
public class VerificacionManager {
    private final VerificacionEntregaRepository verificacionRepository;
    private final CodigoQRRepository qrRepository;
    private final PedidoRepository pedidoRepository;

    public VerificacionManager(
            VerificacionEntregaRepository verificacionRepository,
            CodigoQRRepository qrRepository,
            PedidoRepository pedidoRepository) {
        this.verificacionRepository = verificacionRepository;
        this.qrRepository = qrRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Task<VerificacionEntrega> crearVerificacion(String pedidoId) {
        // 1. Crear la verificación
        VerificacionEntrega verificacion = new VerificacionEntrega(pedidoId);

        return verificacionRepository.crear(verificacion)
                .continueWithTask(task -> {
                    String verificacionId = task.getResult().getId();
                    verificacion.setId(verificacionId); // Importante: establecer el ID

                    // 2. Crear QR de entrega
                    CodigoQR qrEntrega = new CodigoQR(verificacionId, "ENTREGA");
                    return qrRepository.generarCodigoQR(qrEntrega);
                })
                .continueWithTask(task -> {
                    String qrEntregaId = task.getResult().getId();
                    verificacion.setQrEntregaId(qrEntregaId);

                    // 3. Crear QR de pago
                    CodigoQR qrPago = new CodigoQR(verificacion.getId(), "PAGO");
                    return qrRepository.generarCodigoQR(qrPago);
                })
                .continueWithTask(task -> {
                    String qrPagoId = task.getResult().getId();
                    verificacion.setQrPagoId(qrPagoId);

                    // 4. Actualizar verificación con los IDs de QR
                    return verificacionRepository.actualizar(verificacion.getId(), verificacion);
                })
                .continueWith(task -> {
                    // 5. Retornar la verificación completa
                    if (task.isSuccessful()) {
                        return verificacion;
                    } else {
                        throw task.getException();
                    }
                });
    }
}
