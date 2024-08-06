package com.example.nhatro360.controller.mainActivity.fragmentSearch;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentHome.OnRoomClickListener;
import com.example.nhatro360.controller.roomDetailActivity.RoomDetailActivity;
import com.example.nhatro360.model.Room;

import java.util.ArrayList;
import java.util.List;

public class SearchedRoomsFragment extends Fragment implements OnRoomClickListener {
    private TextView tvEmptyMessage;
    private RecyclerView recyclerView;
    private RoomAdapterSingle adapter;
    private List<Room> roomList;
    private boolean showDeleteIcon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single_list_room, container, false);

        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        recyclerView = view.findViewById(R.id.recycler_view_room_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(new ColorDrawable(Color.LTGRAY));

        recyclerView.addItemDecoration(divider);

        roomList = new ArrayList<>();

        if (getArguments() != null) {
            showDeleteIcon = getArguments().getBoolean("showDeleteIcon", false);
        }

        adapter = new RoomAdapterSingle(roomList, this, showDeleteIcon, getContext());
        recyclerView.setAdapter(adapter);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("searchResults")) {
            ArrayList<Room> searchResults = bundle.getParcelableArrayList("searchResults");
            updateRoomList(searchResults);
        }

        return view;
    }

    public void updateRoomList(List<Room> newRoomList) {
        tvEmptyMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        if(newRoomList == null || newRoomList.size() == 0){
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        roomList.clear();
        roomList.addAll(newRoomList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRoomClick(Room room) {
        Log.d(TAG, "Room clicked: " + room.getAddress());
        Log.d(TAG, "Room ID: " + room.getId());

        Intent intent = new Intent(getActivity(), RoomDetailActivity.class);
        intent.putExtra("roomId", room.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
