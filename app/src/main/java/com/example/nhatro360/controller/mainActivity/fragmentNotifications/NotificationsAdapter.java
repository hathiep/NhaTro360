package com.example.nhatro360.controller.mainActivity.fragmentNotifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.model.Notification;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private Context context;

    public NotificationsAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMessage, tvTimestamp, tvType;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvType = itemView.findViewById(R.id.tv_type);
        }

        public void bind(Notification notification) {
            tvMessage.setText(setMessage(notification.getType()));

            Timestamp time = notification.getTime();
            if (time != null) {
                long timeDiff = Timestamp.now().getSeconds() - time.getSeconds();
                String timeAgo;

                if (timeDiff < TimeUnit.MINUTES.toSeconds(1)) {
                    timeAgo = "Ngay lúc này";
                } else if (timeDiff < TimeUnit.HOURS.toSeconds(1)) {
                    long minutes = TimeUnit.SECONDS.toMinutes(timeDiff);
                    timeAgo = minutes + " phút trước";
                } else if (timeDiff < TimeUnit.DAYS.toSeconds(1)) {
                    long hours = TimeUnit.SECONDS.toHours(timeDiff);
                    timeAgo = hours + " giờ trước";
                } else {
                    long days = TimeUnit.SECONDS.toDays(timeDiff);
                    timeAgo = days + " ngày trước";
                }

                tvTimestamp.setText(timeAgo);
            }

            int type = notification.getType();
            switch (type) {
                case 1:
                    tvType.setText("Tạo phòng thành công");
                    tvType.setTextColor(context.getColor(R.color.blue2));
                    break;
                case 2:
                    tvType.setText("Quản trị viên đã duyệt phòng");
                    tvType.setTextColor(context.getColor(R.color.green));
                    break;
                case 3:
                    tvType.setText("Quản trị viên đã từ chối phòng");
                    tvType.setTextColor(context.getColor(R.color.red2));
                    break;
                case 4:
                    tvType.setText("Phòng mới hôm nay");
                    tvType.setTextColor(context.getColor(R.color.black));
                    break;
                case 5:
                    tvType.setText("Đừng bỏ lỡ phòng nhé");
                    tvType.setTextColor(context.getColor(R.color.black));
                    break;
                default:
                    tvType.setText("Thông báo khác");
                    break;
            }
        }

        private String setMessage(Integer type){
            if(type == 1){
                return context.getString(R.string.message1);
            } else if (type == 2) {
                return context.getString(R.string.message2);
            } else if (type == 3) {
                return context.getString(R.string.message3);
            } else if (type == 4) {
                return context.getString(R.string.message4);
            }
            return context.getString(R.string.message5);
        }

    }
}
