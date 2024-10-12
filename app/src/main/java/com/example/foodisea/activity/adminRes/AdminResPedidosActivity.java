package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.databinding.ActivityAdminResPedidosBinding;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.PlatoCantidad;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminResPedidosActivity extends AppCompatActivity {
    ActivityAdminResPedidosBinding binding;
    private Map<String, Cliente> clientesMap = new HashMap<>();
    private Map<String, Pago> pagosMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminResPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        List<Pedido> pedidosList = getPedidosList();
        PedidosAdapter adapter = new PedidosAdapter(this, pedidosList, clientesMap, pagosMap);
        binding.rvPedidos.setAdapter(adapter);
        binding.rvPedidos.setLayoutManager(new GridLayoutManager(this, 2));

        binding.btnBack.setOnClickListener(view -> {
            Intent home = new Intent(this, AdminResHomeActivity.class);
            startActivity(home);
        });
    }

    private List<Pedido> getPedidosList() {
        List<Pedido> pedidos = new ArrayList<>();

        // Crear instancias de Cliente
        Cliente cliente1 = new Cliente("1", "John", "Gomez", "john@example.com", "1234567890", "2118 Thornridge Cir. Syracuse", "12345678", "1990-01-01", null, "Activo", "Cliente");
        Cliente cliente2 = new Cliente("2", "David", "Fernandez", "david@example.com", "0987654321", "2118 Thornridge Cir. Syracuse", "87654321", "1992-05-10", null, "Activo", "Cliente");
        Cliente cliente3 = new Cliente("3", "Juan", "Perez", "juan@example.com", "1122334455", "2118 Thornridge Cir. Syracuse", "23456789", "1988-09-20", null, "Activo", "Cliente");
        Cliente cliente4 = new Cliente("4", "Maria", "Diaz", "maria@example.com", "2233445566", "2118 Thornridge Cir. Syracuse", "34567890", "1995-11-30", null, "Activo", "Cliente");

        clientesMap.put(cliente1.getId(), cliente1);
        clientesMap.put(cliente2.getId(), cliente2);
        clientesMap.put(cliente3.getId(), cliente3);
        clientesMap.put(cliente4.getId(), cliente4);

        // Crear instancias de CodigoQR
        CodigoQR codigoQR1 = new CodigoQR("qrCode1", "#162432", "codigo123", "Generado", new Date());
        CodigoQR codigoQR2 = new CodigoQR("qrCode2", "#182432", "codigo456", "Generado", new Date());
        CodigoQR codigoQR3 = new CodigoQR("qrCode3", "#202432", "codigo789", "Generado", new Date());
        CodigoQR codigoQR4 = new CodigoQR("qrCode4", "#262432", "codigo012", "Generado", new Date());

        // Crear instancias de Pago
        Pago pago1 = new Pago("pagoId1", "#162432", 15.0, "Tarjeta", "Completado", new Date());
        Pago pago2 = new Pago("pagoId2", "#182432", 25.0, "Efectivo", "Pendiente", new Date());
        Pago pago3 = new Pago("pagoId3", "#202432", 15.0, "Tarjeta", "Completado", new Date());
        Pago pago4 = new Pago("pagoId4", "#262432", 15.0, "Efectivo", "Completado", new Date());

        pagosMap.put(pago1.getId(), pago1);
        pagosMap.put(pago2.getId(), pago2);
        pagosMap.put(pago3.getId(), pago3);
        pagosMap.put(pago4.getId(), pago4);

        // Crear listas de PlatoCantidad para cada pedido
        List<PlatoCantidad> platosPedido1 = new ArrayList<>();
        platosPedido1.add(new PlatoCantidad("plato1", 2));
        platosPedido1.add(new PlatoCantidad("plato2", 1));

        List<PlatoCantidad> platosPedido2 = new ArrayList<>();
        platosPedido2.add(new PlatoCantidad("plato3", 1));

        List<PlatoCantidad> platosPedido3 = new ArrayList<>();
        platosPedido3.add(new PlatoCantidad("plato4", 3));

        List<PlatoCantidad> platosPedido4 = new ArrayList<>();
        platosPedido4.add(new PlatoCantidad("plato1", 1));
        platosPedido4.add(new PlatoCantidad("plato3", 2));

        // Añadir pedidos a la lista
        pedidos.add(new Pedido("#162432", "1", "restauranteId1", platosPedido1, null, "Recibido", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode1", "pagoId1"));
        pedidos.add(new Pedido("#182432", "2", "restauranteId2", platosPedido2, null, "En preparación", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode2", "pagoId2"));
        pedidos.add(new Pedido("#202432", "3", "restauranteId3", platosPedido3, null, "En camino", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode3", "pagoId3"));
        pedidos.add(new Pedido("#222432", "4", "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode4", "pagoId4"));

        return pedidos;
    }

    public void mostrarBottonSheet(Pedido pedido) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AdminResPedidosActivity.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.botton_sheet_admin_res_pedidos, null);

        Cliente cliente = clientesMap.get(pedido.getClienteId());
        Pago pago = pagosMap.get(pedido.getPagoId());

        TextView textView = bottomSheetView.findViewById(R.id.nombreCliente);
        textView.setText(cliente.getNombres() + " " + cliente.getApellidos());

        TextView textViewIdPedido = bottomSheetView.findViewById(R.id.idPedido);
        textViewIdPedido.setText(pedido.getId());

        Button botonDetallePedido = bottomSheetView.findViewById(R.id.buttonVerDetalles);
        botonDetallePedido.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminResDetallesPedidosActivity.class);
            intent.putExtra("idPedido", pedido.getId());
            intent.putExtra("direccionDestino", pedido.getDireccionEntrega());
            intent.putExtra("nombreCliente", cliente.getNombres() + " " + cliente.getApellidos());
            intent.putExtra("telefono", cliente.getTelefono());
            intent.putExtra("metodoPago", pago.getMetodoPago());
            intent.putExtra("estadoPago", pago.getEstadoPago());
            startActivity(intent);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}