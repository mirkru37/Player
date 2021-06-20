package com.dev.umieplayer.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.objects.PlayList;

import java.nio.channels.AsynchronousFileChannel;
import java.util.ArrayList;

public class PlayListAdapterMenu extends RecyclerView.Adapter<PlayListAdapterMenu.MyViewHolder> {

    private Context context;

    public void setList(ArrayList<PlayList> playLists) {
        this.playLists = playLists;
        notifyDataSetChanged();
    }

    private ArrayList<PlayList> playLists;
    private FragmentOpener fragmentOpener;
    private String type;


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView itemImage;
        public TextView itemText;
        public LinearLayout clickZone;

        public MyViewHolder(View view) {
            super(view);
            clickZone = view.findViewById(R.id.click_zone);
            itemImage = view.findViewById(R.id.menu_item_image);
            itemText = view.findViewById(R.id.menu_item_text);
        }

    }

    public PlayListAdapterMenu(Context context, ArrayList<PlayList> playLists, FragmentOpener opener, String type) {
        this.context = context;
        this.playLists = playLists;
        this.fragmentOpener = opener;
        this.type = type;
    }


    public PlayListAdapterMenu.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_adapter_menu, parent, false);
        final PlayListAdapterMenu.MyViewHolder vh = new PlayListAdapterMenu.MyViewHolder(view);

        return vh;
    }

    public void onBindViewHolder(@NonNull final PlayListAdapterMenu.MyViewHolder holder, final int position) {
        if(position>=getItemCount()-1){
            holder.itemImage.setImageResource(R.drawable.more_menu);
            holder.itemText.setText("More...");
            holder.clickZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        fragmentOpener.openAllLists();
                }
            });
        }else {
            holder.itemText.setText(playLists.get(position).getName());
            holder.itemImage.setImageBitmap(playLists.get(position).getImage());
            holder.clickZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        fragmentOpener.openPlayList(playLists.get(position),position, type);
                }
            });
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(playLists.size()<=3)
            return playLists.size()+1;
        return 4;
    }
}