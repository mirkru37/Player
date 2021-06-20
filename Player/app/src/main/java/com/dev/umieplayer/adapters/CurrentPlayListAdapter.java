package com.dev.umieplayer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;
import com.dev.umieplayer.fragments.CurrentListFragment;
import com.dev.umieplayer.interfaces.ItemTouchHelperAdapter;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.utils.StorageUtil;

import java.util.ArrayList;
import java.util.Collections;

public class CurrentPlayListAdapter  extends RecyclerView.Adapter<CurrentPlayListAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    public static LayoutInflater lInflater;
    public static ArrayList<MusicItem> musicList;//List of music
    private static ArrayList<MusicItem> musicListFiltered;
    private ServiceMethods serviceMethods;
    private int pressedIndex;
    private TextView prevSong = null;
    private TextView prevAuthor;

    private int currantPos;

    private MusicItem mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;
    private View view;

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
        if(currantPos == position)
            serviceMethods.setList(musicListFiltered, "deleted_curr", position);
        else if(position>currantPos)
            serviceMethods.setList(musicListFiltered, "deleted", position);
        else
            serviceMethods.setList(musicListFiltered, "deleted<", position);
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
        serviceMethods.setList(musicListFiltered, "restore", 0);
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(musicListFiltered, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(musicListFiltered, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        if(currantPos==fromPosition)
            currantPos = toPosition;
        serviceMethods.setList(musicListFiltered,"move",currantPos);

    }

    public void swipeTags(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
//        String temp = String.valueOf(viewHolder.itemView.getTag());
//        Log.e(temp+"      ", String.valueOf(viewHolder1.itemView.getTag()));
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView musicTittle;
        public TextView musicAuthor;
        public ImageView play_Pause;
        public ImageView musicImage;
        public LinearLayout tapArea;

        public MyViewHolder(View view) {
            super(view);
            musicTittle = view.findViewById(R.id.cmName);
            musicTittle.setSelected(true);
            musicAuthor = view.findViewById(R.id.cmAuthor_Album);
            musicAuthor.setSelected(true);
            play_Pause = (ImageView) view.findViewById(R.id.cmPlayPause);
            musicImage = (ImageView) view.findViewById(R.id.cmImage);
            tapArea = view.findViewById(R.id.tap_area);
        }

    }

    public CurrentPlayListAdapter(Context context, ArrayList<MusicItem> arrayList, ServiceMethods sm, View view) {
        this.context = context;
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
    public CurrentPlayListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_list_layout_adapter, parent, false);
        final CurrentPlayListAdapter.MyViewHolder vh = new CurrentPlayListAdapter.MyViewHolder(view);
        return vh;
    }

    private int checkAction() {
        StorageUtil storage = new StorageUtil(context);
        String old = storage.getPrevousData();
        String new_ = musicListFiltered.get(pressedIndex).getData();
        if (new_.equals(old) && old != null) {
            if (serviceMethods.isPlaying())
                return 1;//pause
            else
                return 2;//resume
        }
        old = storage.loadPlayingPlaylist();
        new_ = storage.loadVisiblePlaylist();
        if (!new_.equals(old) || !musicList.equals(musicListFiltered)) {
            //servise set play list
            serviceMethods.setList(musicListFiltered);
            storage.storePlayingPlaylist(storage.loadVisiblePlaylist());
        }
        return 0; //play new song
    }

    @SuppressLint("ResourceAsColor")
    public void onBindViewHolder(@NonNull final CurrentPlayListAdapter.MyViewHolder holder, int position) {
        //set data

        MusicItem item = getMusicItem(position);

        holder.musicTittle.setText(item.getTitle());
        holder.musicAuthor.setText(item.getArtist() + "\u16EB" + item.getAlbum());
        holder.tapArea.setTag(position);
        holder.musicImage.setImageBitmap(item.getImage());

        StorageUtil storage = new StorageUtil(context);
        String old = storage.getCurrentData();
        String new_ = musicListFiltered.get(position).getData();
        if (old.equals(new_)) {
            currantPos = position;
            holder.musicTittle.setTextColor(Color.rgb(176, 49, 204));
            holder.musicAuthor.setTextColor(Color.rgb(234, 137, 255));
            prevSong = holder.musicTittle;
            prevAuthor = holder.musicAuthor;
        } else {
            holder.musicTittle.setTextColor(Color.WHITE);
            holder.musicAuthor.setTextColor(Color.WHITE);
        }

        holder.tapArea.setOnClickListener(view -> {
            pressedIndex = Integer.parseInt(String.valueOf(view.getTag()));
            switch (checkAction()) {
                case 0://play new song
                    serviceMethods.play_new(Integer.parseInt(String.valueOf(view.getTag())), false);
                    break;
                case 1://pause
                    serviceMethods.pause();
                    break;
                case 2://resume
                    serviceMethods.resume();
                    break;
            }
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