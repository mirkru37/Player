package com.dev.umieplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.AddToListHelper;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

public class AddToListAdapter extends RecyclerView.Adapter<AddToListAdapter.MyViewHolder> {

    private Context context;
    private boolean multi = false;
    public static ArrayList<PlayList> plLists;//List of music
    private AddToListHelper addToListHelper;

    public static ArrayList<PlayList> getSelected() {
        return selected;
    }

    public static ArrayList<PlayList> selected = new ArrayList<>();//List of music

    public AddToListAdapter(ArrayList<PlayList> plLists, AddToListHelper asd) {
        this.plLists = (ArrayList<PlayList>) plLists.clone();
        addToListHelper = asd;
        this.plLists.remove(0);
    }


    public AddToListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_create_adapter, parent, false);
        final AddToListAdapter.MyViewHolder vh = new AddToListAdapter.MyViewHolder(view);
        return vh;
    }

    PlayList getMusicItem(int position) {
        return ((PlayList) getItem(position));
    }


    public PlayList getItem(int position) {
        return plLists.get(position);
    }


    public void onBindViewHolder(@NonNull final AddToListAdapter.MyViewHolder holder, int position) {
        PlayList item = plLists.get(position);
        holder.musicTittle.setText(item.getName());
        holder.musicAuthor.setVisibility(View.GONE);
        holder.musicImage.setImageBitmap(item.getImage());
        holder.rootView.setOnClickListener(v -> {
            addToListHelper.addToList(position);
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return plLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView musicTittle;
        public TextView musicAuthor;
        public ImageView musicImage;
        public RelativeLayout musicImageCont;
        public View rootView;

        public MyViewHolder(View view) {
            super(view);
            musicTittle = view.findViewById(R.id.cmName);
            musicTittle.setSelected(true);
            musicAuthor = view.findViewById(R.id.cmAuthor_Album);
            musicAuthor.setSelected(true);
            musicImage = (ImageView) view.findViewById(R.id.cmImage);
            musicImageCont = view.findViewById(R.id.cmImageContainer);
            rootView = view.findViewById(R.id.item_layout);
        }

    }
}
