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
import com.example.foodisea.adapter.repartidor.MessageAdapter;
import com.example.foodisea.models.Message;

import java.util.ArrayList;
import java.util.List;

public class RepartidorChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_repartidor_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Inicializar RecyclerView
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar lista de mensajes y agregar mensajes de prueba
        // Lista de mensajes de prueba con hora de envío
        messageList = new ArrayList<>();
        messageList.add(new Message("¿Estás en camino?", true, R.drawable.rounded_person_24, "8:10 pm"));
        messageList.add(new Message("Sí, estoy en camino al restaurante", false, R.drawable.rounded_person_24, "8:11 pm"));
        messageList.add(new Message("Toca el timbre al llegar", true, R.drawable.rounded_person_24, "8:20 pm"));
        messageList.add(new Message("Ok, eso haré cuando llegue", false, R.drawable.rounded_person_24, "8:25 pm"));
        messageList.add(new Message("Gracias", true, R.drawable.rounded_person_24, "8:25 pm"));

        // Inicializar el adaptador y asignarlo al RecyclerView
        messageAdapter = new MessageAdapter(this, messageList);
        recyclerViewMessages.setAdapter(messageAdapter);

    }
}