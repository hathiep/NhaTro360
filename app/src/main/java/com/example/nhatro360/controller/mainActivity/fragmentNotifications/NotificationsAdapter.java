package com.example.nhatro360.controller.mainActivity.fragmentNotifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.roomDetailActivity.RoomDetailActivity;
import com.example.nhatro360.model.Notification;
import com.example.nhatro360.model.Room;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private Context context;
    private int expandedPosition = -1;

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
        holder.bind(notification, position);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvTimestamp, tvMessage, tvDescription, btnDetail;
        private Room room;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnDetail = itemView.findViewById(R.id.btn_detail);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (expandedPosition == position) {
                    expandedPosition = -1;
                } else {
                    int previousExpandedPosition = expandedPosition;
                    expandedPosition = position;
                    notifyItemChanged(previousExpandedPosition);
                }
                notifyItemChanged(position);
            });
        }

        public void bind(Notification notification, int position) {
            int type = notification.getType();
            tvMessage.setText(getMessage(type));

            setTvTime(notification.getTime());
            setTvType(notification.getType());

            boolean isExpanded = position == expandedPosition;
            tvDescription.setVisibility(isExpanded && type < 4 ? View.VISIBLE : View.GONE);
            btnDetail.setVisibility(isExpanded && type == 2 ? View.VISIBLE : View.GONE);

            if (type < 4) {
                setTvDescription(notification.getMessage());
            }

            if (type == 2 && isExpanded) {
                btnDetail.setOnClickListener(v -> {
                    Intent intent = new Intent(context, RoomDetailActivity.class);
                    intent.putExtra("roomId", notification.getMessage());
                    context.startActivity(intent);
                });
            }
        }

        private void setTvTime(Timestamp time) {
            if (time != null) {
                long timeDiff = Timestamp.now().getSeconds() - time.getSeconds();
                String timeAgo;

                if (timeDiff < TimeUnit.HOURS.toSeconds(1)) {
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
        }

        private void setTvType(Integer type) {
            switch (type) {
                case 1:
                    tvTitle.setText(context.getString(R.string.title1));
                    tvTitle.setTextColor(context.getColor(R.color.blue2));
                    break;
                case 2:
                    tvTitle.setText(context.getString(R.string.title2));
                    tvTitle.setTextColor(context.getColor(R.color.green));
                    break;
                case 3:
                    tvTitle.setText(context.getString(R.string.title3));
                    tvTitle.setTextColor(context.getColor(R.color.red2));
                    break;
                case 4:
                    tvTitle.setText(context.getString(R.string.title4));
                    tvTitle.setTextColor(context.getColor(R.color.black));
                    break;
                case 5:
                    tvTitle.setText(context.getString(R.string.title5));
                    tvTitle.setTextColor(context.getColor(R.color.black));
                    break;
                default:
                    tvTitle.setText("Thông báo khác");
                    tvTitle.setTextColor(context.getColor(R.color.black));
                    break;
            }
        }

        private String getMessage(Integer type) {
            if (type == 1) {
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

        private void setTvDescription(String roomId) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("rooms").document(roomId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            room = documentSnapshot.toObject(Room.class);
                            if (room != null) {
                                String description = "Thông tin phòng:"
                                        + "\n - Tiêu đề: " + room.getTitle()
                                        + "\n - Địa chỉ: " + room.getAddress()
                                        + "\n - Giá: " + formatPrice(room.getPrice()) + "/tháng"
                                        + "\n - Diện tích: " + room.getArea() + " m2"
                                        + "\n - Liên hệ: " + room.getPhone() + " - " + room.getHost();
                                tvDescription.setText(description);
                            } else {
                                Log.d("RoomDetailActivity", "Room object is null");
                            }
                        } else {
                            Log.d("RoomDetailActivity", "Document does not exist");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RoomDetailActivity", "Error fetching document", e);
                    });
        }

        private String formatPrice(String price) {
            DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
            double millions = Integer.parseInt(price) / 1_000_000.0;
            return decimalFormat.format(millions) + " triệu";
        }
    }
}
