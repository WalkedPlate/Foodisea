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
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final List<Mensaje> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Mensaje> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged(); // Actualizar vista si cambia el usuario
    }

    @Override
    public int getItemViewType(int position) {
        Mensaje mensaje = messageList.get(position);
        if (mensaje.getEmisorId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        }
        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Mensaje mensaje = messageList.get(position);
        holder.messageText.setText(mensaje.getTexto());
        holder.messageTime.setText(formatTimestamp(mensaje.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "";

        // Convertir Timestamp a milisegundos
        long milliseconds = timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000;

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTimeInMillis(milliseconds);

        Calendar now = Calendar.getInstance();

        if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
            // Mismo día, muestra hora
            return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                    .format(new Date(milliseconds));
        } else {
            // Día diferente, muestra solo fecha
            return new SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(new Date(milliseconds));
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView messageTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}