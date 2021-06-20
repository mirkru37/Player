package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.callbacks.RecyclerItemClickListener;
import com.dev.umieplayer.adapters.SelectorAdapter;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.objects.MusicItem;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class MusicChooserFragment extends Fragment{

    private String type;
    private RecyclerView mRecyclerView;
    private SelectorAdapter mAdapter;
    private LinearLayoutManager manager;

    private TextView cancel;
    private TextView done;

    private Bitmap listImage;
    private String listName;
    private ArrayList<MusicItem> added;
    private ArrayList<MusicItem> notadded;
    private ArrayList<MusicItem> selected = new ArrayList<>();
    private FragmentOpener fragmentOpener;

    private int index = -1;

    private boolean isMultiSelect = false;

    public MusicChooserFragment(Bitmap image, String name, ArrayList<MusicItem> selected, FragmentOpener fragmentOpener) {
        listImage = image;
        listName = name;
        if (selected==null)
            this.added = new ArrayList<>();
        else
            this.added = selected;
        this.fragmentOpener = fragmentOpener;
    }

    public MusicChooserFragment(Bitmap image, int myIndex, ArrayList<MusicItem> musicItems, FragmentOpener fragmentOpener, String t) {
        listImage = image;
        index = myIndex;
        type = t;
        if (musicItems==null)
            this.added = new ArrayList<>();
        else
            this.added = musicItems;
        this.fragmentOpener = fragmentOpener;
    }

    private void removeAdded() {
        if(added.size()==0)
            return;
        for(int i=0;i<added.size();i++){
            String data = added.get(i).getData();
            for(int j=0;j<notadded.size();j++){
                if(data.equals(notadded.get(j).getData())) {
                    notadded.remove(j);
                    break;
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_music_chooser, container, false);
        done = v.findViewById(R.id._done_action);
        done.setOnClickListener(v1 -> {
            if(selected.size() <= 0)
                Snackbar.make(getActivity().findViewById(R.id.main_layout), "You must to choose at least one music", Snackbar.LENGTH_SHORT).show();
            else{
                added.addAll((Collection<? extends MusicItem>) selected.clone());
                openCreator();
            }
        });
        cancel = v.findViewById(R.id._cancel_action);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreator();
            }
        });
        mRecyclerView = v.findViewById(R.id.list);
        notadded = ((MainActivity)getActivity()).getAllMusic();
        removeAdded();
        mAdapter = new SelectorAdapter(getContext(),notadded);
        manager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect){
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect){
                    selected = new ArrayList<>();
                    isMultiSelect = true;

                }

                multiSelect(position);
            }
        }));

        return v;
    }



    private void openCreator() {
        mAdapter.clear();
        if(index>=0){
            ((MainActivity)getActivity()).setListItems(index,added);
            ((MainActivity)getActivity()).openPlayList(index,type);
        }
        fragmentOpener.openCreator(listName,listImage,added);
    }

    private void multiSelect(int position) {
        MusicItem data = mAdapter.getItem(position);
        if (data != null) {
            if (!selected.contains(data))
                selected.add(data);
            else {
                selected.remove(data);
            }

            if (selected.size() > 0) {
            }
            //show selected item count on action mode.
            else {
                isMultiSelect=false;
                //remove item count from action mode.
                //hide action mode.
            }
            mAdapter.setSelected(selected);
        }
    }

}
