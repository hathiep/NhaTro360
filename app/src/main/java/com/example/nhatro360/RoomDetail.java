package com.example.nhatro360;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RoomDetail extends AppCompatActivity {

    private static final String TAG = "RoomDetailActivity";

    private TextView tvPrice, tvAddress, tvArea, tvTimePosted;
    private ImageView imageWifi, imageWC, imageParking, imageFreeTime, imageKitchen, imageAirConditioner, imageFridge, imageWashingMachine;
    private ViewPager viewPager;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvPrice = findViewById(R.id.tv_price);
        tvAddress = findViewById(R.id.tv_address);
        tvArea = findViewById(R.id.tv_area);
        tvTimePosted = findViewById(R.id.tv_time_posted);

        imageWifi = findViewById(R.id.image_wifi);
        imageWC = findViewById(R.id.image_wc);
        imageParking = findViewById(R.id.image_parking);
        imageFreeTime = findViewById(R.id.image_free_time);
        imageKitchen = findViewById(R.id.image_kitchen);
        imageAirConditioner = findViewById(R.id.image_air_conditioner);
        imageFridge = findViewById(R.id.image_fridge);
        imageWashingMachine = findViewById(R.id.image_washing_machine);

        viewPager = findViewById(R.id.view_pager);

        String roomId = getIntent().getStringExtra("roomId");
        if (roomId != null) {
            fetchRoomDetailsFromFirestore(roomId);
        } else {
            Log.e("RoomDetailActivity", "Room ID is null");
        }
    }

    private void fetchRoomDetailsFromFirestore(String roomId) {
        db.collection("rooms").document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Room room = documentSnapshot.toObject(Room.class);
                        if (room != null) {
                            // Hiển thị thông tin phòng lên các TextView
                            tvPrice.setText(room.getPrice());
                            tvAddress.setText(room.getAddress());
                            tvArea.setText(room.getArea());
                            tvTimePosted.setText(room.getTimePosted());

                            // Các xử lý khác nếu cần
                        } else {
                            Log.d("RoomDetailActivity", "No such document");
                        }
                    } else {
                        Log.d("RoomDetailActivity", "Document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RoomDetailActivity", "Error fetching document", e);
                });
    }

    private void updateUI(Room room) {
        // Set text views
        tvPrice.setText(room.getPrice());
        tvAddress.setText(room.getAddress());
        tvArea.setText(room.getArea());
        tvTimePosted.setText(room.getTimePosted());

        // Set up ViewPager with images
        if (room.getImageResourceIds() != null && !room.getImageResourceIds().isEmpty()) {
            int[] imageResourcesArray = new int[room.getImageResourceIds().size()];
            for (int i = 0; i < room.getImageResourceIds().size(); i++) {
                imageResourcesArray[i] = room.getImageResourceIds().get(i);
            }
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), imageResourcesArray);
            viewPager.setAdapter(adapter);
        }

        // Set visibility and color filter for amenities icons
        setImageVisibilityAndColor(imageWifi, room.isHasWifi());
        setImageVisibilityAndColor(imageWC, room.isHasPrivateWC());
        setImageVisibilityAndColor(imageParking, room.isHasParking());
        setImageVisibilityAndColor(imageFreeTime, room.isHasFreeTime());
        setImageVisibilityAndColor(imageKitchen, room.isHasKitchen());
        setImageVisibilityAndColor(imageAirConditioner, room.isHasAirConditioner());
        setImageVisibilityAndColor(imageFridge, room.isHasFridge());
        setImageVisibilityAndColor(imageWashingMachine, room.isHasWashingMachine());
    }

    // Helper method to set visibility and color filter for amenity icons
    private void setImageVisibilityAndColor(ImageView imageView, boolean isVisible) {
        if (isVisible) {
            imageView.setVisibility(View.VISIBLE);
            imageView.setColorFilter(getResources().getColor(R.color.blue));
        } else {
            imageView.setVisibility(View.GONE);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

}

