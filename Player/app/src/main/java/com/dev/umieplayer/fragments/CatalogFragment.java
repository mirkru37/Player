package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.SongListAdapter;
import com.dev.umieplayer.enums.PlaybackStatus;
import com.dev.umieplayer.interfaces.EditUpdateHelper;
import com.dev.umieplayer.interfaces.HomePageUpdate;
import com.dev.umieplayer.interfaces.MenuOpener;
import com.dev.umieplayer.interfaces.MusicListMethods;
import com.dev.umieplayer.interfaces.OnDeleteListener;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;
import com.dev.umieplayer.utils.StorageUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class CatalogFragment extends Fragment  implements AppBarLayout.OnOffsetChangedListener   {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;
    private String state;
    private int index;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private TextView mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageView albumImage;
    private ImageView buttonBack;
    private RelativeLayout buttonLayout;

    private String albumName;
    private String folder;

    private ArrayList<MusicItem> musicItems;
    private RecyclerView recyclerView;
    private SongListAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private Button buttonPlay;
    private boolean thisPlaying;
    private StorageChooser chooser;

    private Bitmap image;

    public CatalogFragment(String albumName, ArrayList<MusicItem> items) {
        // Required empty public constructor
        this.albumName = albumName;
        folder = albumName;
        this.state = "folder";
        musicItems = (ArrayList<MusicItem>) items.clone();
    }

    public CatalogFragment(PlayList list, String s, int index) {
        albumName = list.getName();
        folder = albumName;
        this.state = s;
        this.index = index;
        musicItems = list.getItems();
        image =  list.getItems().get(0).getImage();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_catalog, container, false);

        chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
                .withMemoryBar(false)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .disableMultiSelect()
                .build();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Log.e("SELECTED_PATH", path);
//                folder = path;
                setFolder(path);
            }
        });
        
        mTitle    = v.findViewById(R.id.main_textview_title);
        mTitle.setSelected(true);
        mToolbar  = v.findViewById(R.id.main_toolbar);
        mTitleContainer = v.findViewById(R.id.infoText);
        mTitleContainer.setSelected(true);
        albumImage = v.findViewById(R.id.infoImage);
        if(state!="folder")
            albumImage.setImageBitmap(image);
        mAppBarLayout   = v.findViewById(R.id.main_appbar);
        buttonBack = v.findViewById(R.id.buttonBack);
        buttonPlay = v.findViewById(R.id.icPlay);
        buttonLayout = v.findViewById(R.id.playLayout);
        final StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
        String old = storageUtil.loadPlayingPlaylist();
        if(old.equals(albumName)){
           if(((MainActivity)getActivity()).isPlaying()) {
               buttonPlay.setText("PAUSE");
               thisPlaying = true;
           }
           else
               thisPlaying = false;
        }

        buttonPlay.setOnClickListener(v1 -> {
            StorageUtil storageUtil1 = new StorageUtil(getActivity().getApplicationContext());
            String old1 = storageUtil1.loadPlayingPlaylist();
            if(!old1.equals(albumName)&&musicItems.size()>0){
                ((MainActivity)getActivity()).setList(musicItems);
                ((MainActivity)getActivity()).play_new(0, true);
                storageUtil1.storePlayingPlaylist(storageUtil1.loadVisiblePlaylist());
                buttonPlay.setText("PAUSE");
                thisPlaying = true;
            }else if(musicItems.size()>0){
                if(thisPlaying){
                    ((MainActivity)getActivity()).pause();
                    buttonPlay.setText("PLAY");
                    thisPlaying = false;
                }else{
                    ((MainActivity)getActivity()).resume();
                    buttonPlay.setText("PAUSE");
                    thisPlaying = true;
                }
            }
        });
        buttonBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView v = (ImageView) arg0;
                        v.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        v.setScaleX(0.9f);
                        v.setScaleY(0.9f);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:{
                        ImageView v = (ImageView) arg0;
                        v.getDrawable().clearColorFilter();
                        v.invalidate();
                        v.setScaleX(1f);
                        v.setScaleY(1f);
                        removeFragment();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:{
                        ImageView v = (ImageView) arg0;
                        v.getDrawable().clearColorFilter();
                        v.invalidate();
                        break;
                    }
                }
                return true;
            }
        });
        mTitle.setText(albumName);
        mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals("folder"))
                    chooser.show();
            }
        });
        mTitleContainer.setText(albumName);
        mTitleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.equals("folder"))
                    chooser.show();
            }
        });
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

            }
        }, getActivity().findViewById(R.id.main_layout), (item, i, onDeleteListener) -> openMenu(item,i,onDeleteListener));
        recyclerView.setAdapter(adapter);
        ((MainActivity)getActivity()).setMusicListMethods(pb -> {
            try{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                    }
                });

            }catch (Exception e){}
        });

    recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState){
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

    private void openMenu(MusicItem item, int i, OnDeleteListener deleteListener) {
        BottomDialogFragmentSong bottomDialogFragmentSong = new BottomDialogFragmentSong(item,i,deleteListener, new EditUpdateHelper() {
            @Override
            public void update() {
                if(albumName=="All")
                adapter.setItems(((MainActivity)getActivity()).getAllMusic());
                else if(albumName == "Download")
                    adapter.setItems(((MainActivity)getActivity()).getPlayLists().get(0).getItems());
                else if(state == "folder")
                    adapter.setItems(((MainActivity)getActivity()).getFolderMusic());
                else if(state == "albums")
                    adapter.setItems(((MainActivity)getActivity()).getAlbums().get(index).getItems());
                else
                    adapter.setItems(((MainActivity)getActivity()).getAuthors().get(index).getItems());

                adapter.notifyDataSetChanged();
            }
        });
        bottomDialogFragmentSong.show(getFragmentManager(),"BottomSheet");
    }

    private void setFolder(String folder) {
        albumName = folder;
        ((MainActivity)getActivity()).setFolderPath(folder);
        mTitleContainer.setText(albumName);
        mTitle.setText(albumName);
        StorageUtil storageUtil = new StorageUtil(getActivity().getApplicationContext());
        if(storageUtil.loadPlayingPlaylist() != albumName) {
            buttonPlay.setText("PLAY");
            storageUtil.storeVisiblePlaylist(albumName);
            ((MainActivity) getActivity()).setFolderPath(folder);
            musicItems = ((MainActivity) getActivity()).getFolderMusic();
            if(musicItems==null)
                musicItems=new ArrayList<MusicItem>();
            //  Toast.makeText(getContext(),musicItems.size()+"fffff////" + folder,Toast.LENGTH_LONG).show();
            adapter.setItems(musicItems);
        }
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

            if(!mIsTheTitleVisible) {
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
            if(mIsTheTitleContainerVisible) {
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

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
