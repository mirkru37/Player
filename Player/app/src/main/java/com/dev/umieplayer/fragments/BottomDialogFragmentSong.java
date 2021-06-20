package com.dev.umieplayer.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.EditUpdateHelper;
import com.dev.umieplayer.interfaces.OnDeleteListener;
import com.dev.umieplayer.objects.MusicItem;

import java.io.File;

@SuppressLint("ValidFragment")
public class BottomDialogFragmentSong extends BottomSheetDialogFragment {

    private LinearLayout edit;
    private LinearLayout add_to_list;
    private LinearLayout share;
    private LinearLayout next_in_list;
    private LinearLayout last_in_list;
    private LinearLayout delete;

    private OnDeleteListener deleteListener;
    private EditUpdateHelper editUpdateHelper;

    private MusicItem audio;
    private int audioIndex;

    public BottomDialogFragmentSong(MusicItem audio, int audioIndex, OnDeleteListener onDeleteListener, EditUpdateHelper editUpdateHelper) {
        this.audio = audio;
        this.deleteListener = onDeleteListener;
        this.audioIndex = audioIndex;
        this.editUpdateHelper = editUpdateHelper;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.custom_bottom_sheet_song, container, false);
        edit = v.findViewById(R.id.edit);
        add_to_list = v.findViewById(R.id.add_to_pl);
        share = v.findViewById(R.id.share);
        next_in_list = v.findViewById(R.id.add_next);
        last_in_list = v.findViewById(R.id.add_last);
        delete = v.findViewById(R.id.delete_song);

        next_in_list.setOnClickListener(v1 -> {
            ((MainActivity)getActivity()).setNextInList(audio, audioIndex);
            close();
        });

        last_in_list.setOnClickListener(v1 -> {
            ((MainActivity)getActivity()).setLastInList(audio, audioIndex);
            close();
        });

        share.setOnClickListener(v1 -> {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = Uri.parse("file://"+audio.getData());
            sharingIntent.setType("audio/*");
            Log.e(audio.getData(),"   ");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sharingIntent.putExtra(Intent.EXTRA_TEXT,"This file shared by Citrus player");
            startActivity(Intent.createChooser(sharingIntent, "Share song"));
            close();
        });

        add_to_list.setOnClickListener(v1 -> {
            DialogAddToListFragment dialogAddToListFragment = new DialogAddToListFragment(((MainActivity)getActivity()).getPlayLists(),audio);
            dialogAddToListFragment.show(getFragmentManager(),"AddToList");
            close();
        });

        delete.setOnClickListener(v1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Delete song?")
                    .setCancelable(false)
                    .setNegativeButton("Cancel", ((dialog, which) -> {

                    }))
                    .setPositiveButton("OK", (dialog, id) -> {
                        Uri uri = Uri.parse(audio.getData());
                        File fdelete = new File(uri.getPath());
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                Toast.makeText(getContext(),"File deleted",Toast.LENGTH_SHORT).show();
                                ((MainActivity)getActivity()).deleteSong(audio);
                                deleteListener.delete(audioIndex);
                            } else {
                                Toast.makeText(getContext(),"File not deleted",Toast.LENGTH_SHORT).show();
                            }
                        }
                        close();
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        edit.setOnClickListener(v1 -> {
            DialogEditFragment dialogEditFragment = new DialogEditFragment(audio, editUpdateHelper);
            dialogEditFragment.show(getFragmentManager(),"EditFrag");
            close();
        });

        return v;
    }

    private void close() {
        this.dismiss();
    }
}