package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.AddToListAdapter;
import com.dev.umieplayer.interfaces.AddToListHelper;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class DialogAddToListFragment extends DialogFragment {

    private ArrayList<PlayList> plLists;
    private RecyclerView mRecycle;
    private AddToListAdapter mAdapter;
    private MusicItem audiol;

    public DialogAddToListFragment(ArrayList<PlayList> playLists,MusicItem audio) {
        // Required empty public constructor
        plLists = playLists;
        this.audiol = audio;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_dialog_add_to_list, container, false);
       // getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mRecycle = v.findViewById(R.id.plChooser);
        mRecycle.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AddToListAdapter(plLists, i -> {
            ((MainActivity)getActivity()).addToList(i+1,audiol);
            close();
        });
        mRecycle.setAdapter(mAdapter);

        return v;
    }

    private void close() {
        this.dismiss();
    }

}
