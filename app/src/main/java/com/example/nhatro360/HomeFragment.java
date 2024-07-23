package com.example.nhatro360;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import com.google.firebase.Timestamp;

public class HomeFragment extends Fragment implements OnRoomClickListener {

    private static final String TAG = "HomeFragment";
    private RecyclerView revNewRoom, revParingRoom, revGeneralRoom;
    private RoomAdapter adapterNewRoom, adapterParingRoom, adapterGeneralRoom;
    private List<Room> listNewRoom, listParingRoom, listGeneralRoom;
    private ImageView imvCreate;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        init(view);
        onClickCreatePost();
        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        // Lấy dữ liệu từ Firestore
        fetchRoomsFromFirestore();
        return view;
    }

    private void init(View view) {
        // Khởi tạo danh sách các phòng
        listNewRoom = new ArrayList<>();
        listParingRoom = new ArrayList<>();
        listGeneralRoom = new ArrayList<>();

        // Khởi tạo RecyclerView và Adapter
        revNewRoom = view.findViewById(R.id.recycler_view_new_room);
        revParingRoom = view.findViewById(R.id.recycler_view_paring_room);
        revGeneralRoom = view.findViewById(R.id.recycler_view_general_room);
        revNewRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revParingRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revGeneralRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapterNewRoom = new RoomAdapter(listNewRoom, this);
        adapterParingRoom = new RoomAdapter(listParingRoom, this);
        adapterGeneralRoom = new RoomAdapter(listGeneralRoom, this);
        revNewRoom.setAdapter(adapterNewRoom);
        revParingRoom.setAdapter(adapterParingRoom);
        revGeneralRoom.setAdapter(adapterGeneralRoom);
        imvCreate = view.findViewById(R.id.imV_create);
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listNewRoom.clear();
                        listParingRoom.clear();
                        listGeneralRoom.clear();

                        Date now = new Date();
                        int newRoomCount = 0, paringRoomCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId(); // Lấy ID của tài liệu Firestore
                            Room room = document.toObject(Room.class);
                            room.setId(roomId); // Đặt ID của tài liệu vào Room

                            // Kiểm tra thời gian đăng
                            Timestamp timePosted = room.getTimePosted();
                            if (timePosted != null) {
                                Date postedDate = timePosted.toDate();
                                long diffInMillis = now.getTime() - postedDate.getTime();
                                long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);
                                if (diffInDays < 1 && newRoomCount < 6) {
                                    listNewRoom.add(room);
                                    newRoomCount++;
                                }
                            }

                            // Kiểm tra loại tin
                            if (room.getPostType() == 2 && newRoomCount < 6) {
                                listParingRoom.add(room);
                            }
                            listGeneralRoom.add(room);
                        }
                        adapterNewRoom.notifyDataSetChanged();
                        adapterParingRoom.notifyDataSetChanged();
                        adapterGeneralRoom.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void onClickCreatePost(){
        imvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Create button clicked");
                Intent intent = new Intent(getActivity(), CreatePost.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getAddress());

        Intent intent = new Intent(getActivity(), RoomDetail.class);
        intent.putExtra("roomId", room.getId()); // Truyền ID của document qua intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
