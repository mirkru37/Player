package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.CurrentPlayListAdapter;
import com.dev.umieplayer.callbacks.SwipeCallback;
import com.dev.umieplayer.callbacks.SwipeCallbackCurrentList;
import com.dev.umieplayer.interfaces.CurrentPlayHelper;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.utils.StorageUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class CurrentListFragment extends Fragment {

    private RecyclerView recyclerView;
    private CurrentPlayListAdapter currentPlayListAdapter;
    private ArrayList<MusicItem> items;

    private ImageView arrowUp;
    private CurrentPlayHelper currentPlayHelper;

    public CurrentListFragment(CurrentPlayHelper currentPlayHelper) {
        this.currentPlayHelper = currentPlayHelper;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_current_list, container, false);
        items = ((MainActivity)getActivity()).getCurrentAudios();
        arrowUp = v.findViewById(R.id.arrowUp);
        arrowUp.setOnClickListener(v1 -> currentPlayHelper.changePage(0));
        recyclerView = v.findViewById(R.id.currentPlayList);
        currentPlayListAdapter = new CurrentPlayListAdapter(getContext(), items, new ServiceMethods() {
            @Override
            public void setList(ArrayList<MusicItem> list) {
            }

            @Override
            public void play_next() {

            }

            @Override
            public void play_prev() {

            }

            @Override
            public void resume() {
///             update nowplay
                ((MainActivity)getActivity()).resume();
                currentPlayListAdapter.notifyDataSetChanged();
                currentPlayHelper.updateCurrAudio(1);
            }

            @Override
            public void pause() {
                ((MainActivity)getActivity()).pause();
                currentPlayListAdapter.notifyDataSetChanged();
                //update nowplay
                currentPlayHelper.updateCurrAudio(0);
            }

            @Override
            public void play_new(int index, boolean shuffle) {
                ((MainActivity)getActivity()).play_new(index,shuffle);
                currentPlayListAdapter.notifyDataSetChanged();
                //update now play
                currentPlayHelper.updateCurrAudio(1);
            }

            @Override
            public void setPlayMode(int i) {

            }

            @Override
            public boolean isPlaying() {
                ///
                return ((MainActivity)getActivity()).isPlaying();
            }

            @Override
            public void setList(ArrayList<MusicItem> list, String state, int index) {
                if(state == "deleted_curr"){
                    ((MainActivity)getActivity()).play_new(index,true);
                    //update nowpalying
                    currentPlayListAdapter.notifyDataSetChanged();
                }else if(state == "deleted<"){
                    //update now play
                    ((MainActivity)getActivity()).setCurrentPlay(list,((MainActivity)getActivity()).getSongIndex()-1);
                }
                StorageUtil storageUtil = new StorageUtil(getContext());
                storageUtil.storePlayingPlaylist("???SPeCiAl@@@play****");
                storageUtil.storeVisiblePlaylist("");
                if(state.equals("move"))
                    ((MainActivity)getActivity()).setCurrentPlay(list, index);
                currentPlayHelper.updateCurrAudio(2);
            }
        },v);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(currentPlayListAdapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeCallbackCurrentList(currentPlayListAdapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return v;
    }

    public void update() {
        try {
            items = ((MainActivity)getActivity()).getCurrentAudios();
            currentPlayListAdapter.setItems(items);
            currentPlayListAdapter.notifyDataSetChanged();
        }catch (Exception e){
        }
    }
}
