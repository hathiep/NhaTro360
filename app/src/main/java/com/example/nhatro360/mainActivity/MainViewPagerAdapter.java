package com.example.nhatro360.mainActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.nhatro360.mainActivity.fragmentAccount.AccountFragment;
import com.example.nhatro360.mainActivity.fragmentHome.HomeFragment;
import com.example.nhatro360.mainActivity.fragmentNotifications.NotificationsFragment;
import com.example.nhatro360.mainActivity.fragmentSearch.SearchFragment;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {
    public MainViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    public MainViewPagerAdapter(FragmentManager childFragmentManager, int[] imageResourcesArray) {
        super(childFragmentManager);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new SearchFragment();
            case 2:
                return new NotificationsFragment();
            case 3:
                return new AccountFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}
