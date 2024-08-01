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
import com.example.nhatro360.controller.mainActivity.fragmentHome.createRoom.CreateRoomActivity;
import com.example.nhatro360.controller.roomDetail.RoomDetailActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Date;
import com.google.firebase.Timestamp;

public class HomeFragment extends Fragment implements OnRoomClickListener, RoomAdapter.OnRoomClickListener {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerViewRooms;
    private RoomAdapter adapter;
    private List<Object> itemList;
    private ImageView imvCreate;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        init(view);
        onClickCreatePost();
        db = FirebaseFirestore.getInstance();
        fetchRoomsFromFirestore();
        return view;
    }

    private void init(View view) {
        itemList = new ArrayList<>();
        recyclerViewRooms = view.findViewById(R.id.recycler_view_list_room);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2); // 2 columns
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Headers take up 2 spans (full width), while rooms take 1 span (half width)
                return (itemList.get(position) instanceof String) ? 2 : 1;
            }
        });
        recyclerViewRooms.setLayoutManager(layoutManager);
        adapter = new RoomAdapter(itemList, this);
        recyclerViewRooms.setAdapter(adapter);
        imvCreate = view.findViewById(R.id.imv_create);
        imvCreate.setImageResource(R.drawable.ic_create);
        imvCreate.setVisibility(View.VISIBLE);
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> listGeneralRoom = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Room room = document.toObject(Room.class);
                            room.setId(document.getId());
                            if(room.getStatus() == 1) listGeneralRoom.add(room);
                        }

                        sortRoomsByTimePosted(listGeneralRoom);

                        List<Room> listNewRoom = new ArrayList<>();
                        List<Room> listParingRoom = new ArrayList<>();
                        List<Room> listApartment = new ArrayList<>();
                        List<Room> listHouse = new ArrayList<>();

                        Date now = new Date();
                        int newRoomCount = 0, paringRoomCount = 0, apartmentCount = 0, houseCount = 0;

                        for (Room room : listGeneralRoom) {
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

                        itemList.clear();
                        if (!listNewRoom.isEmpty()) {
                            itemList.add("title_new_room");
                            itemList.addAll(listNewRoom);
                        }
                        if (!listParingRoom.isEmpty()) {
                            itemList.add("title_paring_room");
                            itemList.addAll(listParingRoom);
                        }
                        if (!listApartment.isEmpty()) {
                            itemList.add("title_apartment");
                            itemList.addAll(listApartment);
                        }
                        if (!listHouse.isEmpty()) {
                            itemList.add("title_house");
                            itemList.addAll(listHouse);
                        }
                        if (!listGeneralRoom.isEmpty()) {
                            itemList.add("title_general_room");
                            itemList.addAll(listGeneralRoom);
                        }

                        adapter.notifyDataSetChanged();
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
                Intent intent = new Intent(getActivity(), CreateRoomActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onRoomClick(Room room) {
        Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
        intent.putExtra("roomId", room.getId()); // Truyền ID của document qua intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
