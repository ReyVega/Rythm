package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


public class SongView extends AppCompatActivity {
    private PlayerView playerView;
    private TextView    tvSongPlaylist,
                        tvSongName,
                        tvSongAuthor;
    private ImageButton ibSongReturn;
    private ImageView ivSongPhoto;

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

        String youtubeUrl = "https://www.youtube.com/watch?v=7-x3uD5z1bQ";

        Log.d("YTextractor", "hola");

        playYouTubeSong(youtubeUrl, player, this, playWhenReady, currentWindow, playbackPosition);

//        MediaItem mediaItem = MediaItem.fromUri("https://www.youtube.com/watch?v=P3cffdsEXXw");
//        player.setMediaItem(mediaItem);

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

    private static void playYouTubeSong(String youtubeUrl, SimpleExoPlayer player, Context context, boolean playWhenReady, int currentWindow, long playbackPosition) {
        Log.d("YTextractor", "hola 2");

        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                Log.d("YTextractor", String.valueOf(ytFiles));
                Log.d("YTextractor", String.valueOf(videoMeta));

                if (ytFiles != null) {
                    int audioTag = 140; //audio tag for m4a, audioBitrate: 128

                    Log.d("YTextractor", "onExtractionComplete: siuuuuuuuuuuuuuuuuuuuuuuu");

                    MediaSource audioSource = new ProgressiveMediaSource
                            .Factory(new DefaultHttpDataSourceFactory())
                            .createMediaSource(MediaItem.fromUri(ytFiles.get(140).getUrl()));

                    player.setMediaSource(audioSource);
                    player.prepare();
                    player.setPlayWhenReady(playWhenReady);
                    player.seekTo(currentWindow, playbackPosition);

                }
            }
        }.extract(youtubeUrl, true, true);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_view);

        playerView = findViewById(R.id.video_view);

        tvSongPlaylist = findViewById(R.id.tvSongPlaylist);
        tvSongName = findViewById(R.id.tvSongName);
        tvSongAuthor = findViewById(R.id.tvSongAuthor);

        ibSongReturn = findViewById(R.id.ibSongReturn);

        ivSongPhoto = findViewById(R.id.ivSongPhoto);

        ibSongReturn.setOnClickListener(v -> {
            finish();
        });


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