package com.example.foodisea.adapter.superAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
import com.example.foodisea.item.LogItem;
import com.example.foodisea.model.AppLog;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    private List<AppLog> logList;
    private Context context;

    public LogsAdapter(Context context, List<AppLog> logList) {
        this.context = context;
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_log_notification, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        AppLog currentItem = logList.get(position);

        holder.tvLogMessage.setText(currentItem.getDetails());

        holder.tvLogTimestamp.setText("Hace " + convertTimestampToBestUnit(currentItem.getTimestamp()));
        //holder.ivIcon.setImageResource(currentItem.getIconResourceId());
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void updateLogs(List<AppLog> newLogs) {
        this.logList = newLogs;
        notifyDataSetChanged();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLogMessage;
        TextView tvLogTimestamp;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvLogMessage = itemView.findViewById(R.id.tvLogMessage);
            tvLogTimestamp = itemView.findViewById(R.id.tvLogTimestamp);
        }
    }

    public static String convertTimestampToBestUnit(long timestamp) {

        long seconds = getSecondsElapsed(timestamp);
        // Convertir el timestamp a segundos
        //long seconds = timestamp / 1000;

        // Si es menos de un minuto
        if (seconds < 60) {
            return seconds + " segundos";
        }
        // Si es menos de una hora
        else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minutos";
        }
        // Si es menos de un día
        else if (seconds < 86400) { // 24 horas * 60 minutos * 60 segundos
            long hours = seconds / 3600;
            return hours + " horas";
        }
        // Si es menos de una semana
        else if (seconds < 604800) { // 7 días * 24 horas * 60 minutos * 60 segundos
            long days = seconds / 86400;
            return days + " días";
        }
        // Si es menos de un mes
        else if (seconds < 2592000) { // 30 días * 24 horas * 60 minutos * 60 segundos
            long weeks = seconds / 604800; // Número de semanas
            return weeks + " semanas";
        }
        // Si es menos de un año
        else if (seconds < 31536000) { // 12 meses * 30 días * 24 horas * 60 minutos * 60 segundos
            long months = seconds / 2592000;
            return months + " meses";
        }
        // Si es un año o más
        else {
            long years = seconds / 31536000;
            return years + " años";
        }
    }
    public static long getSecondsElapsed(long timestampInicial) {
        long timestampActual = System.currentTimeMillis(); // Obtener el tiempo actual en milisegundos
        long diferenciaMilisegundos = timestampActual - timestampInicial; // Diferencia en milisegundos
        return diferenciaMilisegundos / 1000; // Convertir la diferencia a segundos
    }

}
