package com.dev.umieplayer.fragments;


import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.PlayListAdapterMenu;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private PlayListAdapterMenu playListAdapterMenu;
    private PlayListAdapterMenu authorAdapterMenu;
    private PlayListAdapterMenu albumAdapterMenu;
    private ImageView settings;
    private LinearLayout all;
    private LinearLayout folder;
    private String folderPaht;
    private RecyclerView playListRecyclerView;
    private RecyclerView authorRecyclerView;
    private RecyclerView albumRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private LinearLayoutManager alLayoutManager;
    private LinearLayoutManager auLayoutManager;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderPaht = ((MainActivity)getActivity()).getFolderPath();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        AnimationDrawable animationDrawable = (AnimationDrawable) v.findViewById(R.id.layout).getBackground();
        animationDrawable.setEnterFadeDuration(7000);
        animationDrawable.setExitFadeDuration(7000);
        animationDrawable.start();
//        settings = v.findViewById(R.id.but_settings);
//        settings.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View arg0, MotionEvent arg1) {
//                switch (arg1.getAction()) {
//                    case MotionEvent.ACTION_DOWN: {
//                        ImageView v = (ImageView) arg0;
//                        v.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
//                        v.setScaleX(0.85f);
//                        v.setScaleY(0.85f);
//                        v.invalidate();
//                        break;
//                    }
//                    case MotionEvent.ACTION_UP:{
//                        ImageView v = (ImageView) arg0;
//                        v.getDrawable().clearColorFilter();
//                        v.invalidate();
//                        v.setScaleX(1f);
//                        v.setScaleY(1f);
//                        break;
//                    }
//                    case MotionEvent.ACTION_MOVE:{
//                        ImageView v = (ImageView) arg0;
//                        v.getDrawable().clearColorFilter();
//                        v.invalidate();
//                        break;
//                    }
//                }
//                return true;
//            }
//        });
        all = v.findViewById(R.id.all);
        all.setOnClickListener(this);
        folder = v.findViewById(R.id.folder);
        folder.setOnClickListener(this);
        playListRecyclerView = v.findViewById(R.id.playlistsList);
        albumRecyclerView = v.findViewById(R.id.albumList);
        authorRecyclerView = v.findViewById(R.id.authorList);

        mLayoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        alLayoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        auLayoutManager = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);

        playListRecyclerView.setLayoutManager(mLayoutManager);
        albumRecyclerView.setLayoutManager(alLayoutManager);
        authorRecyclerView.setLayoutManager(auLayoutManager);

        playListAdapterMenu = new PlayListAdapterMenu(getContext(), new ArrayList<PlayList>(), new FragmentOpener() {

            @Override
            public void openCatalog(PlayList list, int index,String ty) {

            }

            @Override
            public void openAllLists() {
                ((MainActivity)getActivity()).openPage(2);
            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }

            @Override
            public void openPlayList(PlayList list, int pos, String type) {
                FragmentManager fm = getFragmentManager();
                PlayListFragment playListFragment = new PlayListFragment(list, pos, () -> {
//                        playListAdapterMenu.notifyDataSetChanged();
                    ((MainActivity)getActivity()).updateAdapter();
                },"home");
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,0).replace(R.id.listLayout, playListFragment).commit();
            }
        },"pl");
        playListRecyclerView.setAdapter(playListAdapterMenu);

        albumAdapterMenu = new PlayListAdapterMenu(getContext(), new ArrayList<PlayList>(), new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index, String type) {
                FragmentManager fm = getFragmentManager();
                CatalogFragment playListFragment = new CatalogFragment(list, type,index);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,0).replace(R.id.listLayout, playListFragment).commit();
            }

            @Override
            public void openCatalog(PlayList list, int index,String ty) {

            }

            @Override
            public void openAllLists() {
                ((MainActivity)getActivity()).openPage(2);
            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }
        },"album");
        albumRecyclerView.setAdapter(albumAdapterMenu);

        authorAdapterMenu = new PlayListAdapterMenu(getContext(), new ArrayList<PlayList>(), new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index, String type) {
                FragmentManager fm = getFragmentManager();
                CatalogFragment playListFragment = new CatalogFragment(list,type,index);
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,0).replace(R.id.listLayout, playListFragment).commit();
            }

            @Override
            public void openCatalog(PlayList list, int index,String ty) {

            }

            @Override
            public void openAllLists() {
                ((MainActivity)getActivity()).openPage(2);
            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {

            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }
        },"artist");
        authorRecyclerView.setAdapter(authorAdapterMenu);

        if(((MainActivity) getActivity()).getPlayLists()!=null){
            playListAdapterMenu.setList(((MainActivity) getActivity()).getPlayLists());
            albumAdapterMenu.setList(((MainActivity) getActivity()).getAlbums());
            authorAdapterMenu.setList(((MainActivity) getActivity()).getAuthors());
        }

        return v;
    }

    public void init(){
        playListAdapterMenu.setList(((MainActivity) getActivity()).getPlayLists());
        albumAdapterMenu.setList(((MainActivity) getActivity()).getAlbums());
        authorAdapterMenu.setList(((MainActivity) getActivity()).getAuthors());
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        switch (v.getId()){
            case R.id.all:
                CatalogFragment catalogFragment = new CatalogFragment( "All", ((MainActivity)getActivity()).getAllMusic());
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,0).replace(R.id.listLayout, catalogFragment).commit();
                break;
            case R.id.folder:
                CatalogFragment catalogFragment_ = new CatalogFragment(((MainActivity)getActivity()).getFolderPath(), ((MainActivity)getActivity()).getFolderMusic());
                fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,0).replace(R.id.listLayout, catalogFragment_).commit();
                break;
        }
    }

    public void updateLists() {
        init();
        playListAdapterMenu.notifyDataSetChanged();
        authorAdapterMenu.notifyDataSetChanged();
        albumAdapterMenu.notifyDataSetChanged();
    }

    public void closePl() {
        if(getFragmentManager().findFragmentById(R.id.listLayout)!=null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(getFragmentManager().findFragmentById(R.id.listLayout)).commit();
        }
    }
}
