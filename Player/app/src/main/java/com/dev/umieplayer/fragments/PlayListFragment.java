package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.callbacks.SwipeCallBackPlList;
import com.dev.umieplayer.adapters.SongListAdapter;
import com.dev.umieplayer.enums.PlaybackStatus;
import com.dev.umieplayer.interfaces.EditUpdateHelper;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.interfaces.HomePageUpdate;
import com.dev.umieplayer.interfaces.MenuOpener;
import com.dev.umieplayer.interfaces.OnDeleteListener;
import com.dev.umieplayer.interfaces.PlayListEditMethods;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;
import com.dev.umieplayer.utils.StorageUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class PlayListFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private int myIndex;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private TextView mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageView albumImage;
    private ImageView buttonBack;
    private ImageView buttonMenu;
    private RelativeLayout buttonLayout;

    private String albumName;

    private PlayList playList;
    private ArrayList<MusicItem> musicItems;
    private RecyclerView recyclerView;
    private SongListAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private Button buttonPlay;
    private boolean thisPlaying;
    private HomePageUpdate homePageUpdate;
    private String type;

    public PlayListFragment(PlayList items,int index, HomePageUpdate homePageUpdate, String t) {
        // Required empty public constructor
        playList = items;
        musicItems = items.getItems();
        albumName = items.getName();
        myIndex = index;
        type = t;
        this.homePageUpdate = homePageUpdate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_play_list, container, false);

        mTitle = v.findViewById(R.id.main_textview_title);
        mTitle.setSelected(true);
        mToolbar = v.findViewById(R.id.main_toolbar);
        mTitleContainer = v.findViewById(R.id.infoText);
        mTitleContainer.setSelected(true);
        albumImage = v.findViewById(R.id.infoImage);
        albumImage.setImageBitmap(playList.getImage());
        mAppBarLayout = v.findViewById(R.id.main_appbar);
        buttonBack = v.findViewById(R.id.buttonBack);
        buttonMenu = v.findViewById(R.id.buttonMenu);
        if(albumName.equals("Download"))
            buttonMenu.setVisibility(View.GONE);
        buttonPlay = v.findViewById(R.id.icPlay);
        buttonLayout = v.findViewById(R.id.playLayout);
        final StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
        String old = storageUtil.loadPlayingPlaylist();
        if (old.equals(albumName)) {
            if (((MainActivity) getActivity()).isPlaying()) {
                buttonPlay.setText("PAUSE");
                thisPlaying = true;
            } else
                thisPlaying = false;
        }

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
                String old = storageUtil.loadPlayingPlaylist();
                if (!old.equals(albumName)&&musicItems.size()>0) {
                    ((MainActivity) getActivity()).setList(musicItems);
                    ((MainActivity) getActivity()).play_new(0, true);
                    storageUtil.storePlayingPlaylist(storageUtil.loadVisiblePlaylist());
                    buttonPlay.setText("PAUSE");
                    thisPlaying = true;
                } else if(musicItems.size()>0) {
                    if (thisPlaying) {
                        ((MainActivity) getActivity()).pause();
                        buttonPlay.setText("PLAY");
                        thisPlaying = false;
                    } else {
                        ((MainActivity) getActivity()).resume();
                        buttonPlay.setText("PAUSE");
                        thisPlaying = true;
                    }
                }
            }
        });
        buttonMenu.setOnTouchListener((arg0, arg1) -> {
            switch (arg1.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView v12 = (ImageView) arg0;
                    v12.setScaleX(0.9f);
                    v12.setScaleY(0.9f);
                    v12.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    ImageView v12 = (ImageView) arg0;
                    v12.getDrawable().clearColorFilter();
                    v12.invalidate();
                    v12.setScaleX(1f);
                    v12.setScaleY(1f);
                    BottomDialogFragmentPlayList bottomSheetDialog = BottomDialogFragmentPlayList.getInstance(new PlayListEditMethods() {
                        @Override
                        public void changeName(String name) {
                            ((MainActivity) getActivity()).setListName(myIndex, name);
                            mTitle.setText(name);
                            mTitleContainer.setText(name);
                            StorageUtil storageUtil1 = new StorageUtil(getContext());
                            if (albumName.equals(storageUtil.loadPlayingPlaylist())) {
                                storageUtil.storePlayingPlaylist(name);
                                storageUtil.storeVisiblePlaylist(name);
                            } else
                                storageUtil.storeVisiblePlaylist(name);
                            albumName = name;
                            homePageUpdate.updatePlayLists();
                        }

                        @Override
                        public void changeImage(Bitmap image) {
                            albumImage.setImageBitmap(image);
                            ((MainActivity) getActivity()).setListImage(myIndex, image);
                            homePageUpdate.updatePlayLists();
                        }

                        @Override
                        public void delete() {
                            ((MainActivity) getActivity()).deletePlayList(myIndex);
                            removeFragment();
                        }
                    }, getFragmentOpener(), type);
                    bottomSheetDialog.show(getFragmentManager(), "Custom Bottom Sheet");
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    ImageView v12 = (ImageView) arg0;
                    v12.getDrawable().clearColorFilter();
                    v12.invalidate();
                    break;
                }
            }
            return true;
        });
        buttonBack.setOnTouchListener((arg0, arg1) -> {
            switch (arg1.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView v1 = (ImageView) arg0;
                    v1.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                    v1.setScaleX(0.9f);
                    v1.setScaleY(0.9f);
                    v1.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    ImageView v1 = (ImageView) arg0;
                    v1.getDrawable().clearColorFilter();
                    v1.invalidate();
                    v1.setScaleX(1f);
                    v1.setScaleY(1f);
                    removeFragment();
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
        mTitle.setText(albumName);
        mTitleContainer.setText(albumName);
        StorageUtil storage = new StorageUtil(getContext());
        storage.storeVisiblePlaylist(albumName);

        mAppBarLayout.addOnOffsetChangedListener(this);
        startAlphaAnimation(mTitle, 0, View.INVISIBLE);
        recyclerView = v.findViewById(R.id.musicList);
        mLayoutManager = new LinearLayoutManager(v.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new SongListAdapter(getActivity().getApplicationContext(), musicItems, new ServiceMethods() {
            @Override
            public void setList(ArrayList<MusicItem> list) {
                ((MainActivity) getActivity()).setList(list);
            }

            @Override
            public void play_next() {
                ((MainActivity) getActivity()).play_next();
            }

            @Override
            public void play_prev() {
                ((MainActivity) getActivity()).play_prev();
            }

            @Override
            public void resume() {
                ((MainActivity) getActivity()).resume();
            }

            @Override
            public void pause() {
                ((MainActivity) getActivity()).pause();
            }

            @Override
            public void play_new(int index, boolean shuffle) {
                ((MainActivity) getActivity()).play_new(index, shuffle);
            }

            @Override
            public void setPlayMode(int i) {

            }

            @Override
            public boolean isPlaying() {
                return ((MainActivity) getActivity()).isPlaying();
            }

            @Override
            public void setList(ArrayList<MusicItem> list, String state, int index) {
                ((MainActivity) getActivity()).setList(list, state, index);
            }

        }, getActivity().findViewById(R.id.main_layout), (MusicItem item,int i, OnDeleteListener listener) -> openMenu(item,i,listener));
        if(!albumName.equals("Download")) {
            ItemTouchHelper itemTouchHelper = new
                    ItemTouchHelper(new SwipeCallBackPlList(adapter));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
        recyclerView.setAdapter(adapter);
        ((MainActivity) getActivity()).setMusicListMethods(pb -> {
            try{
                getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    if(storageUtil.loadPlayingPlaylist().equals(storageUtil.loadVisiblePlaylist())) {
                        if (pb.equals(PlaybackStatus.PAUSED)) {
                            buttonPlay.setText("PLAY");
                            thisPlaying = false;
                        } else {
                            buttonPlay.setText("PAUSE");
                            thisPlaying = true;
                        }
                    }else{
                        buttonPlay.setText("PLAY");
                        thisPlaying = false;
                    }
                });
            } catch (Exception e) {
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        buttonLayout.animate().setDuration(100).alpha(0);
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        buttonLayout.animate().setDuration(200).alpha(1);
                        break;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return v;
    }

    private void openMenu(MusicItem item,int i, OnDeleteListener listener) {
        BottomDialogFragmentSong bottomDialogFragmentSong = new BottomDialogFragmentSong(item, i, listener, new EditUpdateHelper() {
            @Override
            public void update() {
                if(albumName=="All")
                adapter.setItems(((MainActivity)getActivity()).getPlayLists().get(myIndex).getItems());
                adapter.notifyDataSetChanged();
            }
        });
        bottomDialogFragmentSong.show(getFragmentManager(),"BottomSheet");
    }

    private FragmentOpener getFragmentOpener() {
        return new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {

            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {

            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image, String name, ArrayList<MusicItem> selected) {
                FragmentManager fm = getFragmentManager();
                MusicChooserFragment musicChooserFragment = new MusicChooserFragment(null, myIndex, musicItems, this,name);
                if(name.equals("home")) {
                    fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayout, musicChooserFragment).commit();
                }else
                    fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll, musicChooserFragment).commit();
            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }
        };
    }


    private void removeFragment() {
        getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(0,R.anim.slide_out).remove(this).commit();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(albumImage, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(albumImage, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


}
