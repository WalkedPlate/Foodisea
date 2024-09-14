package com.example.foodisea.activity.repartidor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.PedidosAdapter;
import com.example.foodisea.entity.Pedido;

import java.util.ArrayList;
import java.util.List;

public class RepartidorRestauranteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repartidor_restaurante);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        RecyclerView rvPedidos = findViewById(R.id.rvPedidos);
        rvPedidos.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        List<Pedido> pedidosList = getPedidosList(); // Method to get your order data
        PedidosAdapter adapter = new PedidosAdapter(pedidosList);
        rvPedidos.setAdapter(adapter);

        // Set the title with the order count
        TextView tvPedidosTitle = findViewById(R.id.tvPedidosTitle);
        tvPedidosTitle.setText(String.format("Pedidos (%d)", pedidosList.size()));




    }

    private List<Pedido> getPedidosList() {
        // This is where you'd normally fetch data from a database or API
        List<Pedido> pedidos = new ArrayList<>();
        pedidos.add(new Pedido("#162432", "John Gomez", "2118 Thornridge Cir. Syracuse", 15.0));
        pedidos.add(new Pedido("#182432", "David Fernandez", "2118 Thornridge Cir. Syracuse", 25.0));
        pedidos.add(new Pedido("#202432", "Juan Perez", "2118 Thornridge Cir. Syracuse", 15.0));
        pedidos.add(new Pedido("#262432", "Maria Diaz", "2118 Thornridge Cir. Syracuse", 15.0));
        return pedidos;
    }
}