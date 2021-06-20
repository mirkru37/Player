package com.dev.umieplayer.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.VerticalViewPager;
import com.dev.umieplayer.interfaces.CurrentPlayHelper;

import java.lang.reflect.Field;

public class NowPlayingFragment extends Fragment implements CurrentPlayHelper {

    private CurrentAudioFragment currentAudioFragment;
    private CurrentListFragment currentListFragment;

    private VerticalViewPager viewPager;
    private MyFragmentPagerAdapter pagerAdapter;

    public NowPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_now_playing, container, false);
        currentAudioFragment = new CurrentAudioFragment(this);
        currentListFragment = new CurrentListFragment(this);
        viewPager = v.findViewById(R.id.now_play_pager);
        pagerAdapter = new MyFragmentPagerAdapter(getFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        return v;
    }

    @Override
    public void changePage(int i) {
        viewPager.setCurrentItem(i);
    }

    @Override
    public void updateCurrAudio(int state) {
        currentAudioFragment.update(state);
    }

    @Override
    public void updateCurrList() {
        currentListFragment.update();
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return currentAudioFragment;
                case 1: return currentListFragment;
                default: return new HomeFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }


}
