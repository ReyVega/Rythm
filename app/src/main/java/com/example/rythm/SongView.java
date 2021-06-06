package com.example.rythm;

import androidx.annotation.NonNull;
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
    private final int BUFFER_SIZE = 11; // recommended to be an odd number

    private HashSet<Integer> bufferedIds = new HashSet<Integer>(2*BUFFER_SIZE);

    private int playlistPosition;

    private List<String> deezerTrackIds;
    private String playlistId, playlistName;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference Deezer2YouTubeCollectionReference = db.collection("HT");


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

    private void getSongsFromFirebase() {
        if (this.playlistId.isEmpty()) return;
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
            addSongToPlayer(this.playlistPosition, 0);
        });
    }

    private void populateBuffer(int position, int bufferSize) {
        Log.d(TAG, "addSongToPlayer: pos: " + position + " | " + deezerTrackIds.size());
        for (int i = 0; i < (bufferSize-1)/2; i++) {

            synchronized (this) {
                addSongToPlayer(position + i + 1, false);
            }

            synchronized (this) {
                addSongToPlayer(position - i - 1, true);
            }
        }
    }

    private void addSongToPlayer(int position, int direction) {
        // Direction
        // 0  -> initial position
        // < 0 -> left
        // > 0 -> right

        //Log.d(TAG, "deezer track ids: " + this.deezerTrackIds.toString());

        if (direction == 0) {
            int numSongs = deezerTrackIds.size();
            addSongToPlayer(position, false);
            populateBuffer(position, Math.min(numSongs, BUFFER_SIZE));
        }
        else if (direction > 0) {
            addSongToPlayer(position+1, false);
        }
        else {
            addSongToPlayer(position-1, true);
        }
    }

    private synchronized void addSongToPlayer(int position, boolean insertAtFirst) {
        int n = deezerTrackIds.size();
        int fixedPosition = (position < 0) ? n+position : position % n;

        checkIfSongIsInFirestoreHashTable(fixedPosition, insertAtFirst);
    }

    private synchronized  void checkIfSongIsInFirestoreHashTable(int position, boolean insertAtFirst) {
        DocumentReference documentReference = Deezer2YouTubeCollectionReference.document(deezerTrackIds.get(position));

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document == null) return;
                if (document.exists()) {
                    String youtubeVideoId = String.valueOf(document.get("youtubeVideoId")),
                            youtubeURL = getString(R.string.youtube_video_endpoint) + youtubeVideoId;

                    fetchYouTubeSongDASH(youtubeURL, position, insertAtFirst, this, player, BUFFER_SIZE, bufferedIds,15);
                    //Log.d(TAG, "checkIfSongIsInFirestoreHashTable: " + position + ": ENCONTRADO");
                } else {
                    //Log.d(TAG, "No such document");
                    fetchYoutubeURL(position, insertAtFirst);
                }
            } else {
                //Log.d(TAG, "get failed with ", task.getException());
                fetchYoutubeURL(position, insertAtFirst);
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

    private synchronized void fetchYoutubeURL(int position, boolean insertAtFirst) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                "http://54.144.232.51:8080/deezer2youtube/" + deezerTrackIds.get(position),
                ytVideoId -> {
                    String youtubeURL = getString(R.string.youtube_video_endpoint) + ytVideoId;

                    //Log.d(TAG, "fetchYoutubeURL: request result: " + ytVideoId);

                    fetchYouTubeSongDASH(youtubeURL, position, insertAtFirst, getBaseContext(), player, BUFFER_SIZE, bufferedIds, 15);
                    addSongToFirestoreHashTable(deezerTrackIds.get(position), ytVideoId);
                }, error -> {
                });

        queue.add(stringRequest);
    }

    private synchronized static void fetchYouTubeSongDASH(String youtubeURL, int fixedPosition, boolean insertAtFirst,
                                         Context context, Player player, int BUFFER_SIZE, HashSet<Integer> bufferedIds, int attempts) {
        new YouTubeExtractor(context) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                if (attempts <= 0) {
                    // youtube URL is probably down TODO implement function to fix URL
                    return;
                }

                if (ytFiles != null) {
                    if (bufferedIds.contains(fixedPosition)) return;
                    else bufferedIds.add(fixedPosition);

                    int audioTag = 140; //audio tag for m4a, audioBitrate: 128
                    String youtubeDashUrl = ytFiles.get(audioTag).getUrl();

                    //Log.d(TAG, "onExtractionComplete: " + youtubeDashUrl);

                    MediaItem mediaItem = new MediaItem.Builder()
                                .setUri(youtubeDashUrl)
                                .setMediaId(String.valueOf(fixedPosition))
                                .build();

                        int currPlayerSize = player.getMediaItemCount();
                        if (insertAtFirst) {
                            if (currPlayerSize >= BUFFER_SIZE) {
                                player.removeMediaItem(currPlayerSize-1);
                            }
                            player.addMediaItem(0, mediaItem);
                        }
                        else {
                            if (currPlayerSize >= BUFFER_SIZE) {
                                player.removeMediaItem(0);
                            }
                            player.addMediaItem(mediaItem);
                        }


                }
                else {
                    fetchYouTubeSongDASH(youtubeURL, fixedPosition, insertAtFirst, context, player,
                                     BUFFER_SIZE, bufferedIds,attempts-1);

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

        nivSongPhoto = findViewById(R.id.nivSongPhoto);

        ImageButton ibSongReturn = findViewById(R.id.ibSongReturn);
        ibSongReturn.setOnClickListener(v -> {
            finish();
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        this.deezerTrackIds = new ArrayList<>();
        Intent intent = getIntent();
        if (intent == null) return;
        this.playlistId = intent.getStringExtra("playlistId");
        this.playlistPosition = intent.getIntExtra("playlistPosition", 0);
        this.playlistName = intent.getStringExtra("playlistName");
        getSongsFromFirebase();
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
        if (mediaItem == null) return;

        updateUiForPlayingMediaItem(mediaItem);


        StringBuilder r = new StringBuilder();
        for (int i=0; i<player.getMediaItemCount(); i++) r.append(player.getMediaItemAt(i).mediaId).append(", ");
        Log.d(TAG, "onMediaItemTransition: " + r);

        if (player.getMediaItemCount() < BUFFER_SIZE) return;


        int newPlaylistPosition = Integer.parseInt(mediaItem.mediaId);

        //Log.d(TAG, "onMediaItemTransition: ");

        if (newPlaylistPosition == playlistPosition) return;
        else if (newPlaylistPosition > playlistPosition) {
            synchronized (this) {
                final int LAST_INDEX = Integer.parseInt(player.getMediaItemAt(player.getMediaItemCount()-1).mediaId);
                addSongToPlayer(LAST_INDEX, 1);
            }
        }
        else {
            synchronized (this) {
                final int FIRST_INDEX = Integer.parseInt(player.getMediaItemAt(0).mediaId);
                addSongToPlayer(FIRST_INDEX, -1);
            }
        }

        this.playlistPosition = Integer.parseInt(mediaItem.mediaId);
    }


    private void updateUiForPlayingMediaItem(MediaItem mediaItem) {
        updateSongMetadata(deezerTrackIds.get(Integer.parseInt(mediaItem.mediaId)));
    }
}