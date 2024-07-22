package com.example.nhatro360;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;

public class CreatPostViewModel extends ViewModel {
    private Room room = new Room(new ArrayList<>(), new ArrayList<>(Collections.nCopies(8, Boolean.FALSE)), 0, 0,0);

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
