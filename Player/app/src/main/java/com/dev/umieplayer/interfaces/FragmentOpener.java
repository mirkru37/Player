package com.dev.umieplayer.interfaces;

import android.graphics.Bitmap;

import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

public interface FragmentOpener {
    void openPlayList(PlayList list, int index, String type);
    void openCatalog(PlayList list, int index, String type);
    void openAllLists();
    void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected);
    void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o);
}
