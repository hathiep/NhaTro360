package com.example.nhatro360.mainActivity.fragmentAccount;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhatro360.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppInformationFragment extends Fragment {

    private ImageView imvBack;
    private TextView tvShareApp, tvSubmitAppReview, tvSubmitFeedbackReport, tvAppHomepage, tvSimilarApp, tvAboutUs;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_information, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        // Gọi hàm ánh xạ view
        init(view);

        // Gọi hàm click các textview
        setOnclickTV();

        return view;
    }

    // Hàm ánh xạ view
    private void init(View view){
        imvBack = view.findViewById(R.id.imV_back);
        tvShareApp = view.findViewById(R.id.tv_share_app);
        tvSubmitAppReview = view.findViewById(R.id.tv_submit_app_review);
        tvSubmitFeedbackReport = view.findViewById(R.id.tv_submit_feedback_reports);
        tvAppHomepage = view.findViewById(R.id.tv_app_homepage);
        tvSimilarApp = view.findViewById(R.id.tv_similar_app);
        tvAboutUs = view.findViewById(R.id.tv_about_us);
    }

    // Hàm click các textview
    private void setOnclickTV(){
        imvBack.setOnClickListener(v -> goBackToAccountFragment());
        setOnclick(tvShareApp, Intent.ACTION_VIEW, "https://thuenhatro360.com/");
        setOnclick(tvSubmitAppReview, Intent.ACTION_VIEW, "https://thuenhatro360.com/");
        setOnclick(tvSubmitFeedbackReport, Intent.ACTION_VIEW, "https://thuenhatro360.com/");
        setOnclick(tvAppHomepage, Intent.ACTION_VIEW, "https://thuenhatro360.com/");
        setOnclick(tvSimilarApp, Intent.ACTION_VIEW, "https://thuenhatro360.com/");
        setOnclick(tvAboutUs, Intent.ACTION_VIEW, "https://thuenhatro360.com/gioi-thieu");
    }

    // Hàm trở về fragment trước đó
    private void goBackToAccountFragment() {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.popBackStack();
    }

    private void setOnclick(TextView tv, String action, String uri){
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(action);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
    }
}