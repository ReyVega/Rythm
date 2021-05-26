package com.example.rythm;

import android.annotation.SuppressLint;

public class Song {
    private String  songName,
                    artistName,
                    coverUrl,
                    deezerTrackId;

    private long duration;

    public Song() {}; //Empty constructor for FireStore

    public Song(String songName, String artistName, long duration, String coverUrl, String deezerTrackId) {
        this.songName = songName;
        this.artistName = artistName;
        this.duration = duration;
        this.coverUrl = coverUrl;
        this.deezerTrackId = deezerTrackId;
    }

    public void setSong(Song song) {
        this.songName = song.songName;
        this.artistName = song.artistName;
        this.duration = song.duration;
        this.coverUrl = song.coverUrl;
        this.deezerTrackId = song.deezerTrackId;
    }


    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }


    public String getDeezerTrackId() { return deezerTrackId; }

    public void setDeezerTrackId(String deezerTrackId) { this.deezerTrackId = deezerTrackId; }

    public long getDuration() {
        return duration;
    }


    private String formatDuration(long duration) {
        if (duration < 0 || duration >= 3600) return "00:00";
        long minutes = duration / 60,
                seconds = duration % 60;
        return minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }
    public String getFormattedDuration() {
        return formatDuration(this.duration);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
