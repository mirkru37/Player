package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.AllListsAdapter;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class AllListsFragment extends Fragment {

    private int state;
    private GridLayoutManager mLayoutManager;
    private RecyclerView mRecycle;
    private AllListsAdapter mAdapter;
    private FragmentOpener fragmentOpener;

    public AllListsFragment(int state, FragmentOpener fragmentOpener) {
        this.state = state;
        this.fragmentOpener = fragmentOpener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_lists, container, false);
        mRecycle = v.findViewById(R.id.allListsRecycleView);
        mLayoutManager = new GridLayoutManager(getContext(),2);
        mRecycle.setLayoutManager(mLayoutManager);
        switch(state){
            case 0://pllist
                mAdapter = new AllListsAdapter(getContext(), ((MainActivity) getActivity()).getPlayLists(), "pllist", new FragmentOpener() {
                    @Override
                    public void openPlayList(PlayList list, int index, String type) {
                        fragmentOpener.openPlayList(list,index, "pl");
                    }

                    @Override
                    public void openCatalog(PlayList list, int index, String sy) {

                    }

                    @Override
                    public void openAllLists() {

                    }

                    @Override
                    public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

                    }

                    @Override
                    public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {
                        fragmentOpener.openCreator(s,default_music_image,o);
                    }
                });
                break;
            case 1://authors
                mAdapter = new AllListsAdapter(getContext(), ((MainActivity) getActivity()).getAuthors(), "authors", new FragmentOpener() {
                    @Override
                    public void openPlayList(PlayList list, int index, String type) {

                    }

                    @Override
                    public void openCatalog(PlayList list, int index,String type) {
                        fragmentOpener.openCatalog(list,index,"artist");
                    }

                    @Override
                    public void openAllLists() {

                    }

                    @Override
                    public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

                    }

                    @Override
                    public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

                    }
                });
                break;
            case 2://albums
                mAdapter = new AllListsAdapter(getContext(), ((MainActivity) getActivity()).getAlbums(), "albums", new FragmentOpener() {
                    @Override
                    public void openPlayList(PlayList list, int index,String type) {

                    }

                    @Override
                    public void openCatalog(PlayList list, int index,String type) {
                        fragmentOpener.openCatalog(list,index,"albums");
                    }

                    @Override
                    public void openAllLists() {

                    }

                    @Override
                    public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

                    }

                    @Override
                    public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

                    }
                });
                break;
        }
        mRecycle.setAdapter(mAdapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void updatePl() {
        try {
            mAdapter.notifyDataSetChanged();
        }catch (Exception e){}
    }
}
