package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentHome.creatPost.CreatePost;
import com.example.nhatro360.controller.mainActivity.fragmentSearch.FragmentSearchedRoom;
import com.example.nhatro360.models.Room;
import com.example.nhatro360.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FragmentPostedRoom extends Fragment  {
    private FragmentSearchedRoom fragmentSearchedRoom;
    private FirebaseFirestore db;
    private ImageView imvBack, imvCreate;
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
        fragmentSearchedRoom = new FragmentSearchedRoom();
        db = FirebaseFirestore.getInstance();

        imvBack = view.findViewById(R.id.imv_back);
        imvCreate = view.findViewById(R.id.imv_action);

        imvBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        imvCreate.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreatePost.class);
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
                        List<String> list = user.getListPostedRoom();
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
