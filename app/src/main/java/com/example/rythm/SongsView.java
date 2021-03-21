package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SongsView extends AppCompatActivity {

    private List<ListElement> elements;
    private FloatingActionButton addSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_view);

        this.addSong = findViewById(R.id.addSong);
        this.addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SongForm.class);
                startActivity(i);
            }
        });

        init();
    }

    public void init() {
        this.elements = new ArrayList<ListElement>();
        this.elements.add(new ListElement("#0000FF","Agus Gay","Agus","Rock"));
        this.elements.add(new ListElement("#0000FF","Agus Gay","Agus","Rock"));
        this.elements.add(new ListElement("#0000FF","Agus Gay","Agus","Rock"));


        ListAdapter la = new ListAdapter(this.elements, this);
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(la);

    }


}