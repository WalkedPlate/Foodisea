package com.example.foodisea.activity.repartidor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.adapter.OrderItemAdapter;
import com.example.foodisea.entity.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class RepartidorVerOrdenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repartidor_ver_orden);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);

        // Sample data
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem("Pizza Calzone European", "Size: P", 2, R.drawable.burger_image));
        orderItems.add(new OrderItem("Pizza Calzone European", "Size: M", 1, R.drawable.burger_image));

        OrderItemAdapter orderItemsAdapter = new OrderItemAdapter(orderItems);
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}