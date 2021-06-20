package com.dev.umieplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadata;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dev.umieplayer.enums.PlaybackStatus;
import com.dev.umieplayer.fragments.HomeFragment;
import com.dev.umieplayer.fragments.LibraryFragment;
import com.dev.umieplayer.fragments.MusicControlFragment;
import com.dev.umieplayer.fragments.NowPlayingFragment;
import com.dev.umieplayer.fragments.PlayListFragment;
import com.dev.umieplayer.fragments.SearchFragment;
import com.dev.umieplayer.interfaces.HomePageUpdate;
import com.dev.umieplayer.interfaces.MusicControlerMethods;
import com.dev.umieplayer.interfaces.MusicListMethods;
import com.dev.umieplayer.interfaces.ServiceMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.objects.PlayList;
import com.dev.umieplayer.transformers.StackTransformer;
import com.dev.umieplayer.utils.StorageUtil;
import com.felixsoares.animatedbottombar.NavigationListner;
import com.felixsoares.animatedbottombar.model.Item;
import com.felixsoares.animatedbottombar.ui.BottomBar;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;


//https://android-arsenal.com/details/1/7414
//dialog fragment
public class MainActivity extends AppCompatActivity implements ServiceMethods {
    private static final int STORAGE_PERMISSION_REQUEST = 1;
    private static final int WRITE_PERMISSION_REQUEST = 2;

    static final int PAGE_COUNT = 3;

    private ArrayList<MusicItem> allMusic= new ArrayList<>();

    private ArrayList<MusicItem> folderMusic = new ArrayList<>();

    private Snackbar snackbar;

    private String folderPath =  Environment.getExternalStorageDirectory() + "/Download";

    private MyViewPager main_pager;
    private PagerAdapter pagerAdapter;
    private BottomBar bottomNavigationView;
    private MenuItem prevMenuItem;
    private MusicControlFragment musicControlFragment;

    private MusicService player;
    boolean serviceBound = false;

    private MusicListMethods musicListMethods;

    private ArrayList<MusicItem> download;
    private ArrayList<PlayList> playLists;
    private ArrayList<PlayList> authors;

    private HomeFragment homeFragment;
    private LibraryFragment libraryFragment;

    public ArrayList<PlayList> getAuthors() {
        return authors;
    }

    public ArrayList<PlayList> getAlbums() {
        return albums;
    }

    private ArrayList<PlayList> albums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

//
//        try {
//            HttpResponse<JsonNode> response = Unirest.get("https://musixmatchcom-musixmatch.p.rapidapi.com/wsr/1.1/track.search?q_track=chop suey&page_size=5&page=1")
//                    .header("X-RapidAPI-Host", "musixmatchcom-musixmatch.p.rapidapi.com")
//                    .header("X-RapidAPI-Key", "c3c0bafb76msha067b08d2106ff6p15898bjsn3145c20b2bbd")
//                    .asJson();
//        } catch (UnirestException e) {
//            Log.e("((((((((((","(");
//            e.printStackTrace();
//        }

        homeFragment = new HomeFragment();
        libraryFragment = new LibraryFragment();
        musicControlFragment = new MusicControlFragment(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.musicControl, musicControlFragment).commit();
        main_pager = (MyViewPager) findViewById(R.id.mainPager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        main_pager.setAdapter(pagerAdapter);
        main_pager.setPageTransformer(true, new StackTransformer());
        main_pager.setPagingEnabled(false);
        bottomNavigationView = (BottomBar) findViewById(R.id.navigationView);
        bottomNavigationView.addItem(new Item("Home", R.drawable.ic_home_white))
                .addItem(new Item("Search", R.drawable.ic_search_white))
                .addItem(new Item("Library",R.drawable.ic_library_music_white));
        bottomNavigationView.setBgColor(getResources().getColor(R.color.colorPrimary));
        bottomNavigationView.setIconColor(Color.WHITE);
        bottomNavigationView.setIndicatorColor(getResources().getColor(R.color.colorPrimary));
        bottomNavigationView.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        bottomNavigationView.setBgIconColor(getResources().getColor(R.color.colorPrimary));
        bottomNavigationView.setupListener(i -> {
                switch (i) {
                    case 0:
                        main_pager.setCurrentItem(0,true);
                        break;
                    case 1:
                        main_pager.setCurrentItem(1,true);
                        break;
                    case 2:
                        main_pager.setCurrentItem(2,true);
                        break;
                }
        });
        bottomNavigationView.build();
        checkStroragePermission();
    }

    public void loadAlbums(String  name, int pos){
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.ALBUM + " LIKE ?";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection,  new String[]{"%"+ name+"%"}, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                try {
                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(data);

                    String title =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //
                    if(title==null||title=="")
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));

                    String album =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //
                    if(album == null||album=="")
                        album  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //
                    if(artist == null||artist=="")
                        artist  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String genre = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //

                    String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

                    String year =  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));

                    if (genre == null)
                        genre = "";
                    if (year == null)
                        year = "";


                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;

                    Bitmap songImage;


                    byte[] art;
                    try {
                        art = metaRetriver.getEmbeddedPicture();
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 2;
                        songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                    } catch (Exception e) {
                        songImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                    }
                    // Save to audioList
                    albums.get(pos).addItem(new MusicItem(data,title,album,artist,songImage,duration,genre,track,year)); // , songImage
                    metaRetriver.release();
                } catch (Exception e) {
                    Log.e("Error while albums", "File destroyed:" + data);
                }

            }
        }
        cursor.close();
        Collections.shuffle(albums);
    }

    public void loadArtist(String  name, int pos){
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.ARTIST + " LIKE ?";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection,  new String[]{"%"+ name+"%"}, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                try {
                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(data);

                    String title =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //
                    if(title==null||title=="")
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));

                    String album =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //
                    if(album == null||album=="")
                        album  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //
                    if(artist == null||artist=="")
                        artist  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String genre = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //

                    String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

                    String year =  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));

                    if (genre == null)
                        genre = "";
                    if (year == null)
                        year = "";


                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;

                    Bitmap songImage;


                    byte[] art;
                    try {
                        art = metaRetriver.getEmbeddedPicture();
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 2;
                        songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                    } catch (Exception e) {
                        songImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                    }
                    // Save to audioList
                    authors.get(pos).addItem(new MusicItem(data,title,album,artist,songImage,duration,genre,track,year)); // , songImage
                    metaRetriver.release();
                } catch (Exception e) {
                    Log.e("Error while albums", "File destroyed:" + data);
                }

            }
        }
        cursor.close();
        Collections.shuffle(authors);
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        new Thread(() -> {
            this.loadDownload();
        }).start();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        allMusic = new ArrayList<>();
        ArrayList<String> albums_name = new ArrayList<>();
        ArrayList<String> artist_name = new ArrayList<>();
        albums = new ArrayList<>();
        playLists = new ArrayList<>();
        authors = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                try {
                    ///////////

                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(data);



                    String title =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //
                    if(title==null||title=="")
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));

                    String album =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //
                    if(album == null||album=="")
                        album  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //
                    if(artist == null||artist=="")
                        artist  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String genre = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //

                    String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

                    String year =  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));

                    if (genre == null)
                        genre = "";
                    if (year == null)
                        year = "";


                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;

                    Bitmap songImage;


                    byte[] art;
                    try {
                        art = metaRetriver.getEmbeddedPicture();
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 2;
                        songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                    } catch (Exception e) {
                        songImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                    }

                    String metaAlb = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    if(metaAlb==null||album.equals(metaAlb)) {
                        if (!albums_name.contains(album) || albums_name.size() == 0) {
                            albums_name.add(album);
                            String finalAlbum = album;
                            albums.add(new PlayList(album, songImage, new ArrayList<>()));
                            new Thread(() -> {
                                this.loadAlbums(finalAlbum, albums_name.size() - 1);
                            }).start();
                        }
                    }else{
                        albums_name.add(album);
                        if (!albums_name.contains(album) || albums_name.size() == 0) {
                            albums.add(new PlayList(album, songImage, new ArrayList<>()));
                        }else{
                            for(int i =0;i<albums_name.size();i++){
                                if(albums_name.get(i).equals(album))
                                    albums.get(i).addItem(new MusicItem(data, title, album, artist, songImage, duration,genre,track,year));
                            }
                        }
                    }

                    String metaArt = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    if(metaArt==null||artist.equals(metaArt)) {
                        if (!artist_name.contains(artist) || artist_name.size() == 0) {
                            artist_name.add(artist);
                            String finalAlbum = artist;
                            authors.add(new PlayList(artist, songImage, new ArrayList<>()));
                            new Thread(() -> {
                                this.loadArtist(finalAlbum, artist_name.size() - 1);
                            }).start();
                        }
                    }else {
                        artist_name.add(artist);
                        if (!artist_name.contains(artist) || artist_name.size() == 0) {
                            artist_name.add(artist);
                            authors.add(new PlayList(artist, songImage, new ArrayList<>()));
                        }else{
                            for(int i =0;i<artist_name.size();i++){
                                if(artist_name.get(i).equals(artist))
                                    authors.get(i).addItem(new MusicItem(data, title, album, artist, songImage, duration,genre,track,year));
                            }
                        }
                    }

                    // Save to audioList
                    allMusic.add(new MusicItem(data, title, album, artist, songImage, duration,genre,track,year)); // , songImage
                    metaRetriver.release();
                } catch (Exception e) {
                    Log.e("Error while loading", "File destroyed:" + data);
                }

            }
        }
        cursor.close();
    }

    private void loadDownload() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0  AND " + MediaStore.Audio.Media.DATA + " LIKE ?";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, new String[]{"%"+ Environment.getExternalStorageDirectory() + "/Download"+"%"}, sortOrder);
        download = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                try {
                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(data);

                    String title =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //
                    if(title==null||title=="")
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));

                    String album =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //
                    if(album == null||album=="")
                        album  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //
                    if(artist == null||artist=="")
                        artist  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String genre = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //

                    String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

                    String year =  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));


                    if (genre == null)
                        genre = "";
                    if (year == null)
                        year = "";


                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;

                    Bitmap songImage;

                    byte[] art;
                    try {
                        art = metaRetriver.getEmbeddedPicture();
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 2;
                        songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                    } catch (Exception e) {
                        songImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                    }


                    // Save to audioList
                    download.add(new MusicItem(data, title, album, artist, songImage, duration,genre,track,year)); // , songImage
                    metaRetriver.release();
                } catch (Exception e) {
                    Log.e("Error while loading", "File destroyed:" + data);
                }
            }
        }
        cursor.close();
        Bitmap image = BitmapFactory.decodeResource(getResources(),R.drawable.download_image);
        StorageUtil storageUtil = new StorageUtil(this);
        playLists = storageUtil.loadPlayLists();
        if(playLists == null)
            playLists = new ArrayList<>();
        playLists.add(0,new PlayList("Download", image, download));
        runOnUiThread(() -> {
            Snackbar.make(findViewById(R.id.main_layout), "Done.", Snackbar.LENGTH_SHORT).show();
            homeFragment.init();
            main_pager.setOffscreenPageLimit(4);
        });
    }

    private void loadFolder() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0  AND " + MediaStore.Audio.Media.DATA + " LIKE ?";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, new String[]{"%"+folderPath+"%"}, sortOrder);
        folderMusic = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                try {
                    MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
                    metaRetriver.setDataSource(data);

                    String title =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); //
                    if(title==null||title=="")
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));

                    String album =  metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); //
                    if(album == null||album=="")
                        album  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); //
                    if(artist == null||artist=="")
                        artist  = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));

                    String genre = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE); //

                    String track = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));

                    String year =  cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR));


                    if (genre == null)
                        genre = "";
                    if (year == null)
                        year = "";


                    int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) / 1000;

                    Bitmap songImage;

                    byte[] art;
                    try {
                        art = metaRetriver.getEmbeddedPicture();
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 2;
                        songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                    } catch (Exception e) {
                        songImage = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);
                    }


                    // Save to audioList
                    folderMusic.add(new MusicItem(data, title, album, artist, songImage, duration,genre,track,year)); // , songImage
                    metaRetriver.release();
                } catch (Exception e) {
                    Log.e("Error while loading", "File destroyed:" + data);
                }
            }
        }
        cursor.close();
    }


    private void checkStroragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
            }

        }else {
            Snackbar.make(findViewById(R.id.main_layout), "Wait. Music is loading.", Snackbar.LENGTH_INDEFINITE).show();
            new Thread(() -> {
                loadAudio();
            }).start();

            new Thread(() -> loadFolder()).start();
            initService();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, final int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        new Thread(new Runnable() {
                            public void run() {
                                loadAudio();
                            }
                        }).start();

                        new Thread(() ->
                                loadFolder()).start();
                                initService();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "The storage permission required for the program could see your audio files", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
                }
                break;
            case WRITE_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "The write storage permission required for the program could edit your audio files", Toast.LENGTH_LONG).show();
                    this.finish();
                }
                break;

        }
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
        loadFolder();
    }

    public String getFolderPath() {
        return folderPath;
    }

    public ArrayList<MusicItem> getFolderMusic() {
        return folderMusic;
    }

    public ArrayList<MusicItem> getAllMusic() {
        return allMusic;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            player.onDestroy();
            unbindService(serviceConnection);
            //service is active
            serviceBound = false;
            player.stopSelf();
        }
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        playLists.remove(0);
        storageUtil.storePlayLists(playLists);
        storageUtil.clean();
    }

    @Override
    public void onBackPressed() {

    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection; {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
                player = binder.getService();
                serviceBound = true;
                player.setMusicControlerMethods(new MusicControlerMethods() {
                    @Override
                    public void updadeData(String text) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicControlFragment.update(text);
                            }
                        });

                    }

                    @Override
                    public void updateState(PlaybackStatus pb) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                musicControlFragment.updateState(pb);
                            }
                        });

                    }
                });
               // player.setActivityContext(getApplicationContext());
//                if (isPlaying())
//                    updateControl(PlaybackStatus.PLAYING);
//                else
//                    updateControl(PlaybackStatus.PAUSED);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serviceBound = false;
            }
        };
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    private void initService() {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MusicService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
        }
    }

    @Override
    public void setList(ArrayList<MusicItem> list) {
        player.setList(list);
    }

    @Override
    public void play_next() {
        player.playNext();
    }

    @Override
    public void play_prev() {
        player.playPrev();
    }

    @Override
    public void resume() {
        player.resume();
    }

    @Override
    public void pause() {
        player.pausePlayer();
    }

    @Override
    public void play_new(int index, boolean shuffle) {
        player.playNewSong(index, shuffle);
    }

    @Override
    public void setPlayMode(int i) {
        player.setPlayMode(i);
        musicControlFragment.setPlayMode(i);
    }

    @Override
    public boolean isPlaying() {
        return player.isPng();
    }

    @Override
    public void setList(ArrayList<MusicItem> list, String state, int index) {
        player.setList(list,state, index);
    }

    public String getSongNextData(int i) {
        return player.getSongData(i);
    }

    public ArrayList<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(ArrayList<PlayList> playLists) {
        this.playLists = playLists;
    }

    public void setListName(int myIndex, String name) {
        playLists.get(myIndex).setName(name);
    }

    public void setListImage(int myIndex, Bitmap image) {
        playLists.get(myIndex).setImage(image);
    }

    public void openPage(int i) {
        main_pager.setCurrentItem(i);
        main_pager.invalidate();
    }

    public void updateAdapter() {
        libraryFragment.updateLists();
        homeFragment.updateLists();
    }

    public void addPlayList(String name, Bitmap image, ArrayList<MusicItem> items) {
        playLists.add(new PlayList(name,image,items));
        updateAdapter();
        Snackbar.make(findViewById(R.id.main_layout),"Song list added successfully.",Snackbar.LENGTH_LONG).show();
    }

    public void deletePlayList(int myIndex) {
        StorageUtil storageUtil = new StorageUtil(this);
        if(storageUtil.loadPlayingPlaylist().equals(storageUtil.loadVisiblePlaylist()))
            player.stop();
        playLists.remove(myIndex);
        updateAdapter();
    }

    public void setListItems(int myIndex, ArrayList<MusicItem> o) {
        playLists.get(myIndex).setItems(o);
    }

    public void openPlayList(int index,String type) {
        FragmentManager fm = getSupportFragmentManager();
        PlayListFragment playListFragment = new PlayListFragment(getPlayLists().get(index), index, () -> updateAdapter(), type);
        if(type.equals("home"))
            fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayout,playListFragment).commit();
        else
            fm.beginTransaction().setCustomAnimations(R.anim.slide_right_anim,R.anim.slide_out).replace(R.id.listLayoutAll,playListFragment).commit();

    }

    public MusicItem getCurrentAudio() {
        return  player.getCurrentAudio();
    }

    public int getSeekPos() {
        return player.getPosn();
    }

    public void setAudioPos(int currentTime_) {
        player.seek(currentTime_);
    }

    public ArrayList<MusicItem> getCurrentAudios() {
        return player.getCurrentAudios();
    }

    public int getSongIndex() {
        return player.getIndex();
    }

    public int getPlayingMode() {
        return player.getPlayingMode();
    }

    public void setCurrentPlay(ArrayList<MusicItem> list) {
        player.setList(list);
    }

    public void setCurrentPlay(ArrayList<MusicItem> list, int index) {
        player.setList(list, index);
    }

    public void setNextInList(MusicItem audio, int i) {
        player.setNextInList(audio, i);
    }

    public void setLastInList(MusicItem audio, int audioIndex) {
        player.setLastInList(audio, audioIndex);
    }

    public void addToList(int i, MusicItem audiol) {
        if(playLists.get(i).getItems().contains(audiol))
            Snackbar.make(findViewById(R.id.main_layout),"This song is already added to play list",Snackbar.LENGTH_SHORT).show();
        else {
            playLists.get(i).getItems().add(audiol);
            Snackbar.make(findViewById(R.id.main_layout),"Done!",Snackbar.LENGTH_SHORT).show();
        }
    }

    public void deleteSong(MusicItem audio) {
        for(int i=0;i<allMusic.size();i++) {
            if(allMusic.get(i).getData().equals(audio.getData())) {
                allMusic.remove(i);
                break;
            }
        }
        for(int i=0;i<folderMusic.size();i++) {
            if(folderMusic.get(i).getData().equals(audio.getData())) {
                folderMusic.remove(i);
                break;
            }
        }
        for(int i=0;i<playLists.size();i++){
            ArrayList<MusicItem> playListItems = playLists.get(i).getItems();
            for(int j = 0;j<playListItems.size();j++){
                if(playListItems.get(j).getData().equals(audio.getData())) {
                    playLists.get(i).getItems().remove(j);
                    break;
                }
            }
        }
        for(int i=0;i<authors.size();i++){
            ArrayList<MusicItem> items = authors.get(i).getItems();
            for(int j=0;j<items.size();j++) {
                if (items.get(j).getData().equals(audio.getData())) {
                    authors.get(i).getItems().remove(j);
                    if (authors.get(i).getItems().size() <= 0) {
                        authors.remove(i);
                        updateAdapter();
                        break;
                    }
                }
            }
        }
        for(int i=0;i<albums.size();i++){
            ArrayList<MusicItem> items = albums.get(i).getItems();
            for(int j=0;j<items.size();j++) {
                if (items.get(j).getData().equals(audio.getData())) {
                    albums.get(i).getItems().remove(j);
                    if (albums.get(i).getItems().size() <= 0) {
                        albums.remove(i);
                        updateAdapter();
                        break;
                    }
                }
            }
        }
        player.notifyItemDeleted(audio);
    }

    public void save(MusicItem audio,String title, String author, String album, String genre, String track, String year, Bitmap image) {
        for(int i=0;i<allMusic.size();i++) {
            if(allMusic.get(i).getData().equals(audio.getData())) {
                if(album!=null&&album!=""&&album.matches(".*\\w.*"))
                allMusic.get(i).setAlbum(album);
                allMusic.get(i).setArtist(author);
                allMusic.get(i).setTitle(title);
                if(genre!="")
                allMusic.get(i).setGenre(genre);
                if(track!=null&&track!="")
                allMusic.get(i).setTrack(track);
                if(year!=null&&year!=""&&year.matches(".*\\w.*"))
                allMusic.get(i).setYear(year);
                if(image!=null)
                allMusic.get(i).setImage(image);
                break;
            }
        }
        for(int i=0;i<folderMusic.size();i++) {
            if(folderMusic.get(i).getData().equals(audio.getData())) {
                if(album!=null&&album!=""&&album.matches(".*\\w.*"))
                folderMusic.get(i).setAlbum(album);
                folderMusic.get(i).setArtist(author);
                folderMusic.get(i).setTitle(title);
                if(genre!="")
                folderMusic.get(i).setGenre(genre);
                if(track!=null&&track!="")
                folderMusic.get(i).setTrack(track);
                if(year!=null&&year!=""&&year.matches(".*\\w.*"))
                folderMusic.get(i).setYear(year);
                if(image!=null)
                folderMusic.get(i).setImage(image);
                break;
            }
        }
        for(int i=0;i<playLists.size();i++){
            ArrayList<MusicItem> playListItems = playLists.get(i).getItems();
            for(int j = 0;j<playListItems.size();j++){
                if(playListItems.get(j).getData().equals(audio.getData())) {
                    if(album!=null&&album!=""&&album.matches(".*\\w.*"))
                        playLists.get(i).getItems().get(j).setAlbum(album);
                    playLists.get(i).getItems().get(j).setArtist(author);
                    playLists.get(i).getItems().get(j).setTitle(title);
                    if(genre!="")
                        playLists.get(i).getItems().get(j).setGenre(genre);
                    if(track!=null&&track!="")
                        playLists.get(i).getItems().get(j).setTrack(track);
                    if(year!=null&&year!=""&&year.matches(".*\\w.*"))
                        playLists.get(i).getItems().get(j).setYear(year);
                    if(image!=null)
                        playLists.get(i).getItems().get(j).setImage(image);
                    break;
                }
            }
        }

        for(int i=0;i<authors.size();i++){
            ArrayList<MusicItem> items = authors.get(i).getItems();
            for(int j=0;j<items.size();j++) {
                if (items.get(j).getData().equals(audio.getData())) {
                    if(album!=null&&album!=""&&album.matches(".*\\w.*"))
                        authors.get(i).getItems().get(j).setAlbum(album);
                    authors.get(i).getItems().get(j).setArtist(author);
                    authors.get(i).getItems().get(j).setTitle(title);
                    if(genre!="")
                        authors.get(i).getItems().get(j).setGenre(genre);
                    if(track!=null&&track!="")
                        authors.get(i).getItems().get(j).setTrack(track);
                    if(year!=null&&year!=""&&year.matches(".*\\w.*"))
                        authors.get(i).getItems().get(j).setYear(year);
                    if(image!=null)
                        authors.get(i).getItems().get(j).setImage(image);
                   break;
                }
            }
        }
        for(int i=0;i<albums.size();i++){
            ArrayList<MusicItem> items = albums.get(i).getItems();
            for(int j=0;j<items.size();j++) {
                if (items.get(j).getData().equals(audio.getData())) {
                    if(album!=null&&album!=""&&album.matches(".*\\w.*"))
                        albums.get(i).getItems().get(j).setAlbum(album);
                    albums.get(i).getItems().get(j).setArtist(author);
                    albums.get(i).getItems().get(j).setTitle(title);
                    if(genre!="")
                        albums.get(i).getItems().get(j).setGenre(genre);
                    if(track!=null&&track!="")
                        albums.get(i).getItems().get(j).setTrack(track);
                    if(year!=null&&year!=""&&year.matches(".*\\w.*"))
                        albums.get(i).getItems().get(j).setYear(year);
                    if(image!=null)
                        albums.get(i).getItems().get(j).setImage(image);
                    break;
                }
            }
        }
        if(album!=null&&album!=""&&album.matches(".*\\w.*"))
            audio.setAlbum(album);
        audio.setArtist(author);
        audio.setTitle(title);
        if(genre!="")
            audio.setGenre(genre);
        if(track!=null&&track!="")
            audio.setTrack(track);
        if(year!=null&&year!=""&&year.matches(".*\\w.*"))
            audio.setYear(year);
        if(image!=null)
            audio.setImage(image);
        //update current play
        player.updateOnEdit(audio);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return homeFragment;
                case 1: return new SearchFragment();
                case 2: return libraryFragment;
                default: return new HomeFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

    }

    public void openCurrentPlay(){
        closePlaylists();
        FragmentManager fm = getSupportFragmentManager();
        NowPlayingFragment nowPlayingFragment = new NowPlayingFragment();
        fm.beginTransaction().setCustomAnimations(R.anim.slide_up,R.anim.slide_down).replace(R.id.current_play_layout,nowPlayingFragment).commit();
    }

    public void closePlaylists(){
            homeFragment.closePl();
            libraryFragment.closePl();
    }

    public void setMusicListMethods(MusicListMethods musicListMethods) {
        this.musicListMethods = musicListMethods;
        player.setMusicListMethods( this.musicListMethods);
    }
}
