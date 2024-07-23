package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.models.Room;

import java.util.ArrayList;
import java.util.List;

public class FragmentSingleListRoom extends Fragment {

    private RecyclerView recyclerView;
    private RoomAdapterSingle adapter;
    private List<Room> roomList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_list_room, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_room_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(new ColorDrawable(Color.LTGRAY)); // Chỉnh màu sắc đường kẻ

        recyclerView.addItemDecoration(divider);

        roomList = new ArrayList<>();
        adapter = new RoomAdapterSingle(roomList, room -> {
            // Handle room click
        });
        recyclerView.setAdapter(adapter);

        // Load search results if available
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("searchResults")) {
            ArrayList<Room> searchResults = bundle.getParcelableArrayList("searchResults");
            if (searchResults != null) {
                roomList.clear();
                roomList.addAll(searchResults);
                adapter.notifyDataSetChanged();
            }
        }

        return view;
    }

    public void updateRoomList(List<Room> newRooms) {
        if (newRooms != null) {
            roomList.clear();
            roomList.addAll(newRooms);
            adapter.notifyDataSetChanged();
        }
    }
}
