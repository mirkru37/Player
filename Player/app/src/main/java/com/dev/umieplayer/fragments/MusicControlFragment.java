package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.callbacks.OnSwipeListener;
import com.dev.umieplayer.R;
import com.dev.umieplayer.enums.PlaybackStatus;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.utils.StorageUtil;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class MusicControlFragment extends Fragment {

    private String nextText = "";

    private PlaybackStatus state = PlaybackStatus.PAUSED;

    private int centerPos;
    private int endPosRight;
    private int endPosLeft;

    private TextView text;
    private TextView text2;
    private ImageView modeImg;
    private ImageView play_pauseImg;

    private RelativeLayout layout;
    private RelativeLayout curr_click_zone;

    private int playMode; //0-all 1-one 2-shuffle

    private ServiceMethods serviceMethods;
    private int animDuration = 500;

    public MusicControlFragment(ServiceMethods serviceMethods) {
        // Required empty public constructor
        this.serviceMethods = serviceMethods;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music_control, container, false);
        text = v.findViewById(R.id.controlText);
        text2 = v.findViewById(R.id.controlText2);
//        curr_click_zone = v.findViewById(R.id.click_zone_curr);
//        curr_click_zone.setOnClickListener(v1 -> {
//            ((MainActivity)getActivity()).openCurrentPlay();
//        });
        text2.setAlpha(0);
        text.setSelected(true);
        text2.setSelected(true);
        modeImg = v.findViewById(R.id.playingMode);
        final StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
        playMode = storageUtil.getPlayMode();
        switch (playMode){
            case 0:
                modeImg.setImageResource(R.drawable.ic_repeat);
                break;
            case 1:
                modeImg.setImageResource(R.drawable.ic_repeat_one);
                break;
            case 2:
                modeImg.setImageResource(R.drawable.ic_shuffle);
                break;
        }

        modeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playMode<2)
                    playMode++;
                else
                    playMode=0;
                serviceMethods.setPlayMode(playMode);
            }
        });

        play_pauseImg = v.findViewById(R.id.play_pause);
        play_pauseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == PlaybackStatus.PAUSED&&storageUtil.loadPlayingPlaylist()!=""){
                    try {
                        updateState(PlaybackStatus.PLAYING);
                        serviceMethods.resume();
                    }catch (Exception e){}
                }else if(storageUtil.loadPlayingPlaylist()!="") {
                    try {
                        updateState(PlaybackStatus.PAUSED);
                        serviceMethods.pause();
                    }catch (Exception e){}
                }
            }
        });
        layout = v.findViewById(R.id.controlLayout);

        layout.setOnTouchListener(new OnSwipeListener(getActivity().getApplicationContext()){

            @Override
            public void onTap() {
                super.onTap();
                if(((MainActivity)getActivity()).getCurrentAudio()!=null)
                    ((MainActivity)getActivity()).openCurrentPlay();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                try {
                    nextText = ((MainActivity) getActivity()).getSongNextData(1);
                    animLeft();
                }catch (Exception e){}
            }

            @Override
            public void onSwipeRight() {
            super.onSwipeRight();
                try {
                    nextText = ((MainActivity) getActivity()).getSongNextData(-1);
                    animRight();
                }catch (Exception e){}
            }
        });

        return v;
    }

    private void calculate(){
        endPosRight = (int) play_pauseImg.getX();
        centerPos = (int) text.getX();
        endPosLeft = centerPos - (int) text.getWidth();
    }

    public void update(String nextText){
        if(nextText == null) {
            text2.setText(text.getText());
            text2.setX(text.getX());
            text.setText("");
            text2.setText("");
            updateState(PlaybackStatus.PAUSED);
            text.setAlpha(0);
            text2.setAlpha(1);
        }
        text2.setText(text.getText());
        text2.setX(text.getX());
        text.setText(nextText);
        text.setAlpha(0);
        text2.setAlpha(1);
        text2.animate().alpha(0).setDuration(250);
        text.animate().alpha(1).setDuration(400);
    }

    private void animLeft(){
        serviceMethods.play_next();
        text2.setText(text.getText());
        text2.setX(text.getX());
        text.setText(nextText);
        calculate();
        text.setAlpha(0);
        text2.setAlpha(1);
        text.setX(endPosRight);
        text2.animate().alpha(0).x(endPosLeft).setDuration(animDuration);
        text.animate().alpha(1).x(centerPos).setDuration(animDuration);
    }

    private void animRight(){
        serviceMethods.play_prev();
        text2.setText(text.getText());
        text2.setX(text.getX());
        text.setText(nextText);
        calculate();
        text.setAlpha(0);
        text2.setAlpha(1);
        text.setX(endPosLeft);
        text2.animate().alpha(0).x(endPosRight).setDuration(animDuration);
        text.animate().alpha(1).x(centerPos).setDuration(animDuration);
    }

    public void updateState(PlaybackStatus pb) {
        state = pb;
        if(state == PlaybackStatus.PLAYING){
            play_pauseImg.setImageResource(R.drawable.ic_pause);
        }else
            play_pauseImg.setImageResource(R.drawable.ic_play);
    }

    public void setPlayMode(int i) {
        switch (i){
            case 0:
                modeImg.setImageResource(R.drawable.ic_repeat);
                break;
            case 1:
                modeImg.setImageResource(R.drawable.ic_repeat_one);
                break;
            case 2:
                modeImg.setImageResource(R.drawable.ic_shuffle);
                break;
        }
    }
}
