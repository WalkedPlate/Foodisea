package com.example.foodisea.adapter.repartidor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.models.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList; // Lista de mensajes

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar el layout de item_message.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Si el mensaje es del usuario actual (remitente)
        if (message.isSentByCurrentUser()) {
            holder.senderMessage.setVisibility(View.VISIBLE);
            holder.senderProfileImage.setVisibility(View.VISIBLE);
            holder.senderTime.setVisibility(View.VISIBLE);

            holder.receiverMessage.setVisibility(View.GONE);
            holder.receiverProfileImage.setVisibility(View.GONE);
            holder.receiverTime.setVisibility(View.GONE);

            holder.senderMessage.setText(message.getText());
            holder.senderProfileImage.setImageResource(message.getProfileImageResId());
            holder.senderTime.setText(message.getTime()); // Asigna la hora del mensaje
        } else {
            // Si el mensaje es del receptor
            holder.receiverMessage.setVisibility(View.VISIBLE);
            holder.receiverProfileImage.setVisibility(View.VISIBLE);
            holder.receiverTime.setVisibility(View.VISIBLE);

            holder.senderMessage.setVisibility(View.GONE);
            holder.senderProfileImage.setVisibility(View.GONE);
            holder.senderTime.setVisibility(View.GONE);

            holder.receiverMessage.setText(message.getText());
            holder.receiverProfileImage.setImageResource(message.getProfileImageResId());
            holder.receiverTime.setText(message.getTime()); // Asigna la hora del mensaje
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size(); // Tamaño de la lista de mensajes
    }

    // ViewHolder interno para el RecyclerView
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, receiverMessage, senderTime, receiverTime;
        public ImageView senderProfileImage, receiverProfileImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.senderMessage);
            receiverMessage = itemView.findViewById(R.id.receiverMessage);
            senderProfileImage = itemView.findViewById(R.id.senderProfileImage);
            receiverProfileImage = itemView.findViewById(R.id.receiverProfileImage);
            senderTime = itemView.findViewById(R.id.senderTime);
            receiverTime = itemView.findViewById(R.id.receiverTime);
        }
    }
}
