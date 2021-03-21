package com.example.rythm;

public class ListElement {
    public String color,
                  songName,
                  artistName,
                  genreName;

    public ListElement(String color, String songName, String artistName, String genreName) {
        this.color = color;
        this.songName = songName;
        this.artistName = artistName;
        this.genreName = genreName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
}
