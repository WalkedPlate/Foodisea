package com.example.foodisea.activity.repartidor;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.PedidosAdapter;
import com.example.foodisea.databinding.ActivityRepartidorRestauranteBinding;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.CodigoQR;
import com.example.foodisea.model.Pago;
import com.example.foodisea.model.Pedido;
import com.example.foodisea.model.PlatoCantidad;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepartidorRestauranteActivity extends AppCompatActivity {

    private ActivityRepartidorRestauranteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // binding
        binding = ActivityRepartidorRestauranteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar insets del sistema para la ventana
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar RecyclerView para mostrar los pedidos
        binding.rvPedidos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        List<Pedido> pedidosList = getPedidosList(); // Método para obtener los datos de pedidos
        PedidosAdapter adapter = new PedidosAdapter(this, pedidosList);
        binding.rvPedidos.setAdapter(adapter);

        // Configurar el título con la cantidad de pedidos
        binding.tvPedidosTitle.setText(String.format("Pedidos (%d)", pedidosList.size()));

        // Obtener los datos pasados desde el intent
        Intent intent = getIntent();
        String restaurantName = intent.getStringExtra("name");
        float restaurantRating = intent.getFloatExtra("rating", 0);
        int restaurantImage = intent.getIntExtra("image", R.drawable.restaurant_image);
        String restaurantLocation = intent.getStringExtra("location");

        // Asignar los datos del restaurante al UI
        binding.tvRestaurantName.setText(restaurantName);
        binding.tvRestaurantAddress.setText(restaurantLocation);

        // Iniciar los botones
        setupButtonListeners();

    }

    private List<Pedido> getPedidosList() {
        // Obtener desde bd
        List<Pedido> pedidos = new ArrayList<>();

        // Crear instancias de Cliente
        Cliente cliente1 = new Cliente("1", "John", "Gomez", "john@example.com", "1234567890", "2118 Thornridge Cir. Syracuse", "12345678", "1990-01-01", null, "Activo", "Cliente");
        Cliente cliente2 = new Cliente("2", "David", "Fernandez", "david@example.com", "0987654321", "2118 Thornridge Cir. Syracuse", "87654321", "1992-05-10", null, "Activo", "Cliente");
        Cliente cliente3 = new Cliente("3", "Juan", "Perez", "juan@example.com", "1122334455", "2118 Thornridge Cir. Syracuse", "23456789", "1988-09-20", null, "Activo", "Cliente");
        Cliente cliente4 = new Cliente("4", "Maria", "Diaz", "maria@example.com", "2233445566", "2118 Thornridge Cir. Syracuse", "34567890", "1995-11-30", null, "Activo", "Cliente");

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

        // Crear listas de PlatoCantidad para cada pedido
        List<PlatoCantidad> platosPedido1 = new ArrayList<>();
        platosPedido1.add(new PlatoCantidad("plato1", 2));  // 2 unidades del plato con ID "plato1"
        platosPedido1.add(new PlatoCantidad("plato2", 1));  // 1 unidad del plato con ID "plato2"

        List<PlatoCantidad> platosPedido2 = new ArrayList<>();
        platosPedido2.add(new PlatoCantidad("plato3", 1));  // 1 unidad del plato con ID "plato3"

        List<PlatoCantidad> platosPedido3 = new ArrayList<>();
        platosPedido3.add(new PlatoCantidad("plato4", 3));  // 3 unidades del plato con ID "plato4"

        List<PlatoCantidad> platosPedido4 = new ArrayList<>();
        platosPedido4.add(new PlatoCantidad("plato1", 1));  // 1 unidad del plato con ID "plato1"
        platosPedido4.add(new PlatoCantidad("plato3", 2));  // 2 unidades del plato con ID "plato3"

        // Añadir pedidos a la lista
        // Añadir pedidos a la lista
        pedidos.add(new Pedido("#162432", cliente1.getId(), "restauranteId1", platosPedido1, null, "Recibido", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR1.getId(), pago1.getId()));
        pedidos.add(new Pedido("#182432", cliente2.getId(), "restauranteId2", platosPedido2, null, "En preparación", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR2.getId(), pago2.getId()));
        pedidos.add(new Pedido("#202432", cliente3.getId(), "restauranteId3", platosPedido3, null, "En camino", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR3.getId(), pago3.getId()));
        pedidos.add(new Pedido("#222432", cliente4.getId(), "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR4.getId(), pago4.getId()));
        pedidos.add(new Pedido("#262432", cliente4.getId(), "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR4.getId(), pago4.getId()));
        pedidos.add(new Pedido("#232423", cliente4.getId(), "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR4.getId(), pago4.getId()));
        pedidos.add(new Pedido("#274765", cliente4.getId(), "restauranteId4", platosPedido4, null, "Entregado", new Date(), "2118 Thornridge Cir. Syracuse", codigoQR4.getId(), pago4.getId()));

        return pedidos;
    }

    private void setupButtonListeners() {

        //Botón para regresar
        binding.btnBack.setOnClickListener(v -> {
            finish(); // Cierra la actividad actual y regresa
        });

        //Botón de favoritos
        binding.btnFavorite.setOnClickListener(v -> {
            //...
        });

    }
}