package com.example.nhatro360;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnRoomClickListener {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerView;
    private RoomAdapter adapter;
    private List<Room> roomList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo danh sách các phòng
        roomList = new ArrayList<>();

        // Khởi tạo RecyclerView và Adapter
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new RoomAdapter(roomList, this);
        recyclerView.setAdapter(adapter);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Lấy dữ liệu từ Firestore
        fetchRoomsFromFirestore();

        return view;
    }

    private void fetchRoomsFromFirestore() {
        db.collection("rooms")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        roomList.clear(); // Xóa danh sách trước khi thêm dữ liệu mới
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId(); // Lấy ID của tài liệu Firestore
                            Room room = document.toObject(Room.class);
                            room.setId(roomId); // Đặt ID của tài liệu vào Room
                            roomList.add(room);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
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
