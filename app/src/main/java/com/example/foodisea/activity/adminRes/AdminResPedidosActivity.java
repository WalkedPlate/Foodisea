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
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.databinding.ActivityAdminResPedidosBinding;
import com.example.foodisea.dto.PedidoConCliente;
import com.example.foodisea.model.AdministradorRestaurante;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.repository.PedidoRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminResPedidosActivity extends AppCompatActivity {
    ActivityAdminResPedidosBinding binding;
    private Map<String, Cliente> clientesMap = new HashMap<>();
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

        // Obtener al admin logueado
        sessionManager = SessionManager.getInstance(this);
        administradorRestauranteActual = sessionManager.getAdminRestauranteActual();
    }

    private void highlightOrder(String orderId) {
        // Implementa la lógica para resaltar o mostrar el pedido específico
    }


    //Obtiene lista de pedidos de la BD
    public void obtenerListaPedidos(){
        pedidoRepository.getPedidosActivosRestaurante(administradorRestauranteActual.getRestauranteId())
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

        // Lineas
        View primeraLinea = bottomSheetView.findViewById(R.id.linea1);
        View segundaLinea = bottomSheetView.findViewById(R.id.linea2);
        View terceraLinea = bottomSheetView.findViewById(R.id.linea3);


        if(Objects.equals(pedidoConCliente.getPedido().getEstado(), "Recibido")){
            imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
            textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
        } else{
            if(Objects.equals(pedidoConCliente.getPedido().getEstado(),"Preparando")){
                imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                primeraLinea.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_medium));
            } else{
                if(Objects.equals(pedidoConCliente.getPedido().getEstado(),"En camino")){
                    imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    imageViewOrdenEnCamino.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenEnCamino.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    primeraLinea.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_medium));
                    segundaLinea.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_medium));
                } else{
                    imageViewOrdenRecibida.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenRecibida.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    imageViewOrdenPreparando.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenPreparando.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    imageViewOrdenEnCamino.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenEnCamino.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    imageViewOrdenEntregada.setImageTintList(ContextCompat.getColorStateList(this,R.color.btn_medium));
                    textViewOrdenEntregada.setTextColor(ContextCompat.getColor(this, R.color.btn_medium));
                    primeraLinea.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_medium));
                    segundaLinea.setBackgroundColor(ContextCompat.getColor(this, R.color.btn_medium));
                    terceraLinea.setBackgroundColor(ContextCompat.getColor(this,R.color.btn_medium));
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
            intent.putExtra("estadoPago", pedidoConCliente.getPedido().getEstado());
            intent.putExtra("montoPedido",pedidoConCliente.getPedido().getMontoTotal());
            intent.putExtra("listaIdProductosCantidad",new ArrayList<>(pedidoConCliente.getPedido().getProductos()));
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