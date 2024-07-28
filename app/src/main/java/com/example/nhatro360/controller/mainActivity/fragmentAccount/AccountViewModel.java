package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AccountViewModel extends ViewModel {
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();

    public void setUserEmail(String email) {
        this.userEmail.setValue(email);
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }
}
