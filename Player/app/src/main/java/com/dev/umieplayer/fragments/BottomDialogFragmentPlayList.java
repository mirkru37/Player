package com.dev.umieplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.interfaces.PlayListEditMethods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.acl.LastOwnerException;

@SuppressLint("ValidFragment")
public class BottomDialogFragmentPlayList extends BottomSheetDialogFragment {

    private final int GALERY_REQUEST = 0;
    private final int CAMERA_REQUEST = 1;
    private final String type;
    private PlayListEditMethods playListEditMethods;
    private String userChoosenTask;
    private FragmentOpener fragmentOpener;

    public BottomDialogFragmentPlayList(PlayListEditMethods playListEditMethods, FragmentOpener fragmentOpener,String type) {
        this.playListEditMethods = playListEditMethods;
        this.fragmentOpener =fragmentOpener;
        this.type = type;
    }

    public static BottomDialogFragmentPlayList getInstance(PlayListEditMethods playListEditMethods, FragmentOpener fragmentOpener, String type) {
        return new BottomDialogFragmentPlayList(playListEditMethods, fragmentOpener, type);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.custom_bottom_sheet, container, false);

        (view.findViewById(R.id.change_name)).setOnClickListener(v -> {
            closeSelf();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Change name");

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);


            builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(String.valueOf(input.getText()).equals("")){
                        Toast.makeText(getContext(),"The name cannot consist of spaces or be empty!",Toast.LENGTH_LONG).show();
                    }else{
                        String text = String.valueOf(input.getText());
                        if(!text.matches(".*\\w.*")){
                            Toast.makeText(getContext(),"The name cannot consist of spaces or be empty!",Toast.LENGTH_LONG).show();
                        }else{
                            playListEditMethods.changeName(text);
                        }
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        });
        (view.findViewById(R.id.add_songs)).setOnClickListener(v -> {
            addSongs();
            closeSelf();
        });
        (view.findViewById(R.id.change_image)).setOnClickListener(v -> {selectImage();});
        (view.findViewById(R.id.delete_pl)).setOnClickListener(v -> {
            // DO SOMETHING
            playListEditMethods.delete();
            closeSelf();
        });
        return view;
    }

    private void addSongs() {
        fragmentOpener.openSelector(null,type,null);
    }

    private void closeSelf() {
        this.dismiss();
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
        playListEditMethods.changeImage(thubnail);
        closeSelf();
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
        playListEditMethods.changeImage(bm);
        closeSelf();
    }

}
