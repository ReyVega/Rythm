package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class SongsView extends AppCompatActivity {

    private List<ListElement> elements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_view);

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