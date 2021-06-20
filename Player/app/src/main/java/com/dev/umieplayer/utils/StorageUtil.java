package com.dev.umieplayer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StorageUtil {

    private final String STORAGE = "com.eukalyptus.eukalyptusplayer.STORAGE";
    private final String PREFERENCES = "com.eukalyptus.eukalyptusplayer.PREFERENCES";
    private SharedPreferences preferences;
    private Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }

    public void storeAudio(ArrayList<MusicItem> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }

    public void storeCurrentData(String id){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("audioID", id);
        editor.apply();
    }

    public String getCurrentData(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        String json = preferences.getString("audioID", "");
        return json;
    }

    public void storePreviousData(String id){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("prevAudioID", id);
        editor.apply();
    }

    public String getPrevousData(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        String json = preferences.getString("prevAudioID", "");
        return json;
    }


    public void storeAudioIndex(int index) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public int loadAudioIndex() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);//return -1 if no data found
    }

    public void storeVisiblePlaylist(String plname){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("plName", plname);
        editor.apply();
    }

    public String loadVisiblePlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("plName", "");//return -1 if no data found
    }

    public void storePlayingPlaylist(String plname){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("plprevName", plname);
        editor.apply();
    }

    public String loadPlayingPlaylist() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getString("plprevName", "");//return -1 if no data found
    }

    public void storePlayMode(int playMode){
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("playMode", playMode);
        editor.apply();
    }

    public int getPlayMode(){
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getInt("playMode", 0);//0-repeat all
    }

    public void clean(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public void storePlayLists(ArrayList<PlayList> playLists){
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(playLists);
        editor.putString("playLists", json);
        editor.apply();
    }

    public ArrayList<PlayList> loadPlayLists(){
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("playLists", null);
        Type type = new TypeToken<ArrayList<PlayList>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
