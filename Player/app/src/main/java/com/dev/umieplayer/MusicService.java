package com.dev.umieplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import com.dev.umieplayer.enums.PlaybackStatus;
import com.dev.umieplayer.interfaces.MusicControlerMethods;
import com.dev.umieplayer.interfaces.MusicListMethods;
import com.dev.umieplayer.objects.MusicItem;
import com.dev.umieplayer.utils.StorageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,   AudioManager.OnAudioFocusChangeListener  {

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;
    public static final String ACTION_PLAY = "com.eukalyptus.eukalyptusplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.eukalyptus.eukalyptusplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.eukalyptus.eukalyptusplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.eukalyptus.eukalyptusplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.eukalyptus.eukalyptusplayer.ACTION_STOP";

    private ArrayList<MusicItem> allMusic;
    private ArrayList<MusicItem> filteredMusic;
    private MusicItem activeAudio = null;

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.storePlayMode(playMode);
        if(playMode == 2){
            if(filteredMusic!=null) {
                Collections.shuffle(filteredMusic);
                //fixIndex
                fixIndextoShuffle();
            }
        }else if(playMode == 0){
            if(allMusic!=null) {
                filteredMusic = (ArrayList<MusicItem>) allMusic.clone();
                fixIndextoNormal();
            }
        }
    }

    private int playMode; //0-all 1-list once 2-shuffle
    private int audioIndex = 0;
    private MediaPlayer mediaPlayer;
    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private AudioManager audioManager;

    // Binder given to clients
    private final IBinder iBinder = new LocalBinder();
    private MusicListMethods musicListMethods;
    private MusicControlerMethods musicControlerMethods;

    @Override
    public void onCreate() {
        super.onCreate();
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        playMode = storageUtil.getPlayMode();
        initMediaPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public void setMusicListMethods(MusicListMethods musicListMethods) {
        this.musicListMethods = musicListMethods;
    }

    public void setMusicControlerMethods(MusicControlerMethods musicControlerMethods) {
        this.musicControlerMethods = musicControlerMethods;
    }


    public String getSongData(int i) {
        if(audioIndex+i<0){
            String author = filteredMusic.get(filteredMusic.size()-1).getArtist();
            String name = filteredMusic.get(filteredMusic.size()-1).getTitle();
            return name+"\u16EB"+author;
        }else if(audioIndex+i>=filteredMusic.size()){
            String author = filteredMusic.get(0).getArtist();
            String name = filteredMusic.get(0).getTitle();
            return name+"\u16EB"+author;
        } else if(filteredMusic!=null){
            String author = filteredMusic.get(audioIndex+i).getArtist();
            String name = filteredMusic.get(audioIndex+i).getTitle();
            return name+"\u16EB"+author;
        }
        return "";
    }

    public void stop() {
        pausePlayer();
        activeAudio = null;
        removeNotification();
        musicControlerMethods.updadeData(null);
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.storePlayingPlaylist("");
    }

    public MusicItem getCurrentAudio() {
        return activeAudio;
    }

    public ArrayList<MusicItem> getCurrentAudios() {
        return filteredMusic;
    }

    public int getIndex() {
        return audioIndex;
    }

    public void setIndex(int i) {
        audioIndex = i;
    }

    public int getPlayingMode() {
        return playMode;
    }

    public void setList(ArrayList<MusicItem> list, int index) {
        setIndex(index);
     //   this.audioIndex = index;
        allMusic = list;
        filteredMusic = list;
        if(playMode == 2){
            Collections.shuffle(filteredMusic, new Random());
              fixIndextoShuffle();
        }
    }

    public void setLastInList(MusicItem audio, int index){
        if(filteredMusic==null||filteredMusic.size()<=0) {
            Toast.makeText(getApplicationContext(), "There is no playlist playing", Toast.LENGTH_SHORT).show();
            return;
        }
        if(audio.getData().equals(filteredMusic.get(filteredMusic.size()-1).getData())) {
            Toast.makeText(getApplicationContext(), "This song is already last in list", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.storePlayingPlaylist("???SPeCiAl@@@play****");
        musicListMethods.update(PlaybackStatus.PLAYING);
        changeAudioPosToLast(audio, index);
    }

    private void changeAudioPosToLast(MusicItem audio, int i) {
        if (i < audioIndex)
            audioIndex -= 1;
        filteredMusic.remove(i);
        filteredMusic.add(audio);
        if (playMode != 2) {
            allMusic.remove(i);
            allMusic.add(audio);
        }
    }

    public void setNextInList(MusicItem audio,int index) {
        if(filteredMusic==null||filteredMusic.size()<=0) {
            Toast.makeText(getApplicationContext(), "There is no playlist playing", Toast.LENGTH_SHORT).show();
            return;
        }
        if(audio.getData().equals(activeAudio.getData())) {
            Toast.makeText(getApplicationContext(), "This song is already playing", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
        storageUtil.storePlayingPlaylist("???SPeCiAl@@@play****");
        musicListMethods.update(PlaybackStatus.PLAYING);
        checkAudioPosToNext(audio, index);
    }

    private void checkAudioPosToNext(MusicItem audio, int i) {
        if (i < audioIndex)
            audioIndex -= 1;
        filteredMusic.remove(i);
        filteredMusic.add(audioIndex + 1, audio);
        if (playMode != 2) {
            allMusic.remove(i);
            allMusic.add(audioIndex + 1, audio);
        }
    }

    public void notifyItemDeleted(MusicItem audio) {
        if(filteredMusic!=null&&filteredMusic.size()>0){
            for(int i=0;i<filteredMusic.size();i++){
                if(filteredMusic.get(i).getData().equals(audio.getData())){
                    filteredMusic.remove(i);
                    allMusic.remove(audio);
                    if(activeAudio.getData().equals(audio.getData())){
                        StorageUtil storageUtil = new StorageUtil(getApplicationContext());
                        storageUtil.storePlayingPlaylist("");
                        stop();
                    }
                    return;
                }
            }
        }
    }

    public void updateOnEdit(MusicItem audio) {
        if(activeAudio!=null&&activeAudio.getData().equals(audio.getData())){
            activeAudio = audio;
            PlaybackStatus pb;
            if(isPng())
                pb = PlaybackStatus.PLAYING;
            else
                pb = PlaybackStatus.PAUSED;
            buildNotification(pb);
            musicControlerMethods.updadeData(audio.getTitle()+"\u16EB"+audio.getArtist());
        }
        if(allMusic!=null&&allMusic.size()>0&&audioIndex>=0){
            filteredMusic.set(audioIndex,audio);
            if(playMode!=2)
                allMusic.set(audioIndex,audio);
            else{
                for(int i =0;i<allMusic.size();i++){
                    if(allMusic.get(i).getData().equals(audio.getData()))
                        allMusic.set(i,audio);
                    return;
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
    }

    //pass song list
    public void setList(ArrayList<MusicItem> theSongs){
        allMusic= (ArrayList<MusicItem>) theSongs.clone();
        filteredMusic= (ArrayList<MusicItem>) theSongs.clone();
        if(playMode == 2){
            Collections.shuffle(filteredMusic, new Random());
          //  fixIndextoShuffle();
        }
    }

    public void setList(ArrayList<MusicItem> theSongs, String state, int index){
        if(state.equals("deleted")) {
            if (activeAudio==null||activeAudio.getData().equals(allMusic.get(index).getData())) {
              stop();
                return;
            }
            allMusic = (ArrayList<MusicItem>) theSongs.clone();
            filteredMusic = (ArrayList<MusicItem>) theSongs.clone();
            if (playMode == 2) {
                Collections.shuffle(filteredMusic, new Random());
                if (audioIndex >= 0)
                    fixIndextoShuffle();
            } else
                fixDeleted();
        }else{
            allMusic = (ArrayList<MusicItem>) theSongs.clone();
            filteredMusic = (ArrayList<MusicItem>) theSongs.clone();
            if (playMode == 2) {
                Collections.shuffle(filteredMusic, new Random());
                if (audioIndex >= 0)
                    fixIndextoShuffle();
            } else
                fixDeleted();
        }
    }

    private void fixDeleted() {
        for(int i=0;i<filteredMusic.size();i++) {
            if (activeAudio.getData().equals(filteredMusic.get(i).getData())) {
                audioIndex = i;
                return;
            }
        }
    }

    //play a song
    public void playSong() {
        //play
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set the data source
        try {
            mediaPlayer.setDataSource(activeAudio.getData());
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mediaPlayer.prepareAsync();
        buildNotification(PlaybackStatus.PLAYING);
        musicControlerMethods.updateState(PlaybackStatus.PLAYING);
    }

    //set the song
    public void setSong(int songIndex){
        audioIndex=songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
            switch (playMode){
                case  1:playSong();
                    break;
                default: playNext();
                    break;
            }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playing
        mp.start();
        //build notification
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    //playback methods
    public int getPosn(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDur(){
        return mediaPlayer.getDuration();
    }

    public boolean isPng(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
        buildNotification(PlaybackStatus.PAUSED);
        musicControlerMethods.updateState(PlaybackStatus.PAUSED);
    }

    public void playNewSong(int index, boolean shuffle){
        setSong(index);
        if(playMode == 2&&shuffle)
            fixIndextoShuffle();
        activeAudio = filteredMusic.get(this.audioIndex);
        playSong();
        musicControlerMethods.updadeData(activeAudio.getTitle()+"\u16EB"+activeAudio.getArtist());
        StorageUtil st = new StorageUtil(getApplicationContext());
        st.storeCurrentData(activeAudio.getData());
        st.storePreviousData(activeAudio.getData());
    }

    public void seek(int posn){
        mediaPlayer.seekTo(posn);
    }

    public void resume(){
        mediaPlayer.start();
        buildNotification(PlaybackStatus.PLAYING);
        musicControlerMethods.updateState(PlaybackStatus.PLAYING);
    }

    //skip to previous track
    public void playPrev(){
        audioIndex--;
        if(audioIndex<0) audioIndex=filteredMusic.size()-1;
        activeAudio = filteredMusic.get(audioIndex);
        playSong();
    }

    //skip to next
    public void playNext() {
        audioIndex++;
        if (audioIndex >= filteredMusic.size()) audioIndex = 0;
        activeAudio = filteredMusic.get(audioIndex);
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
     //   new StorageUtil(getApplicationContext()).clean();
        removeNotification();
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = R.drawable.ic_play;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = R.drawable.ic_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = R.drawable.ic_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = activeAudio.getImage();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // notificationBuilder.setChannelId("PLAYER_CHANNEL");
            notificationBuilder = new NotificationCompat.Builder(this, "PLAYER_CHANNEL");
        }else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }

        // Create a new Notification
        notificationBuilder
                .setContentIntent(pendInt)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(Color.WHITE)
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_play)
                // Set Notification content information
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                .setOngoing(true)
                // Add playback actions
                .addAction(R.drawable.ic_prev, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_next, "next", playbackAction(2));

        NotificationManager notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("PLAYER_CHANNEL", "com.eukalyptus.eukalyptusplayer", NotificationManager.IMPORTANCE_LOW);
            mChannel.setSound(null,null);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        if(playbackStatus == PlaybackStatus.PLAYING) {
            StorageUtil st = new StorageUtil(getApplicationContext());
            st.storeCurrentData(activeAudio.getData());
            st.storePreviousData(activeAudio.getData());
        }else{
            StorageUtil st = new StorageUtil(getApplicationContext());
            st.storeCurrentData("");
        }

        try {
        //    updateMusicControl(playbackStatus);
        }catch (Exception e){

        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        musicListMethods.update(playbackStatus);
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        StorageUtil st = new StorageUtil(getApplicationContext());
        st.storeCurrentData("");
        try {
            musicControlerMethods.updateState(PlaybackStatus.PAUSED);
         //   musicControlMethods.update(activeAudio,PlaybackStatus.PAUSED,shuffle,repeatMode);
        //    listControler.changePlayPause();
        }catch (Exception e){}
    }

    @Nullable
    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MusicService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                //initMediaPlayer();
            } catch (Exception e) {
                e.printStackTrace();
                stopSelf();
            }
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
            musicControlerMethods.updateState(PlaybackStatus.PLAYING);
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
            musicControlerMethods.updateState(PlaybackStatus.PAUSED);
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
            if(audioIndex+1>=filteredMusic.size()){
                String author = filteredMusic.get(0).getArtist();
                String name = filteredMusic.get(0).getTitle();
                musicControlerMethods.updadeData(name+"\u16EB"+author);
            } else if(filteredMusic!=null){
                String author = filteredMusic.get(audioIndex+1).getArtist();
                String name = filteredMusic.get(audioIndex+1).getTitle();
                musicControlerMethods.updadeData(name+"\u16EB"+author);
            }else
                musicControlerMethods.updadeData("ERROR"+"\u16EB"+"ERROR");
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
            if(audioIndex-1<0){
                String author = filteredMusic.get(filteredMusic.size()-1).getArtist();
                String name = filteredMusic.get(filteredMusic.size()-1).getTitle();
                musicControlerMethods.updadeData(name+"\u16EB"+author);
            } else if(filteredMusic!=null){
                String author = filteredMusic.get(audioIndex-1).getArtist();
                String name = filteredMusic.get(audioIndex-1).getTitle();
                musicControlerMethods.updadeData(name+"\u16EB"+author);
            }else
                musicControlerMethods.updadeData("ERROR"+"\u16EB"+"ERROR");
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    private void updateMetaData() {
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.default_music_image);

        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, image) // activeAudio.getImage()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        //updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resume();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pausePlayer();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrev();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }
    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
//        switch (focusState) {
//            case AudioManager.AUDIOFOCUS_GAIN:
//                // resume playback
//                if (mediaPlayer == null) initMediaPlayer();
//                else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
//                mediaPlayer.setVolume(1.0f, 1.0f);
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS:
//                // Lost focus for an unbounded amount of time: stop playback and release media player
//                if(mediaPlayer!=null) {
//                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
//                }
//                // mediaPlayer.release();
//                //mediaPlayer = null;
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
//                // Lost focus for a short time, but we have to stop
//                // playback. We don't release the media player because playback
//                // is likely to resume
//                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
//                break;
//            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
//                // Lost focus for a short time, but it's ok to keep playing
//                // at an attenuated level
//                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
//                break;
//        }
    }

    public void fixIndextoShuffle(){
        for(int i=0;i<filteredMusic.size();i++) {
            if (allMusic.get(audioIndex).getData().equals(filteredMusic.get(i).getData())) {
                audioIndex = i;
                return;
            }
        }
    }

    public void fixIndextoNormal(){
        for(int i=0;i<filteredMusic.size();i++) {
            if (allMusic.get(i).getData().equals(activeAudio.getData())) {
                audioIndex = i;
                return;
            }
        }
    }

}
