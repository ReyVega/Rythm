package com.example.rythm;

import com.google.firebase.Timestamp;

public class Playlist {
    private String  name,
                    playlistId,
                    imageURL;

    private boolean isUserTheOwner;

    private Timestamp lastModified;

    public Playlist(String name, String playlistId, boolean isUserTheOwner) {
        this.name = name;
        this.playlistId = playlistId;
        this.isUserTheOwner = isUserTheOwner;

    public Playlist(String name, String playlistId) {
        this.name = name;
        this.playlistId = playlistId;
    }

    public Playlist(String name, String playlistId, String imageURL) {
        this.name = name;
        this.playlistId = playlistId;
        this.imageURL = imageURL;
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

    public void setPlaylist(Playlist p) {
        name = p.name;
        playlistId = p.playlistId;
        isUserTheOwner = p.isUserTheOwner;
        lastModified = p.lastModified;
    }

    public boolean isUserTheOwner() {
        return isUserTheOwner;
    }

    public void setUserTheOwner(boolean userTheOwner) {
        isUserTheOwner = userTheOwner;
    }

    public Timestamp getLastModified() {
        return lastModified;
    }

    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    public int compareTo(Playlist p) {
        if (this.lastModified == null && p.lastModified == null) return -1;
        else if (this.lastModified == null) return -1;
        else if (p.lastModified == null) return 1;
        else return  p.lastModified.compareTo(this.lastModified);
      
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
