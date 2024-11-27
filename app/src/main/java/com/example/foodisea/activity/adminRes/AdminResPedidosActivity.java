package com.example.foodisea.activity.adminRes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.data.SessionManager;
import com.example.foodisea.databinding.ActivityAdminResPedidosBinding;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.Producto;
import com.example.foodisea.model.ProductoCantidad;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminResPedidosActivity extends AppCompatActivity {
    ActivityAdminResPedidosBinding binding;
    private Map<String, Cliente> clientesMap = new HashMap<>();
    private Map<String, Pago> pagosMap = new HashMap<>();
    private PedidoRepository pedidoRepository;
    private List<PedidoConCliente> listaCompletaPedidos = new ArrayList<>();
    private SessionManager sessionManager;
    private AdministradorRestaurante administradorRestauranteActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminResPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = SessionManager.getInstance(this);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        validateSession();


        //List<Pedido> pedidosList = getPedidosList();
        pedidoRepository = new PedidoRepository(this);

        /*List<ProductoCantidad> platosPedido = new ArrayList<>();
        platosPedido.add(new ProductoCantidad("plato1", 2));
        platosPedido.add(new ProductoCantidad("plato2", 1));

        Pedido pedido = new Pedido();
        pedido.setClienteId("CL001");
        pedido.setCodigoQrId("QR001");
        pedido.setDireccionEntrega("Av.Roca y Bologna 274");
        pedido.setEstado("Recibido");
        pedido.setPagoId("PAG001");
        pedido.setProductos(platosPedido);
        pedido.setRepartidorId("RP001");
        pedido.setRestauranteId("REST001");

        pedidoRepository.crearPedido(pedido);*/

        obtenerListaPedidos();

        binding.btnBack.setOnClickListener(view -> {
            Intent home = new Intent(this, AdminResHomeActivity.class);
            startActivity(home);
        });

        String highlightOrderId = getIntent().getStringExtra("highlightOrderId");
        if (highlightOrderId != null) {
            // Resaltar o scrollear al pedido específico
            highlightOrder(highlightOrderId);
        }

    }

    /**
     * Verifica la existencia de una sesión válida
     */
    private void validateSession() {
        //loadingDialog.show("Verificando sesión...");

        sessionManager.checkExistingSession(this, new SessionManager.SessionCallback() {
            @Override
            public void onSessionValid(Usuario usuario) {
                administradorRestauranteActual = sessionManager.getAdminRestauranteActual();
                //loadingDialog.dismiss();

            }

            @Override
            public void onSessionError() {

            }
        });
    }

    private void highlightOrder(String orderId) {
        // Implementa la lógica para resaltar o mostrar el pedido específico
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
        List<ProductoCantidad> platosPedido1 = new ArrayList<>();
        platosPedido1.add(new ProductoCantidad("plato1", 2));
        platosPedido1.add(new ProductoCantidad("plato2", 1));

        List<ProductoCantidad> platosPedido2 = new ArrayList<>();
        platosPedido2.add(new ProductoCantidad("plato3", 1));

        List<ProductoCantidad> platosPedido3 = new ArrayList<>();
        platosPedido3.add(new ProductoCantidad("plato4", 3));

        List<ProductoCantidad> platosPedido4 = new ArrayList<>();
        platosPedido4.add(new ProductoCantidad("plato1", 1));
        platosPedido4.add(new ProductoCantidad("plato3", 2));

        // Añadir pedidos a la lista
        pedidos.add(new Pedido("#162432", "1", "restauranteId1", platosPedido1, null, "Recibido", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode1", "pagoId1"));
        pedidos.add(new Pedido("#182432", "2", "restauranteId2", platosPedido2, null, "En preparación", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode2", "pagoId2"));
        pedidos.add(new Pedido("#202432", "3", "restauranteId3", platosPedido3, null, "En camino", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode3", "pagoId3"));
        pedidos.add(new Pedido("#222432", "4", "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", "qrCode4", "pagoId4"));

        return pedidos;
    }

    //Obtiene lista de pedidos de la BD
    public void obtenerListaPedidos(){
        pedidoRepository.getPedidosActivosRestaurante("REST001")
                .addOnSuccessListener(pedidos -> {
                    // Guardar la lista completa
                    listaCompletaPedidos = pedidos;
                    setupReciclerView();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar productos: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                    Log.d("error",e.getMessage());
                });
    }

    public void mostrarBottonSheet(PedidoConCliente pedidoConCliente) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(AdminResPedidosActivity.this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.botton_sheet_admin_res_pedidos, null);

        //Cliente cliente = clientesMap.get(pedido.getClienteId());
        //Pago pago = pagosMap.get(pedido.getPagoId());

        TextView textView = bottomSheetView.findViewById(R.id.nombreCliente);
        textView.setText(pedidoConCliente.getCliente().getNombres() + " " + pedidoConCliente.getCliente().getApellidos());

        TextView textViewIdPedido = bottomSheetView.findViewById(R.id.idPedido);
        textViewIdPedido.setText(pedidoConCliente.getPedido().getId());

        TextView textViewOrdenRecibida = bottomSheetView.findViewById(R.id.step1_text);
        ImageView imageViewOrdenRecibida = bottomSheetView.findViewById(R.id.step1_icon);

        TextView textViewOrdenPreparando = bottomSheetView.findViewById(R.id.step2_text);
        ImageView imageViewOrdenPreparando = bottomSheetView.findViewById(R.id.step2_icon);

        TextView textViewOrdenEnCamino = bottomSheetView.findViewById(R.id.step3_text);
        ImageView imageViewOrdenEnCamino = bottomSheetView.findViewById(R.id.step3_icon);

        TextView textViewOrdenEntregada = bottomSheetView.findViewById(R.id.step4_text);
        ImageView imageViewOrdenEntregada = bottomSheetView.findViewById(R.id.step4_icon);

        if(Objects.equals(pedidoConCliente.getPedido().getEstado(), "Recibido")){
            imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
            textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.orange));
        } else{
            if(Objects.equals(pedidoConCliente.getPedido().getEstado(),"Preparando")){
                imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.orange));
                imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.orange));
            } else{
                if(Objects.equals(pedidoConCliente.getPedido().getEstado(),"En camino")){
                    imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    imageViewOrdenEnCamino.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenEnCamino.setTextColor(ContextCompat.getColor(this, R.color.orange));
                } else{
                    imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    imageViewOrdenEnCamino.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenEnCamino.setTextColor(ContextCompat.getColor(this, R.color.orange));
                    imageViewOrdenEntregada.setImageTintList(ContextCompat.getColorStateList(this,R.color.orange));
                    textViewOrdenEntregada.setTextColor(ContextCompat.getColor(this, R.color.orange));
                }
            }
        }

        Button botonDetallePedido = bottomSheetView.findViewById(R.id.buttonVerDetalles);
        botonDetallePedido.setOnClickListener(view -> {
            Intent intent = new Intent(this, AdminResDetallesPedidosActivity.class);
            intent.putExtra("idPedido", pedidoConCliente.getPedido().getId());
            intent.putExtra("direccionDestino", pedidoConCliente.getCliente().getDireccion());
            intent.putExtra("nombreCliente", pedidoConCliente.getCliente().getNombres() + " " + pedidoConCliente.getCliente().getApellidos());
            intent.putExtra("telefono", pedidoConCliente.getCliente().getTelefono());
            intent.putExtra("metodoPago", pedidoConCliente.getPedido().getPagoId());
            intent.putExtra("estadoPago", pedidoConCliente.getPedido().getEstado());
            startActivity(intent);
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void setupReciclerView(){
        //Se manda la lista de pedidos a un adapter
        PedidosAdapter adapter = new PedidosAdapter(listaCompletaPedidos,this);
        binding.rvPedidos.setAdapter(adapter);
        binding.rvPedidos.setLayoutManager(new GridLayoutManager(this, 2));
    }
}