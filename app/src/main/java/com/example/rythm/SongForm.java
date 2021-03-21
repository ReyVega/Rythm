package com.example.rythm;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SongForm extends AppCompatActivity {

    private Button btnSongCancel,
                   btnSongSave;
    private Spinner sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_form);

        this.btnSongCancel = findViewById(R.id.btnSongCancel);
        this.btnSongSave = findViewById(R.id.btnSaveSong);
        this.sp = findViewById(R.id.genreMusic);

        this.btnSongCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        this.btnSongSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner,getResources().getStringArray(R.array.list));
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
        this.sp.setAdapter(adapter);
    }
}