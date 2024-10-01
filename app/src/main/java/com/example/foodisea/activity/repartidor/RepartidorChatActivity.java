package com.example.foodisea.activity.repartidor;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.MessageAdapter;
import com.example.foodisea.databinding.ActivityRepartidorChatBinding;
import com.example.foodisea.model.Message;

import java.util.ArrayList;
import java.util.List;

public class RepartidorChatActivity extends AppCompatActivity {

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    ActivityRepartidorChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // binding
        binding = ActivityRepartidorChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar los insets de la ventana para adaptarse a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar RecyclerView usando binding
        binding.recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar lista de mensajes y agregar mensajes de prueba
        messageList = new ArrayList<>();
        messageList.add(new Message("¿Estás en camino?", true, R.drawable.rounded_person_24, "8:10 pm"));
        messageList.add(new Message("Sí, estoy en camino al restaurante", false, R.drawable.rounded_person_24, "8:11 pm"));
        messageList.add(new Message("Toca el timbre al llegar", true, R.drawable.rounded_person_24, "8:20 pm"));
        messageList.add(new Message("Ok, eso haré cuando llegue", false, R.drawable.rounded_person_24, "8:25 pm"));
        messageList.add(new Message("Gracias", true, R.drawable.rounded_person_24, "8:25 pm"));

        // Inicializar el adaptador y asignarlo al RecyclerView
        messageAdapter = new MessageAdapter(this, messageList);
        binding.recyclerViewMessages.setAdapter(messageAdapter);
    }
}