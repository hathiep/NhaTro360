package com.example.nhatro360.mainActivity.fragmentAccount;

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
import com.example.nhatro360.mainActivity.fragmentSearch.SearchedRoomsFragment;
import com.example.nhatro360.model.Room;
import com.example.nhatro360.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class SavedRoomsFragment extends Fragment  {
    private SearchedRoomsFragment fragmentSearchedRoom;
    private FirebaseFirestore db;
    private ImageView imvBack, imvUnsaved;
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
        db = FirebaseFirestore.getInstance();
        imvBack = view.findViewById(R.id.imv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.saved_room);
        imvUnsaved = view.findViewById(R.id.imv_action);
        imvUnsaved.setImageResource(0);

        imvBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.popBackStack();
        });


        fragmentSearchedRoom = new SearchedRoomsFragment();

        email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        getCurrentUser();

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
                        List<String> list = user.getSavedRooms();
                        if(list.size() == 0) list.add("x");
                        getsavedRooms(list);
                    }
                });
    }

    private void getsavedRooms(List<String> savedRooms) {
        CollectionReference roomsRef = db.collection("rooms");

        roomsRef.whereIn(FieldPath.documentId(), savedRooms)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Room> rooms = new ArrayList<>();
                        List<String> foundRoomIds = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            String roomId = document.getId();
                            Room room = document.toObject(Room.class);
                            room.setId(roomId);
                            rooms.add(room);
                            foundRoomIds.add(roomId);
                        }
                        fragmentSearchedRoom.updateRoomList(rooms);

                        // Loại bỏ các roomId không có trong kết quả truy vấn khỏi savedRooms
                        List<String> roomsToRemove = new ArrayList<>(savedRooms);
                        roomsToRemove.removeAll(foundRoomIds);

                        if (!roomsToRemove.isEmpty()) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                            db.collection("users")
                                    .whereEqualTo("email", userEmail)
                                    .get()
                                    .addOnCompleteListener(userTask -> {
                                        if (userTask.isSuccessful() && !userTask.getResult().isEmpty()) {
                                            DocumentSnapshot userDoc = userTask.getResult().getDocuments().get(0);
                                            db.collection("users").document(userDoc.getId())
                                                    .update("savedRooms", FieldValue.arrayRemove(roomsToRemove.toArray()))
                                                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Removed invalid room IDs from savedRooms"))
                                                    .addOnFailureListener(e -> Log.w("Firestore", "Error removing invalid room IDs", e));
                                        } else {
                                            Log.d("Firestore", "Error getting user document: ", userTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                    }
                });

    }
}