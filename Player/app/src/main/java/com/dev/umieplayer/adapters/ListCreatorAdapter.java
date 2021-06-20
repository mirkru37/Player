package com.dev.umieplayer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.interfaces.ItemTouchHelperAdapter;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;

import java.util.ArrayList;
import java.util.Collections;

public class ListCreatorAdapter extends RecyclerView.Adapter<ListCreatorAdapter.MyViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private View view;
    public static ArrayList<MusicItem> musicList;//List of music
    private FragmentOpener fragmentOpener;
    private MusicItem mRecentlyDeletedItem;
    private int mRecentlyDeletedItemPosition;

    public void setItems(ArrayList<MusicItem> musicItems) {
        musicList = musicItems;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public void deleteItem(int position) {
        mRecentlyDeletedItem = musicList.get(position-1);
        mRecentlyDeletedItemPosition = position;
        musicList.remove(position-1);
        notifyItemRemoved(position);
        showUndoSnackbar();
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(view, "Deleted.",
                Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", v -> undoDelete());
        snackbar.show();
    }

    private void undoDelete() {
        musicList.add(mRecentlyDeletedItemPosition-1,
                mRecentlyDeletedItem);
        notifyItemInserted(mRecentlyDeletedItemPosition);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if(fromPosition==0||toPosition==0){
            return;
        }
        else if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(musicList, i-1, i );
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(musicList, i-1, i - 2);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView musicTittle;
        public TextView musicAuthor;
        public ImageView musicImage;
        public RelativeLayout musicImageCont;
        public View mainLayout;

        public MyViewHolder(View view) {
            super(view);
            musicTittle = view.findViewById(R.id.cmName);
            musicTittle.setSelected(true);
            musicAuthor = view.findViewById(R.id.cmAuthor_Album);
            musicAuthor.setSelected(true);
            musicImage = (ImageView) view.findViewById(R.id.cmImage);
            musicImageCont = view.findViewById(R.id.cmImageContainer);
            mainLayout = view.findViewById(R.id.item_layout);
        }

    }

    public ListCreatorAdapter(Context context, ArrayList<MusicItem> arrayList, FragmentOpener fragmentOpener, View view) {
        this.context = context;
        this.musicList = arrayList;
        this.fragmentOpener = fragmentOpener;
        this.view = view;
    }

    MusicItem getMusicItem(int position) {
        return ((MusicItem) getItem(position));
    }


    public MusicItem getItem(int position) {
        return musicList.get(position);
    }


    public ListCreatorAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_create_adapter, parent, false);
        final ListCreatorAdapter.MyViewHolder vh = new ListCreatorAdapter.MyViewHolder(view);
        return vh;
    }

    public void onBindViewHolder(@NonNull final ListCreatorAdapter.MyViewHolder holder, int position) {
        //set data
        if(position==0){
            //add
            holder.musicImageCont.setVisibility(View.GONE);
            holder.musicAuthor.setVisibility(View.GONE);
            holder.musicTittle.setTextSize(context.getResources().getDimension(R.dimen.textSmallSizePr));
            holder.musicTittle.setGravity(Gravity.CENTER);
            holder.musicTittle.setText("+Add songs");
            holder.musicTittle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentOpener.openSelector(null, null,null);
                }
            });

        }else{
            holder.musicTittle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            holder.musicImageCont.setVisibility(View.VISIBLE);
            holder.musicAuthor.setVisibility(View.VISIBLE);
            holder.musicTittle.setTextSize(context.getResources().getDimension(R.dimen.textdefaultSizePr));
            holder.musicTittle.setGravity(Gravity.LEFT);
            MusicItem item = getMusicItem(position-1);
            holder.musicTittle.setText(item.getTitle());
            holder.musicAuthor.setText(item.getArtist() + "\u16EB" + item.getAlbum());
            holder.musicImage.setImageBitmap(item.getImage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return musicList.size()+1;
    }
}
