package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nhatro360.R;
import com.example.nhatro360.controller.Login;
import com.example.nhatro360.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.FirebaseFirestore;


public class AccountFragment extends Fragment {
    private ImageView imvEdit, imvPostedRoom, imvSavedRoom;
    private TextView tvFullName, btnLogout;
    private AccountViewModel viewModel;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
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
        tvFullName = view.findViewById(R.id.tv_fullName);
        imvPostedRoom = view.findViewById(R.id.imv_posted_room);
        imvSavedRoom = view.findViewById(R.id.imv_saved_room);
        btnLogout = view.findViewById(R.id.btn_logout);
        viewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        imvPostedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentPostedRoom())
                    .addToBackStack("FragmentPostedRoom")
                    .commit();
        });

        imvSavedRoom.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new FragmentSavedRoom())
                    .addToBackStack("FragmentSavedRoom")
                    .commit();
        });
        btnLogout.setOnClickListener(v -> showLogoutDialog());

    }

    private void getCurrentUser(){

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            viewModel.setUserEmail(userEmail);
            db.collection("users").whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            DocumentSnapshot userDoc = task.getResult().getDocuments().get(0);
                            User user = userDoc.toObject(User.class);
                            tvFullName.setText(user.getFullName());
                        }
                    });
        }

    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Bạn có muốn đăng xuất?");
        builder.setMessage("Xác nhận đăng xuất khỏi ứng dụng?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            auth.signOut();
            dialog.dismiss();
            show_dialog("Đăng xuất thành công!", 1);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getContext(), Login.class);
                startActivity(intent);
            }, 2000);
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Hiển thị Dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void show_dialog(String s, int time){
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Thông báo");
        progressDialog.setMessage(s);
        progressDialog.show();

        new Handler().postDelayed(progressDialog::dismiss, time * 1000);
    }
}
