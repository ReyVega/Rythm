package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class SongView extends AppCompatActivity {
    private TextView    tvSongPlaylist,
                        tvSongName,
                        tvSongAuthor,
                        tvSongElapsedTime,
                        tvSongRemainingTime;
    private ImageButton ibSongReturn,
                        ibSongPrevious,
                        ibSongPlayPause,
                        ibSongNext;
    private SeekBar sbSongSlider;
    private ImageView ivSongPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_view);

        tvSongPlaylist = findViewById(R.id.tvSongPlaylist);
        tvSongName = findViewById(R.id.tvSongName);
        tvSongAuthor = findViewById(R.id.tvSongAuthor);
        tvSongElapsedTime = findViewById(R.id.tvSongElapsedTime);
        tvSongRemainingTime = findViewById(R.id.tvSongRemainingTime);

        ibSongReturn = findViewById(R.id.ibSongReturn);
        ibSongPrevious = findViewById(R.id.ibSongPrevious);
        ibSongPlayPause = findViewById(R.id.ibSongPlayPause);
        ibSongNext = findViewById(R.id.ibSongNext);

        sbSongSlider = findViewById(R.id.sbSongSlider);

        ivSongPhoto = findViewById(R.id.ivSongPhoto);

        ibSongReturn.setOnClickListener(v -> {

        });

        ibSongPrevious.setOnClickListener(v -> {

        });

        ibSongPlayPause.setOnClickListener(v -> {

        });

        ibSongNext.setOnClickListener(v -> {

        });

    }
}