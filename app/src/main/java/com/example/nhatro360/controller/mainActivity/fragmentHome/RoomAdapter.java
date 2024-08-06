package com.example.nhatro360.controller.mainActivity.fragmentHome;

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
import com.example.nhatro360.model.Room;
import com.google.firebase.Timestamp;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROOM = 1;
    private static List<Object> itemList;
    private OnRoomClickListener onRoomClickListener;

    public RoomAdapter(List<Object> itemList, OnRoomClickListener onRoomClickListener) {
        this.itemList = itemList;
        this.onRoomClickListener = onRoomClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return (itemList.get(position) instanceof String) ? TYPE_HEADER : TYPE_ROOM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_room, parent, false);
            return new TitleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
            return new RoomViewHolder(view, onRoomClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            String header = (String) itemList.get(position);
            ((TitleViewHolder) holder).bind(header);
        } else if (holder instanceof RoomViewHolder) {
            Room room = (Room) itemList.get(position);
            ((RoomViewHolder) holder).bind(room);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvAction;
        private ImageView imvIcon;

        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            imvIcon = itemView.findViewById(R.id.imv_icon);
            tvAction = itemView.findViewById(R.id.tv_action);
        }

        public void bind(String header) {
            if ("title_new_room".equals(header)) {
                tvTitle.setText(R.string.new_room);
                imvIcon.setImageResource(R.drawable.ic_new_room);
                tvAction.setText("");
//                tvAction.setOnClickListener(view -> {
//
//                });
            } else if ("title_paring_room".equals(header)) {
                tvTitle.setText(R.string.pairing_room);
                imvIcon.setImageResource(R.drawable.ic_paring_room);
                tvAction.setText("");
//                tvAction.setOnClickListener(view -> {
//
//                });
            }
            else if ("title_apartment".equals(header)) {
                tvTitle.setText(R.string.apartment);
                imvIcon.setImageResource(R.drawable.ic_apartment);
                tvAction.setText("");
//                tvAction.setOnClickListener(view -> {
//
//                });
            }
            else if ("title_house".equals(header)) {
                tvTitle.setText(R.string.house);
                imvIcon.setImageResource(R.drawable.ic_house);
                tvAction.setText("");
//                tvAction.setOnClickListener(view -> {
//
//                });
            }
            if ("title_general_room".equals(header)) {
                tvTitle.setText(R.string.general_room);
                imvIcon.setImageResource(R.drawable.ic_general_room);
                tvAction.setText("");
            }
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imvImage;
        private TextView tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;
        private RelativeLayout relativeLayout;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
            imvImage = itemView.findViewById(R.id.item_image);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            relativeLayout = itemView.findViewById(R.id.relative_layout);
            this.onRoomClickListener = onRoomClickListener;
            itemView.setOnClickListener(this);
        }

        public void bind(Room room) {
            if (room.getImages() != null && !room.getImages().isEmpty()) {
                Glide.with(imvImage.getContext())
                        .load(room.getImages().get(room.getAvatar()))
                        .into(imvImage);
            }
            tvPrice.setText(formatPrice(room.getPrice()));
            tvAddress.setText(room.getAddress());
            tvArea.setText("DT " + room.getArea() + " m²");

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

            itemView.post(() -> {
                int width = relativeLayout.getWidth();
                int height = (int) (width * 10.0 / 16.0);
                ViewGroup.LayoutParams layoutParams = relativeLayout.getLayoutParams();
                layoutParams.height = height;
                relativeLayout.setLayoutParams(layoutParams);
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Room room = (Room) itemList.get(position);
                onRoomClickListener.onRoomClick(room);
            }
        }

        private static String formatPrice(String price) {
            DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
            double millions = Integer.parseInt(price) / 1_000_000.0;
            return decimalFormat.format(millions) + " triệu";
        }
    }

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }
}
