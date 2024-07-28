package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhatro360.R;
import com.example.nhatro360.models.Room;
import com.example.nhatro360.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.fragment.app.FragmentManager;


public class AccountFragment extends Fragment {
    private ImageView imvEdit, imvPostedRoom, imvSavedRoom;
    private AccountViewModel viewModel;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        init(view);
        getCurrentUser();

        return view;
    }

    private void init(View view){
        imvEdit = view.findViewById(R.id.imv_edit);
        imvPostedRoom = view.findViewById(R.id.imv_posted_room);
        imvSavedRoom = view.findViewById(R.id.imv_saved_room);
        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Thực hiện transaction để chuyển đến Fragment đích khi cần thiết
        imvPostedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentPostedRoom())
                    .addToBackStack("FragmentPostedRoom") // Đặt tên cụ thể cho back stack
                    .commit();
        });

        imvSavedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentSavedRoom())
                    .addToBackStack("FragmentSavedRoom") // Đặt tên cụ thể cho back stack
                    .commit();
        });

    }

    private void getCurrentUser(){

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            User user = userDoc.toObject(User.class);
                            viewModel.setUser(user);
                        }
                    });
        }

    }
}
