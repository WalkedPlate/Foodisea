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
import com.example.foodisea.model.Mensaje;
import com.example.foodisea.model.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Mensaje> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Mensaje> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Mensaje mensaje = messageList.get(position);

        if (mensaje.getEmisorId().equals(currentUserId)) {
            holder.senderMessage.setVisibility(View.VISIBLE);
            holder.senderTime.setVisibility(View.VISIBLE);

            holder.receiverMessage.setVisibility(View.GONE);
            holder.receiverTime.setVisibility(View.GONE);

            holder.senderMessage.setText(mensaje.getTexto());
            holder.senderTime.setText(formatTimestamp(mensaje.getTimestamp()));
        } else {
            holder.receiverMessage.setVisibility(View.VISIBLE);
            holder.receiverTime.setVisibility(View.VISIBLE);

            holder.senderMessage.setVisibility(View.GONE);
            holder.senderTime.setVisibility(View.GONE);

            holder.receiverMessage.setText(mensaje.getTexto());
            holder.receiverTime.setText(formatTimestamp(mensaje.getTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "";

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(timestamp);

        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            // Mismo día, muestra hora
            return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new Date(timestamp));
        } else {
            // Día diferente, muestra solo fecha
            return new SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(new Date(timestamp));
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessage, receiverMessage, senderTime, receiverTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.senderMessage);
            receiverMessage = itemView.findViewById(R.id.receiverMessage);
            senderTime = itemView.findViewById(R.id.senderTime);
            receiverTime = itemView.findViewById(R.id.receiverTime);
        }
    }
}