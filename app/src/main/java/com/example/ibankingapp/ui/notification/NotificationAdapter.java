package com.example.ibankingapp.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ibankingapp.R;
import com.example.ibankingapp.entity.NotificationEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<NotificationEntity> list;
    public void setData(List<NotificationEntity> list){
        this.list = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification,parent,false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        NotificationEntity notification = list.get(position);
        holder.txtTitle.setText(notification.getTitle());
        holder.txtMessage.setText(notification.getMessage());
        long timestamp = notification.getTimestamp(); // Lấy giá trị long

        // 1. Tạo đối tượng Date
        Date date = new Date(timestamp);

        // 2. Định dạng ngày giờ theo ý muốn (ví dụ: "HH:mm dd/MM/yyyy")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        // 3. Chuyển đổi và gán vào TextView
        String formattedTime = formatter.format(date);
        holder.txtTime.setText(formattedTime);

    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMessage, txtTime;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
        }
    }
}
