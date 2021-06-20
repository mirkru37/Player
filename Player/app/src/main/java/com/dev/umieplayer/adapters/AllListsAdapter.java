package com.dev.umieplayer.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;

public class AllListsAdapter extends RecyclerView.Adapter<AllListsAdapter.MyViewHolder> {

    private String type;
    private Context context;

    private FragmentOpener fragmentOpener;

    public void setList(ArrayList<PlayList> playLists) {
        this.playLists = playLists;
        notifyDataSetChanged();
    }

    private ArrayList<PlayList> playLists;

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

    public AllListsAdapter(Context context, ArrayList<PlayList> playLists, String type, FragmentOpener fragmentOpener) {
        this.type = type;
        this.context = context;
        this.playLists = playLists;
        this.fragmentOpener = fragmentOpener;
    }


    public AllListsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view fill
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_adapter_menu, parent, false);
        final AllListsAdapter.MyViewHolder vh = new AllListsAdapter.MyViewHolder(view);

        return vh;
    }

    public void onBindViewHolder(@NonNull final AllListsAdapter.MyViewHolder holder, final int position) {
        if (position == 0 && type == "pllist") {
            holder.itemImage.setImageResource(R.drawable.new_list);
            holder.itemText.setText("Create new");
            holder.clickZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragmentOpener.openCreator("Play list" + (playLists.size()+1), null, null);
                }
            });
        } else {
            int pos = position;
            if(type == "pllist")
                pos--;
            holder.itemText.setText(playLists.get(pos).getName());
            holder.itemImage.setImageBitmap(playLists.get(pos).getImage());
            holder.clickZone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(type == "pllist")
                        fragmentOpener.openPlayList(playLists.get(position-1),position-1, "pl");
                    else
                        fragmentOpener.openCatalog(playLists.get(position),position,type);
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
        if(type == "pllist")
            return playLists.size()+1;
        else
            return playLists.size();
    }
}