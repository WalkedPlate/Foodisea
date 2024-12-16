package com.example.foodisea.activity.repartidor;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;
import com.example.foodisea.adapter.repartidor.MessageAdapter;
import com.example.foodisea.databinding.ActivityRepartidorChatBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Chat;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Mensaje;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.repository.ChatRepository;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepartidorChatActivity extends AppCompatActivity {
    private ActivityRepartidorChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Mensaje> messageList;
    private ChatRepository chatRepository;
    private UsuarioRepository usuarioRepository;
    private String chatId;
    private String repartidorId;
    private String clienteId;
    private Chat chatInfo;
    private SessionManager sessionManager;
    private Repartidor repartidorActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRepartidorChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        repartidorActual = sessionManager.getRepartidorActual();
        if (repartidorActual != null) {
            repartidorId = repartidorActual.getId();
        }

        initializeVariables();
        setupViews();
        setupChatListener();
        loadChatInfo();
    }

    private void initializeVariables() {
        chatRepository = new ChatRepository();
        usuarioRepository = new UsuarioRepository();
        messageList = new ArrayList<>();

        chatId = getIntent().getStringExtra("chatId");
        if (chatId == null || chatId.isEmpty()) {
            Toast.makeText(this, "Error: Chat no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(this, messageList);
        messageAdapter.setCurrentUserId(repartidorId);
        binding.recyclerViewMessages.setAdapter(messageAdapter);

        binding.backButton.setOnClickListener(v -> finish());
        binding.sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadChatInfo() {
        chatRepository.getChatById(chatId, chat -> {
            if (chat != null) {
                chatInfo = chat;
                clienteId = chat.getClienteId();

                if (clienteId != null && !clienteId.isEmpty()) {
                    usuarioRepository.getUserById(clienteId)
                            .addOnSuccessListener(usuario -> {
                                if (usuario instanceof Cliente) {
                                    Cliente cliente = (Cliente) usuario;

                                    // Establecer nombre
                                    String nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                                    binding.userName.setText(nombreCompleto);

                                    // Cargar foto usando Glide
                                    if (cliente.getFoto() != null && !cliente.getFoto().isEmpty()) {
                                        Glide.with(this)
                                                .load(cliente.getFoto())
                                                .placeholder(R.drawable.ic_profile)
                                                .error(R.drawable.ic_profile)
                                                .circleCrop()
                                                .into(binding.userProfileImage);
                                    } else {
                                        binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                                    }
                                } else {
                                    binding.userName.setText("Cliente");
                                    binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Chat", "Error al obtener información del cliente", e);
                                binding.userName.setText("Cliente");
                                binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                            });
                } else {
                    binding.userName.setText("Cliente");
                    binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                }

                // Estado del pedido
                if (chat.getEstadoPedido() != null) {
                    binding.userStatus.setText("Pedido: " + chat.getEstadoPedido());
                }
            }
        }, e -> Toast.makeText(this, "Error al cargar información del chat",
                Toast.LENGTH_SHORT).show());
    }

    private void setupChatListener() {
        chatRepository.getMensajes(chatId, mensajes -> {
            messageList.clear();
            messageList.addAll(mensajes);
            messageAdapter.notifyDataSetChanged();
            scrollToBottom();
        }, e -> Toast.makeText(RepartidorChatActivity.this,
                "Error al cargar mensajes: " + e.getMessage(),
                Toast.LENGTH_SHORT).show());
    }

    private void sendMessage() {
        String texto = binding.messageInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        binding.sendButton.setEnabled(false);

        Mensaje mensaje = new Mensaje();
        mensaje.setChatId(chatId);
        mensaje.setEmisorId(repartidorId);
        mensaje.setTexto(texto);
        mensaje.setTimestamp(new Timestamp(new Date())); // Usar Timestamp
        mensaje.setTipo("texto");

        chatRepository.enviarMensaje(mensaje, unused -> {
            binding.messageInput.setText("");
            binding.sendButton.setEnabled(true);

            if (chatInfo != null) {
                chatInfo.setUltimoMensaje(texto);
                chatInfo.setTimestamp(new Timestamp(new Date())); // Usar Timestamp
                chatRepository.actualizarChat(chatInfo, null, e ->
                        Log.e("Chat", "Error al actualizar último mensaje", e));
            }
        }, e -> {
            Toast.makeText(this, "Error al enviar mensaje: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            binding.sendButton.setEnabled(true);
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            binding.recyclerViewMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }
}