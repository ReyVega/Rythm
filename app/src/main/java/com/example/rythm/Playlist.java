package com.example.rythm;

public class Playlist {
    private String  name,
                    playlistId;

    public Playlist(String name, String playlistId) {

        this.name = name;
        this.playlistId = playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }
}
