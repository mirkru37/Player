package com.dev.umieplayer.interfaces;

import com.dev.umieplayer.enums.PlaybackStatus;

public interface MusicControlerMethods {
    void updadeData(String text);
    void updateState(PlaybackStatus pb);
}
