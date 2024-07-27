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
    private AccountViewModel viewModel;
    private FirebaseFirestore db;
    private ImageView imvBack, imvCreate;
    private TextView tvTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_room, container, false);

        init(view);

        return view;
    }

    private void init(View view){
        db = FirebaseFirestore.getInstance();
        imvBack = view.findViewById(R.id.imv_back);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(R.string.posted_room);
        imvCreate = view.findViewById(R.id.imv_action);
        imvCreate.setImageResource(R.drawable.icon_create2);

        imvBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            int backStackCount = fragmentManager.getBackStackEntryCount();
            if (backStackCount > 0) {
                fragmentManager.popBackStack(); // Quay về fragment trước đó
            } else {
                requireActivity().getSupportFragmentManager().popBackStack("FragmentPostedRoom", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        imvCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), CreatePost.class);
                startActivity(intent);
            }
        });

        fragmentSearchedRoom = new FragmentSearchedRoom();

        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    List<String> listPostedRoom = user.getListPostedRoom();
                    getListPostedRoom(listPostedRoom);
                }
            }
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_list_room, fragmentSearchedRoom)
                .commit();
    }

    private void getListPostedRoom(List<String> listPostedRoom) {
        CollectionReference roomsRef = db.collection("rooms");

        roomsRef.whereIn(FieldPath.documentId(), listPostedRoom)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Room> rooms = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String roomId = document.getId();
                                Room room = document.toObject(Room.class);
                                room.setId(roomId);
                                rooms.add(room);
                            }
                            Log.e(TAG, "" + rooms);
                            fragmentSearchedRoom.updateRoomList(rooms);
                        } else {
                            Log.d("Firestore", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
