package com.example.nhatro360;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import com.bumptech.glide.Glide;

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
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imvImage;
        private TextView tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
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
                        .load(room.getImages().get(0))
                        .into(imvImage);
            }
            tvPrice.setText(room.getPrice());
            tvAddress.setText(room.getAddress());
            tvArea.setText("DT " + room.getArea() +" m2");
            tvTimePosted.setText(room.getTimePosted());
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
}
