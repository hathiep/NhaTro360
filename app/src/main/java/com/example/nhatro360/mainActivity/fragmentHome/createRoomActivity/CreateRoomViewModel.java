package com.example.nhatro360.mainActivity.fragmentHome.createRoomActivity;

import androidx.lifecycle.ViewModel;

import com.example.nhatro360.model.Room;

import java.util.ArrayList;
import java.util.Collections;

public class CreateRoomViewModel extends ViewModel {
    private Room room = new Room(new ArrayList<>(), new ArrayList<>(Collections.nCopies(8, Boolean.FALSE)), 0, 0,0);

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
