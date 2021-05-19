package com.example.rythm;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


public class SongView extends AppCompatActivity implements EventListener {
    private static final String TAG = "aiuda";
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

    private List<Song> songs;

    private String playlistId, playlistName, selectedDeezerTrackId;



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
        player.addListener(this);


        String youtubeUrl = "https://www.youtube.com/watch?v=7-x3uD5z1bQ";


        Log.d("YTextractor", "hola");
        //fetchYouTubeSong(youtubeUrl, player, this, playWhenReady, currentWindow, playbackPosition);

        player.prepare();
        player.setPlayWhenReady(playWhenReady);

        player.seekTo(currentWindow, 0);

//        MediaItem mediaItem = MediaItem.fromUri("https://www.youtube.com/watch?v=P3cffdsEXXw");
//        player.setMediaItem(mediaItem);

    }

//    private void fetchYouTubeSongs() {
//        if (this.songs == null) return;
//        Log.d("aiuda", "fetchYouTubeSongs: " + this.songs.toString());
//
//
//        for (Song song : this.songs) {
//            String youtubeURL = "https://www.youtube.com/watch?v=7-x3uD5z1bQ"; // get from youtube api
//            fetchYouTubeSong(youtubeURL, player, this);
//        }
//
//        player.seekTo(currentWindow, playbackPosition);
//    }

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            player.release();
            player = null;
        }
    }

    private void getSongsFromFirebase() {
        //Log.d(TAG, "getSongsFromFirebase: " + "playlist id: " + playlistId);
        if (this.playlistId.isEmpty()) return;
        Query songsQuery = db.collection("Songs").whereEqualTo("playlistId", playlistId).orderBy("deezerTrackId");;
        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
            int pos = 0;
           if (documentSnapshots == null) return;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    String deezerTrackId = String.valueOf(doc.getDocument().get("deezerTrackId"));
                    Log.d("TOMATE", "getSongsFromFirebase: track id" + deezerTrackId);
                    // buscar en el ht
                    fetchSongMetadata(deezerTrackId, pos++);
                }
            }
        });
    }

    private void fetchSongMetadata(String deezerTrackId, int pos) {
        // solo va a ser usada cuando no este registrada la cancion en el hash table
        this.queue = RequestController.getInstance(this).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                getString(R.string.track_endpoint_deezer_api) + deezerTrackId, null,
                track -> {
                    try {
                        String songName = track.getString("title");
                        JSONObject artist = track.getJSONObject("artist");
                        String artistName = artist.getString("name");
                        int duration = track.getInt("duration");
                        JSONObject album = track.getJSONObject("album");
                        String coverUrl = album.getString("cover");
                        //Log.d(TAG, "fetchSongMetadata: " + songName + " " + artistName + " " + duration);

                        Song newSong = new Song(songName, artistName, duration, coverUrl, deezerTrackId);
                        //this.songs.add(newSong);

                        String youtubeURL = "https://www.youtube.com/watch?v=7-x3uD5z1bQ"; // get from youtube api
                        intentos = 15;
                        //Log.d(TAG, "fetchSongMetadata: " + songName + ", " + intentos);
                        fetchYouTubeSong(youtubeURL, deezerTrackId, selectedDeezerTrackId, pos,  player, this, playbackPosition, currentWindow,  15);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d("JSON", "onErrorResponse: " + error.getMessage()));

        queue.add(jsonObjectRequest);
    }

    private static void fetchYouTubeSong(String youtubeURL, String deezerTrackId, String selectedDeezerTrackId, int pos, SimpleExoPlayer player, Context context, long playbackPosition, int currentWindow, int attempts) {
        Log.d("YTex", "playYouTubeSong: ");
        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                //Log.d("aiuda", "ytFiles: " + String.valueOf(ytFiles));
               // Log.d("aiuda", "videoMeta" +  String.valueOf(videoMeta));
                if (attempts <= 0) {
                    Log.d(TAG, "onExtractionComplete: chetossssss");
                    return;
                }

                if (ytFiles != null) {
                    int audioTag = 140; //audio tag for m4a, audioBitrate: 128

                    /*MediaSource audioSource = new ProgressiveMediaSource
                            .Factory(new DefaultHttpDataSourceFactory())
                            .createMediaSource(MediaItem.fromUri(ytFiles.get(audioTag).getUrl()));*/

                    MediaItem mediaItem = new MediaItem.Builder()
                            .setUri(ytFiles.get(audioTag).getUrl())
                            .setMediaId(deezerTrackId)
                            .build();

                    player.addMediaItem(mediaItem);

                    if (deezerTrackId.equals(selectedDeezerTrackId)) {
                       // Log.d(TAG, "onExtractionComplete: POS:::" + pos);
                       // Log.d(TAG, "onExtractionComplete: en sogn view: " + deezerTrackId);
//                        player.seekTo(currentWindow, pos);
                    }
                    //Log.d(TAG, "onExtractionComplete: agregados:" + player.getMediaItemCount());


                }
                else {
                    intentos--;
                    fetchYouTubeSong(youtubeURL, deezerTrackId, selectedDeezerTrackId, pos, player, context, playbackPosition, currentWindow, attempts-1);
                }
            }
        }.extract(youtubeURL, true, true);
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

    private void updateSongMetadata(String deezerTrackId) {

        if (deezerTrackId == null || this.playlistName == null) return;
        
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
        this.songs = new ArrayList<>();
        Intent intent = getIntent();
        if (intent == null) return;
        this.playlistId = intent.getStringExtra("playlistId");
        this.playbackPosition = intent.getIntExtra("playbackPosition", 0);
        this.playlistName = intent.getStringExtra("playlistName");
        this.selectedDeezerTrackId = intent.getStringExtra("deezerTrackId");
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
        getSongsFromFirebase();

        //fetchSongMetadata();
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

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
        //Log.d(TAG, "updateUiForPlayingMediaItem: holaaaaaaaaaaaaaaaaaaaaaa");

        assert mediaItem != null;
        updateUiForPlayingMediaItem(mediaItem);
    }

    private void updateUiForPlayingMediaItem(MediaItem mediaItem) {
        updateSongMetadata(mediaItem.mediaId);
    }
}