package com.dev.umieplayer.interfaces;

import com.dev.umieplayer.objects.MusicItem;

import java.util.ArrayList;

public interface ServiceMethods {
    void setList(ArrayList<MusicItem> list);
    void play_next();
    void play_prev();
    void resume();
    void pause();
    void play_new(int index, boolean shuffle);
    void setPlayMode(int i);
    boolean isPlaying();
    void setList(ArrayList<MusicItem> list,String state, int index);
}
