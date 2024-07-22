package com.example.nhatro360;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentConfirm extends Fragment {

    private ScrollView scrollView;
    private EditText edtTitle, edtHost, edtPhone, edtDetail;
    private TextView tvTitleCount, tvDetailCount, tvWarning, tvSpace;
    private Room room;
    private CreatPostViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm, container, false);

        init(view);

        return view;
    }

    private void init(View view){
        viewModel = new ViewModelProvider(requireActivity()).get(CreatPostViewModel.class);
        room = viewModel.getRoom();
        scrollView = view.findViewById(R.id.scrollView);
        edtTitle = view.findViewById(R.id.edt_title);
        edtHost = view.findViewById(R.id.edt_host);
        edtPhone = view.findViewById(R.id.edt_phone);
        edtDetail = view.findViewById(R.id.edt_detail);
        edtTitle.setText(room.getTitle());
        edtHost.setText(room.getHost());
        edtPhone.setText(room.getPhone());
        edtDetail.setText(room.getDetail());

        tvTitleCount = view.findViewById(R.id.tv_title_count);
        tvDetailCount = view.findViewById(R.id.tv_detail_count);
        tvWarning = view.findViewById(R.id.tv_warning);
        tvSpace = view.findViewById(R.id.tv_space);

        setCharacterCount(edtTitle, tvTitleCount, Integer.parseInt(getString(R.string.limitTitle)));
        setCharacterCount(edtDetail, tvDetailCount, Integer.parseInt(getString(R.string.limitDetail)));
        setDetailFocus();
        setWarning();
    }

    private void setCharacterCount(EditText edt, TextView tv, int limit){
        edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tv.setText(charSequence.length() + "/" + limit);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setDetailFocus(){
        edtDetail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tvSpace.setVisibility(View.VISIBLE);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, edtDetail.getTop());
                        }
                    });
                } else {
                    tvSpace.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setWarning(){

        String warning = "* Bằng việc tiếp tục đăng tin nghĩa là bạn đã đồng ý với Điều khoản và Chính sách của chúng tôi.";
        SpannableString spannableString = new SpannableString(warning);

        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Khởi chạy URL trên trình duyệt ngoài
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.term_policy_url)));
                widget.getContext().startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.blue2)); // Màu xanh
                ds.setUnderlineText(true); // Gạch chân
            }
        };

        spannableString.setSpan(termsClickableSpan, 57, 81, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvWarning.setText(spannableString);
        tvWarning.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public Room getRoom(){
        room.setTitle(edtTitle.getText().toString());
        room.setHost(edtHost.getText().toString());
        room.setPhone(edtPhone.getText().toString());
        room.setDetail(edtDetail.getText().toString());
        return room;
    }
}
