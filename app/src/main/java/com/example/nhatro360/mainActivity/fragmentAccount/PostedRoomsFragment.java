package com.example.nhatro360.mainActivity.fragmentAccount;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.nhatro360.R;
import com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity.CreateRoomActivity;
import com.example.nhatro360.mainActivity.fragmentSearch.SearchedRoomsFragment;
import com.example.nhatro360.model.Room;
import com.example.nhatro360.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class PostedRoomsFragment extends Fragment  {
    private SearchedRoomsFragment fragmentSearchedRoom;
    private FirebaseFirestore db;
    private ImageView imvBack, imvCreate;
    private TextView tvTitle;
    private String email;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_room, container, false);

        init(view);

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        return view;
    }

    private void init(View view){
        fragmentSearchedRoom = new SearchedRoomsFragment();
        db = FirebaseFirestore.getInstance();
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.posted_room);
        imvBack = view.findViewById(R.id.imv_back);
        imvCreate = view.findViewById(R.id.imv_action);

        imvBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        imvCreate.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateRoomActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getCurrentUser();

        Bundle bundle = new Bundle();
        bundle.putBoolean("showDeleteIcon", true);
        fragmentSearchedRoom.setArguments(bundle);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_list_room, fragmentSearchedRoom)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        getCurrentUser();
    }

    private void getCurrentUser() {
        db.collection("users").whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                        User user = userDoc.toObject(User.class);
                        List<String> list = user.getPostedRooms();
                        if(list.size() == 0) list.add("x");
                        getListPostedRoom(list);
                    }
                });
    }

    private void getListPostedRoom(List<String> listPostedRoom) {
        CollectionReference roomsRef = db.collection("rooms");

        roomsRef.whereIn(FieldPath.documentId(), listPostedRoom)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> rooms = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId();
                            Room room = document.toObject(Room.class);
                            room.setId(roomId);
                            rooms.add(room);
                        }
                        fragmentSearchedRoom.updateRoomList(rooms);
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }
}
