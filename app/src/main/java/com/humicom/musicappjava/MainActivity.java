package com.humicom.musicappjava;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MusicForegroundService.MusicServiceBinder binder;
    private boolean bound = false;

    private TextView statusText;
    private Button playPauseBtn;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof MusicForegroundService.MusicServiceBinder) {
                binder = (MusicForegroundService.MusicServiceBinder) service;
                bound = true;
                updateUi();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            binder = null;
            updateUi();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.statusText);
        playPauseBtn = findViewById(R.id.playPauseBtn);
        playPauseBtn.setOnClickListener(v -> {
            if (binder != null) {
                if (binder.isPlaying()) binder.pause(); else binder.play();
                updateUi();
            }
        });
        startForegroundService(new Intent(this, MusicForegroundService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, MusicForegroundService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }

    private void updateUi() {
        boolean playing = binder != null && binder.isPlaying();
        if (statusText != null)
            statusText.setText(playing ? "Playing" : "Paused");
        if (playPauseBtn != null)
            playPauseBtn.setText(playing ? "Pause" : "Play");
    }
}
