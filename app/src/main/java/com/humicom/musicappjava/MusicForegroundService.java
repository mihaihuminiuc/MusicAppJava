package com.humicom.musicappjava;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import android.support.v4.media.session.MediaSessionCompat;

public class MusicForegroundService extends Service {
    public static final String CHANNEL_ID = "music_playback";
    public static final String ACTION_TOGGLE = "com.humicom.musicappjava.action.TOGGLE";
    public static final String ACTION_PLAY = "com.humicom.musicappjava.action.PLAY";
    public static final String ACTION_STOP = "com.humicom.musicappjava.action.STOP";
    private ExoPlayer player;
    private MediaSessionCompat mediaSessionCompat;
    private final MusicServiceBinder binder = new MusicServiceBinder();

    public class MusicServiceBinder extends Binder {
        public boolean isPlaying() { return player != null && player.isPlaying(); }
        public void play() { ensurePlayer(); if (!player.isPlaying()) player.play(); }
        public void pause() { if (player != null && player.isPlaying()) player.pause(); }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        ensurePlayer();
        startForeground(1, buildNotification());
    }

    private void ensurePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            // Example free test stream (Icecast):
            MediaItem item = MediaItem.fromUri("https://ice1.somafm.com/groovesalad-128-mp3");
            player.setMediaItem(item);
            player.prepare();
        }
        if (mediaSessionCompat == null) {
            mediaSessionCompat = new MediaSessionCompat(this, "music_session");
            mediaSessionCompat.setActive(true);
        }
    }

    private Notification buildNotification() {
    ensurePlayer();
    Intent openIntent = new Intent(this, MainActivity.class);
    PendingIntent contentPi = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);

    // Action intents
    PendingIntent playPi = PendingIntent.getService(this, 10,
        new Intent(this, MusicForegroundService.class).setAction(ACTION_PLAY), PendingIntent.FLAG_IMMUTABLE);
    PendingIntent stopPi = PendingIntent.getService(this, 11,
        new Intent(this, MusicForegroundService.class).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE);

    boolean isPlaying = player != null && player.isPlaying();
    String status = (player == null) ? "Stopped" : (isPlaying ? "Playing" : "Paused");

    NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Streaming Music")
        .setContentText(status)
        .setContentIntent(contentPi)
        .setOnlyAlertOnce(true)
        .setOngoing(isPlaying) // only ongoing while actively playing
        // Always show both buttons to satisfy 'stop and start button'
        .addAction(R.mipmap.button_start, "Play", playPi)
        .addAction(R.mipmap.button_stop, "Stop", stopPi)
        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(mediaSessionCompat.getSessionToken())
            .setShowActionsInCompactView(0,1)
        );
    return b.build();
    }

    private void updateNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.notify(1, buildNotification());
    }

    private void createChannel() {
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW);
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(ch);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ensurePlayer();
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_TOGGLE.equals(action)) {
                if (player.isPlaying()) player.pause(); else player.play();
                updateNotification();
            } else if (ACTION_PLAY.equals(action)) {
                if (player == null) ensurePlayer();
                if (!player.isPlaying()) player.play();
                // If service not in foreground (after stop) re-promote it
                startForeground(1, buildNotification());
            } else if (ACTION_STOP.equals(action)) {
                if (player != null) {
                    player.pause();
                    player.release();
                    player = null; // mark stopped
                }
                // Keep session for now (optional to release). Update notification not ongoing.
                stopForeground(false); // leave notification for restart
                updateNotification();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mediaSessionCompat != null)
            mediaSessionCompat.release();
        if (player != null)
            player.release();
        super.onDestroy();
    }
}
