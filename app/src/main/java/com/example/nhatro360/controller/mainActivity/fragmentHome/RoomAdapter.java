package com.example.nhatro360.controller.mainActivity.fragmentHome;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhatro360.R;
import com.example.nhatro360.models.Room;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener onRoomClickListener;

    public RoomAdapter(List<Room> roomList, OnRoomClickListener onRoomClickListener) {
        this.roomList = roomList;
        this.onRoomClickListener = onRoomClickListener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(itemView, onRoomClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        holder.bind(room);

        // Tính toán chiều cao cho RelativeLayout theo tỉ lệ 16:9
        holder.itemView.post(() -> {
            int width = holder.relativeLayout.getWidth();
            int height = (int) (width * 10.0 / 16.0);
            ViewGroup.LayoutParams layoutParams = holder.relativeLayout.getLayoutParams();
            layoutParams.height = height;
            holder.relativeLayout.setLayoutParams(layoutParams);
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout relativeLayout;
        private ImageView imvImage;
        private TextView tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            imvImage = itemView.findViewById(R.id.item_image);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            this.onRoomClickListener = onRoomClickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(Room room) {
            // Load the first image from the images list using Glide
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                Glide.with(imvImage.getContext())
                        .load(room.getImages().get(room.getAvatar()))
                        .into(imvImage);
            }
            tvPrice.setText(formatPrice(room.getPrice()));
            tvAddress.setText(room.getAddress());
            tvArea.setText("DT " + room.getArea() + " m2");

            // Calculate time difference and set tvTimePosted
            Timestamp timePosted = room.getTimePosted();
            long timeDiff = Timestamp.now().getSeconds() - timePosted.getSeconds();

            if (timeDiff < TimeUnit.HOURS.toSeconds(1)) {
                long minutes = TimeUnit.SECONDS.toMinutes(timeDiff);
                tvTimePosted.setText(minutes + " phút");
            } else if (timeDiff < TimeUnit.DAYS.toSeconds(1)) {
                long hours = TimeUnit.SECONDS.toHours(timeDiff);
                tvTimePosted.setText(hours + " giờ");
            } else {
                long days = TimeUnit.SECONDS.toDays(timeDiff);
                tvTimePosted.setText(days + " ngày");
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Room room = roomList.get(position);
                Log.d("RoomAdapter", "Room clicked: " + room.getAddress());
                onRoomClickListener.onRoomClick(room);
            }
        }
    }

    private String formatPrice(String price){
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        double millions = Integer.parseInt(price) / 1_000_000.0;
        return decimalFormat.format(millions) + " triệu";
    }

}
