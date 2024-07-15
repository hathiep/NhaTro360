package com.example.nhatro360;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RoomDetail extends AppCompatActivity {

    private static final String TAG = "RoomDetailActivity";

    private TextView tvPrice, tvTitle, tvAddress, tvContact, tvArea, tvTimePosted, tvUtilities, tvInfor;
    private List<TextView> listTvUtilites = new ArrayList<>();
    private List<ImageView> listImvUtilites = new ArrayList<>();
    private ViewPager viewPager;

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        init();

        String roomId = getIntent().getStringExtra("roomId");
        if (roomId != null) {
            fetchRoomDetailsFromFirestore(roomId);
        } else {
            Log.e("RoomDetailActivity", "Room ID is null");
        }
    }

    // Initialize views
    private void init() {
        tvPrice = findViewById(R.id.tv_price);
        tvTitle = findViewById(R.id.tv_title);
        tvAddress = findViewById(R.id.tv_address);
        tvContact = findViewById(R.id.tv_contact);
        tvArea = findViewById(R.id.tv_area);
        tvTimePosted = findViewById(R.id.tv_time_posted);
        tvUtilities = findViewById(R.id.tv_utilities);
        viewPager = findViewById(R.id.view_pager);
            listImvUtilites.add(findViewById(R.id.imv_wifi));
            listImvUtilites.add(findViewById(R.id.imv_wc));
            listImvUtilites.add(findViewById(R.id.imv_parking));
            listImvUtilites.add(findViewById(R.id.imv_free_time));
            listImvUtilites.add(findViewById(R.id.imv_kitchen));
            listImvUtilites.add(findViewById(R.id.imv_air_conditioner));
            listImvUtilites.add(findViewById(R.id.imv_fridge));
            listImvUtilites.add(findViewById(R.id.imv_washing_machine));
            listTvUtilites.add(findViewById(R.id.tv_wifi));
            listTvUtilites.add(findViewById(R.id.tv_wc));
            listTvUtilites.add(findViewById(R.id.tv_parking));
            listTvUtilites.add(findViewById(R.id.tv_free_time));
            listTvUtilites.add(findViewById(R.id.tv_kitchen));
            listTvUtilites.add(findViewById(R.id.tv_air_conditioner));
            listTvUtilites.add(findViewById(R.id.tv_fridge));
            listTvUtilites.add(findViewById(R.id.tv_washing_machine));
        tvInfor = findViewById(R.id.tv_infor);
    }

    private void fetchRoomDetailsFromFirestore(String roomId) {
        db.collection("rooms").document(roomId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Room room = documentSnapshot.toObject(Room.class);
                        if (room != null) {
                            // Hiển thị thông tin phòng lên các TextView
                            updateUI(room);
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
        tvPrice.setText(room.getPrice() + "/tháng");

        // Set up ViewPager with images
        if (room.getImages() != null && !room.getImages().isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, room.getImages());
            viewPager.setAdapter(adapter);
        }

        tvTitle.setText(room.getTitle());
        tvAddress.setText(room.getAddress());
        tvContact.setText(room.getPhone() + " - " +  room.getHost());
        tvArea.setText("DT " + room.getArea() +" m2");
        tvTimePosted.setText(room.getTimePosted());

        int num_utilities = setUtilities(room.isHasWifi(), 0) + setUtilities(room.isHasPrivateWC(), 1) + setUtilities(room.isHasParking(), 2)
                + setUtilities(room.isHasFreeTime(), 3) + setUtilities(room.isHasKitchen(), 4) + setUtilities(room.isHasAirConditioner(), 5)
                + setUtilities(room.isHasFridge(), 6) + setUtilities(room.isHasWashingMachine(), 7);
        tvUtilities.setText("Tiện ích phòng (" + num_utilities + ")");
        tvInfor.setText(room.getDetail().replaceAll("\n", "<br>"));
    }

    private int setUtilities(boolean utiliy, int i){
        if(utiliy){
            listImvUtilites.get(i).setColorFilter(ContextCompat.getColor(listImvUtilites.get(i).getContext(), R.color.blue2), PorterDuff.Mode.SRC_IN);
            listTvUtilites.get(i).setTextColor(ContextCompat.getColor(listTvUtilites.get(i).getContext(), R.color.blue2));
            return 1;
        }
        return 0;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }
}
