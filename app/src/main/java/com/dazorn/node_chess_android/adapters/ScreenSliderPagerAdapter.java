package com.dazorn.node_chess_android.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dazorn.node_chess_android.activities.MainActivity;
import com.dazorn.node_chess_android.fragments.ChatFragment;
import com.dazorn.node_chess_android.fragments.MainFragment;
import com.dazorn.node_chess_android.fragments.ProfileFragment;
import com.dazorn.node_chess_android.fragments.RankingsFragment;
import com.dazorn.node_chess_android.fragments.SettingsFragment;

public class ScreenSliderPagerAdapter extends FragmentStateAdapter {
    private static final Fragment[] _pages = {
            new ProfileFragment(),
            new RankingsFragment(),
            new MainFragment(),
            new ChatFragment(),
            new SettingsFragment()
    };

    public ScreenSliderPagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return _pages[position];
    }

    @Override
    public int getItemCount() {
        return _pages.length;
    }
}
