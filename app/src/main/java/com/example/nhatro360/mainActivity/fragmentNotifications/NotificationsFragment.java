package com.example.nhatro360.mainActivity.fragmentNotifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nhatro360.R;
import com.example.nhatro360.model.Notification;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new NotificationsAdapter(notificationList, getContext());
        recyclerView.setAdapter(adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Lấy danh sách thông báo trên firebase
        db.collection("users").whereEqualTo("email", currentUserEmail).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot userDocument = task.getResult().getDocuments().get(0);
                            List<Map<String, Object>> notifications = (List<Map<String, Object>>) userDocument.get("notifications");
                            if (notifications != null) {
                                for (Map<String, Object> notificationMap : notifications) {
                                    // Directly map the notificationMap to a Notification object
                                    Notification notification = new Notification(
                                            (String) notificationMap.get("message"),
                                            (Timestamp) notificationMap.get("time"),
                                            ((Long) notificationMap.get("type")).intValue()
                                    );
                                    notificationList.add(notification);
                                }
                                Collections.sort(notificationList, (n1, n2) -> n2.getTime().compareTo(n1.getTime()));
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Log.w("NotificationsFragment", "Error getting user document.", task.getException());
                    }
                });

        return view;
    }
}
