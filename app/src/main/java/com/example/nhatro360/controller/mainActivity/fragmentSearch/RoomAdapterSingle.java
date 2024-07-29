package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentHome.OnRoomClickListener;
import com.example.nhatro360.model.Room;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RoomAdapterSingle extends RecyclerView.Adapter<RoomAdapterSingle.RoomViewHolder> {

    private List<Room> roomList;
    private OnRoomClickListener onRoomClickListener;
    private boolean showDeleteIcon;
    private Context context;

    public RoomAdapterSingle(List<Room> roomList, OnRoomClickListener onRoomClickListener, boolean showDeleteIcon, Context context) {
        this.roomList = roomList;
        this.onRoomClickListener = onRoomClickListener;
        this.showDeleteIcon = showDeleteIcon;
        this.context = context;
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
        Log.d("RoomAdapter", "Binding room: " + room.getAddress());
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class RoomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imvImage, imvDelete;
        private TextView tvTitle, tvPrice, tvAddress, tvArea, tvTimePosted;
        private OnRoomClickListener onRoomClickListener;

        public RoomViewHolder(@NonNull View itemView, OnRoomClickListener onRoomClickListener) {
            super(itemView);
            imvImage = itemView.findViewById(R.id.item_image);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvArea = itemView.findViewById(R.id.tv_area);
            tvTimePosted = itemView.findViewById(R.id.tv_time_posted);
            imvDelete = itemView.findViewById(R.id.imv_delete);

            this.onRoomClickListener = onRoomClickListener;
            itemView.setOnClickListener(this);

            if (!showDeleteIcon) {
                imvDelete.setVisibility(View.GONE);
            } else {
                imvDelete.setVisibility(View.VISIBLE);
                imvDelete.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Room room = roomList.get(position);
                        showDeleteConfirmationDialog(room);
                    }
                });
            }
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
            tvPrice.setText(formatPrice(room.getPrice()));
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

        private void showDeleteConfirmationDialog(Room room) {
            new AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa phòng này?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        showDeletingDialog(room);
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void showDeletingDialog(Room room) {
            AlertDialog deletingDialog = new AlertDialog.Builder(context)
                    .setTitle("Đang xóa...")
                    .setView(new ProgressBar(context))
                    .setCancelable(false)
                    .show();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            List<String> imagePaths = room.getImages();

            // Function to delete images from Storage
            deleteImagesFromStorage(imagePaths, room, db, storage, deletingDialog);
        }

        private void deleteImagesFromStorage(List<String> imagePaths, Room room, FirebaseFirestore db, FirebaseStorage storage, AlertDialog deletingDialog) {
            if (imagePaths == null || imagePaths.isEmpty()) {
                // If no images to delete, proceed with deleting room
                deleteRoomFromFirestore(room, db, deletingDialog);
                return;
            }

            for (String imagePath : imagePaths) {
                StorageReference imageRef = storage.getReferenceFromUrl(imagePath);
                imageRef.delete().addOnSuccessListener(aVoid -> {
                    Log.d("RoomAdapter", "Image successfully deleted!");
                    // Check if all images are deleted
                    if (imagePaths.indexOf(imagePath) == imagePaths.size() - 1) {
                        // All images deleted, proceed with deleting room
                        deleteRoomFromFirestore(room, db, deletingDialog);
                    }
                }).addOnFailureListener(exception -> {
                    Log.w("RoomAdapter", "Error deleting image", exception);
                    deletingDialog.dismiss();
                });
            }
        }

        private void deleteRoomFromFirestore(Room room, FirebaseFirestore db, AlertDialog deletingDialog) {
            db.collection("rooms").document(room.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        db.collection("users")
                                .whereEqualTo("email", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                                        db.collection("users").document(userDoc.getId())
                                                .update("listPostedRoom", FieldValue.arrayRemove(room.getId()))
                                                .addOnSuccessListener(aVoid2 -> {
                                                    roomList.remove(room);
                                                    notifyItemRemoved(getAdapterPosition());
                                                    notifyItemRangeChanged(getAdapterPosition(), roomList.size());
                                                    Log.d("RoomAdapter", "Room successfully deleted!");
                                                    deletingDialog.dismiss();
                                                    Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("RoomAdapter", "Error deleting room ID from user", e);
                                                    deletingDialog.dismiss();
                                                });
                                    } else {
                                        deletingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    deletingDialog.dismiss();
                                    Log.w("RoomAdapter", "Error finding user", e);
                                });

                    })
                    .addOnFailureListener(e -> {
                        Log.w("RoomAdapter", "Error deleting room", e);
                        deletingDialog.dismiss();
                    });
        }


    }

    private String formatPrice(String price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        double millions = Integer.parseInt(price) / 1_000_000.0;
        return decimalFormat.format(millions) + " triệu";
    }
}
