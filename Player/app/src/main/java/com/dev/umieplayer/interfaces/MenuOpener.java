package com.dev.umieplayer.interfaces;

import com.dev.umieplayer.objects.MusicItem;

public interface MenuOpener {
    void open(MusicItem item, int i, OnDeleteListener onDeleteListener);
}
