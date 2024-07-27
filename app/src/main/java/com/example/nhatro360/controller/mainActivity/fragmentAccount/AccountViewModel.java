package com.example.nhatro360.controller.mainActivity.fragmentAccount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.nhatro360.models.User;

public class AccountViewModel extends ViewModel {
    private final MutableLiveData<User> user = new MutableLiveData<>();

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public LiveData<User> getUser() {
        return user;
    }
}
