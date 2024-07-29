package com.example.nhatro360.controller.mainActivity.fragmentHome;

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

import com.example.nhatro360.R;
import com.example.nhatro360.model.Room;
import com.example.nhatro360.controller.mainActivity.fragmentHome.createRoom.CreateRoom;
import com.example.nhatro360.controller.roomDetail.RoomDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;
import com.google.firebase.Timestamp;

public class HomeFragment extends Fragment implements OnRoomClickListener {

    private static final String TAG = "HomeFragment";
    private RecyclerView revNewRoom, revParingRoom, revApartment, revHouse, revGeneralRoom;
    private RoomAdapter adapterNewRoom, adapterParingRoom, adapterApartment, adapterHouse, adapterGeneralRoom;
    private List<Room> listNewRoom, listParingRoom, listApartment, listHouse, listGeneralRoom;
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
        listApartment = new ArrayList<>();
        listHouse = new ArrayList<>();
        listGeneralRoom = new ArrayList<>();

        // Khởi tạo RecyclerView và Adapter
        revNewRoom = view.findViewById(R.id.recycler_view_new_room);
        revParingRoom = view.findViewById(R.id.recycler_view_paring_room);
        revApartment = view.findViewById(R.id.recycler_view_apartment);
        revHouse = view.findViewById(R.id.recycler_view_house);
        revGeneralRoom = view.findViewById(R.id.recycler_view_general_room);
        revNewRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revParingRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revApartment.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revHouse.setLayoutManager(new GridLayoutManager(getContext(), 2));
        revGeneralRoom.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapterNewRoom = new RoomAdapter(listNewRoom, this);
        adapterParingRoom = new RoomAdapter(listParingRoom, this);
        adapterApartment = new RoomAdapter(listApartment, this);
        adapterHouse = new RoomAdapter(listHouse, this);
        adapterGeneralRoom = new RoomAdapter(listGeneralRoom, this);
        revNewRoom.setAdapter(adapterNewRoom);
        revParingRoom.setAdapter(adapterParingRoom);
        revApartment.setAdapter(adapterApartment);
        revHouse.setAdapter(adapterHouse);
        revGeneralRoom.setAdapter(adapterGeneralRoom);
        imvCreate = view.findViewById(R.id.imv_create);
        imvCreate.setImageResource(R.drawable.ic_create);
        imvCreate.setVisibility(View.VISIBLE);
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listNewRoom.clear();
                        listParingRoom.clear();
                        listApartment.clear();
                        listHouse.clear();
                        listGeneralRoom.clear();

                        Date now = new Date();
                        int newRoomCount = 0, paringRoomCount = 0, apartmentCount = 0, houseCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId(); // Lấy ID của tài liệu Firestore
                            Room room = document.toObject(Room.class);
                            room.setId(roomId);
                            listGeneralRoom.add(room);
                        }
                        sortRoomsByTimePosted(listGeneralRoom);
                        for (Room room : listGeneralRoom){
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
                            if (room.getPostType() == 2 && paringRoomCount < 6) {
                                listParingRoom.add(room);
                                paringRoomCount++;
                            }
                            if ((room.getRoomType() == 2 || room.getRoomType() == 3) && apartmentCount < 6) {
                                listApartment.add(room);
                                apartmentCount++;
                            }
                            if (room.getRoomType() == 4 && houseCount < 6) {
                                listHouse.add(room);
                                houseCount++;
                            }
                        }
                        adapterNewRoom.notifyDataSetChanged();
                        adapterParingRoom.notifyDataSetChanged();
                        adapterApartment.notifyDataSetChanged();
                        adapterHouse.notifyDataSetChanged();
                        adapterGeneralRoom.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void sortRoomsByTimePosted(List<Room> rooms) {
        Collections.sort(rooms, new Comparator<Room>() {
            @Override
            public int compare(Room room1, Room room2) {
                if (room1.getTimePosted() == null || room2.getTimePosted() == null) {
                    return 0;
                }
                return room2.getTimePosted().compareTo(room1.getTimePosted());
            }
        });
    }

    private void onClickCreatePost(){
        imvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Create button clicked");
                Intent intent = new Intent(getActivity(), CreateRoom.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getAddress());

        Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
        intent.putExtra("roomId", room.getId()); // Truyền ID của document qua intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
