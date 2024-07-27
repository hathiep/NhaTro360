package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.mainActivity.fragmentSearch.FragmentSearchedRoom;
import com.example.nhatro360.models.Room;
import com.example.nhatro360.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FragmentSavedRoom extends Fragment  {
    private FragmentSearchedRoom fragmentSearchedRoom;
    private AccountViewModel viewModel;
    private FirebaseFirestore db;
    private ImageView imvBack, imvUnsaved;
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
        tvTitle.setText(R.string.saved_room);
        imvUnsaved = view.findViewById(R.id.imv_action);
        imvUnsaved.setImageResource(0);

        imvBack.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireFragmentManager();
            int backStackCount = fragmentManager.getBackStackEntryCount();
            if (backStackCount > 0) {
                fragmentManager.popBackStack(); // Quay về fragment trước đó
            } else {
                requireActivity().getSupportFragmentManager().popBackStack("FragmentSavedRoom", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        fragmentSearchedRoom = new FragmentSearchedRoom();

        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        viewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                if (user != null) {
                    List<String> listSavedRoom = user.getListSavedRoom();
                    getListSavedRoom(listSavedRoom);
                }
            }
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_list_room, fragmentSearchedRoom)
                .commit();
    }

    private void getListSavedRoom(List<String> listSavedRoom) {
        CollectionReference roomsRef = db.collection("rooms");

        roomsRef.whereIn(FieldPath.documentId(), listSavedRoom)
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
