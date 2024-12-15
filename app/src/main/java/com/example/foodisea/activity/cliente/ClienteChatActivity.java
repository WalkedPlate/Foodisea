package com.example.foodisea.activity.cliente;

import android.os.Bundle;
import android.util.Log;
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
import com.example.foodisea.databinding.ActivityClienteChatBinding;
import com.example.foodisea.manager.SessionManager;
import com.example.foodisea.model.Chat;
import com.example.foodisea.model.Cliente;
import com.example.foodisea.model.Mensaje;
import com.example.foodisea.model.Repartidor;
import com.example.foodisea.repository.ChatRepository;
import com.example.foodisea.repository.UsuarioRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClienteChatActivity extends AppCompatActivity {
    private ActivityClienteChatBinding binding;
    private MessageAdapter messageAdapter;
    private List<Mensaje> messageList;
    private ChatRepository chatRepository;
    private UsuarioRepository usuarioRepository;
    private String chatId;
    private String clienteId;
    private String repartidorId;
    private Chat chatInfo;
    private SessionManager sessionManager;
    private Cliente clienteActual;
    private String pedidoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClienteChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = SessionManager.getInstance(this);
        clienteActual = sessionManager.getClienteActual();
        if (clienteActual != null) {
            clienteId = clienteActual.getId();
        }

        initializeVariables();

        // Solo configuramos las vistas y cargamos el chat si encontramos el chatId
        findOrCreateChat();
    }

    private void initializeVariables() {
        chatRepository = new ChatRepository();
        usuarioRepository = new UsuarioRepository();
        messageList = new ArrayList<>();

        pedidoId = getIntent().getStringExtra("pedidoId");
        if (pedidoId == null || pedidoId.isEmpty()) {
            Toast.makeText(this, "Error: Pedido no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void findOrCreateChat() {
        chatRepository.getChatByPedidoId(pedidoId, chat -> {
            // Si encontramos un chat existente
            if (chat != null) {
                chatId = chat.getId();
                setupViews();
                setupChatListener();
                loadChatInfo();
            } else {
                // Si no existe un chat, mostramos un mensaje de error
                Toast.makeText(this, "No hay chat disponible para este pedido",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }, e -> {
            Toast.makeText(this, "Error al cargar el chat: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupViews() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        messageAdapter = new MessageAdapter(this, messageList);
        messageAdapter.setCurrentUserId(clienteId);
        binding.recyclerViewMessages.setAdapter(messageAdapter);

        binding.backButton.setOnClickListener(v -> finish());
        binding.sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadChatInfo() {
        chatRepository.getChatById(chatId, chat -> {
            if (chat != null) {
                chatInfo = chat;
                repartidorId = chat.getRepartidorId();

                if (repartidorId != null && !repartidorId.isEmpty()) {
                    usuarioRepository.getUserById(repartidorId)
                            .addOnSuccessListener(usuario -> {
                                if (usuario instanceof Repartidor) {
                                    Repartidor repartidor = (Repartidor) usuario;

                                    // Establecer nombre del repartidor
                                    String nombreCompleto = repartidor.getNombres() + " " + repartidor.getApellidos();
                                    binding.userName.setText(nombreCompleto);

                                    // Cargar foto del repartidor
                                    if (repartidor.getFoto() != null && !repartidor.getFoto().isEmpty()) {
                                        Glide.with(this)
                                                .load(repartidor.getFoto())
                                                .placeholder(R.drawable.ic_profile)
                                                .error(R.drawable.ic_profile)
                                                .circleCrop()
                                                .into(binding.userProfileImage);
                                    } else {
                                        binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                                    }
                                } else {
                                    binding.userName.setText("Repartidor");
                                    binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Chat", "Error al obtener información del repartidor", e);
                                binding.userName.setText("Repartidor");
                                binding.userProfileImage.setImageResource(R.drawable.ic_profile);
                            });
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
        }, e -> Toast.makeText(ClienteChatActivity.this,
                "Error al cargar mensajes: " + e.getMessage(),
                Toast.LENGTH_SHORT).show());
    }

    private void sendMessage() {
        String texto = binding.messageInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        binding.sendButton.setEnabled(false);

        Mensaje mensaje = new Mensaje();
        mensaje.setChatId(chatId);
        mensaje.setEmisorId(clienteId);
        mensaje.setTexto(texto);
        mensaje.setTimestamp(new Timestamp(new Date()));
        mensaje.setTipo("texto");

        chatRepository.enviarMensaje(mensaje, unused -> {
            binding.messageInput.setText("");
            binding.sendButton.setEnabled(true);

            if (chatInfo != null) {
                chatInfo.setUltimoMensaje(texto);
                chatInfo.setTimestamp(new Timestamp(new Date()));
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