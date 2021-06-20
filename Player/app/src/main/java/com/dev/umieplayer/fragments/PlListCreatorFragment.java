package com.dev.umieplayer.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.umieplayer.MainActivity;
import com.dev.umieplayer.R;
import com.dev.umieplayer.callbacks.SwipeCallback;
import com.dev.umieplayer.adapters.ListCreatorAdapter;
import com.dev.umieplayer.interfaces.FragmentOpener;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class PlListCreatorFragment extends Fragment {

    private final int GALERY_REQUEST = 0;
    private final int CAMERA_REQUEST = 1;

    private ImageView listImage;
    private EditText listName;
    private RecyclerView list;
    private ListCreatorAdapter madapter;
    private TextView cancel;
    private TextView done;

    private ArrayList<MusicItem> items = null;
    private String name;
    private Bitmap image;

    private String userChoosenTask;

    private FragmentOpener fragmentOpener;


    public PlListCreatorFragment(String name, Bitmap image, ArrayList<MusicItem> items, FragmentOpener fragmentOpener) {
        // Required empty public constructor
        this.name = name;
        if(image!=null)
            this.image = image;
        else
            this.image = null;
        if(items!=null)
            this.items = items;
        else
            this.items = new ArrayList<>();
        this.fragmentOpener = fragmentOpener;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pl_list_creator, container, false);
        listImage = v.findViewById(R.id.listImage);
        listImage.setOnClickListener(v1 -> selectImage());
        listName = v.findViewById(R.id.new_listText);
        list = v.findViewById(R.id.newList_songs);
        madapter = new ListCreatorAdapter(getActivity().getApplicationContext(), items, new FragmentOpener() {
            @Override
            public void openPlayList(PlayList list, int index,String type) {

            }

            @Override
            public void openCatalog(PlayList list, int index,String type) {

            }

            @Override
            public void openAllLists() {

            }

            @Override
            public void openSelector(Bitmap image_, String name, ArrayList<MusicItem> selected) {
                fragmentOpener.openSelector(image, String.valueOf(listName.getText()), items);
                hideKeyboard(getActivity());
            }

            @Override
            public void openCreator(String s, Bitmap default_music_image, ArrayList<MusicItem> o) {

            }
        },getActivity().findViewById(R.id.main_layout));
        list.setLayoutManager(new LinearLayoutManager(v.getContext()));
        list.setAdapter(madapter);
        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeCallback(madapter));
        itemTouchHelper.attachToRecyclerView(list);
        if(image==null)
            listImage.setImageResource(R.drawable.default_music_image);
        else
            listImage.setImageBitmap(image);
        listName.setText(name);
        cancel = v.findViewById(R.id._cancel_action);
        done = v.findViewById(R.id._done_action);
        done.setOnClickListener(v13 -> {
            String name = String.valueOf(listName.getText());
            if(!name.matches(".*\\w.*"))
                Snackbar.make(getActivity().findViewById(R.id.main_layout),"The name cannot consist of spaces or be empty!",Snackbar.LENGTH_LONG).show();
            else if(items.size()<=0)
                Snackbar.make(getActivity().findViewById(R.id.main_layout),"Song list cannot be empty!",Snackbar.LENGTH_LONG).show();
            else{
                if(image==null)
                    image = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                ((MainActivity)getActivity()).addPlayList(name,image,items);
                removeFrag();
            }
        });
        cancel.setOnClickListener(v12 -> {
           removeFrag();
            hideKeyboard(getActivity());
        });
        return v;
    }


    private void removeFrag(){
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
        listImage.setImageBitmap(thubnail);
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
        listImage.setImageBitmap(bm);
        image = bm;
    }
}