package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentHome.OnRoomClickListener;
import com.example.nhatro360.models.Room;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class RoomAdapterSingle extends RecyclerView.Adapter<RoomAdapterSingle.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener onRoomClickListener;

    public RoomAdapterSingle(List<Room> roomList, OnRoomClickListener onRoomClickListener) {
        this.roomList = roomList;
        this.onRoomClickListener = onRoomClickListener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room_single, parent, false);
        return new RoomViewHolder(itemView, onRoomClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);
        Log.d("RoomAdapter", "Binding room: " + room.getAddress()); // Thêm log
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imvImage;
        private TextView tvTitle,tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
            imvImage = itemView.findViewById(R.id.item_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            this.onRoomClickListener = onRoomClickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(Room room) {
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                ViewTreeObserver viewTreeObserver = imvImage.getViewTreeObserver();

                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        imvImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        int width = imvImage.getWidth();
                        int height = (width * 4) / 3;
                        imvImage.getLayoutParams().height = height;
                        imvImage.requestLayout();
                    }
                });
                Glide.with(imvImage.getContext())
                        .load(room.getImages().get(room.getAvatar()))
                        .into(imvImage);
            }
            tvTitle.setText(room.getTitle());
            tvPrice.setText(room.getPrice());
            tvAddress.setText(room.getAddress());
            tvArea.setText("DT " + room.getArea() + " m2");

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
                onRoomClickListener.onRoomClick(room);
            }
        }
    }
}
