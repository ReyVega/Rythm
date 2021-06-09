package com.example.rythm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;


public class SongView extends AppCompatActivity implements EventListener {
    private static final String TAG = "aiuda";
    private PlayerView playerView;
    private TextView    tvSongPlaylist,
                        tvSongName,
                        tvSongAuthor;
    private NetworkImageView nivSongPhoto;

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private boolean isFirstSongAdded;

    private int selectedPosition;

    private List<String> deezerTrackIds;
    private String playlistId, playlistName;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference Deezer2YouTubeCollectionReference = db.collection("HT"),
                                      SongsReproductionsCollectionReference = db.collection("SongsReproductions");


    private RequestQueue queue = RequestController.getInstance(this.getBaseContext()).getRequestQueue();


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
        player.prepare();
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
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

    private void resetPlayer() {
        if (player != null) {
            playWhenReady = true;
            playbackPosition = 0;
            currentWindow = 0;
            player.release();
            player = null;
        }
        queue.cancelAll(this);


    }

    private void getSongsFromFirebase() {
        if (this.playlistId == null || this.playlistId.isEmpty()) return;
        Query songsQuery = db.collection("Songs")
                            .whereEqualTo("playlistId", playlistId)
                            .orderBy("addedTimestamp")
                            .orderBy("deezerTrackId");

        songsQuery.addSnapshotListener((documentSnapshots, e) -> {
           if (documentSnapshots == null) return;
            for (DocumentChange doc: documentSnapshots.getDocumentChanges()){
                if (doc.getType() == DocumentChange.Type.ADDED){
                    String deezerTrackId = String.valueOf(doc.getDocument().get("deezerTrackId"));
                    this.deezerTrackIds.add(deezerTrackId);
                }
            }
            addFirstSong();
        });

    }

    private void addFirstSong() {
        addSongToPlayer(selectedPosition);
    }

    private void fillPlaylist(int position) {
        if (player == null) return;

        List<MediaItem> prev = new ArrayList<>();

        for (int i = 0; i < position; i++) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri("")
                    .setMediaId(String.valueOf(i))
                    .build();

            prev.add(mediaItem);
        }

        player.addMediaItems(0 , prev);

        List<MediaItem> next = new ArrayList<>();

        for (int i = position+1; i < deezerTrackIds.size(); i++) {
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri("")
                    .setMediaId(String.valueOf(i))
                    .build();

            next.add(mediaItem);
        }

        player.addMediaItems(position+1, next);

        for (int i = 0; i < deezerTrackIds.size()/2; i++) {
            addSongToPlayer(position + i + 1);
            addSongToPlayer(position - i - 1);
        }
    }


    private void addSongToPlayer(int position) {
        int n = deezerTrackIds.size();
        int fixedPosition = (position < 0) ? n+position : position % n;

        checkIfSongIsInFirestoreHashTable(fixedPosition);
    }

    private void checkIfSongIsInFirestoreHashTable(int position) {
        DocumentReference documentReference = Deezer2YouTubeCollectionReference.document(deezerTrackIds.get(position));

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document == null) return;
                if (document.exists()) {
                    String youtubeVideoId = String.valueOf(document.get("youtubeVideoId")),
                            youtubeURL = getString(R.string.youtube_video_endpoint) + youtubeVideoId;

                    fetchYouTubeSongDASH(youtubeURL, position,15);
                } else {
                    fetchYoutubeURL(position);
                }
            } else {
                fetchYoutubeURL(position);
            }
        });

    }

    private void addSongToFirestoreHashTable(String deezerTrackId, String youtubeVideoId) {
        Map<String, Object> songObj = new HashMap<>();
        songObj.put("youtubeVideoId", youtubeVideoId);

        Deezer2YouTubeCollectionReference.document(deezerTrackId).set(songObj).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Song added successfully to HT!");
            }
        }).addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    private void fetchYoutubeURL(int position) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                "http://54.144.232.51:8080/deezer2youtube/" + deezerTrackIds.get(position),
                ytVideoId -> {
                    String youtubeURL = getString(R.string.youtube_video_endpoint) + ytVideoId;

                    Log.d(TAG, "fetchYoutubeURL: request result: " + ytVideoId);

                    fetchYouTubeSongDASH(youtubeURL, position, 15);
                    addSongToFirestoreHashTable(deezerTrackIds.get(position), ytVideoId);
                }, error -> {
                    if (!isFirstSongAdded) {
                        fillPlaylist(position);
                        isFirstSongAdded = true;
                        Toast.makeText(this, "Song not available right now", Toast.LENGTH_LONG).show();
                    }
                });

        stringRequest.setTag(this);

        queue.add(stringRequest);
    }

    @SuppressLint("StaticFieldLeak")
    private void fetchYouTubeSongDASH(String youtubeURL, int fixedPosition, int attempts) {
        new YouTubeExtractor(this) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (player == null) return;
                if (attempts <= 0) {
                    if (!isFirstSongAdded) {
                        fillPlaylist(fixedPosition);
                        isFirstSongAdded = true;
                        Toast.makeText(getBaseContext(), "Song not available right now", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                if (ytFiles != null) {

                    int audioTag = 140; //audio tag for m4a, audioBitrate: 128
                    String youtubeDashUrl = ytFiles.get(audioTag).getUrl();

                    MediaItem mediaItem = new MediaItem.Builder()
                            .setUri(youtubeDashUrl)
                            .setMediaId(String.valueOf(fixedPosition))
                            .build();


                    if (!isFirstSongAdded) {
                        isFirstSongAdded = true;
                        player.addMediaItem(mediaItem);
                        fillPlaylist(fixedPosition);
                    }
                    else {
                        player.removeMediaItem(fixedPosition);
                        player.addMediaItem(fixedPosition, mediaItem);
                    }

                }
                else {
                    fetchYouTubeSongDASH(youtubeURL, fixedPosition,attempts-1);
                }
            }
        }.extract(youtubeURL, true, true);
    }

    private void  increaseSongReproductionsInFirestore(int position) {
        Map<String, Object> songObj = new HashMap<>();

        DocumentReference documentReference = SongsReproductionsCollectionReference
                .document(deezerTrackIds.get(position));

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document == null) return;
                if (document.exists()) {
                    try {
                        long reproductions = (long) document.get("reproductions");
                        songObj.put("reproductions", reproductions+1);

                    } catch (Exception e) {
                        songObj.put("reproductions", 1);
                    }
                    documentReference.set(songObj);
                } else {
                    songObj.put("reproductions", 1);
                    documentReference.set(songObj);
                }
            }
        });
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

        jsonObjectRequest.setTag(this);
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

        nivSongPhoto = findViewById(R.id.nivSongPhoto);

        ImageButton ibSongReturn = findViewById(R.id.ibSongReturn);
        ibSongReturn.setOnClickListener(v -> {
            resetPlayer();
            finish();
        });


    }



    @Override
    public void onStart() {
        super.onStart();
        this.deezerTrackIds = new ArrayList<>();
        Log.d(TAG, "onStart: intent" + getIntent().toString());
        Intent intent = getIntent();
        if (intent == null) return;
        this.playlistId = intent.getStringExtra("playlistId");
        this.selectedPosition = intent.getIntExtra("playlistPosition", 0);
        this.playlistName = intent.getStringExtra("playlistName");
        String selectedDeezerTrackId = intent.getStringExtra("selectedDeezerTrackId");
        Log.d(TAG, "onStart: " + playlistId + " " + selectedPosition + " " + playlistName + " " + selectedDeezerTrackId);
        if (playlistId != null) getSongsFromFirebase();
        else {
            deezerTrackIds.add(selectedDeezerTrackId);
            addFirstSong();
        }
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

    @Override
    public void onMediaItemTransition(@Nullable MediaItem mediaItem, @Player.MediaItemTransitionReason int reason) {
        if (mediaItem == null || mediaItem.mediaId.equals("")) return;

        assert mediaItem.playbackProperties != null;
        String uri = mediaItem.playbackProperties.uri.toString();
        if (uri.equals("")) {
            player.next();
        }

        increaseSongReproductionsInFirestore(Integer.parseInt(mediaItem.mediaId));

        updateUiForPlayingMediaItem(mediaItem);
    }


    private void updateUiForPlayingMediaItem(MediaItem mediaItem) {
        try {
            updateSongMetadata(deezerTrackIds.get(Integer.parseInt(mediaItem.mediaId)));
        } catch (NumberFormatException ignored) {}
    }
}