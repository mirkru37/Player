package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.CurrentPlayHelper;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.transformers.ExpandingViewPagerTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import rm.com.audiowave.AudioWaveView;
import rm.com.audiowave.OnProgressListener;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class CurrentAudioFragment extends Fragment {

    private int allTime_ = 125;
    private int currentTime_ = 0;
    private float progress;
    private Runnable moveSeekBarThread;
    private Handler handler;

    private ImageView back;
    private AudioWaveView waveView;
    private TextView currentTime;
    private TextView allTime;
    private TextView name;
    private TextView artist;
    private ImageView playPause;
    private ImageView prev;
    private ImageView next;
    private ImageView down;
    private ImageView changeMode;

    private MusicItem currentAudio;
    private boolean changePos = true;

    private ViewPager pager;
    private MyPagerAdapter myPagerAdapter;

    private ArrayList<MusicItem> items;

    private TextView count;
    private int songIndex;
    private boolean first;

    private int mode;

    private CurrentPlayHelper currentPlayHelper;

    public CurrentAudioFragment(CurrentPlayHelper currentPlayHelper) {
        // Required empty public constructor
        this.currentPlayHelper = currentPlayHelper;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_current_audio, container, false);

        count = v.findViewById(R.id.itemCount);
        pager = v.findViewById(R.id.currentAudioImages);
        handler = new Handler();
        mode = ((MainActivity)getActivity()).getPlayingMode();
        prev = v.findViewById(R.id.prevButt);
        next = v.findViewById(R.id.nextButt);
        down = v.findViewById(R.id.downButt);
        changeMode = v.findViewById(R.id.playingMode);
        playPause = v.findViewById(R.id.playPauseButt);
        items = ((MainActivity) getActivity()).getCurrentAudios();
        songIndex = ((MainActivity)getActivity()).getSongIndex();
        currentAudio = ((MainActivity) getActivity()).getCurrentAudio();
        name = v.findViewById(R.id.songName);
        artist = v.findViewById(R.id.songArtist);
        currentTime = v.findViewById(R.id.currentTime);

        allTime = v.findViewById(R.id.audioTime);
        back = v.findViewById(R.id.buttonBack);
        back.setOnTouchListener((arg0, arg1) -> {
            switch (arg1.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView v1 = (ImageView) arg0;
                    v1.setScaleX(0.9f);
                    v1.setScaleY(0.9f);
                    v1.invalidate();
                    v1.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    ImageView v1 = (ImageView) arg0;
                    v1.getDrawable().clearColorFilter();
                    v1.invalidate();
                    v1.setScaleX(1f);
                    v1.setScaleY(1f);
                    close();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    ImageView v1 = (ImageView) arg0;
                    v1.getDrawable().clearColorFilter();
                    v1.invalidate();
                    break;
                }
            }
            return true;
        });
        waveView = v.findViewById(R.id.seekBar);

        waveView.setOnProgressListener(new OnProgressListener() {
            @Override
            public void onStartTracking(float v) {
                //do nothing

                changePos = false;
            }

            @Override
            public void onStopTracking(float v) {
                //setAudioProgress
                currentTime_ = (int) (allTime_ * (v / 100));
                ((MainActivity) getActivity()).setAudioPos(currentTime_ * 1000);
                changePos = true;
                moveSeekBarThread.run();
            }

            @Override
            public void onProgressChanged(float v, boolean b) {
                //change progress value
                if (!changePos)
                    currentTime_ = (int) (allTime_ * (v / 100));
                currentTime.setText(getTime(currentTime_));
            }
        });

        updateData();


        handler.removeCallbacks(moveSeekBarThread);
        moveSeekBarThread = new Runnable() {
            public void run() {
                try {
                    if (((MainActivity) getActivity()).isPlaying() && changePos) {
                        currentTime_ = ((MainActivity) getActivity()).getSeekPos() / 1000;
                        progress = ((float) currentTime_ / (float) allTime_) * 100;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    waveView.setProgress(progress);
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
                    if(Math.abs((float)allTime_ - currentTime_) <=0.5f)
                        playNext();
                    if (changePos)
                        handler.postDelayed(this, 10); //Looping the thread after 0.1 second
                    // seconds
                }catch (Exception e){
                }
            }
        };
        moveSeekBarThread.run();
        // handler.postDelayed(moveSeekBarThread, 1000); //Looping the thread after 0.1 second


        myPagerAdapter = new MyPagerAdapter();
        pager.setAdapter(myPagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                if(!first) {
                    songIndex = i;
                    count.setText((i + 1) + " of " + items.size());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) getActivity()).play_new(i,false);
                            playPause.setImageResource(R.drawable.ic_pause);
                        }
                    }).start();
                    currentAudio = items.get(i);
                    updateData();
                }else{
                    first =false;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        pager.setClipToPadding(false);
        pager.setPadding(getPixelsFromDPs(getActivity(), (int) getResources().getDimension(R.dimen.viewpagerimagepadding)),0, getPixelsFromDPs(getActivity(), (int) getResources().getDimension(R.dimen.viewpagerimagepadding)),0);
        pager.setPageTransformer(false, new ExpandingViewPagerTransformer());
        if(songIndex!=0)
            first = true;
        pager.setCurrentItem(songIndex);

        count.setText((songIndex+1)+" of "+items.size());

        if(((MainActivity)getActivity()).isPlaying())
            playPause.setImageResource(R.drawable.ic_pause);

        playPause.setOnClickListener(v12 -> {
            currentPlayHelper.updateCurrList();
            if(((MainActivity)getActivity()).isPlaying()) {
                playPause.setImageResource(R.drawable.ic_play);
                ((MainActivity)getActivity()).pause();
            }
            else {
                playPause.setImageResource(R.drawable.ic_pause);
                ((MainActivity)getActivity()).resume();
            }
        });

        next.setOnClickListener(v13 -> {
            ((MainActivity)getActivity()).play_next();
            if(songIndex<items.size()-1)
                songIndex++;
            else
                songIndex=0;
            pager.setCurrentItem(songIndex);
        });

        prev.setOnClickListener(v14 -> {
            ((MainActivity)getActivity()).play_prev();
            if(songIndex>0)
                songIndex--;
            else
                songIndex = items.size()-1;
            pager.setCurrentItem(songIndex);
        });

        switch (mode){
            case 0://all
                changeMode.setImageResource(R.drawable.ic_repeat);
                break;
            case 1://repeat one
                changeMode.setImageResource(R.drawable.ic_repeat_one);
                break;
            case 2:// shuffle
                changeMode.setImageResource(R.drawable.ic_shuffle);
                break;
        }

        changeMode.setOnClickListener(v15 -> {
            switch (mode){
                case 0://all
                    mode = 1;
                    changeMode.setImageResource(R.drawable.ic_repeat_one);
                    ((MainActivity)getActivity()).setPlayMode(mode);
                    break;
                case 1://repeat one
                    mode = 2;
                    changeMode.setImageResource(R.drawable.ic_shuffle);
                    ((MainActivity)getActivity()).setPlayMode(mode);
                    first = true;
                    updateShuffle();
                    break;
                case 2:// shuffle
                    mode = 0;
                    changeMode.setImageResource(R.drawable.ic_repeat);
                    ((MainActivity)getActivity()).setPlayMode(mode);
                    first = true;
                    updateShuffle();
                    break;
            }
        });

        down.setOnClickListener(v16 -> {
           currentPlayHelper.changePage(1);
        });
        return v;
    }

    private void playNext() {
       if(mode!=1) {
           pager.setCurrentItem(songIndex+1);
           //currentAudio = items.get(songIndex);
       }
    }

    private void updateShuffle() {
        items = ((MainActivity)getActivity()).getCurrentAudios();
        songIndex = ((MainActivity)getActivity()).getSongIndex();
        pager.setCurrentItem(songIndex);
        pager.getAdapter().notifyDataSetChanged();
        count.setText((songIndex + 1) + " of " + items.size());
        currentPlayHelper.updateCurrList();
    }

    private void updateData() {
        currentTime_=0;
        name.setText(currentAudio.getTitle());
        artist.setText(currentAudio.getArtist());
        allTime_ = currentAudio.getDuration();
        byte[] bytes = readAudioFileData(currentAudio.getData());
        waveView.setRawData(bytes);
        currentTime.setText(getTime(currentTime_));
        allTime.setText(getTime(allTime_));
        progress = ((float) currentTime_ / (float) allTime_) * 100;
        waveView.setProgress((int) progress);
        currentPlayHelper.updateCurrList();
    }

    public static int getPixelsFromDPs(Activity activity, int dps){

        Resources r = activity.getResources();

        int  px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
    }

    private String getTime(int currentTime_) {
        String time = "0:00";
        int minutes = 0;
        int seconds = 0;
        if (currentTime_ != 0) {
            if (currentTime_ >= 60) {
                minutes = 0;
                while (currentTime_ >= 60) {
                    currentTime_ -= 60;
                    minutes++;
                }
                seconds = currentTime_;
                if (seconds >= 10)
                    time = minutes + ":" + seconds;
                else
                    time = minutes + ":0" + seconds;
            } else {
                seconds = currentTime_;
                if (seconds >= 10)
                    time = "0:" + seconds;
                else
                    time = "0:0" + seconds;
            }
        }


        return time;
    }

    private void close() {
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().setCustomAnimations(0,R.anim.slide_down).remove((getFragmentManager().findFragmentById(R.id.current_play_layout))).commit();
    }

    public byte[] readAudioFileData(final String filePath) {
        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {

            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];

            //read file into bytes[]
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return bytesArray;
    }

    public void update(int state) {
        first = true;
        items = ((MainActivity) getActivity()).getCurrentAudios();
        songIndex = ((MainActivity)getActivity()).getSongIndex();
        currentAudio = ((MainActivity) getActivity()).getCurrentAudio();
        name.setText(currentAudio.getTitle());
        artist.setText(currentAudio.getArtist());
        allTime_ = currentAudio.getDuration();
        byte[] bytes = readAudioFileData(currentAudio.getData());
        waveView.setRawData(bytes);
        currentTime.setText(getTime(currentTime_));
        allTime.setText(getTime(allTime_));
        progress = ((float) currentTime_ / (float) allTime_) * 100;
        waveView.setProgress((int) progress);
        pager.getAdapter().notifyDataSetChanged();
        pager.setCurrentItem(songIndex);
        count.setText((songIndex + 1) + " of " + items.size());
        if(state == 0)
            playPause.setImageResource(R.drawable.ic_play);
        else if(state == 1)
            playPause.setImageResource(R.drawable.ic_pause);
        else if(((MainActivity)getActivity()).isPlaying()) {
            playPause.setImageResource(R.drawable.ic_pause);
        }else
            playPause.setImageResource(R.drawable.ic_play);
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(getContext());

            Bitmap imageBit = items.get(position).getImage();
            imageView.setImageBitmap(imageBit);
            RoundedBitmapDrawable RBD = RoundedBitmapDrawableFactory.create(getResources(), imageBit);
            RBD.setCornerRadius(25);
            RBD.setAntiAlias(true);
            imageView.setImageDrawable(RBD);

            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

}
