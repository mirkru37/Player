package com.dev.umieplayer.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PlayList {
    private String name;
    private byte[] image;
    private ArrayList<MusicItem> items;

    public PlayList(String name, Bitmap image, ArrayList<MusicItem> items) {
        this.name = name;
        setImage(image);
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        Bitmap image = BitmapFactory.decodeByteArray(this.image , 0, this.image.length);
        return image;
    }

    public void setImage(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        this.image = stream.toByteArray();
    }

    public ArrayList<MusicItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<MusicItem> items) {
        this.items = items;
    }

    public void addItem(MusicItem item){
        items.add(item);
    }
}
