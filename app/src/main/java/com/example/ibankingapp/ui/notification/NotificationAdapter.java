package com.example.ibankingapp.ui.notification;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationEntity notification = list.get(position);

        if (notification == null) return;

        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());

        //thời gian
        long timestamp = notification.getTimestamp();
        holder.tvTime.setText(getTimeAgo(timestamp));

        //trạng thái Đã đọc / Chưa đọc
        if (notification.isRead()) {
            holder.ivUnreadDot.setVisibility(View.GONE);
            holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
        } else {
            holder.ivUnreadDot.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(Color.parseColor("#FF0000"));
        }

        // 4. (Tùy chọn) Đổi Icon theo loại thông báo
        if (notification.getTitle().contains("Nạp tiền")) {
            holder.ivIcon.setImageResource(R.drawable.ic_wallet); // Icon ví
        } else if (notification.getTitle().contains("Rút tiền")) {
            holder.ivIcon.setImageResource(R.drawable.ic_transfer_24); // Icon chuyển tiền
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_notify_2); // Icon mặc định
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // Hàm format thời gian kiểu "Vừa xong", "5 phút trước"
    private String getTimeAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < 60 * 1000) return "Vừa xong";
        if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + " phút trước";
        if (diff < 24 * 60 * 60 * 1000) return (diff / (60 * 60 * 1000)) + " giờ trước";

        // Nếu lâu hơn 1 ngày thì hiện ngày tháng
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(time));
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvMessage, tvTime;
        View ivUnreadDot;
        ImageView ivIcon;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotiTitle);
            tvMessage = itemView.findViewById(R.id.tvNotiMessage);
            tvTime = itemView.findViewById(R.id.tvNotiTime);
            ivUnreadDot = itemView.findViewById(R.id.ivUnreadDot);
            ivIcon = itemView.findViewById(R.id.ivNotiIcon);
        }
    }
}