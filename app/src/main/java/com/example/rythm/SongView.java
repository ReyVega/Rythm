package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;

public class SongView extends AppCompatActivity {
    private PlayerView playerView;
//    private TextView    tvSongPlaylist,
//                        tvSongName,
//                        tvSongAuthor,
//                        tvSongElapsedTime,
//                        tvSongRemainingTime;
//    private ImageButton ibSongReturn,
//                        ibSongPrevious,
//                        ibSongPlayPause,
//                        ibSongNext;
//    private SeekBar sbSongSlider;
//    private ImageView ivSongPhoto;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private SimpleExoPlayer player;


    private void initializePlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
        }
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri("https://firebasestorage.googleapis.com/v0/b/rythm-51501.appspot.com/o/Watermelon%20Sugar.mp3?alt=media&token=c37df0e6-e18a-4ae1-bcad-b4de551211d4");
        player.setMediaItem(mediaItem);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        player.prepare();
    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_view);

        playerView = findViewById(R.id.video_view);


//        tvSongPlaylist = findViewById(R.id.tvSongPlaylist);
//        tvSongName = findViewById(R.id.tvSongName);
//        tvSongAuthor = findViewById(R.id.tvSongAuthor);
//        tvSongElapsedTime = findViewById(R.id.tvSongElapsedTime);
//        tvSongRemainingTime = findViewById(R.id.tvSongRemainingTime);
//
//        ibSongReturn = findViewById(R.id.ibSongReturn);
//        ibSongPrevious = findViewById(R.id.ibSongPrevious);
//        ibSongPlayPause = findViewById(R.id.ibSongPlayPause);
//        ibSongNext = findViewById(R.id.ibSongNext);
//
//        sbSongSlider = findViewById(R.id.sbSongSlider);
//
//        ivSongPhoto = findViewById(R.id.ivSongPhoto);
//
//        ibSongReturn.setOnClickListener(v -> {
//
//        });
//
//        ibSongPrevious.setOnClickListener(v -> {
//
//        });
//
//        ibSongPlayPause.setOnClickListener(v -> {
//
//        });
//
//        ibSongNext.setOnClickListener(v -> {
//
//        });

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer();
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }
}