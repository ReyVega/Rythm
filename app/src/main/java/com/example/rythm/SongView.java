package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


public class SongView extends AppCompatActivity {
    private PlayerView playerView;
    private TextView    tvSongPlaylist,
                        tvSongName,
                        tvSongAuthor;
    private ImageButton ibSongReturn;
    private NetworkImageView nivSongPhoto;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;
    public static int intentos = 15;



    private RequestQueue queue;

    //Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        Log.d("YTex", "playYouTubeSong: ");
        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                Log.d("YTex", "ytFiles: " + String.valueOf(ytFiles));
                Log.d("YTex", "videoMeta" +  String.valueOf(videoMeta));
                if (intentos <= 0) return;

                if (ytFiles != null) {
                    intentos = 0;
                    int audioTag = 140; //audio tag for m4a, audioBitrate: 128

                    MediaSource audioSource = new ProgressiveMediaSource
                            .Factory(new DefaultHttpDataSourceFactory())
                            .createMediaSource(MediaItem.fromUri(ytFiles.get(audioTag).getUrl()));

                    player.setMediaSource(audioSource);
                    player.prepare();
                    player.setPlayWhenReady(playWhenReady);
                    player.seekTo(currentWindow, playbackPosition);

                }
                else {
                    intentos--;
                    playYouTubeSong(youtubeUrl, player, context, playWhenReady, currentWindow, playbackPosition);
                }
            }
        }.extract(youtubeUrl, true, true);
    }

    private void loadCover(String coverUrl){
        this.nivSongPhoto.setDefaultImageResId(R.drawable.exo_ic_default_album_image);
        this.nivSongPhoto.setErrorImageResId(R.drawable.exo_ic_default_album_image);
        ImageLoader imageLoader = RequestController.getInstance(getBaseContext()).getImageLoader();

        imageLoader.get(coverUrl, ImageLoader.getImageListener(nivSongPhoto,
                R.drawable.exo_ic_default_album_image, android.R.drawable
                        .ic_dialog_alert));
        nivSongPhoto.setImageUrl(coverUrl, imageLoader);
    }

    private void fetchSongMetadata() {
        Intent intent = getIntent();
        String  deezerTrackId = intent.getStringExtra("deezerTrackId"),
                playlistName = intent.getStringExtra("playlistName");

        if (deezerTrackId == null || playlistName == null) return;

        this.queue = RequestController.getInstance(this.getBaseContext()).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getString(R.string.track_endpoint_deezer_api) + deezerTrackId, null,
                track -> {
                    try {
                        String songName = track.getString("title");
                        JSONObject artist = track.getJSONObject("artist");
                        String artistName = artist.getString("name");
                        JSONObject album = track.getJSONObject("album");
                        String coverUrl = album.getString("cover");

                        tvSongPlaylist.setText(playlistName);
                        tvSongName.setText(songName);
                        tvSongAuthor.setText(artistName);

                        loadCover(coverUrl);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
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

        nivSongPhoto = findViewById(R.id.nivSongPhoto);

        ibSongReturn.setOnClickListener(v -> {
            finish();
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        intentos = 15;
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
        fetchSongMetadata();
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