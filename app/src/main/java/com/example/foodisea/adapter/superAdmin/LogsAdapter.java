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
import com.example.foodisea.entity.LogItem;

import java.util.List;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    private List<LogItem> logList;
    private Context context;

    public LogsAdapter(Context context, List<LogItem> logList) {
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
        LogItem currentItem = logList.get(position);

        holder.tvLogMessage.setText(currentItem.getMessage());
        holder.tvLogTimestamp.setText(currentItem.getTimestamp());
        holder.ivIcon.setImageResource(currentItem.getIconResourceId());
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public void updateLogs(List<LogItem> newLogs) {
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
}
