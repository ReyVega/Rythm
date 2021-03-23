package com.example.rythm;

public class Song {
    private String  songName,
                    artistName,
                    genreName,
                    userId;

    public Song() {}; //Empty constructor for FireStore

    public Song(String songName, String artistName, String genreName, String userId) {
        this.songName = songName;
        this.artistName = artistName;
        this.genreName = genreName;
        this.userId = userId;
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

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getUserId() { return userId; }

    public void setUserId(String userId) { this.userId = userId; }
}
