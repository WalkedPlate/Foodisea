package com.example.foodisea.activity.repartidor;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.MessageAdapter;
import com.example.foodisea.databinding.ActivityRepartidorChatBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Mensaje;
import com.example.foodisea.model.Message;
import com.example.foodisea.repository.ChatRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepartidorChatActivity extends AppCompatActivity {

    private MessageAdapter messageAdapter;
    private List<Mensaje> messageList;
    private ChatRepository chatRepository;
    private String chatId;
    private String emisorId;

    ActivityRepartidorChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityRepartidorChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar variables
        chatRepository = new ChatRepository();
        messageList = new ArrayList<>();
        chatId = getIntent().getStringExtra("chatid");
        emisorId = getIntent().getStringExtra("clienteId");

        // Configuración del RecyclerView
        binding.recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(this, messageList);
        binding.recyclerViewMessages.setAdapter(messageAdapter);

        // Escuchar mensajes en Firestore
        escucharMensajes();

        // Configurar botón de enviar
        binding.sendButton.setOnClickListener(v -> enviarMensaje());
    }

    private void escucharMensajes() {
        chatRepository.getMensajes(chatId, mensajes -> {
            messageList.clear();
            messageList.addAll(mensajes);
            messageAdapter.notifyDataSetChanged();
            binding.recyclerViewMessages.scrollToPosition(messageList.size() - 1);
        }, e -> Toast.makeText(this, "Error al obtener mensajes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void enviarMensaje() {
        String texto = binding.messageInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        Mensaje mensaje = new Mensaje();
        mensaje.setChatId(chatId);
        mensaje.setEmisorId(emisorId);
        mensaje.setTexto(texto);
        mensaje.setTimestamp(System.currentTimeMillis());
        mensaje.setTipo("texto");

        chatRepository.enviarMensaje(mensaje, unused -> {
            binding.messageInput.setText(""); // Limpiar el campo de texto
        }, e -> Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}