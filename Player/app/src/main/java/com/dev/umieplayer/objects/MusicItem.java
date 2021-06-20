package com.dev.umieplayer.objects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class MusicItem {

    private String data;
    private String title;
    private String album;
    private String artist;
    private byte[] image;
    private int duration;

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    private String genre;
    private String track;
    private String year;

    public Bitmap getImage() {
        Bitmap image = BitmapFactory.decodeByteArray(this.image , 0, this.image.length);
        return image;
    }

    public void setImage(Bitmap image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        this.image = stream.toByteArray();
    }

    public MusicItem(String data, String title, String album, String artist, Bitmap image, int duration, String genre, String track, String year) {
        this.duration = duration;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        this.image = stream.toByteArray();
        this.genre = genre;
        this.track = track;
        this.year = year;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
