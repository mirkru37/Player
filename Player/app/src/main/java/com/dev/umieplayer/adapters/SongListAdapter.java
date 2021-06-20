package com.dev.umieplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.EditUpdateHelper;
import com.dev.umieplayer.interfaces.ItemTouchHelperAdapter;
import com.dev.umieplayer.interfaces.MenuOpener;
import com.dev.umieplayer.interfaces.OnDeleteListener;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.utils.StorageUtil;

import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;
import java.util.Collections;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.MyViewHolder> implements ItemTouchHelperAdapter, OnDeleteListener {

    private Context context;
    public static LayoutInflater lInflater;
    public static ArrayList<MusicItem> musicList;//List of music
    private static ArrayList<MusicItem> musicListFiltered;
    private ServiceMethods serviceMethods;
    private int pressedIndex;
    private TextView prevSong = null;
    private TextView prevAuthor;

    private MusicItem mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private View view;
    private int currentPos;
    private MenuOpener menuOpener;

    public void setItems(ArrayList<MusicItem> musicItems) {
        musicList = musicItems;
        musicListFiltered = musicItems;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void deleteItem(int position) {
        mRecentlyDeletedItem = musicListFiltered.get(position);
        mRecentlyDeletedItemPosition = position;
        musicListFiltered.remove(position);
        notifyItemRemoved(position);
        showUndoSnackbar();
        StorageUtil storageUtil = new StorageUtil(getContext());
        if(storageUtil.loadPlayingPlaylist().equals(storageUtil.loadVisiblePlaylist()))
            serviceMethods.setList(musicListFiltered,"deleted",position);
        notifyDataSetChanged();
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(view, "Deleted.",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", v -> undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        musicList.add(mRecentlyDeletedItemPosition,
                mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
        StorageUtil storageUtil = new StorageUtil(getContext());
        if(storageUtil.loadPlayingPlaylist().equals(storageUtil.loadVisiblePlaylist()))
        serviceMethods.setList(musicListFiltered,"restore",0);
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(musicListFiltered, i, i+1 );
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(musicListFiltered, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition,toPosition);
        if(currentPos==fromPosition)
            currentPos = toPosition;
        serviceMethods.setList(musicListFiltered,"move", currentPos);

    }

    @Override
    public void delete(int position) {
        notifyItemRemoved(position);
       // musicListFiltered.remove(position);
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView musicTittle;
        public TextView musicAuthor;
        public ImageView play_Pause;
        public ImageView musicImage;
        public LinearLayout tapArea;
        public ImageView menu;

        public MyViewHolder(View view) {
            super(view);
            musicTittle = view.findViewById(R.id.cmName);
            musicTittle.setSelected(true);
            musicAuthor = view.findViewById(R.id.cmAuthor_Album);
            musicAuthor.setSelected(true);
            play_Pause = (ImageView) view.findViewById(R.id.cmPlayPause);
            musicImage = (ImageView) view.findViewById(R.id.cmImage);
            tapArea = view.findViewById(R.id.tap_area);
            menu = view.findViewById(R.id.itemMenu);
        }

    }

    public SongListAdapter(Context context, ArrayList<MusicItem> arrayList, ServiceMethods sm, View view, MenuOpener menuOpener) {
        this.context = context;
        this.menuOpener = menuOpener;
        lInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.musicList = arrayList;
        this.musicListFiltered = arrayList;
        serviceMethods = sm;
        this.view = view;
    }

    MusicItem getMusicItem(int position) {
        return ((MusicItem) getItem(position));
    }


    public MusicItem getItem(int position) {
        return musicListFiltered.get(position);
    }

    @SuppressLint("ResourceAsColor")
    public SongListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_adapter, parent, false);
        final MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    private int checkAction() {
        StorageUtil storage = new StorageUtil(context);
        String old = storage.getPrevousData();
        String new_ = musicListFiltered.get(pressedIndex).getData();
        if(new_.equals(old)&&old!=null){
            if(serviceMethods.isPlaying())
                return 1;//pause
            else
                return 2;//resume
        }
        old = storage.loadPlayingPlaylist();
        new_ = storage.loadVisiblePlaylist();
        if(!new_.equals(old)||!musicList.equals(musicListFiltered)){
            //servise set play list
            serviceMethods.setList(musicListFiltered);
            storage.storePlayingPlaylist(storage.loadVisiblePlaylist());
        }
        return 0; //play new song
    }

    @SuppressLint("ResourceAsColor")
    public void onBindViewHolder(@NonNull final SongListAdapter.MyViewHolder holder, int position) {
        //set data

        MusicItem item = getMusicItem(position);

        holder.musicTittle.setText(item.getTitle());
        holder.musicAuthor.setText(item.getArtist() + "\u16EB" + item.getAlbum());
        holder.tapArea.setTag(position);
        holder.musicImage.setImageBitmap(item.getImage());
        StorageUtil storage = new StorageUtil(context);
        String old = storage.getCurrentData();
        String new_ = musicListFiltered.get(position).getData();
        if(old.equals(new_)) {
            holder.musicTittle.setTextColor(Color.rgb(176, 49, 204));
            holder.musicAuthor.setTextColor(Color.rgb(234, 137, 255));
            prevSong = holder.musicTittle;
            prevAuthor = holder.musicAuthor;
            currentPos = position;
        }else{
            holder.musicTittle.setTextColor(Color.WHITE);
            holder.musicAuthor.setTextColor(Color.WHITE);
        }

        holder.tapArea.setOnClickListener(view -> {
            pressedIndex = Integer.parseInt(String.valueOf(view.getTag()));  //Integer.parseInt(String.valueOf(view.getTag())), view, serviceMethods.isPlaying()
            switch(checkAction()){
                case 0://play new song
                    serviceMethods.play_new( Integer.parseInt(String.valueOf(view.getTag())),true);
                    break;
                case 1://pause
                    serviceMethods.pause();
                    break;
                case 2://resume
                    serviceMethods.resume();
                    break;
            }
        });

        holder.menu.setOnClickListener(v -> {
            menuOpener.open(musicListFiltered.get(position),position,this);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return musicListFiltered.size();
    }
}
