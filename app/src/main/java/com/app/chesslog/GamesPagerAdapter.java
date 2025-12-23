package com.app.chesslog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GamesPagerAdapter extends FragmentStateAdapter {

    public GamesPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return new CollectionGamesFragment();
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}