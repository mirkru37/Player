package com.dev.umieplayer.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;
import com.dev.umieplayer.objects.MusicItem;

import java.util.ArrayList;

public class SelectorAdapter extends RecyclerView.Adapter<SelectorAdapter.MyViewHolder> {

    private Context context;
    public static ArrayList<MusicItem> musicList;//List of music
    public static ArrayList<MusicItem> selected = new ArrayList<>();//List of music

    public void clear() {
        selected = new ArrayList<>();
        notifyDataSetChanged();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
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

    public SelectorAdapter(Context context, ArrayList<MusicItem> arrayList) {
        this.context = context;
        this.musicList = arrayList;
    }

    MusicItem getMusicItem(int position) {
        return ((MusicItem) getItem(position));
    }


    public MusicItem getItem(int position) {
        return musicList.get(position);
    }


    public SelectorAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_create_adapter, parent, false);
        final SelectorAdapter.MyViewHolder vh = new SelectorAdapter.MyViewHolder(view);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onBindViewHolder(@NonNull final SelectorAdapter.MyViewHolder holder, int position) {
            MusicItem item = getMusicItem(position);
            holder.musicTittle.setText(item.getTitle());
            holder.musicAuthor.setText(item.getArtist() + "\u16EB" + item.getAlbum());
            holder.musicImage.setImageBitmap(item.getImage());
        if (selected.contains(item)){
            //if item is selected then,set foreground color of FrameLayout.
            holder.rootView.setBackground(new ColorDrawable(ContextCompat.getColor(context,R.color.colorSelected)));
            holder.rootView.setAlpha(0.2f);
        }
        else {
            //else remove selected item color.
            holder.rootView.setBackground(new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
        }
    }

    public void setSelected(ArrayList<MusicItem> selectedIds) {
        this.selected = selectedIds;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
