package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.adapters.GenrePicherAdapter;
import com.dev.umieplayer.interfaces.EditUpdateHelper;
import com.dev.umieplayer.objects.MusicItem;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class DialogEditFragment extends DialogFragment {
    private final int GALERY_REQUEST = 0;
    private final int CAMERA_REQUEST = 1;
    private String userChoosenTask;

    private static final String[] GENRES = {"Blues", "Jazz", "Rock", "Country", "Hip Hop", "Rap", "R&B", "K-Pop", "Pop", "Classical", "Blues", "Heavy Metal", "Folk", "Reggae", "Industrial", "Techno", "Dubstap", "Hard Bass"} ;
    private MusicItem audio;

    private ImageView editImage;
    private EditText editArtist;
    private EditText editName;
    private EditText editAlbum;
    private TextView editGenre;
    private EditText trackNum;
    private TextView editYear;

    private Bitmap image = null;

    private Button cancel;
    private Button done;

    private  int year;
    private String genre;

    GenrePicherAdapter genrePicherAdapter;
    EditUpdateHelper editUpdateHelper;

    public DialogEditFragment(MusicItem a, EditUpdateHelper editUpdateHelper) {
        this.audio = a;
        this.editUpdateHelper = editUpdateHelper;
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_edit, container, false);
        // Inflate the layout for this fragment
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        editImage = v.findViewById(R.id.editImage);
        editImage.setImageBitmap(audio.getImage());
        image = audio.getImage();

        editArtist = v.findViewById(R.id.editArtist);
        editArtist.setText(audio.getArtist());

        editName  = v.findViewById(R.id.editName);
        editName.setText(audio.getTitle());

        editAlbum = v.findViewById(R.id.editAlbum);
        editAlbum.setText(audio.getAlbum());

        editGenre =  v.findViewById(R.id.editGenre);
        editGenre.setText(audio.getGenre());

        trackNum =  v.findViewById(R.id.editTrackNum);
        if(audio.getTrack()=="")
            trackNum.setText(String.valueOf(audio.getTrack()));

        editYear =  v.findViewById(R.id.editYear);
        editYear.setText(audio.getYear());

        cancel = v.findViewById(R.id.cancel_action);
        done = v.findViewById(R.id._done_action);

        cancel.setOnClickListener(v1 -> {
            close();
        });

        editYear.setOnClickListener(v1 -> {
            showYearDialog();
        });

        editGenre.setOnClickListener(v1 -> {
            showGenreDialog();
        });

        done.setOnClickListener(v1 -> {
            done();
        });

        editImage.setOnClickListener(v1 -> {
            selectImage();
        });

        return v;
    }

    private void done() {
        String title = String.valueOf(editName.getText());
        String author = String.valueOf(editArtist.getText());
        String album = String.valueOf(editAlbum.getText());
        String genre = String.valueOf(editGenre.getText());
        String track = String.valueOf(trackNum.getText());
        String year = String.valueOf(editYear.getText());

        if(title.isEmpty()||author.isEmpty()||!title.matches(".*\\w.*")||!author.matches(".*\\w.*")){
            Toast.makeText(getContext(),"Fields are incorrect",Toast.LENGTH_SHORT).show();
        }else{
            //save data
            save(title,author,album,genre,track,year);
            close();
        }

    }

    private void save(String title, String author, String album, String genre, String track, String year) {
        MP3File mp3file = null;
        try {
            mp3file = new MP3File(new File(audio.getData()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ID3v24Tag tag = new ID3v24Tag();
        mp3file.setID3v2Tag(tag);

        try {
            tag.setField(FieldKey.ARTIST,author);
            if(album!=null&&album!=""&&album.matches(".*\\w.*"))
            tag.setField(FieldKey.ALBUM,album);
            tag.setField(FieldKey.TITLE,title);
            if(genre!="")
            tag.setField(FieldKey.GENRE,genre);
            if(track!=null&&track!="")
            tag.setField(FieldKey.TRACK,track);
            if(year!=null&&year!=""&&year.matches(".*\\w.*"))
            tag.setField(FieldKey.YEAR,year);
            if(image!=null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] img = stream.toByteArray();
                tag.setField(tag.createArtworkField(img, "image/jpeg"));
            }
            mp3file.commit();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("(((((","((((((");
        }
        ((MainActivity)getActivity()).save(audio,title,author,album,genre,track,year,image);
        editUpdateHelper.update();
    }


    private void selectImage(){
        final CharSequence[] items = {"Take photo","Choose from gallery","Cancel"};

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Change photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Take photo")){
                    userChoosenTask = "Take photo";
                    cameraIntent();
                }else if(items[i].equals("Choose from gallery")){
                    userChoosenTask = "Choose from gallery";
                    galleryIntent();
                }else if (items[i].equals("Cancel"))
                    dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void galleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select File"), GALERY_REQUEST);
    }

    private void cameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALERY_REQUEST)
            onSelectesFromGalleryResult(data);
        else if(requestCode == CAMERA_REQUEST)
            onCaptureImageResult(data);
    }

    private void onCaptureImageResult(Intent data){
        Bitmap thubnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thubnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        thubnail = cropBitmap(thubnail);
        editImage.setImageBitmap(thubnail);
        image = thubnail;
    }

    private Bitmap cropBitmap(Bitmap srcBmp) {
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }
        return dstBmp;
    }

    private void onSelectesFromGalleryResult(Intent data){
        Bitmap bm = null;
        if(data!=null)
            try{
                bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        bm = cropBitmap(bm);
        editImage.setImageBitmap(bm);
        image = bm;
    }

    private void close() {
        this.dismiss();
    }

    public void showYearDialog() {

        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.yeardialog);
        Button set = (Button) d.findViewById(R.id.button1);
        Button cancel = (Button) d.findViewById(R.id.button2);
        TextView year_text=(TextView)d.findViewById(R.id.year_text);
        Button allGenres = d.findViewById(R.id.allGenres);
        allGenres.setVisibility(View.GONE);
        if(audio.getYear()!="")
            year = Integer.parseInt(audio.getYear());
        else
            year = Calendar.getInstance().get(Calendar.YEAR);

        year_text.setText("Choose year");
        final NumberPicker nopicker = (NumberPicker) d.findViewById(R.id.numberPicker1);

        nopicker.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));
        nopicker.setMinValue(1000);
        nopicker.setWrapSelectorWheel(false);
        nopicker.setValue(year);
        nopicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        set.setOnClickListener(v -> {
            editYear.setText(String.valueOf(nopicker.getValue()));
            year = nopicker.getValue();
            d.dismiss();
        });
        cancel.setOnClickListener(v -> d.dismiss());
        d.show();


    }

    public void showGenreDialog() {
        final Dialog d = new Dialog(getActivity());
        d.setContentView(R.layout.yeardialog);
        Button set = (Button) d.findViewById(R.id.button1);
        Button cancel = (Button) d.findViewById(R.id.button2);
        TextView year_text=(TextView)d.findViewById(R.id.year_text);
        Button allGenres = d.findViewById(R.id.allGenres);

        allGenres.setOnClickListener(v -> {
            showAdvancelGenreDialog();
            d.dismiss();
        });

        if(audio.getGenre()!="")
            genre = audio.getGenre();

        year_text.setText("Choose genre");
        final NumberPicker nopicker = (NumberPicker) d.findViewById(R.id.numberPicker1);

        nopicker.setDisplayedValues(GENRES);

        nopicker.setMaxValue(GENRES.length-1);
        nopicker.setMinValue(0);
        nopicker.setWrapSelectorWheel(false);
        nopicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        set.setOnClickListener(v -> {
            editGenre.setText(String.valueOf(nopicker.getDisplayedValues()[nopicker.getValue()]));
            d.dismiss();
        });
        cancel.setOnClickListener(v -> d.dismiss());
        d.show();


    }

    private void showAdvancelGenreDialog() {
        final Dialog v = new Dialog(getActivity());
        v.setContentView(R.layout.advaced_genres);
        EditText search = v.findViewById(R.id.search);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                genrePicherAdapter.filter(s);
            }
        });

        RecyclerView recyclerView = v.findViewById(R.id.genreChooser);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        genrePicherAdapter = new GenrePicherAdapter(v1 -> {
            genre = genrePicherAdapter.getSelected();
            editGenre.setText(genre);
            v.dismiss();
        });
        recyclerView.setAdapter(genrePicherAdapter);
        v.show();
    }

}
